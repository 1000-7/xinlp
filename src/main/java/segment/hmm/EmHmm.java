package segment.hmm;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;


/**
 * @Author unclewang
 * @Date 2018-12-02 15:24
 * 利用EM算法计算HMM分词需要的Pi，A，B三个参数
 */
@Slf4j
public class EmHmm {
    protected static final double MIN = -3.14e+100;

    private double precision = 1e-7;
    /**
     * 序列长度，T
     */
    private int sequenceLen;
    /**
     * 观测序列，比如 红白红就是0，1，0
     */
    private int[] sequence;
    /**
     * 初始状态概率
     **/
    protected double[] pi;
    /**
     * 转移概率 A
     **/
    protected double[][] transferProbability;
    /**
     * 发射概率 观测概率 B
     **/
    protected double[][] emissionProbability;
    /**
     * 定义无穷小
     **/
    public static final double INFINITY = Double.MIN_VALUE;
    /**
     * 状态值集合的大小 N
     **/
    protected int stateNum = 4;
    /**
     * 观测值集合的大小 出现了几种可能性，分词是各种汉字的集合
     **/
    protected int observationNum;

    /**
     * 词典和id双向对应map
     */
    private BiMap<String, Integer> wordId;

    private RandomGenerator rg;


    public EmHmm() {
    }

    public void train(int[] sequence) {
        train(sequence, 100, this.precision);
    }

    public void train(int[] sequence, int maxIter) {
        train(sequence, maxIter, this.precision);
    }

    public void train(int[] sequence, int maxIter, double precision) {
        this.sequence = sequence;
        this.sequenceLen = sequence.length;
        fpbp(sequence, maxIter, precision);
    }

    private void fpbp(int[] sequence, int maxIter, double precision) {
        double[][] alpha = new double[sequence.length][stateNum];
        double[][] beta = new double[sequence.length][stateNum];
        double[][] gamma = new double[sequence.length][stateNum];
        if (sequenceLen <= 1) {
            return;
        }
        double[][][] ksi = new double[sequenceLen - 1][stateNum][stateNum];
        int iter = 0;
        while (iter < maxIter) {
            log.info("iter" + iter + "...");
            double[][] oldTransferProbability = backupA();
            //迭代过程
            reCalAlpha(sequence, alpha);
            reCalBeta(sequence, beta);
            reCalGamma(alpha, beta, gamma);
            reCalKsi(sequence, alpha, beta, ksi);
            reCalLambda(sequence, gamma, ksi);
            double error = difference(transferProbability, oldTransferProbability);
            if (error < precision) {
                break;
            }
            iter++;
        }
    }

    protected double[][] backupA() {
        double[][] oldA = new double[stateNum][stateNum];
        for (int i = 0; i < stateNum; i++) {
            for (int j = 0; j < stateNum; j++) {
                oldA[i][j] = transferProbability[i][j];
            }
        }
        return oldA;
    }

    private double difference(double[][] transferProbability, double[][] oldTransferProbability) {
        double res = MIN;
        for (int i = 0; i < transferProbability.length; i++) {
            for (int j = 0; j < transferProbability[0].length; j++) {
                double minus = FastMath.abs(oldTransferProbability[i][j] - transferProbability[i][j]);
                if (minus > res) {
                    res = minus;
                }
            }
        }
        return res;
    }

    private void reCalLambda(int[] sequence, double[][] gamma, double[][][] ksi) {
        updateA(gamma, ksi);
        updateB(sequence, gamma);
        updatePi(gamma);
    }

    private void updatePi(double[][] gamma) {
        log.info("更新初始概率Pi...");
        //idea提示说这样好。。。。
        System.arraycopy(gamma[0], 0, pi, 0, pi.length);
    }

    private void updateB(int[] sequence, double[][] gamma) {
        log.info("更新观测概率B...");
        ArrayList<Double> emission = new ArrayList<>();
        for (int j = 0; j < stateNum; j++) {
            double[] gammaJ = new double[sequenceLen];
            for (int t = 0; t < sequenceLen; t++) {
                gammaJ[t] = gamma[t][j];
            }
            for (int k = 0; k < observationNum; k++) {
                for (int t = 0; t < sequenceLen; t++) {
                    if (sequence[t] == k) {
                        emission.add(gamma[t][j]);
                    }
                }
                double[] emissionArray = new double[emission.size()];
                for (int i = 0; i < emission.size(); i++) {
                    emissionArray[i] = emission.get(i);
                }
                emissionProbability[j][k] = sumLog(emissionArray) - sumLog(gammaJ);
            }
        }
    }

    private void updateA(double[][] gamma, double[][][] ksi) {
        log.info("更新转移概率A...");
        for (int i = 0; i < stateNum; i++) {
            double[] gammaI = new double[ksi.length];
            for (int t = 0; t < ksi.length; t++) {
                gammaI[t] = gamma[t][i];
            }
            for (int j = 0; j < stateNum; j++) {
                double[] ksiSum = new double[ksi.length];
                for (int t = 0; t < ksi.length; t++) {
                    ksiSum[t] = ksi[t][i][j];
                }
                transferProbability[i][j] = sumLog(ksiSum) - sumLog(gammaI);
            }
        }
    }

    private void reCalKsi(int[] sequence, double[][] alpha, double[][] beta, double[][][] ksi) {
        log.info("计算ksi...");
        for (int t = 0; t < sequenceLen - 1; t++) {
            double[] sum = new double[stateNum * stateNum];
            for (int i = 0; i < stateNum; i++) {
                for (int j = 0; j < stateNum; j++) {
                    ksi[t][i][j] = alpha[t][i] + transferProbability[i][j] + emissionProbability[j][sequence[t + 1]] + beta[t + 1][j];
                    sum[i * stateNum + j] = ksi[t][i][j];
                }
            }
            double logSum = sumLog(sum);
            for (int i = 0; i < stateNum; i++) {
                for (int j = 0; j < stateNum; j++) {
                    ksi[t][i][j] /= logSum;
                }
            }
        }
    }

    private void reCalGamma(double[][] alpha, double[][] beta, double[][] gamma) {
        log.info("计算gamma...");
        for (int t = 0; t < sequenceLen; t++) {
            for (int i = 0; i < stateNum; i++) {
                gamma[t][i] = alpha[t][i] + beta[t][i];
            }
            double sum = sumLog(gamma[t]);
            for (int j = 0; j < stateNum; j++) {
                gamma[t][j] = gamma[t][j] - sum;
            }
        }
    }

    private void reCalBeta(int[] sequence, double[][] beta) {
        log.info("计算Beta...");
        for (int i = 0; i < stateNum; i++) {
            beta[sequenceLen - 1][i] = 1;
        }
        double[] bp = new double[stateNum];
        for (int t = sequenceLen - 2; t >= 0; t--) {
            for (int i = 0; i < stateNum; i++) {
                for (int j = 0; j < stateNum; j++) {
                    bp[j] = beta[t + 1][j] + emissionProbability[j][sequence[t + 1]] * transferProbability[i][j];
                }
                beta[t][i] = sumLog(bp);
            }
        }
    }

    //前向算法
    private void reCalAlpha(int[] sequence, double[][] alpha) {
        log.info("计算alpha...");
        for (int i = 0; i < stateNum; i++) {
            alpha[0][i] = pi[i] + emissionProbability[i][sequence[0]];
        }

        double[] fp = new double[stateNum];
        for (int t = 1; t < sequenceLen; t++) {
            for (int i = 0; i < stateNum; i++) {
                for (int j = 0; j < stateNum; j++) {
                    fp[j] = alpha[t - 1][j] + transferProbability[j][i];
                }
                alpha[t][i] = sumLog(fp) + emissionProbability[i][sequence[t]];
            }
        }
    }


    private void randomInit(int seed) {
        rg = new MersenneTwister(seed);
        randomInitPi();
        randomInitA();
        randomInitB();
    }

    private void randomInitPi() {
        log.info("正在初始化参数Pi");
        pi = generate();
        pi[1] = MIN;
        pi[2] = MIN;
    }

    //效果太差，转移矩阵就不随机了
    private void randomInitA() {
        log.info("正在初始化参数A");
        transferProbability = new double[][]{
                {MIN, -0.510825623765990, -0.916290731874155, MIN},
                {-0.5897149736854513, MIN, MIN, -0.8085250474669937},
                {MIN, -0.33344856811948514, -1.2603623820268226, MIN},
                {-0.7211965654669841, MIN, MIN, -0.6658631448798212}};
    }


    //为了找到各种汉字，不使用里面的参数
    private void randomInitB() {
        try {
            log.info("正在初始化参数B");
            String list = FileUtils.readFileToString(new File(System.getProperty("user.dir") + "/src/main/resources/B.json"), "UTF8");
            JSONObject jsonObject = JSON.parseObject(list);
            Map<String, Double> bMap = toDouble(JSON.parseObject(jsonObject.get("B").toString()).getInnerMap());
            Map<String, Double> eMap = toDouble(JSON.parseObject(jsonObject.get("E").toString()).getInnerMap());
            Map<String, Double> mMap = toDouble(JSON.parseObject(jsonObject.get("M").toString()).getInnerMap());
            Map<String, Double> sMap = toDouble(JSON.parseObject(jsonObject.get("S").toString()).getInnerMap());
            HashSet<String> charSet = new HashSet<>(bMap.keySet());
            charSet.addAll(eMap.keySet());
            charSet.addAll(mMap.keySet());
            charSet.addAll(sMap.keySet());
            //一共有多少字
            observationNum = charSet.size();
            emissionProbability = new double[observationNum][stateNum];
            wordId = HashBiMap.create();
            int i = 0;
            for (String s : charSet) {
                wordId.put(s, i);
                emissionProbability[i] = generate();
                i++;
            }

            RealMatrix ordEmission = new Array2DRowRealMatrix(emissionProbability);
            RealMatrix newEmission = ordEmission.transpose();
            emissionProbability = new double[stateNum][observationNum];
            emissionProbability = newEmission.getData();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private double[] generate() {
        NormalDistribution nd = new NormalDistribution(rg, -1, 100);
        double[] oneEmission = new double[4];
        double sum = 0;
        for (int i = 0; i < 4; i++) {
            oneEmission[i] = FastMath.abs(nd.sample());
            sum += oneEmission[i];
        }
        for (int i = 0; i < 4; i++) {
            oneEmission[i] = FastMath.log(oneEmission[i] / sum);
        }
        return oneEmission;
    }

    private static Map<String, Double> toDouble(Map<String, Object> map) {
        Map<String, Double> res = new HashMap<>();
        map.forEach((key, value) -> res.put(key, ((BigDecimal) value).doubleValue()));
        return res;
    }


    public static void print(double[] arrays) {
        for (double array : arrays) {
            System.out.print(array + "\t");
        }
        System.out.print("\n");
    }

    public static void print(double[][] arrays) {
        for (int i = 0; i < arrays.length; i++) {
            for (int j = 0; j < arrays[0].length; j++) {
                System.out.print(arrays[i][j] + "\t");
            }
            System.out.print("\n");
        }
        System.out.println();
    }

    private double sumLog(double[] logArr) {
        if (logArr.length == 0) {
            return MIN;
        }
        double max = max(logArr);
        double result = 0;
        for (int i = 0; i < logArr.length; i++) {
            result += Math.exp(logArr[i] - max);
        }
        return max + Math.log(result);
    }

    private static double max(double[] arr) {
        double max = arr[0];
        for (int i = 1; i < arr.length; i++) {
            max = arr[i] > max ? arr[i] : max;
        }
        return max;
    }

    public String viterbi(int[] observeSequence) {
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
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < observationNum; i++) {
            builder.append(wordId.inverse().get(observeSequence[i]));
            System.out.print(wordId.inverse().get(observeSequence[i]));
            if (mostLikelyStateSequence[i] == 1 || mostLikelyStateSequence[i] == 3) {
                builder.append(" ");
                System.out.print(" ");
            }
        }

        return builder.toString();
    }

    private int[] sentence2int(String s) {
        char[] chars = s.toCharArray();
        int[] integers = new int[chars.length];
        for (int i = 0; i < integers.length; i++) {
            integers[i] = wordId.getOrDefault(String.valueOf(chars[i]), 10001);
        }
        return integers;
    }

    public String viterbi(String s) {
        StringBuilder builder = new StringBuilder();
        String[] sentences = s.split("[,.?;。，]");

        for (String sentence : sentences) {
            builder.append(viterbi(sentence2int(sentence)));
        }
        return builder.toString();
    }

    public static void main(String[] args) throws IOException {
        List<String> fileLines = null;
        try {
            fileLines = FileUtils.readLines(new File(System.getProperty("user.dir") + "/src/main/resources/pku_test.utf8"), "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        EmHmm hmm = new EmHmm();
        hmm.randomInit(100);
        int d = 0;
        FileWriter fw = new FileWriter("viterbiHmm.txt", true);
        for (String s : Objects.requireNonNull(fileLines)) {
            log.info("正在训练第" + d + "行...");
            String[] paras = s.replaceAll("[“”]", "").split("[,.?;。，!:：！？（）]");
            for (String para : paras) {
                d++;
                int len = para.toCharArray().length > 50 ? 50 : para.toCharArray().length;
                hmm.train(hmm.sentence2int(s), len);
                if (d % 100 == 0) {
                    fw.write(d + "\t" + hmm.viterbi("今天的天气很好，出来散心挺不错，武汉大学特别好，提高人民的生活水平") + "\n");
                }
            }
        }
        hmm.viterbi("今天的天气很好，出来散心挺不错，武汉大学特别好，提高人民的生活水平");
    }
}
