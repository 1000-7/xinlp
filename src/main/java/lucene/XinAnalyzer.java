package lucene;

import lombok.extern.java.Log;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;
import segment.Segment;
import segment.bilstmcrf.BLCSegment;
import segment.crf.tcp.XinCRFSegmentClient;
import segment.hmm.XinHmmSegment;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author unclewang
 * @Date 2018-12-11 11:17
 */
@Log
public class XinAnalyzer extends Analyzer {
    public static enum TYPE {
        HMM_XIN,
        CRF_XIN,
        BILSTMCRF_XIN
    }

    /**
     * 分词类型
     */
    private Map<String, String> args;

    public XinAnalyzer(TYPE type) {
        this.args = new HashMap<>();
        args.put("type", type.name());
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        Tokenizer tokenizer = getTokenizer(this.args);
        return new TokenStreamComponents(tokenizer);
    }


    private Tokenizer getTokenizer(Map<String, String> args) {
        log.info("to create tokenizer " + args);
        String type = args.get("type");
        if (type == null) {
            type = TYPE.HMM_XIN.name();
        }

        Segment segment = null;

        switch (TYPE.valueOf(type)) {
            case CRF_XIN:
                segment = new XinCRFSegmentClient();
                break;
            case HMM_XIN:
                segment = new XinHmmSegment();
                break;
            case BILSTMCRF_XIN:
                segment = new BLCSegment();
                break;
            default:
                break;
        }
        return new XinTokenizer(segment);
    }
}
