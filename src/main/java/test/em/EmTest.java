package test.em;

import org.junit.jupiter.api.Test;

/**
 * @author unclewang
 */
public class EmTest {
    @Test
    public void test() {
        //每个硬币初始一次为正的概率
        double[] yita = m(0.2, 0.5);
        for (int i = 0; i < 100; i++) {
            yita = m(yita);
            System.out.println(yita[0] + "\t" + yita[1]);
        }
    }

    public double[] m(double... yita) {
        int[] nums = {5, 5, 9, 1, 8, 2, 4, 6, 7, 3};
        double[] e = new double[5];
        double[] m = new double[5];
        double[] m_ = new double[5];
        double[] n = new double[5];
        double[] n_ = new double[5];
        for (int i = 0; i < e.length; i++) {
            //e步
            e[i] = e(yita[0], nums[i * 2], yita[1]);
            m[i] = e[i] * nums[2 * i];
            m_[i] = e[i] * nums[2 * i + 1];
            n[i] = (1 - e[i]) * nums[2 * i];
            n_[i] = (1 - e[i]) * nums[2 * i + 1];
        }
        double yita1 = sum(m) / (sum(m) + sum(m_));
        double yita2 = sum(n) / (sum(n) + sum(n_));
        System.out.println("开始迭代");
        print(e);
        print(m);
        print(m_);
        print(n);
        print(n_);
        return new double[]{yita1, yita2};
    }

    public void print(double[] nums) {
        for (double a : nums) {
            System.out.print(a + "\t");
        }
        System.out.println();
    }

    public double sum(double[] nums) {
        double sum = 0;
        for (double a : nums) {
            sum += a;
        }
        return sum;
    }

    public double e(double a, double b, double c) {
        double e1 = Math.pow(a, b) * Math.pow(1 - a, 10 - b);
        double e2 = Math.pow(c, 10 - b) * Math.pow(1 - c, b);
        return e1 / (e1 + e2);
    }
}
