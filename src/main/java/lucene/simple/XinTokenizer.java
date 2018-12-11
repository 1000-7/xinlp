package lucene.simple;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import segment.hmm.XinHmmSegment;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * @Author unclewang
 * @Date 2018-12-11 11:22
 */
public class XinTokenizer extends Tokenizer {
    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
    private final PositionIncrementAttribute positionAttr = addAttribute(PositionIncrementAttribute.class);
    private final TypeAttribute typeAtt = addAttribute(TypeAttribute.class);
    private SegmentWrapper segment;
    private int totalOffset = 0;

    public XinTokenizer(XinHmmSegment segment) {
        this.segment = new SegmentWrapper(input, segment);
    }

    @Override
    final public boolean incrementToken() throws IOException {
        clearAttributes();
        Atom atom;
        atom = segment.next();


        if (atom != null) {
            //每一个词都是1个，因为不支持智能分词（今天天气==>今天，天气，天天）
            positionAttr.setPositionIncrement(1);
            termAtt.setEmpty().append(atom.getContent());
            termAtt.setLength(atom.getLen());
            offsetAtt.setOffset(totalOffset + atom.getOffe(), totalOffset + atom.getOffe() + atom.getLen());
            System.out.println(totalOffset + atom.getOffe());
            typeAtt.setType("word");
            return true;
        } else {
            totalOffset += segment.getOffset();
            return false;
        }
    }


    @Override
    public void reset() throws IOException {
        super.reset();
        segment.reset(new BufferedReader(this.input));
    }
}
