package segment.crf;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class XinCRFConfig {
    public static final String CONF_FILE = "xincrf.properties";
    public static String filePath;
    public static String modelPath;
    public static String binModelPath;

    static {
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(CONF_FILE);
        Properties props = new Properties();
        try {
            props.load(is);
        } catch (
                IOException e) {
            e.printStackTrace();
        }
        filePath = props.getProperty("crf.model.filePath");
        modelPath = props.getProperty("crf.model.modelPath");
        binModelPath = props.getProperty("crf.model.binModelPath");

    }

    public static String getConfFile() {
        return CONF_FILE;
    }

    public static String getFilePath() {
        return filePath;
    }

    public static String getModelPath() {
        return modelPath;
    }

    public static <T extends Object> void print(T[][] arrays) {
        for (int i = 0; i < arrays.length; i++) {
            for (int j = 0; j < arrays[0].length; j++) {
                System.out.print(arrays[i][j] + "\t");
            }
            System.out.print("\n");
        }
        System.out.println();
    }
}
