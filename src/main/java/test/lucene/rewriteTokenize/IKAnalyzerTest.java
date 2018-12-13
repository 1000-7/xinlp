package test.lucene.rewriteTokenize;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;


/**
 * IKAnalyzer分詞器集成測試:
 * 細粒度切分：把詞分到最細
 * 智能切分：根據詞庫進行拆分符合我們的語言習慣
 *
 * @author THINKPAD
 */
public class IKAnalyzerTest {
    private static void doToken(TokenStream ts) throws IOException {
        ts.reset();
        CharTermAttribute cta = ts.getAttribute(CharTermAttribute.class);
        while (ts.incrementToken()) {
            System.out.print(cta.toString() + "|");
        }
        System.out.println();
        ts.end();
        ts.close();
    }

    public static void main(String[] args) throws IOException {

        String etext = "Analysis is one of the main causes of slow indexing. Simply put, the more you analyze the slower analyze the indexing (in most cases).";
        String chineseText = "张三说的确实在理";
        /**
         * ikanalyzer 中文分詞器 因為Analyzer的createComponents方法API改變了 需要我們自己實現
         * 分析器IKAnalyzer4Lucene7和分詞器IKTokenizer4Lucene7
         */
        // IKAnalyzer 細粒度切分
        try (Analyzer ik = new IKAnalyzer4Lucene7();) {
            TokenStream ts = ik.tokenStream("content", etext);
            System.out.println("IKAnalyzer中文分詞器 細粒度切分，英文分詞效果：");
            doToken(ts);
            ts = ik.tokenStream("content", chineseText);
            System.out.println("IKAnalyzer中文分詞器 細粒度切分，中文分詞效果：");
            doToken(ts);
        }

        // IKAnalyzer 智能切分
        try (Analyzer ik = new IKAnalyzer4Lucene7(true);) {
            TokenStream ts = ik.tokenStream("content", etext);
            System.out.println("IKAnalyzer中文分詞器 智能切分，英文分詞效果：");
            doToken(ts);
            ts = ik.tokenStream("content", chineseText);
            System.out.println("IKAnalyzer中文分詞器 智能切分，中文分詞效果：");
            doToken(ts);
        }
    }
}