package lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class XinAnalyzerApp {
    @Test
    public void test() {
        Analyzer analyzer = new XinAnalyzer(XinAnalyzer.TYPE.HMM_XIN);
        String text = "今天天气很不错/今天可以出去玩/你喜欢什么颜色";
        TokenStream tokenStream = analyzer.tokenStream("content", text);
        CharTermAttribute attribute = tokenStream.addAttribute(CharTermAttribute.class);
        try {
            tokenStream.reset();
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (true) {
            try {
                if (!tokenStream.incrementToken()) {
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println(attribute.toString());
        }
    }
}
