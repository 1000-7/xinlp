package lucene.simple;

import lombok.extern.java.Log;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;
import segment.hmm.XinHmmSegment;

/**
 * @Author unclewang
 * @Date 2018-12-11 11:17
 */
@Log
public class XinAnalyzer extends Analyzer {
//    public static enum TYPE {
//        HMM_XIN,
//        CRF_XIN,
//        BILSTMCRF_XIN
//    }
//
//    /**
//     * 分词类型
//     */
//    private Map<String, String> args;
//
//    public XinAnalyzer(TYPE type) {
//        this.args = new HashMap<>();
//        args.put("type", type.name());
//    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        Tokenizer tokenizer = new XinTokenizer(new XinHmmSegment());
        return new TokenStreamComponents(tokenizer);
    }

//    @Override
//    protected TokenStreamComponents createComponents(String fieldName) {
//        BufferedReader reader = new BufferedReader(new StringReader(fieldName));
//        Tokenizer tokenizer = null;
//        tokenizer = getTokenizer(reader, this.args);
//        return new TokenStreamComponents(tokenizer);
//    }

//    private Tokenizer getTokenizer(BufferedReader reader, Map<String, String> args) {
//        log.info("to create tokenizer " + args);
//        String type = args.get("type");
//        if (type == null) {
//            type = TYPE.HMM_XIN.name();
//        }
//
//        Analysis analysis = null;
//
//        switch (TYPE.valueOf(type)) {
//            case CRF_XIN:
//                analysis = new CrfAnalysis();
//                break;
//            case HMM_XIN:
//                analysis = new HmmAnalysis();
//                break;
//            case BILSTMCRF_XIN:
//                analysis = new CrfAnalysis();
//                break;
//            default:
//                break;
//        }
//        if (reader != null) {
//            analysis.resetContent(reader);
//        }
//        return new XinTokenizer(analysis);
//    }
}
