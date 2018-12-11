package learn.lucene.rewriteTokenize;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;

import java.io.IOException;

/**
 * 因為Analyzer的createComponents方法API改變了需要重新實現分詞器
 *
 * @author THINKPAD
 */
public class IKTokenizer4Lucene7 extends Tokenizer {

    // IK分詞器實現
    private IKSegmenter _IKImplement;

    // 詞元文本屬性
    private final CharTermAttribute termAtt;
    // 詞元位移屬性
    private final OffsetAttribute offsetAtt;
    // 詞元分類屬性（該屬性分類參考org.wltea.analyzer.core.Lexeme中的分類常量）
    private final TypeAttribute typeAtt;
    // 記錄最後一個詞元的結束位置
    private int endPosition;

    /**
     * @param in
     * @param useSmart
     */
    public IKTokenizer4Lucene7(boolean useSmart) {
        super();
        offsetAtt = addAttribute(OffsetAttribute.class);
        termAtt = addAttribute(CharTermAttribute.class);
        typeAtt = addAttribute(TypeAttribute.class);
        _IKImplement = new IKSegmenter(input, useSmart);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.lucene.analysis.TokenStream#incrementToken()
     */
    @Override
    final public boolean incrementToken() throws IOException {
        // 清除所有的詞元屬性
        clearAttributes();
        Lexeme nextLexeme = _IKImplement.next();
        if (nextLexeme != null) {
            // 將Lexeme轉成Attributes
            // 設置詞元文本
            termAtt.append(nextLexeme.getLexemeText());
            // 設置詞元長度
            termAtt.setLength(nextLexeme.getLength());
            // 設置詞元位移
            offsetAtt.setOffset(nextLexeme.getBeginPosition(),
                                nextLexeme.getEndPosition());
            // 記錄分詞的最後位置
            endPosition = nextLexeme.getEndPosition();
            System.out.println(endPosition);
            // 記錄詞元分類
            typeAtt.setType(nextLexeme.getLexemeTypeString());
            // 返會true告知還有下個詞元
            return true;
        }
        // 返會false告知詞元輸出完畢
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.lucene.analysis.Tokenizer#reset(java.io.Reader)
     */
    @Override
    public void reset() throws IOException {
        super.reset();
        _IKImplement.reset(input);
    }

    @Override
    public final void end() {
        // set final offset
        int finalOffset = correctOffset(this.endPosition);
        offsetAtt.setOffset(finalOffset, finalOffset);
    }
}