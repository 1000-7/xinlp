package learn.gmm;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * @Author unclewang
 * @Date 2018/11/15 14:15
 */
@Slf4j
public class GmmTest {
    //生成数据
    private final static int N = 100;
    private static Double[] men = new Double[(int) (N * 3.5)];
    private static Double[] women = new Double[N * 4];
    private static Double[] children = new Double[(int) (N * 2.5)];
    private static Double[] people = new Double[men.length + women.length + children.length];
    private static Double[] m = new Double[people.length];
    private static Double[] w = new Double[people.length];
    private static Double[] c = new Double[people.length];
    private static Double[] e = new Double[people.length];
    private RandomGenerator rg = new MersenneTwister(100);

    /**
     * EM算法参数定义
     */
    private Double[] mean;
    private Double[] sd;
    //k的和应该等于1,k[0]对应men，k[1]对应women，k[2]对应children
    private Double[] k;

    /**
     * TODO
     */
    @Test
    public void test() {
        init();
        for (int i = 0; i < 400; i++) {
            e();
            m();
        }
    }

    @Test
    public void m() {
        mean = new Double[]{reCountMean(m), reCountMean(w), reCountMean(c)};
        sd = new Double[]{reCountSd(m), reCountSd(w), reCountSd(c)};
        k = new Double[]{reCountK(m), reCountK(w), reCountK(c)};
        System.err.println(k[0] + "\t" + k[1] + "\t" + k[2]);
    }

    @Test
    public void e() {
        for (int i = 0; i < e.length; i++) {
            m[i] = k[0] * getP(people[i], mean[0], sd[0]);
            w[i] = k[1] * getP(people[i], mean[1], sd[1]);
            c[i] = k[2] * getP(people[i], mean[2], sd[2]);
            e[i] = m[i] + w[i] + c[i];
            m[i] /= e[i];
            w[i] /= e[i];
            c[i] /= e[i];
        }
        log.info("迭代结果：" + reCountMean(m) + "\t" + reCountMean(w) + "\t" + reCountMean(c));
    }


    public double reCountMean(Double[] d) {
        double sum = 0;
        double meanSum = 0;
        for (int i = 0; i < e.length; i++) {
            meanSum += d[i];
            sum += d[i] * people[i];
        }
        return sum / meanSum;
    }

    public double reCountSd(Double[] d) {
        double newMean = reCountMean(d);
        double sdSum = 0;
        double meanSum = 0;
        for (int i = 0; i < e.length; i++) {
            sdSum += d[i] * FastMath.pow(people[i] - newMean, 2);
            meanSum += d[i];
        }
        return sdSum / meanSum;
    }

    public double reCountK(Double[] d) {
        double meanSum = 0;
        for (int i = 0; i < d.length; i++) {
            meanSum += d[i];
        }
        return meanSum / people.length;
    }

    @Test
    public void testLength() {
        System.out.println(men.length);
        System.out.println(women.length);
        System.out.println(children.length);
        System.out.println(men.length + women.length + children.length);
        System.out.println(people.length);
        System.out.println(e.length);
    }

    @Test
    public void init() {
        generate();
        //初始化参数，因为猜测来自三种人的分布，所以数组的长度都是3
        mean = new Double[]{-170.0, 1600.3, 103.5};
        sd = new Double[]{10.0, 10.2, 23.5};
        k = new Double[]{0.3, 0.3, 0.4};
        log.info("(1)正态分布的均值初始值设定：" + mean[0] + "\t" + mean[1] + "\t" + mean[2]);
    }


    @Test
    public void testGetP() {
        System.out.println(getP(0, 0, 1));
        System.out.println(getP(3, 0, 1));
        System.out.println(getP(-3, 0, 1));
        System.out.println(getP(1, 162, 13));
    }


    public double getP(double x, double mean, double sd) {
        NormalDistribution nd = new NormalDistribution(mean, sd);
        double p = Math.abs(nd.cumulativeProbability(x));
        return p > 0.5 ? 1 - p : p;
    }

    public Double[] generatePeople(Double[] people, double mean, double sd) {
        for (int i = 0; i < people.length; i++) {
            people[i] = normal(mean, sd);
        }
        return people;
    }

    public Double[] generate() {
        log.info("正在生成1000个人的数据");
        men = generatePeople(men, -178, 5);
        women = generatePeople(women, 1630, 5);
        children = generatePeople(children, 100, 4);
        log.info("数据分布情况介绍：\n" + "平均值\t-178\t1630\t100\n标准差\t5\t5\t4");
        for (int i = 0; i < people.length; i++) {
            if (i < men.length) {
                people[i] = men[i];
            } else if (i < men.length + women.length) {
                people[i] = women[i - men.length];
            } else {
                people[i] = children[i - men.length - women.length];
            }
        }
        List<Double> peopleList = Arrays.asList(people);
        Collections.shuffle(peopleList, new Random(10));
        people = peopleList.toArray(new Double[]{});

//        print(people);
        return people;
    }

    public double normal(double mean, double sd) {
        NormalDistribution nd = new NormalDistribution(rg, mean, sd);
        return nd.sample();
    }

    public <T extends Object> void print(T[] nums) {
        for (T a : nums) {
            System.out.print(a + "\t");
        }
        System.out.println();
    }
}
