package learn.hmm.baumwelch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;

/**
 * 无监督学习的HMM实现
 * 少量数据建议串行
 * 大量数据，几十万，百万甚至更高的数据强烈建议并行训练,性能是串行的好4倍以上
 *
 * @author outsider
 */
public class UnsupervisedFirstOrderGeneralHmm {
    private double precision = 1e-7;
    /**
     * 训练数据长度
     */
    private int sequenceLen;
    public Logger logger = Logger.getLogger(UnsupervisedFirstOrderGeneralHmm.class.getName());
    /**
     * 初始状态概率
     **/
    protected double[] pi;
    /**
     * 转移概率
     **/
    protected double[][] transferProbability1;
    /**
     * 发射概率
     **/
    protected double[][] emissionProbability;
    /**
     * 定义无穷大
     **/
    public static final double INFINITY = (double) -Math.pow(2, 31);
    /**
     * 状态值集合的大小
     **/
    protected int stateNum;
    /**
     * 观测值集合的大小
     **/
    protected int observationNum;

    public UnsupervisedFirstOrderGeneralHmm() {
        super();
    }

    public UnsupervisedFirstOrderGeneralHmm(int stateNum, int observationNum, double[] pi,
                                            double[][] transferProbability1, double[][] emissionProbability) {
        this.stateNum = stateNum;
        this.observationNum = observationNum;
        this.pi = pi;
        this.transferProbability1 = transferProbability1;
        this.emissionProbability = emissionProbability;
    }

    public UnsupervisedFirstOrderGeneralHmm(int stateNum, int observationNum) {
        this.stateNum = stateNum;
        this.observationNum = observationNum;
        initParameters();
    }
    /**
     * λ是HMM参数的总称
     */

    /**
     * 训练方法
     *
     * @param x         训练序列数据
     * @param maxIter   最大迭代次数
     * @param precision 精度
     */
    public void train(int[] x, int maxIter, double precision) {
        this.sequenceLen = x.length;
        baumWelch(x, maxIter, precision);
    }


    /**
     * baumWelch算法迭代求解
     * 迭代时存在这样的现象：新参数和上一次的参数差反而会变大，但是到后面这个误差值几乎会收敛
     * 所以迭代终止的条件有2个：
     * 1.达到最大迭代次数
     * 2.参数A，B，pi中的值相比上一次的最大误差小于某个精度值则认为收敛
     * 3.若1中给的精度值太大，则可能导致无法收敛，所以增加了一个条件，如果当前迭代的误差和上一次迭代的误差小于某个值（这里给定1e-7），
     * 可以认为收敛了。
     *
     * @param x         观测序列
     * @param maxIter   最大迭代次数，如果传入<=0的数则默认为Integer.MAX_VALUE,相当于不收敛就不跳出循环
     * @param precision 参数误差的精度小于precision就认为收敛
     */
    protected void baumWelch(int[] x, int maxIter, double precision) {
        int iter = 0;
        double oldMaxError = 0;
        if (maxIter <= 0) {
            maxIter = Integer.MAX_VALUE;
        }
        //初始化各种参数
        double[][] alpha = new double[sequenceLen][stateNum];
        double[][] beta = new double[sequenceLen][stateNum];
        double[][] gamma = new double[sequenceLen][stateNum];
        double[][][] ksi = new double[sequenceLen][stateNum][stateNum];
        while (iter < maxIter) {
            logger.info("\niter" + iter + "...");
            long start = System.currentTimeMillis();
            //计算各种参数，为更新模型参数做准备，对应EM中的E步
            calcAlpha(x, alpha);
            calcBeta(x, beta);
            calcGamma(x, alpha, beta, gamma);
            calcKsi(x, alpha, beta, ksi);
            //更新参数，对应EM中的M步
            double[][] oldA = generateOldA();
            //double[][] oldB = generateOldB();
            //double[] oldPi = pi.clone();
            updateLambda(x, gamma, ksi);
            //double maxError = calcError(oldA, oldPi, oldB);
            double maxError = calcError(oldA, null, null);
            logger.info("max_error:" + maxError);
            if (maxError < precision || (Math.abs(maxError - oldMaxError)) < this.precision) {
                logger.info("参数已收敛....");
                break;
            }
            oldMaxError = maxError;
            iter++;
            long end = System.currentTimeMillis();
            logger.info("本次迭代结束,耗时:" + (end - start) + "毫秒");
        }
        logger.info("最终参数:");
        logger.info("pi:" + Arrays.toString(pi));
        logger.info("A:");
        for (int i = 0; i < transferProbability1.length; i++) {
            logger.info(Arrays.toString(transferProbability1[i]));
        }
    }

    /**
     * 保存旧的参数A
     *
     * @return
     */
    protected double[][] generateOldA() {
        double[][] oldA = new double[stateNum][stateNum];
        for (int i = 0; i < stateNum; i++) {
            for (int j = 0; j < stateNum; j++) {
                oldA[i][j] = transferProbability1[i][j];
            }
        }
        return oldA;
    }

    /**
     * 暂时只计算参数A的误差
     * 发现计算B和pi会发现参数误差越来越大的现象，基本不能收敛
     *
     * @return
     */
    protected double calcError(double[][] oldA, double[] oldPi, double[][] oldB) {
        double maxError = 0;
        for (int i = 0; i < stateNum; i++) {
			/*double tmp1 = Math.abs(pi[i] - oldPi[i]);
			maxError = tmp1 > maxError ? tmp1 : maxError;*/
            for (int j = 0; j < stateNum; j++) {
                double tmp = Math.abs(oldA[i][j] - transferProbability1[i][j]);
                maxError = tmp > maxError ? tmp : maxError;
            }
			/*for(int k =0; k < observationNum; k++) {
				double tmp2 = Math.abs(emissionProbability[i][k] - oldB[i][k]);
				maxError = tmp2 > maxError ? tmp2 : maxError;
			}*/
        }
        return maxError;
    }

    /**
     * 概率初始化为0
     */
    public void initParameters() {
        //初始概率随机初始化
        pi = new double[stateNum];
        transferProbability1 = new double[stateNum][stateNum];
        emissionProbability = new double[stateNum][observationNum];
        //概率初始化为0
        for (int i = 0; i < stateNum; i++) {
            pi[i] = INFINITY;
            for (int j = 0; j < stateNum; j++) {
                transferProbability1[i][j] = INFINITY;
            }
            for (int k = 0; k < observationNum; k++) {
                emissionProbability[i][k] = INFINITY;
            }
        }
    }

    /**
     * 数组求和
     *
     * @param arr
     * @return
     */
    public static double sum(double[] arr) {
        double sum = 0;
        for (int i = 0; i < arr.length; i++) {
            sum += arr[i];
        }
        return sum;
    }



    /**
     * 前向算法，根据当前参数λ计算α
     * α是一个序列长度*状态长度的矩阵
     * 已检测，应该没有问题
     */
    protected void calcAlpha(int[] x, double[][] alpha) {
        logger.info("计算alpha...");
        long start = System.currentTimeMillis();
        //double[][] alpha = new double[sequenceLen][stateNum];
        //alpha t=0初始值
        for (int i = 0; i < stateNum; i++) {
            alpha[0][i] = pi[i] + emissionProbability[i][x[0]];
        }
        double[] logProbaArr = new double[stateNum];
        for (int t = 1; t < sequenceLen; t++) {
            for (int i = 0; i < stateNum; i++) {
                for (int j = 0; j < stateNum; j++) {
                    logProbaArr[j] = (alpha[t - 1][j] + transferProbability1[j][i]);
                }
                alpha[t][i] = logSum(logProbaArr) + emissionProbability[i][x[t]];
            }
        }
        long end = System.currentTimeMillis();
        logger.info("计算结束...耗时:" + (end - start) + "毫秒");
        //return alpha;
    }

    /**
     * 后向算法，根据当前参数λ计算β
     *
     * @param x
     */
    protected void calcBeta(int[] x, double[][] beta) {
        logger.info("计算beta...");
        long start = System.currentTimeMillis();
        //double[][] beta = new double[sequenceLen][stateNum];
        //初始概率beta[T][i] = 1
        for (int i = 0; i < stateNum; i++) {
            beta[sequenceLen - 1][i] = 1;
        }
        double[] logProbaArr = new double[stateNum];
        for (int t = sequenceLen - 2; t >= 0; t--) {
            for (int i = 0; i < stateNum; i++) {
                for (int j = 0; j < stateNum; j++) {
                    logProbaArr[j] = transferProbability1[i][j] +
                            emissionProbability[j][x[t + 1]] +
                            beta[t + 1][j];
                }
                beta[t][i] = logSum(logProbaArr);
            }
        }
        long end = System.currentTimeMillis();
        logger.info("计算结束...耗时:" + (end - start) + "毫秒");
        //return beta;
    }

    /**
     * 根据当前参数λ计算ξ
     *
     * @param x     观测结点
     * @param alpha 前向概率
     * @param beta  后向概率
     */
    protected void calcKsi(int[] x, double[][] alpha, double[][] beta, double[][][] ksi) {
        logger.info("计算ksi...");
        long start = System.currentTimeMillis();
        //double[][][] ksi = new double[sequenceLen][stateNum][stateNum];
        double[] logProbaArr = new double[stateNum * stateNum];
        for (int t = 0; t < sequenceLen - 1; t++) {
            int k = 0;
            for (int i = 0; i < stateNum; i++) {
                for (int j = 0; j < stateNum; j++) {
                    ksi[t][i][j] = alpha[t][i] + transferProbability1[i][j] +
                            emissionProbability[j][x[t + 1]] + beta[t + 1][j];
                    logProbaArr[k++] = ksi[t][i][j];
                }
            }
            double logSum = logSum(logProbaArr);//分母
            for (int i = 0; i < stateNum; i++) {
                for (int j = 0; j < stateNum; j++) {
                    ksi[t][i][j] -= logSum;//分子除分母
                }
            }
        }
        long end = System.currentTimeMillis();
        logger.info("计算结束...耗时:" + (end - start) + "毫秒");
        //return ksi;
    }

    /**
     * 根据当前参数λ，计算γ
     *
     * @param x
     */
    protected void calcGamma(int[] x, double[][] alpha, double[][] beta, double[][] gamma) {
        logger.info("计算gamma...");
        long start = System.currentTimeMillis();
        //double[][] gamma  = new double[sequenceLen][stateNum];
        for (int t = 0; t < sequenceLen; t++) {
            //分母需要求LogSum
            for (int i = 0; i < stateNum; i++) {
                gamma[t][i] = alpha[t][i] + beta[t][i];
            }
            double logSum = logSum(gamma[t]);//分母部分
            for (int j = 0; j < stateNum; j++) {
                gamma[t][j] = gamma[t][j] - logSum;
            }
        }
        long end = System.currentTimeMillis();
        logger.info("计算结束...耗时:" + (end - start) + "毫秒");
        //return gamma;
    }

    /**
     * 更新参数
     */
    protected void updateLambda(int[] x, double[][] gamma, double[][][] ksi) {
        //顺序可以颠倒
        updatePi(gamma);
        updateA(ksi, gamma);
        updateB(x, gamma);
    }

    /**
     * 更新参数pi
     *
     * @param gamma
     */
    public void updatePi(double[][] gamma) {
        //更新HMM中的参数pi
        for (int i = 0; i < stateNum; i++) {
            pi[i] = gamma[0][i];
        }
    }

    /**
     * 更新参数A
     *
     * @param ksi
     * @param gamma
     */
    protected void updateA(double[][][] ksi, double[][] gamma) {
        logger.info("更新参数转移概率A...");
        ////由于在更新A都要用到对不同状态的前T-1的gamma值求和，所以这里先算
        double[] gammaSum = new double[stateNum];
        double[] tmp = new double[sequenceLen - 1];
        for (int i = 0; i < stateNum; i++) {
            for (int t = 0; t < sequenceLen - 1; t++) {
                tmp[t] = gamma[t][i];
            }
            gammaSum[i] = logSum(tmp);
        }
        long start1 = System.currentTimeMillis();
        //更新HMM中的参数A
        double[] ksiLogProbArr = new double[sequenceLen - 1];
        for (int i = 0; i < stateNum; i++) {
            for (int j = 0; j < stateNum; j++) {
                for (int t = 0; t < sequenceLen - 1; t++) {
                    ksiLogProbArr[t] = ksi[t][i][j];
                }
                transferProbability1[i][j] = logSum(ksiLogProbArr) - gammaSum[i];
            }
        }
        long end1 = System.currentTimeMillis();
        logger.info("更新完毕...耗时:" + (end1 - start1) + "毫秒");
    }

    /**
     * 更新参数B
     *
     * @param x
     * @param gamma
     */
    protected void updateB(int[] x, double[][] gamma) {
        //下面需要用到gamma求和为了减少重复计算，这里直接先计算
        //由于在更新B时都要用到对不同状态的所有gamma值求和，所以这里先算
        double[] gammaSum2 = new double[stateNum];
        double[] tmp2 = new double[sequenceLen];
        for (int i = 0; i < stateNum; i++) {
            for (int t = 0; t < sequenceLen; t++) {
                tmp2[t] = gamma[t][i];
            }
            gammaSum2[i] = logSum(tmp2);
        }
        logger.info("更新状态下分布概率B...");
        long start2 = System.currentTimeMillis();
        ArrayList<Double> valid = new ArrayList<Double>();
        for (int i = 0; i < stateNum; i++) {
            for (int k = 0; k < observationNum; k++) {
                valid.clear();//由于这里没有初始化造成了计算出错的问题
                for (int t = 0; t < sequenceLen; t++) {
                    if (x[t] == k) {
                        valid.add(gamma[t][i]);
                    }
                }
                //B[i][k]，i状态下k的分布为概率0，
                if (valid.size() == 0) {
                    emissionProbability[i][k] = INFINITY;
                    continue;
                }
                //对分子求logSum
                double[] validArr = new double[valid.size()];
                for (int q = 0; q < valid.size(); q++) {
                    validArr[q] = valid.get(q);
                }
                double validSum = logSum(validArr);
                //分母的logSum已经在上面做了
                emissionProbability[i][k] = validSum - gammaSum2[i];
            }
        }
        long end2 = System.currentTimeMillis();
        logger.info("更新完毕...耗时:" + (end2 - start2) + "毫秒");
    }


    /**
     * logSum计算技巧
     *
     * @return
     */
    public double logSum(double[] logProbaArr) {
        if (logProbaArr.length == 0) {
            return INFINITY;
        }
        double max = max(logProbaArr);
        double result = 0;
        for (int i = 0; i < logProbaArr.length; i++) {
            result += Math.exp(logProbaArr[i] - max);
        }
        return max + Math.log(result);
    }

    /**
     * 设置先验概率pi
     * 必须传入取对数后的概率
     *
     * @param pi
     */
    public void setPriorPi(double[] pi) {
        this.pi = pi;
    }

    /**
     * 设置先验转移概率A
     * 必须传入取对数的概率
     *
     * @param trtransferProbability1
     */
    public void setPriorTransferProbability1(double[][] trtransferProbability1) {
        this.transferProbability1 = trtransferProbability1;
    }

    /**
     * 设置先验状态下的观测分布概率，B
     * 必须传入取对数的概率
     *
     * @param emissionProbability
     */
    public void setPriorEmissionProbability(double[][] emissionProbability) {
        this.emissionProbability = emissionProbability;
    }

    public static double max(double[] arr) {
        double max = arr[0];
        for (int i = 1; i < arr.length; i++) {
            max = arr[i] > max ? arr[i] : max;
        }
        return max;
    }

    /**
     * 维特比解码
     *
     * @param O 观测序列,输入的是经过编码处理的，而不是原始数据，
     *          比如，如果序列是字符串，那么输入必须是一系列的字符的编码而不是字符本身
     * @return 返回预测结果，
     */
    public int[] verterbi(int[] O) {
        double[][] deltas = new double[O.length][this.stateNum];
        //保存deltas[t][i]的值是由上一个哪个状态产生的
        int[][] states = new int[O.length][this.stateNum];
        //初始化deltas[0][]
        for (int i = 0; i < this.stateNum; i++) {
            deltas[0][i] = pi[i] + emissionProbability[i][O[0]];
        }
        //计算deltas
        for (int t = 1; t < O.length; t++) {
            for (int i = 0; i < this.stateNum; i++) {
                deltas[t][i] = deltas[t - 1][0] + transferProbability1[0][i];
                for (int j = 1; j < this.stateNum; j++) {
                    double tmp = deltas[t - 1][j] + transferProbability1[j][i];
                    if (tmp > deltas[t][i]) {
                        deltas[t][i] = tmp;
                        states[t][i] = j;
                    }
                }
                deltas[t][i] += emissionProbability[i][O[t]];
            }
        }
        //回溯找到最优路径
        int[] predict = new int[O.length];
        double max = deltas[O.length - 1][0];
        for (int i = 1; i < this.stateNum; i++) {
            if (deltas[O.length - 1][i] > max) {
                max = deltas[O.length - 1][i];
                predict[O.length - 1] = i;
            }
        }
        for (int i = O.length - 2; i >= 0; i--) {
            predict[i] = states[i + 1][predict[i + 1]];
        }
        return predict;
    }

    //测试
    public static void main(String[] args) {
        UnsupervisedFirstOrderGeneralHmm hmm = new UnsupervisedFirstOrderGeneralHmm(4, 65536);
        //关闭日志打印
        //CONLPLogger.closeLogger(hmm.logger);
        //由于是监督学习的语料所以这里需要去掉其中的分隔符
        String path = "/Users/unclewang/Idea_Projects/xinlp/src/main/resources/pku_training.splitBy2space.utf8";
        String data = IOUtils.readText(path, "utf-8");
        String[] d2 = data.split("  ");
        StringBuilder sb = new StringBuilder();
        for (String word : d2) {
            sb.append(word);
        }
        data = sb.toString();
        //训练数据
        int[] x = SegmentationUtils.str2int(data);
        //由于串行很慢，可以只取训练数据的前10000个来训练
        int[] minX = new int[10000];
        System.arraycopy(x, 0, minX, 0, 10000);
        //训练之前设置先验概率，必须设置，EM对初始值敏感，如果不设置默认为都为0，所有参数都将一样，没有意义
        //如果只给了其中一些参数的先验值，可以随机初始化其他参数，例如
        //hmm.randomInitA();
        //hmm.randomInitB();
        //hmm.randomInitPi();
//        hmm.randomInitAllParameters();
        //设置先验信息至少设置参数pi，A，B中的一个
        hmm.setPriorPi(new double[]{-1.138130826175848, -2.632826946498266, -1.138130826175848, -1.2472622308278396});
        hmm.setPriorTransferProbability1((double[][]) IOUtils.readObject("src/A"));
        hmm.setPriorEmissionProbability((double[][]) IOUtils.readObject("src/B"));
        //开始训练
        hmm.train(minX, -1, 0.5);
        String str = "原标题：日媒拍到了现场罕见一幕" +
                "据日本新闻网（NNN）9月8日报道，日前，日本海上自卫队现役最大战舰之一的直升机航母“加贺”号在南海航行时，遭多艘中国海军战舰抵近跟踪监视。";
        //将词转换为对应的Unicode码
        int[] O = SegmentationUtils.str2int(str);
        int[] predict = hmm.verterbi(O);
        System.out.println(Arrays.toString(predict));
        String[] res = SegmentationUtils.decode(predict, str);
        System.out.println(Arrays.toString(res));
    }
}		
 

