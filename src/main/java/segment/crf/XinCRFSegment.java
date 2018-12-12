package segment.crf;

import lombok.Data;
import lombok.extern.java.Log;
import lucene.simple.Atom;
import segment.Segment;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @Author unclewang
 * @Date 2018-11-27 18:59
 */
@Log
@Data
public class XinCRFSegment implements Segment {
    private static XinCRFModel xinCRFModel;
    private static DoubleArrayTrie<FeatureFunction> featureFunctionTrie;
    private static List<FeatureTemplate> featureTemplateList;
    private static Double[][] matrix;
    private static String[] id2tag;
    private static Map<String, Integer> tag2id;

    static {
        log.info("模型正在加载中");
        log.info("尝试直接读取模型文件");
        xinCRFModel = XinCRFModel.load(XinCRFConfig.getModelPath());
        if (xinCRFModel == null) {
            log.info("直接读取模型文件失败，重新构建XinCRFModel");
            long start = System.currentTimeMillis();
            xinCRFModel = XinCRFModel.getInstance();
            log.info("重新创建模型成功，共耗时" + (System.currentTimeMillis() - start) + "ms");
        }
        featureFunctionTrie = xinCRFModel.getFeatureFunctionTrie();
        featureTemplateList = xinCRFModel.getFeatureTemplateList();
        matrix = xinCRFModel.getMatrix();
        id2tag = xinCRFModel.getId2tag();
        tag2id = xinCRFModel.getTag2id();
        assert xinCRFModel != null;
//        XinCRFConfig.print(matrix);
    }

    public String viterbi(String sentence) {
        XinTable table = sentence2XinTable(sentence);
        return viterbi(table);
    }

    private String viterbi(XinTable table) {
        int observationNum = table.size();
        if (observationNum == 0) {
            return "";
        }
        int stateNum = xinCRFModel.getId2tag().length;

        Double[][] emissionProbability = new Double[observationNum][stateNum];
        for (int t = 0; t < observationNum; t++) {
            //找到t时刻的观测值所有相关的模版以及模版的得分
            LinkedList<double[]> scoreList = findAllTemplateOfTableI(table, t);
            for (int i = 0; i < stateNum; i++) {
                //找到t时刻的观测值为各种状态的得分
                emissionProbability[t][i] = computeScore(scoreList, i);
            }
        }

//        XinCRFConfig.print(emissionProbability);
        if (observationNum == 1) {
            double maxScore = -1e10;
            int bestTag = 0;
            for (int tag = 0; tag < emissionProbability[0].length; ++tag) {
                if (emissionProbability[0][tag] > maxScore) {
                    maxScore = emissionProbability[0][tag];
                    bestTag = tag;
                }
            }
            table.setLast(0, id2tag[bestTag]);
            return "";
        }
        Integer[][] path = new Integer[observationNum][stateNum];
        Double[][] deltas = new Double[observationNum][stateNum];
        for (int i = 0; i < stateNum; i++) {
            deltas[0][i] = emissionProbability[0][i];
            path[0][i] = i;
        }
        for (int t = 1; t < observationNum; t++) {
            for (int i = 0; i < stateNum; i++) {
                double maxScore = -1e10;
                for (int j = 0; j < stateNum; j++) {
                    double tmp = deltas[t - 1][j] + matrix[j][i] + emissionProbability[t][i];
                    if (tmp > maxScore) {
                        maxScore = tmp;
                        deltas[t][i] = tmp;
                        path[t][i] = j;
                    }
                }
            }
        }


//        XinCRFConfig.print(path);
//        XinCRFConfig.print(deltas);


        if (deltas[observationNum - 1][1] > deltas[observationNum - 1][3]) {
            table.v[observationNum - 1][1] = id2tag[1];
        } else {
            table.v[observationNum - 1][1] = id2tag[3];
        }

        //找最优路径，注意最后一个字不是所有状态的最大值，而是E(1)和S(3)的最大值
        for (int i = observationNum - 2; i >= 0; i--) {
            table.v[i][1] = id2tag[path[i + 1][tag2id.get(table.v[i + 1][1])]];
//            table.setLast(i, id2tag[path[i + 1][tag2id.get(table.get(i + 1, 1))]]);
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < table.v.length; i++) {
            sb.append(table.v[i][0]);
            if ("S".equals(table.v[i][1]) || "E".equals(table.v[i][1])) {
                sb.append("\t");
            }
        }
        System.out.println(sb.toString());
        return sb.toString();
    }

    //计算所有模版在某个隐藏状态值的得分
    private Double computeScore(LinkedList<double[]> scoreList, int i) {
        double score = 0.0;
        for (double[] w : scoreList) {
            score += w[i];
        }
        return score;
    }


    /**
     * 找到所有具体实例的模版函数
     *
     * @param table
     * @param current
     */
    private LinkedList<double[]> findAllTemplateOfTableI(XinTable table, int current) {
        LinkedList<double[]> scoreList = new LinkedList<>();
        for (FeatureTemplate ft : featureTemplateList) {
            //找到所有具体实例的模版函数
            char[] o = ft.generateParameter(table, current);
            //找到模版函数的参数
            FeatureFunction featureFunction = featureFunctionTrie.getFunction(o);
            //存在函数
            if (featureFunction != null) {
                scoreList.add(featureFunction.getW());
            }
        }
        return scoreList;
    }


    private XinTable sentence2XinTable(String sentence) {
        char[] chars = sentence.toCharArray();
        int size = chars.length;
        String[][] v = new String[size][2];
        for (int i = 0; i < size; i++) {
            v[i][0] = String.valueOf(chars[i]);
            v[i][1] = "?";
        }
        XinTable xinTable = new XinTable();
        xinTable.setV(v);
        return xinTable;
    }


    public static void main(String[] args) {
        XinCRFSegment xinCRFSegment = new XinCRFSegment();
        xinCRFSegment.viterbi("今天天气很好");
        xinCRFSegment.viterbi("你好");
        xinCRFSegment.viterbi("商品和服务");
        xinCRFSegment.viterbi("武汉大学非常美");
        xinCRFSegment.viterbi("我是中国人");
        xinCRFSegment.viterbi("迈向充满希望的新司机");
        xinCRFSegment.viterbi("香港特别行政区");
    }

    @Override
    public List<Atom> seg(String text) {
        String[] strings = viterbi(text).trim().split("[\t\n]");
        return strings2AtomList(strings);
    }
}
