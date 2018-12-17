package mining.config;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import lombok.Data;
import org.junit.jupiter.api.Test;
import tools.PathUtils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

@Data
public class Config {
    private static String vocabularyPath = PathUtils.getDataPath() + "/vocabulary.txt";
    private static String termIdPath = PathUtils.getDataPath() + "/termid.txt";
    private static String prePath = PathUtils.getDataPath() + "/20news-18828";
    private static String postPath = PathUtils.getDataPath() + "/post.20news-18828";
    private static String stopwordsPath = PathUtils.getDataPath() + "/stopwords.txt";
    private static String tfidfsPath = PathUtils.getDataPath() + "/tfidfs.txt";
    private static BiMap<Integer, String> idPostFiles = HashBiMap.create();
    private static AtomicInteger id;

    static {
        id = new AtomicInteger(0);
    }

    public static String getVocabularyPath() {
        return vocabularyPath;
    }

    public static String getPrePath() {
        return prePath;
    }

    public static String getPostPath() {
        return postPath;
    }

    public static String getStopwordsPath() {
        return stopwordsPath;
    }

    public static String getTfidfsPath() {
        return tfidfsPath;
    }

    public static String getTermIdPath() {
        return termIdPath;
    }

    public static BiMap<Integer, String> getIdPostFiles(String filepath) {
        Path path = Paths.get(filepath);
        for (File file : Objects.requireNonNull(path.toFile().listFiles())) {
            if (file.isDirectory()) {
                getIdPostFiles(file.getAbsolutePath());
            } else {
                idPostFiles.put(id.getAndIncrement(), file.getAbsolutePath());
            }
        }
        return idPostFiles;
    }

    @Test
    public void test() {
        getIdPostFiles(postPath);

        System.out.println(idPostFiles.size());
        System.out.println(idPostFiles.get(0));
    }
}
