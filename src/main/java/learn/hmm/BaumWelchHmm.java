package learn.hmm;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

import java.util.ArrayList;
import java.util.Random;

/**
 * @Author unclewang
 * @Date 2018/11/20 00:11
 * hmm参数训练
 */
@Getter
@Setter
@Slf4j
public class BaumWelchHmm {
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
     * 转移概率
     **/
    protected double[][] transferProbability;
    /**
     * 发射概率 观测概率
     **/
    protected double[][] emissionProbability;
    /**
     * 定义无穷大
     **/
    public static final double INFINITY = Double.MIN_VALUE;
    /**
     * 状态值集合的大小 N
     **/
    protected int stateNum;
    /**
     * 观测值集合的大小 出现了几种可能性，红白球的话就是2
     **/
    protected int observationNum;

    public BaumWelchHmm() {
    }

    public BaumWelchHmm(double[] pi, double[][] transferProbability, double[][] emissionProbability, int stateNum, int observationNum) {
        this.pi = pi;
        this.transferProbability = transferProbability;
        this.emissionProbability = emissionProbability;
        this.stateNum = stateNum;
        this.observationNum = observationNum;
    }

    public BaumWelchHmm(int stateNum, int observationNum) {
        this.stateNum = stateNum;
        this.observationNum = observationNum;
        initParameters();
    }

    private void initParameters() {
        //初始概率随机初始化
        pi = new double[stateNum];
        transferProbability = new double[stateNum][stateNum];
        emissionProbability = new double[stateNum][observationNum];
        Random random = new Random();
        random.setSeed(100);
        for (int i = 0; i < stateNum; i++) {
            pi[i] = Math.abs(random.nextInt(100));
            for (int j = 0; j < stateNum; j++) {
                transferProbability[i][j] = Math.abs(random.nextDouble()) * 100;
            }
            for (int j = 0; j < observationNum; j++) {
                emissionProbability[i][j] = Math.abs(random.nextDouble()) * 100;
            }
        }
        //归一化
        double sumPi = sum(pi);
        for (int i = 0; i < stateNum; i++) {
            pi[i] = pi[i] / sumPi;
            double sumTransferProbability = sum(transferProbability[i]);
            double sumEmissionProbability = sum(transferProbability[i]);
            for (int j = 0; j < stateNum; j++) {
                transferProbability[i][j] = transferProbability[i][j] / sumTransferProbability;
            }
            for (int j = 0; j < observationNum; j++) {
                emissionProbability[i][j] = emissionProbability[i][j] / sumEmissionProbability;
            }
        }
    }


    /**
     * @param maxIter 迭代次数
     */
    public void train(int maxIter) {
        train(this.sequence, maxIter);
    }

    /**
     * @param sequence 序列
     * @param maxIter  迭代次数
     */
    public void train(int[] sequence, int maxIter) {
        train(sequence, maxIter, this.precision);
    }

    /**
     * @param sequence  序列
     * @param maxIter   迭代次数
     * @param precision 精度
     */
    public void train(int[] sequence, int maxIter, double precision) {
        this.sequence = sequence;
        this.sequenceLen = sequence.length;
        baumWelch(sequence, maxIter, precision);
    }

    /**
     * 两个条件，结束算法
     * 1.达到最大迭代次数
     * 2.两次迭代，误差小于精度
     *
     * @param sequence
     * @param maxIter
     * @param precision
     */
    private void baumWelch(int[] sequence, int maxIter, double precision) {
        int iter = 0;
        //局部变量占用栈内存，但是数组占用堆内存，所以可直接赋值更新
        double[][] alpha = new double[sequenceLen][stateNum];
        double[][] beta = new double[sequenceLen][stateNum];
        double[][] gamma = new double[sequenceLen][stateNum];
        /**
         * ksi的时刻只到T-1
         */
        double[][][] ksi = new double[sequenceLen - 1][stateNum][stateNum];
        while (iter < maxIter) {
            log.info("\niter" + iter + "...");
            long start = System.currentTimeMillis();
            //迭代过程
            reCalAlpha(sequence, alpha);
            reCalBeta(sequence, beta);
            reCalGamma(alpha, beta, gamma);
            reCalKsi(sequence, alpha, beta, ksi);
            updateLambda(sequence, gamma, ksi);
            print(transferProbability);
            print(emissionProbability);
            iter++;
            long end = System.currentTimeMillis();

            log.info("本次迭代结束,耗时:" + (end - start) + "毫秒");
        }
    }

    private void updateLambda(int[] sequence, double[][] gamma, double[][][] ksi) {
        updateTransferProbability(gamma, ksi);
        updateEmissionProbability(sequence, gamma);
        updatePi(gamma);
    }

    /**
     * @param gamma
     */
    private void updatePi(double[][] gamma) {
        for (int i = 0; i < stateNum; i++) {
            pi[i] = gamma[0][i];
        }
    }


    private void updateEmissionProbability(int[] sequence, double[][] gamma) {
        log.info("更新发射概率（观测概率）...");
        long start = System.currentTimeMillis();
        RealMatrix gammaMatrix = new Array2DRowRealMatrix(gamma);
        ArrayList<Double> emission = new ArrayList<>();
        for (int i = 0; i < stateNum; i++) {
            double[] gammaI = gammaMatrix.getColumn(i);
            double gammaSum = 0;
            for (int t = 0; t < sequenceLen; t++) {
                gammaSum += gammaI[t];
            }

            // 第i个盒子，k1=红,k2=白
            for (int k = 0; k < observationNum; k++) {
                emission.clear();
                for (int t = 0; t < sequenceLen; t++) {
                    if (sequence[t] == k) {
                        emission.add(gamma[t][i]);
                    }
                }
                if (emission.size() == 0) {
                    emissionProbability[i][k] = Double.MIN_VALUE;
                }
                Double[] emissionArray = emission.toArray(new Double[]{});
                emissionProbability[i][k] = sum(emissionArray) / gammaSum;
            }
        }
        long end = System.currentTimeMillis();
        log.info("更新发射概率结束...耗时:" + (end - start) + "毫秒");
    }

    private void updateTransferProbability(double[][] gamma, double[][][] ksi) {
        log.info("更新转移概率...");
        long start = System.currentTimeMillis();
        RealMatrix gammaMatrix = new Array2DRowRealMatrix(gamma);
        for (int i = 0; i < stateNum; i++) {
            double[] gammaI = gammaMatrix.getColumn(i);
            double gammaSum = 0;
            for (int t = 0; t < sequenceLen - 1; t++) {
                gammaSum += gammaI[t];
            }
            for (int j = 0; j < stateNum; j++) {
                double ksiSum = 0;
                for (int t = 0; t < sequenceLen - 1; t++) {
                    ksiSum += ksi[t][i][j];
                }
                transferProbability[i][j] = ksiSum / gammaSum;
            }
        }
        long end = System.currentTimeMillis();
        log.info("更新转移概率...耗时:" + (end - start) + "毫秒");
    }


    /**
     * 前向算法
     */
    protected double[][] reCalAlpha(int[] sequence, double[][] alpha) {
        log.info("计算alpha...");
        long start = System.currentTimeMillis();
        //alpha t=0初始值 sequenceLen = 0
        for (int i = 0; i < stateNum; i++) {
            alpha[0][i] = pi[i] * emissionProbability[i][sequence[0]];
        }
        double[] fp = new double[stateNum];
        for (int t = 1; t < sequenceLen; t++) {
            for (int i = 0; i < stateNum; i++) {
                for (int j = 0; j < stateNum; j++) {
                    fp[j] = alpha[t - 1][j] * transferProbability[j][i];
                }
                alpha[t][i] = sum(fp) * emissionProbability[i][sequence[t]];
            }
        }

        long end = System.currentTimeMillis();
        log.info("计算alpha结束...耗时:" + (end - start) + "毫秒");
        print(alpha);
        return alpha;
    }

    /**
     * 后项算法
     *
     * @param sequence
     * @param beta
     */
    protected double[][] reCalBeta(int[] sequence, double[][] beta) {
        log.info("计算beta...");
        long start = System.currentTimeMillis();
        for (int i = 0; i < stateNum; i++) {
            beta[sequenceLen - 1][i] = 1;
        }
        double[] bp = new double[stateNum];
        for (int t = sequenceLen - 2; t >= 0; t--) {
            for (int i = 0; i < stateNum; i++) {
                for (int j = 0; j < stateNum; j++) {
                    bp[j] = beta[t + 1][j] * emissionProbability[j][sequence[t + 1]] * transferProbability[i][j];
                }
                beta[t][i] = sum(bp);
            }
        }
        print(beta);
        long end = System.currentTimeMillis();
        log.info("计算beta...耗时:" + (end - start) + "毫秒");
        return beta;
    }

    /**
     * @param alpha
     * @param beta
     * @param gamma
     */
    protected double[][] reCalGamma(double[][] alpha, double[][] beta, double[][] gamma) {
        log.info("计算gamma...");
        long start = System.currentTimeMillis();
        for (int t = 0; t < sequenceLen; t++) {
            for (int i = 0; i < stateNum; i++) {
                gamma[t][i] = alpha[t][i] * beta[t][i];
            }
            double sum = sum(gamma[t]);
            for (int j = 0; j < stateNum; j++) {
                gamma[t][j] = gamma[t][j] / sum;
            }
        }
        long end = System.currentTimeMillis();
        log.info("计算gamma...耗时:" + (end - start) + "毫秒");
        print(gamma);
        return gamma;
    }


    /**
     * @param sequence
     * @param alpha
     * @param beta
     * @param ksi
     */
    protected double[][][] reCalKsi(int[] sequence, double[][] alpha, double[][] beta, double[][][] ksi) {
        log.info("计算ksi...");
        long start = System.currentTimeMillis();
        for (int t = 0; t < ksi.length; t++) {
            double sum = 0;
            for (int i = 0; i < stateNum; i++) {
                for (int j = 0; j < stateNum; j++) {
                    ksi[t][i][j] = alpha[t][i] * transferProbability[i][j] * emissionProbability[j][sequence[t + 1]] * beta[t + 1][j];
                    sum += ksi[t][i][j];
                }
            }
            for (int i = 0; i < stateNum; i++) {
                for (int j = 0; j < stateNum; j++) {
                    ksi[t][i][j] /= sum;
                }
            }
        }
        long end = System.currentTimeMillis();
        log.info("计算ksi...耗时:" + (end - start) + "毫秒");
        for (int t = 0; t < ksi.length; t++) {
            print(ksi[t]);
        }
        return ksi;
    }

    /**
     * 数组求和
     *
     * @param arr
     * @return
     */
    public double sum(double[] arr) {
        double sum = 0;
        for (int i = 0; i < arr.length; i++) {
            sum += arr[i];
        }
        return sum;
    }

    public double sum(Double[] arr) {
        double sum = 0;
        for (int i = 0; i < arr.length; i++) {
            sum += arr[i];
        }
        return sum;
    }

    public <T extends Object> void print(T[] nums) {
        for (T a : nums) {
            System.out.print(a + "\t");
        }
        System.out.println();
    }

    public void print(double[][] nums) {
        StringBuffer sb = new StringBuffer();
        sb.append("打印二维数组\n");
        for (int i = 0; i < nums.length; i++) {
            for (int j = 0; j < nums[0].length; j++) {
                sb.append(nums[i][j] + "\t");
            }
            sb.append("\n");
        }
        log.info(String.valueOf(sb));
    }




    public void methodTest(BaumWelchHmm bwh) {
        bwh.setObservationNum(2);
        bwh.setStateNum(3);
        bwh.setPi(new double[]{0.2, 0.4, 0.4});
        bwh.setSequenceLen(3);
        bwh.setSequence(new int[]{0, 1, 0});
        bwh.setTransferProbability(new double[][]{{0.5, 0.2, 0.3}, {0.3, 0.5, 0.2}, {0.2, 0.3, 0.5}});
        bwh.setEmissionProbability(new double[][]{{0.5, 0.5}, {0.4, 0.6}, {0.7, 0.3}});
        double[][] alpha = bwh.reCalAlpha(bwh.getSequence(), new double[bwh.getSequenceLen()][bwh.getStateNum()]);
        double[][] beta = bwh.reCalBeta(bwh.getSequence(), new double[bwh.getSequenceLen()][bwh.getStateNum()]);
        bwh.reCalGamma(alpha, beta, new double[bwh.getSequenceLen()][bwh.getStateNum()]);
        bwh.reCalKsi(bwh.getSequence(), alpha, beta, new double[bwh.getSequenceLen() - 1][bwh.getStateNum()][bwh.getStateNum()]);
    }

    public static void main(String[] args) {
        BaumWelchHmm bwh = new BaumWelchHmm(3, 4);
        bwh.setSequence(new int[]{0, 1, 0, 2, 3, 1, 1, 0, 3, 2});
//        bwh.setPi(new double[]{0.2, 0.4, 0.4});
//        bwh.setTransferProbability(new double[][]{{0.2, 0.5, 0.3}, {0.3, 0.5, 0.2}, {0.2, 0.3, 0.5}});
//        bwh.setEmissionProbability(new double[][]{{0.3, 0.2, 0.2, 0.3}, {0.4, 0.4, 0.1, 0.1}, {0.2, 0.4, 0.1, 0.3}});
//        bwh.methodTest(bwh);
        bwh.train(10);
    }
}
