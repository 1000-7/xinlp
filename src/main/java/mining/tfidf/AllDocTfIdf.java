package mining.tfidf;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.io.Files;
import lombok.extern.slf4j.Slf4j;
import mining.config.Config;
import org.apache.commons.math3.util.FastMath;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

/**
 * @Author unclewang
 * @Date 2018-12-16 15:55
 */
@Slf4j
public class AllDocTfIdf {
    private static String postFiles = Config.getPostPath();
    private static HashMap<Integer, HashMap<Integer, Double>> idTf = new HashMap<>();
    private static HashMap<Integer, HashMap<Integer, Double>> idTfIDf = new HashMap<>();
    private static HashMap<Integer, HashSet<Integer>> idDf = new HashMap<>();
    private static double[] idf;
    private static BiMap<Integer, String> idFiles = Config.getIdPostFiles(postFiles);
    private static HashSet<Integer> noFeatureWordId = new HashSet<>();
    private static BiMap<Integer, Integer> termIdVocabularyId = HashBiMap.create();


    private void generateAllTfDf() {
        log.info("正在计算文档频率");
        for (int i = 0; i < idFiles.size(); i++) {
            OneDocTfDf oneDocTfDf = new OneDocTfDf();
            log.info("正在计算文档:" + i);
            oneDocTfDf.calOneFileTf(idFiles.get(i));
            idTf.put(i, oneDocTfDf.getIdTf());
            idDf.put(i, oneDocTfDf.getIdDf());
        }
    }

    private void generateIdNums() {
        log.info("正在生成word的文档频率");
        Set<Integer> ids = Vocabulary.getWordIds().values();
        int fileSize = idFiles.size();
        int size = ids.size();
        int[] idNums = new int[size];

        for (Map.Entry<Integer, HashSet<Integer>> entry : idDf.entrySet()) {
            for (Integer i : entry.getValue()) {
                idNums[i] += 1;
            }
        }
        int sum = 0;
        int max = 0;
        idf = new double[size];
        for (int i = 0; i < size; i++) {
            idf[i] = FastMath.log(((fileSize + 1) * 1.0) / (idNums[i] + 1)) + 1;
            sum += idNums[i];
            if (idNums[i] > max) {
                max = idNums[i];
            }
        }
        System.out.println(size + "个词一共出现了" + sum + "次");
        System.out.println(size + "个词，平均一个词出现了" + sum * 1.0 / size + "次");
        System.out.println(size + "个词中出现的最高次数为" + max + "次");
    }

    private HashMap<Integer, HashMap<Integer, Double>> calTfIdf() {
        HashMap<Integer, HashMap<Integer, Double>> idTfIDf = new HashMap<>();
        setNoFeatureWordId();
        for (Map.Entry<Integer, HashMap<Integer, Double>> entry : idTf.entrySet()) {
            HashMap<Integer, Double> tfIdf = new HashMap<>();
            int key = entry.getKey();
            HashMap<Integer, Double> value = entry.getValue();
            for (Map.Entry<Integer, Double> tfEntry : value.entrySet()) {
                if (!noFeatureWordId.contains(tfEntry.getKey())) {
                    tfIdf.put(tfEntry.getKey(), tfEntry.getValue() * idf[tfEntry.getKey()]);
                }
            }
            idTfIDf.put(key, tfIdf);
        }
        return idTfIDf;
    }

    private void printAllDocTfIdf(HashMap<Integer, HashMap<Integer, Double>> idTfIDf) {
        String path = Config.getTfidfsPath();
        StringBuilder sb = new StringBuilder();

        for (Map.Entry<Integer, HashMap<Integer, Double>> entry : idTfIDf.entrySet()) {
            String json = JSON.toJSONString(entry.getValue());
            sb.append(entry.getKey() + "\t\t\t" + json + "\n");
        }
        try {
            Files.write(sb.toString().getBytes(), new File(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 这个写的有点麻烦了，不过就这样吧，
     * termid和vocabulary的id不一样，本来想着简化了
     * 后来发现vocabulary太大了，没法矩阵运算，所以利用DF选取特征词以后，在进行SVD
     */
    private void generateTermID() {
        log.info("重新生成特征词term的id");
        BiMap<String, Integer> wordIds = Vocabulary.getWordIds();
        for (int i = 0; i < Vocabulary.getWordSize() - noFeatureWordId.size(); ) {
            for (Integer v : wordIds.values()) {
                if (!noFeatureWordId.contains(v)) {
                    termIdVocabularyId.put(i, v);
                    i++;
                }
            }
        }
        StringBuilder sb = new StringBuilder();
        termIdVocabularyId.forEach((integer, integer2) -> sb.append(integer + "\t\t\t" + integer2 + "\n"));
        try {
            Files.write(sb.toString().getBytes(), new File(Config.getTermIdPath()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.info("生成特征词term的id结束");
    }


    /**
     * DF法选取特征词，最大为1000，最小为3
     */
    private void setNoFeatureWordId() {
        int maxDF = 100;
        int minDF = 3;

        double minIdf = FastMath.log(((idFiles.size() + 1) * 1.0) / (maxDF + 1)) + 1;
        double maxIdf = FastMath.log(((idFiles.size() + 1) * 1.0) / (minDF + 1)) + 1;
        for (int i = 0; i < idf.length; i++) {
            if (idf[i] < minIdf || idf[i] > maxIdf) {
                noFeatureWordId.add(i);
            }
        }
        log.info("一共剔除了" + noFeatureWordId.size() + "个单词");
        System.out.println(maxIdf + "\t" + minIdf);
    }

    public static BiMap<Integer, Integer> loadTermid() throws FileNotFoundException {
        BiMap<Integer, Integer> termId = HashBiMap.create();
        if (new File(Config.getTermIdPath()).exists()) {
            log.info("文件已存在，直接读取");
            try {
                List<String> stringList = Files.readLines(new File(Config.getTermIdPath()), Charset.defaultCharset());
                for (String s : stringList) {
                    String[] sp = s.split("\t\t\t");
                    termId.put(Integer.parseInt(sp[0]), Integer.parseInt(sp[1]));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            throw new FileNotFoundException("先运行loadAllDocTfIdf方法就好了");
        }
        return termId;
    }

    public HashMap<Integer, HashMap<Integer, Double>> loadAllDocTfIdf() {
        HashMap<Integer, HashMap<Integer, Double>> idTfIDf = new HashMap<>();
        if (new File(Config.getTfidfsPath()).exists()) {
            log.info("文件已存在，直接读取");
            try {
                List<String> stringList = Files.readLines(new File(Config.getTfidfsPath()), Charset.defaultCharset());
                for (String s : stringList) {
                    String[] split = s.split("\t\t\t");
                    HashMap map = JSONObject.parseObject(split[1], HashMap.class);
                    HashMap<Integer, Double> doubleHashMap = new HashMap<>();
                    for (Object entry : map.keySet()) {
                        int key = Integer.parseInt(entry.toString());
                        doubleHashMap.put(key, Double.parseDouble(map.get(entry).toString()));
                    }
                    idTfIDf.put(Integer.parseInt(split[0]), doubleHashMap);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            log.info("文件不存在，开始生成");
            generateAllTfDf();
            generateIdNums();
            idTfIDf = calTfIdf();
            printAllDocTfIdf(idTfIDf);
            generateTermID();
        }
        log.info("文件已经读取结束");
        return idTfIDf;
    }

    public static void main(String[] args) {
        AllDocTfIdf allDocTfIdf = new AllDocTfIdf();
        HashMap<Integer, HashMap<Integer, Double>> idTfIDf = allDocTfIdf.loadAllDocTfIdf();
        System.out.println(idTfIDf.size());
    }
}
