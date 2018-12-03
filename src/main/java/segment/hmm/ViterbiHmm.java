package segment.hmm;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import segment.crf.XinCRFConfig;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * viterbi算法
 * 已知状态转移矩阵A、概率观测矩阵B、初始状态概率向量Pi和观测序列O
 * 求可能性最大的状态序列
 * 在分词问题上，状态集合是BEMS
 * O是"武汉大学真美"
 * I是"BMMESS"
 * 下面是Jieba分词的参数
 * Pi是
 * {'B': -0.26268660809250016,
 * 'E': -3.14e+100,
 * 'M': -3.14e+100,
 * 'S': -1.4652633398537678}
 * A是
 * {'B': {'E': -0.510825623765990, 'M': -0.916290731874155},
 * 'E': {'B': -0.5897149736854513, 'S': -0.8085250474669937},
 * 'M': {'E': -0.33344856811948514, 'M': -1.2603623820268226},
 * 'S': {'B': -0.7211965654669841, 'S': -0.6658631448798212}}
 * B是
 * {'B': {'\u4e00': -3.6544978750449433,
 * '\u4e01': -8.125041941842026,
 * '\u4e03': -7.817392401429855,
 * '\u4e07': -6.3096425804013165,
 * '\u4e08': -8.866689067453933,
 * '\u4e09': -5.932085850549891,
 * '\u4e0a': -5.739552583325728,
 * '\u4e0b': -5.997089097239644,
 * '\u4e0d': -4.274262055936421,
 * '\u4e0e': -8.355569307500769,
 * ...},
 * 'E': {'\u4e00': -6.044987536255073,
 * '\u4e01': -9.075800412310807,
 * '\u4e03': -9.198842005220659,
 * '\u4e07': -7.655326112989935,
 * '\u4e08': -9.02382100266782,
 * '\u4e09': -7.978829805438807,
 * '\u4e0a': -5.323135439997585,
 * '\u4e0b': -5.739644714409899,
 * ...},
 * 'M': {...},
 * 'S': {...}
 * }
 *
 * @author unclewang
 */
@Data
@Slf4j
public class ViterbiHmm {

    private static char[] state = new char[]{'B', 'E', 'M', 'S'};
    /**
     * 状态值集合的大小 N
     **/
    protected static int stateNum = state.length;

    protected static final Double MIN = -3.14e+100;
    /**
     * 初始状态概率Pi
     **/
    protected Double[] pi;
    /**
     * 转移概率A
     **/
    protected Double[][] transferProbability;
    /**
     * 发射概率B
     **/
    protected Double[][] emissionProbability;

    /**
     * 观测值集合的大小 出现了几种可能性，红白球的话就是2,分词的话机会是词表的长度
     **/
    protected int observationNum;

    /**
     * 观测序列O，比如 武汉大学真美
     */
    private Integer[] observeSequence;
    /**
     * 词典和id双向对应map
     */
    private BiMap<String, Integer> wordId;

    /**
     * 使用jieba分词使用的概率
     */
    public void initLambda() {
        initPi();
        initA();
        initB();
    }


    /**
     * 维特比算法
     */
    public void viterbi(String s) {
        initLambda();
        String[] sentences = s.split("[,.?;。，]");
        for (String sentence : sentences) {
            viterbi(str2int(sentence));
        }
    }


    public void viterbi(Integer[] observeSequence) {
        observationNum = observeSequence.length;
        Integer[][] path = new Integer[observationNum][stateNum];
        Double[][] deltas = new Double[observationNum][stateNum];

        for (int i = 0; i < stateNum; i++) {
            deltas[0][i] = pi[i] + emissionProbability[i][observeSequence[0]];
            path[0][i] = i;
        }

        for (int t = 1; t < observationNum; t++) {
            for (int i = 0; i < stateNum; i++) {
                deltas[t][i] = deltas[t - 1][0] + transferProbability[0][i];
                path[t][i] = 0;
                for (int j = 1; j < stateNum; j++) {
                    double tmp = deltas[t - 1][j] + transferProbability[j][i];
                    if (tmp > deltas[t][i]) {
                        deltas[t][i] = tmp;
                        path[t][i] = j;
                    }
                }
                deltas[t][i] += emissionProbability[i][observeSequence[t]];
            }
        }

        //找最优路径，注意最后一个字不是所有状态的最大值，而是E(1)和S(3)的最大值
        Integer[] mostLikelyStateSequence = new Integer[observationNum];
        mostLikelyStateSequence[observationNum - 1] = deltas[observationNum - 1][1] >= deltas[observationNum - 1][3] ? 1 : 3;

        for (int i = mostLikelyStateSequence.length - 2; i >= 0; i--) {
            mostLikelyStateSequence[i] = path[i + 1][mostLikelyStateSequence[i + 1]];
        }
        for (int i = 0; i < observationNum; i++) {
            System.out.print(wordId.inverse().get(observeSequence[i]));
            if (mostLikelyStateSequence[i] == 1 || mostLikelyStateSequence[i] == 3) {
                System.out.print(" ");
            }
        }

    }

    public Integer[] str2int(String s) {
        char[] chars = s.toCharArray();
        Integer[] res = new Integer[chars.length];
        for (int i = 0; i < chars.length; i++) {
            res[i] = wordId.getOrDefault(String.valueOf(chars[i]), 1);
        }
        return res;
    }


    private void initB() {
        try {
            String list = FileUtils.readFileToString(new File(System.getProperty("user.dir") + "/src/main/resources/B.json"), "UTF8");
            JSONObject jsonObject = JSON.parseObject(list);
            Map<String, Double> bMap = toDouble(JSON.parseObject(jsonObject.get("B").toString()).getInnerMap());
            Map<String, Double> eMap = toDouble(JSON.parseObject(jsonObject.get("E").toString()).getInnerMap());
            Map<String, Double> mMap = toDouble(JSON.parseObject(jsonObject.get("M").toString()).getInnerMap());
            Map<String, Double> sMap = toDouble(JSON.parseObject(jsonObject.get("S").toString()).getInnerMap());
            HashSet<String> wordSet = new HashSet<>(bMap.keySet());
            wordSet.addAll(eMap.keySet());
            wordSet.addAll(mMap.keySet());
            wordSet.addAll(sMap.keySet());
            emissionProbability = new Double[stateNum][wordSet.size()];
            wordId = HashBiMap.create();
            int i = 0;
            for (String s : wordSet) {
                wordId.put(s, i);
                emissionProbability[0][i] = bMap.getOrDefault(s, MIN);
                emissionProbability[1][i] = eMap.getOrDefault(s, MIN);
                emissionProbability[2][i] = mMap.getOrDefault(s, MIN);
                emissionProbability[3][i] = sMap.getOrDefault(s, MIN);
                i++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Map<String, Double> toDouble(Map<String, Object> map) {
        Map<String, Double> res = new HashMap<>();
        map.forEach((key, value) -> res.put(key, ((BigDecimal) value).doubleValue()));
        return res;
    }


    private void initA() {
        transferProbability = new Double[][]{
                {MIN, -0.510825623765990, -0.916290731874155, MIN},
                {-0.5897149736854513, MIN, MIN, -0.8085250474669937},
                {MIN, -0.33344856811948514, -1.2603623820268226, MIN},
                {-0.7211965654669841, MIN, MIN, -0.6658631448798212}};
    }

    private void initPi() {
        pi = new Double[]{-0.26268660809250016, -3.14e+100, -3.14e+100, -1.4652633398537678};
    }

    public <T extends Object> void print(T[] nums) {
        for (T a : nums) {
            System.out.print(a + "\t");
        }
        System.out.println();
    }

    public <T extends Object> void print(T[][] nums) {
        for (int i = 0; i < nums.length; i++) {
            for (int j = 0; j < nums[0].length; j++) {
                System.out.print(nums[i][j] + "\t");
            }
            System.out.println();
        }
        System.out.println();
    }

    @Test
    public void segmentTest() {
        initLambda();
        viterbi("今天的天气很好，出来散心挺不错，武汉大学特别好，提高人民的生活水平");
    }
}
