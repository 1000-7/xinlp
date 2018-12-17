package tools;

import org.junit.jupiter.api.Test;

import java.io.File;

public class PathUtils {
    private static String rootPath = null;
    private static String xinlpPath = null;
    private static String dataPath = null;

    static {
        rootPath = System.getProperty("user.home");
        xinlpPath = rootPath + "/.xinlp";
        File file = new File(xinlpPath);
        if (!file.exists()) {
            file.mkdir();
        }
        dataPath = xinlpPath + "/data";
        file = new File(dataPath);
        if (!file.exists()) {
            file.mkdir();
        }
    }

    public static String getRootPath() {
        return rootPath;
    }

    public static String getXinlpPath() {
        return xinlpPath;
    }

    public static String getDataPath() {
        return dataPath;
    }

    @Test
    public void test() {
        System.out.println(dataPath);
    }
}
