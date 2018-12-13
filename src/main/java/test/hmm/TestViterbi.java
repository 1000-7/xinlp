package test.hmm;

import junit.framework.TestCase;

import static test.hmm.TestViterbi.Activity.*;
import static test.hmm.TestViterbi.Weather.Rainy;
import static test.hmm.TestViterbi.Weather.Sunny;

//这个测试天气和运动
public class TestViterbi extends TestCase {
    static enum Weather {
        Rainy,
        Sunny,
    }

    static enum Activity {
        Walk,
        Shop,
        Clean,
    }

    static int[] states = new int[]{Rainy.ordinal(), Sunny.ordinal()};
    static int[] observations = new int[]{Walk.ordinal(), Shop.ordinal(), Clean.ordinal()};
    double[] start_probability = new double[]{0.6, 0.4};
    double[][] transititon_probability = new double[][]{
            {0.7, 0.3},
            {0.4, 0.6},
    };
    double[][] emission_probability = new double[][]{
            {0.1, 0.4, 0.5},
            {0.6, 0.3, 0.1},
    };

    public void testCompute() throws Exception {
        for (int i = 0; i < start_probability.length; ++i) {
            start_probability[i] = -Math.log(start_probability[i]);
        }
        for (int i = 0; i < transititon_probability.length; ++i) {
            for (int j = 0; j < transititon_probability[i].length; ++j) {
                transititon_probability[i][j] = -Math.log(transititon_probability[i][j]);
            }
        }
        for (int i = 0; i < emission_probability.length; ++i) {
            for (int j = 0; j < emission_probability[i].length; ++j) {
                emission_probability[i][j] = -Math.log(emission_probability[i][j]);
            }
        }
        int[] result = compute(observations, states, start_probability, transititon_probability, emission_probability);
        for (int r : result) {
            System.out.print(Weather.values()[r] + " ");
        }
        System.out.println();
    }

    /**
     * 求解HMM模型，所有概率请提前取对数
     *
     * @param obs     观测序列
     * @param states  隐状态
     * @param start_p 初始概率（隐状态）
     * @param trans_p 转移概率（隐状态）
     * @param emit_p  发射概率 （隐状态表现为显状态的概率）
     * @return 最可能的序列
     */
    public static int[] compute(int[] obs, int[] states, double[] start_p, double[][] trans_p, double[][] emit_p) {
        int _max_states_value = 0;
        for (int s : states) {
            _max_states_value = Math.max(_max_states_value, s);
        }
        ++_max_states_value;
        double[][] V = new double[obs.length][_max_states_value];
        int[][] path = new int[_max_states_value][obs.length];

        for (int y : states) {
            V[0][y] = start_p[y] + emit_p[y][obs[0]];
            path[y][0] = y;
        }

        for (int t = 1; t < obs.length; ++t) {
            int[][] newpath = new int[_max_states_value][obs.length];

            for (int y : states) {
                double prob = Double.MAX_VALUE;
                int state;
                for (int y0 : states) {
                    double nprob = V[t - 1][y0] + trans_p[y0][y] + emit_p[y][obs[t]];
                    if (nprob < prob) {
                        prob = nprob;
                        state = y0;
                        // 记录最大概率
                        V[t][y] = prob;
                        // 记录路径
                        System.arraycopy(path[state], 0, newpath[y], 0, t);
                        newpath[y][t] = y;
                    }
                }
            }

            path = newpath;
        }

        double prob = Double.MAX_VALUE;
        int state = 0;
        for (int y : states) {
            if (V[obs.length - 1][y] < prob) {
                prob = V[obs.length - 1][y];
                state = y;
            }
        }

        return path[state];
    }
}
