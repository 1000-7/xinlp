package mining.data;

import lombok.extern.slf4j.Slf4j;
import mining.config.Config;
import mining.tfidf.Word;
import org.junit.jupiter.api.Test;
import tools.Stemmer;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * @Author unclewang
 * @Date 2018-12-15 21:04
 */
@Slf4j
public class PreProcess20News implements PreProcess {
    private String prePath = Config.getPrePath();
    private String postPath = Config.getPostPath();
    private static HashSet<String> stopWordsSet = new HashSet<>();
    private static Stemmer stemmer = null;
    private static Map<String, Word> vocabulary = new HashMap<>();

    static {
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(Config.getStopwordsPath())));
            String s = null;
            while ((s = br.readLine()) != null) {
                stopWordsSet.add(s);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void preProcess(String currentPath) throws IOException {
        File currentDir = new File(currentPath);
        if (!currentDir.exists()) {
            throw new FileNotFoundException(currentPath + "，没有这个文件夹");
        }
        String subStrDir = currentPath.replaceAll(prePath, "");
        String postDir = postPath + subStrDir;

        File postFile = new File(postDir);
        if (!postFile.exists()) {
            postFile.mkdir();
        }
        File[] preFiles = currentDir.listFiles();
        assert preFiles != null;
        for (int i = 0; i < preFiles.length; i++) {
            String preFilePath = preFiles[i].getCanonicalPath();
            String preFileName = preFiles[i].getName();
            String stemPath = postDir + "/" + preFileName;
            if (new File(preFilePath).isDirectory()) {
                preProcess(preFilePath);
            } else {
                log.info("正在处理：" + preFilePath);
                createStemFile(preFilePath, stemPath);
                log.info(stemPath);
            }
        }
    }

    private void createStemFile(String preFilePath, String stemPath) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(stemPath))) {
            try (BufferedReader br = new BufferedReader(new FileReader(preFilePath))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String stemLine = processLine(line);
                    bw.write(stemLine);
                }
                bw.flush();
            }
        }
    }

    private String processLine(String line) {
        line = line.toLowerCase();
        String[] sp = line.split("[^a-z]");
        StringBuilder resLine = new StringBuilder();
        for (String s : sp) {
            if (!"".equals(s) && !stopWordsSet.contains(s)) {
                resLine.append(stem(s)).append("\n");
                if (!vocabulary.containsKey(s)) {
                    Word word = new Word();
                    String stem = stem(s);
                    word.setString(s);
                    word.setStemString(stem);
                    vocabulary.put(s, word);
                }
            }
        }
        return resLine.toString();
    }

    private String stem(String word) {
        stemmer = new Stemmer();
        stemmer.add(word.toCharArray(), word.length());
        stemmer.stem();
        return stemmer.toString();
    }

    private void vocabulary2file() {
        try (FileWriter fw = new FileWriter(new File(Config.getVocabularyPath()))) {
            int id = 0;
            for (Map.Entry<String, Word> entry : vocabulary.entrySet()) {
                fw.write(id + "\t" + entry.getKey() + "\t" + entry.getValue().getStemString() + "\n");
                id++;
            }
            fw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void test() throws IOException {
//        stem("beautiful");
//        stem("wonderful");
        preProcess(prePath);
        vocabulary2file();
    }
}
