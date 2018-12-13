package test.hmm;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.junit.jupiter.api.Test;

/**
 * @Author unclewang
 * @Date 2018/11/16 20:44
 */
public class HmmTest {
    Double[] pi = new Double[]{0.2, 0.25, 0.25, 0.25};
    double[][] stateTransfer = new double[][]{{0, 1, 0, 0}, {0.4, 0, 0.6, 0}, {0, 0.4, 0, 0.6}, {0, 0, 0.5, 0.5}};
    double[][] observeProbability = new double[][]{{0.5, 0.5}, {0.3, 0.7}, {0.6, 0.4}, {0.8, 0.2}};

    RealMatrix stateTransferMatrix = new Array2DRowRealMatrix(stateTransfer);
    RealMatrix observeProbabilityMatrix = new Array2DRowRealMatrix(observeProbability);
    Integer[] observeSequence = new Integer[]{0, 1, 0, 1, 0};


    //观测序列长度
    Integer T = observeSequence.length;
    //状态长度
    Integer N = pi.length;
    //viterbi算法
    Integer[][] path = new Integer[T][N];
    Double[][] dp = new Double[T][N];
    //前向算法
    Double[] alpha = new Double[N];
    Double[][] fp = new Double[T][N];
    //后向算法
    Double[] beta = new Double[N];
    Double[][] bp = new Double[T][N];


    /**
     * 书上的例子的参数
     */
    public void initExample() {
        pi = new Double[]{0.2, 0.4, 0.4};
        stateTransfer = new double[][]{{0.5, 0.2, 0.3}, {0.3, 0.5, 0.2}, {0.2, 0.3, 0.5}};
        observeProbability = new double[][]{{0.5, 0.5}, {0.4, 0.6}, {0.7, 0.3}};

        stateTransferMatrix = new Array2DRowRealMatrix(stateTransfer);

        observeProbabilityMatrix = new Array2DRowRealMatrix(observeProbability);
        observeSequence = new Integer[]{0, 1, 0};


        T = observeSequence.length;
        N = pi.length;

        path = new Integer[T][N];
        dp = new Double[T][N];

        alpha = new Double[N];
        fp = new Double[T][N];

        beta = new Double[N];
        bp = new Double[T][N];

    }

    /**
     * 概率预测问题
     * 前向算法
     */
    @Test
    public void forwardProbability() {
//        initExample();
        /**
         * 计算初始值
         */
        double[] s = observeProbabilityMatrix.getColumn(observeSequence[0]);
        for (int i = 0; i < N; i++) {
            alpha[i] = pi[i] * s[i];
        }
        fp[0] = alpha;

        for (int i = 1; i < T; i++) {
            alpha = reCountAlpha(alpha, observeSequence[i]);
            fp[i] = alpha;
        }
        double probability = 0.0;
        for (int i = 0; i < N; i++) {
            probability += fp[T - 1][i];
        }
        System.err.println(probability);
    }


    /**
     * 重新计算序列出现概率
     * 前向算法
     *
     * @param d
     * @param state
     * @return
     */
    public Double[] reCountAlpha(Double[] d, int state) {
        Double[] result = new Double[d.length];
        for (int i = 0; i < d.length; i++) {
            double[] iTransfer = stateTransferMatrix.getColumn(i);
            double sum = 0;
            for (int j = 0; j < iTransfer.length; j++) {
                sum += iTransfer[j] * d[j];
            }
            double[] s = observeProbabilityMatrix.getColumn(state);
            result[i] = sum * s[i];
        }
        return result;
    }


    /**
     * 参数学习问题，假定上面的pi和状态转移矩阵和观测概率矩阵都是刚被初始化的，并不是最能生成观测序列的
     * 利用EM算法实现的baum-welch算法
     */
    @Test
    public void learning() {
        initExample();
        forwardProbability();
        backwardProbability();
        Double[][] gamma = dot(fp, bp);
        print(fp);
        print(bp);
        print(gamma);
    }

    private Double[][] dot(Double[][] fp, Double[][] bp) {
        Double[][] fpBp = new Double[fp.length][fp[0].length];
        for (int i = 0; i < fp.length; i++) {
            for (int j = 0; j < fp[0].length; j++) {
                fpBp[i][j] = fp[i][j] * bp[i][j];
            }
        }
        return fpBp;
    }


    @Test
    public void backwardProbability() {
        Double[] beta = new Double[N];
        for (int i = 0; i < N; i++) {
            beta[i] = 1.0;
        }
        bp[T - 1] = beta;
        for (int i = T - 2; i >= 0; i--) {
            beta = reCountBeta(beta, observeSequence[i + 1]);
            bp[i] = beta;
        }
    }

    public Double[] reCountBeta(Double[] beta, int state) {
        Double[] result = new Double[N];
        for (int i = 0; i < N; i++) {
            double[] iTransfer = stateTransferMatrix.getRow(i);
            double[] b = observeProbabilityMatrix.getColumn(state);
            double sum = 0;
            for (int j = 0; j < beta.length; j++) {
                //就是b[j]乘的地方不一样
                sum += beta[j] * iTransfer[j] * b[j];
            }
            result[i] = sum;
        }
        return result;
    }


    /**
     * 序列预测问题，viterbi算法
     */
    @Test
    public void prediction() {
        initExample();
        //第一步，初始化
        double[] s = observeProbabilityMatrix.getColumn(observeSequence[0]);
        for (int i = 0; i < N; i++) {
            dp[0][i] = pi[i] * s[i];
            path[0][i] = i;
        }
        for (int i = 1; i < T; i++) {
            //每一个t的概率都是前面N个t-1的概率乘以各种转移之后的最大的;
            for (int j = 0; j < N; j++) {
                double[] observeProbabilityMatrixColumn = observeProbabilityMatrix.getColumn(observeSequence[i]);
                updateDP(i, j, observeProbabilityMatrixColumn[j]);
            }
        }
        Integer[] mostLikelyStateSequence = new Integer[T];
        mostLikelyStateSequence[T - 1] = max(dp[T - 1]);

        for (int i = mostLikelyStateSequence.length - 2; i >= 0; i--) {
            mostLikelyStateSequence[i] = path[i][mostLikelyStateSequence[i + 1]];
        }
        print(mostLikelyStateSequence);
    }

    public void updateDP(int row, int col, double observeP) {
        Double[] ijState = new Double[N];
        for (int i = 0; i < N; i++) {
            ijState[i] = dp[row - 1][i] * stateTransfer[i][col];
        }
        int index = max(ijState);
        path[row][col] = index;
        dp[row][col] = ijState[index] * observeP;
    }

    public int max(Double[] d) {
        double max = Double.MIN_VALUE;
        int index = 0;
        for (int i = 0; i < d.length; i++) {
            if (d[i] > max) {
                index = i;
                max = d[i];
            }
        }
        return index;
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
}
