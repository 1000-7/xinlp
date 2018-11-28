package learn.newton;

import org.junit.Test;

public class Derivative {
    private static final double Error = 1.1 * Math.pow(1.1, -16);
    /**
     * 在用计算机解决问题时，需要注意的是计算机浮点数本身就会有误差，例如对于double类型，该误差为u=1.1*10^(-16)。
     * https://blog.csdn.net/fangqingan_java/article/details/48685093
     */
    private static final double DELTA_X = Math.pow(Error, -1.0 / 2);
    private static final double DELTA_X_Center = Math.pow(Error, -1.0 / 3);
    private double e = 0.001;
    private double maxCycle = 100;

    public double f(double x1, double x2) {
        return x1 * x1 + x1 * x2 - 3 * x1 + 4 * x2;
    }

    public double f(double x) {
        return x * x - 3 * x + 2;
    }

    public double d1(double x) {
        return (f(x + DELTA_X_Center) - f(x - DELTA_X_Center)) / (2 * DELTA_X_Center);
    }

    public double pd1(double x1, double x2) {
        return ((f(x1 + DELTA_X_Center, x2) - f(x1 - DELTA_X_Center, x2)) / (2 * DELTA_X_Center)) * ((f(x1, x2 + DELTA_X_Center) - f(x1, x2 - DELTA_X_Center)) / (2 * DELTA_X_Center));
    }

    public double d2(double d1) {
        return (d1(d1 + DELTA_X_Center) - d1(d1 - DELTA_X_Center)) / (2 * DELTA_X_Center);
    }


    public double newton(double x) {

        for (int i = 0; i < this.maxCycle; i++) {
            double gx = d1(x);
            double hx = d2(x);
            if (Math.abs(gx) < this.e) {
                break;
            }
            x = x - gx / hx;
        }
        return x;
    }

    public double globalNewton(double x) {
        for (int i = 0; i < this.maxCycle; i++) {
            double gx = d1(x);
            double hx = d2(x);
            double dx = -gx / hx;
            if (Math.abs(gx) < this.e) {
                break;
            }
            x = x - gx / hx;
        }
        return x;
    }


    @Test
    public void testD1() {
        System.out.println(d1(2));
        System.out.println(pd1(2, 2));
        System.out.println(d2(2));
        System.out.println("牛顿法得到的最小x为：" + newton(2) + "，此时函数值为：" + f(newton(2)));
    }

}
