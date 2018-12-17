package segment.crf;

import tools.PathUtils;

public class XinCRFConfig {
    public static String filePath;
    public static String modelPath;
    public static String binModelPath;

    static {
        filePath = PathUtils.getDataPath() + "/segment/crf/modelc1.5.txt";
        modelPath = PathUtils.getDataPath() + "/segment/crf/xincrfc1.5.model";
        binModelPath = PathUtils.getDataPath() + "xincrf.model.bin";
    }


    public static String getFilePath() {
        return filePath;
    }

    public static String getModelPath() {
        return modelPath;
    }
}
