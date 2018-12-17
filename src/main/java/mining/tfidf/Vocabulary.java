package mining.tfidf;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.io.Files;
import mining.config.Config;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

public class Vocabulary {
    private static BiMap<String, Integer> wordIds = HashBiMap.create();
    private static Multimap<String, String> stemWords = HashMultimap.create();
    private static int wordSize = 0;

    static {
        try {
            List<String> words = Files.readLines(new File(Config.getVocabularyPath()), Charset.defaultCharset());
            int id = 0;
            for (String s : words) {
                String[] split = s.split("\t");
                if (!wordIds.containsKey(split[2])) {
                    wordIds.put(split[2], id++);
                }
                stemWords.put(split[2], split[1]);
            }
            wordSize = wordIds.size();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static BiMap<String, Integer> getWordIds() {
        return wordIds;
    }

    public static Multimap<String, String> getStemWords() {
        return stemWords;
    }

    public static int getWordSize() {
        return wordSize;
    }

    @Test
    public void test() {
        System.out.println(wordIds.inverse().get(0));
        System.out.println(wordIds.inverse().get(2477));
        System.out.println(stemWords.keySet().size());
    }
}
