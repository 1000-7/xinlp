package mining.tfidf;

import com.google.common.collect.BiMap;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * @Author unclewang
 * @Date 2018-12-16 15:56
 */
@Data
@Slf4j
public class OneDocTfDf {
    private HashMap<Integer, Double> idTf = new HashMap<>();
    private HashSet<Integer> idDf = new HashSet<>();
    private static BiMap<String, Integer> wordIds = Vocabulary.getWordIds();


    /**
     * @throws IOException 词频（TF） = 某个词在文章中的出现次数 / 拥有最高词频的词的次数
     */
    public void calOneFileTf(String filepath) {
        List<String> words = null;
        try {
            words = Files.readAllLines(Paths.get(filepath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert words != null;
        double maxValue = 0;
        for (String word : words) {
            int id = wordIds.get(word.trim());
            if (!idTf.containsKey(id)) {
                idTf.put(id, 0.0);

            }
            double cur = idTf.get(id) + 1.0;
            idTf.put(id, cur);
            idDf.add(id);
            if (cur > maxValue) {
                maxValue = cur;
            }
        }
        for (Map.Entry<Integer, Double> entry : idTf.entrySet()) {
            idTf.put(entry.getKey(), entry.getValue() / maxValue);
        }
    }

    @Test
    public void test() {
        calOneFileTf("/Users/unclewang/.xinlp/data/post.20news-18828/comp.windows.x/66410");
        System.out.println(idDf);
        calOneFileTf("/Users/unclewang/.xinlp/data/post.20news-18828/comp.windows.x/66411");
        System.out.println(idDf);
    }
}
