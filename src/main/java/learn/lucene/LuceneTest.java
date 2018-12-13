package learn.lucene;

import com.hankcs.lucene.HanLPAnalyzer;
import learn.lucene.rewriteTokenize.IKAnalyzer4Lucene7;
import lucene.XinAnalyzer;
import org.ansj.lucene7.AnsjAnalyzer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Iterator;

public class LuceneTest {

    public static void main(String[] a) throws IOException, ParseException {
        Analyzer analyzer = new XinAnalyzer(XinAnalyzer.TYPE.HMM_XIN);
        analyzer.setVersion(Version.LUCENE_7_4_0);

        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        // Store the index in memory:
        Directory directory = new RAMDirectory();
        // To store an index on disk, use this instead:
        //Directory directory = FSDirectory.open("/tmp/testindex");

        IndexWriter iwriter = new IndexWriter(directory, config);
        Document doc = new Document();
        String text = "今天天气很不错";
        doc.add(new Field("fieldname", text, TextField.TYPE_STORED));
        iwriter.addDocument(doc);
        Document doc1 = new Document();
        String text1 = "今天很不错";
        doc1.add(new Field("fieldname", text1, TextField.TYPE_STORED));

        iwriter.addDocument(doc1);
        iwriter.close();

        // Now search the index:
        DirectoryReader ireader = DirectoryReader.open(directory);
        IndexSearcher isearcher = new IndexSearcher(ireader);
        // Parse a simple query that searches for "text":
        QueryParser parser = new QueryParser("fieldname", analyzer);
        Query query = parser.parse("很");
        ScoreDoc[] hits = isearcher.search(query, 10).scoreDocs;
//        Assertions.assertEquals(1, hits.length);
        // Iterate through the results:
        for (int i = 0; i < hits.length; i++) {
            Document hitDoc = isearcher.doc(hits[i].doc);
            System.out.println(hitDoc.get("fieldname"));
//            Assertions.assertEquals("This is the text to be indexed.", hitDoc.get("fieldname"));
        }
        ireader.close();
        directory.close();
    }


    @Test
    public void testStandardAnalyzer() throws Exception {
        StandardAnalyzer standardAnalyzer = new StandardAnalyzer();
        print(standardAnalyzer);
    }


    @Test
    public void testSmartChineseAnalyzer() throws Exception {
        SmartChineseAnalyzer smartChineseAnalyzer = new SmartChineseAnalyzer();
        print(smartChineseAnalyzer);
    }

    /**
     * @throws Exception
     * @Description: 测试自定义停用词
     * 里面用的char数组
     */
    @Test
    public void testMySmartChineseAnalyzer() throws Exception {
        CharArraySet charArraySet = new CharArraySet(0, true);

        // 系统默认停用词
        Iterator<Object> iterator = SmartChineseAnalyzer.getDefaultStopSet().iterator();
        while (iterator.hasNext()) {
            char[] chars = (char[]) iterator.next();
//            for (char a : chars) {
//                System.out.println(a);
//            }
            charArraySet.add(iterator.next());
        }

        // 自定义停用词
        String[] myStopWords = {"对", "的", "是", "其中"};
        for (String stopWord : myStopWords) {
            charArraySet.add(stopWord);
        }
        SmartChineseAnalyzer smartChineseAnalyzer = new SmartChineseAnalyzer(charArraySet);
        print(smartChineseAnalyzer);
    }

    @Test
    public void testIKAnalyzer() throws Exception {
        Analyzer analyzer = new IKAnalyzer4Lucene7();
        print(analyzer);
    }

    @Test
    public void testAnsjAnalyzer() throws Exception {
        Analyzer analyzer = new AnsjAnalyzer(AnsjAnalyzer.TYPE.base_ansj);
        print(analyzer);
    }

    @Test
    public void testXinAnalyzer() throws Exception {
        Analyzer xanalyxer = new XinAnalyzer(XinAnalyzer.TYPE.BILSTMCRF_XIN);
        print(xanalyxer);
    }

    @Test
    public void testHanAnalyzer() throws Exception {
        Analyzer xanalyxer = new HanLPAnalyzer();
        print(xanalyxer);
    }

    private void print(Analyzer analyzer) throws Exception {
        String text = "今天天气很不错/今天可以出去玩/你喜欢什么颜色";
        TokenStream tokenStream = analyzer.tokenStream("content", text);
        CharTermAttribute attribute = tokenStream.addAttribute(CharTermAttribute.class);
        tokenStream.reset();
        while (tokenStream.incrementToken()) {
            System.out.println(attribute.toString());
        }
    }
}
