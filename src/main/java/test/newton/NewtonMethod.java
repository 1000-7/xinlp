package test.newton;
 
/**
 * @Author unclewang
 * @Date 2018-11-26 14:44
 */
public class NewtonMethod {
	private double originalX;// 初始点
	private double e;// 误差阈值
	private double maxCycle;// 最大循环次数
 
	/**
	 * 构造方法
	 * 
	 * @param originalX 初始值
	 * @param e 误差阈值
	 * @param maxCycle 最大循环次数
	 */
	public NewtonMethod(double originalX, double e, double maxCycle) {
		this.setOriginalX(originalX);
		this.setE(e);
		this.setMaxCycle(maxCycle);
	}
 
	// 一系列get和set方法
	public double getOriginalX() {
		return originalX;
	}
 
	public void setOriginalX(double originalX) {
		this.originalX = originalX;
	}
 
	public double getE() {
		return e;
	}
 
	public void setE(double e) {
		this.e = e;
	}
 
	public double getMaxCycle() {
		return maxCycle;
	}
 
	public void setMaxCycle(double maxCycle) {
		this.maxCycle = maxCycle;
	}
 
	/**
	 * 原始函数
	 * 
	 * @param x 变量
	 * @return 原始函数的值
	 */
	public double getOriginal(double x) {
		return x * x - 3 * x + 2;
	}
 
	/**
	 * 一次导函数
	 * 
	 * @param x 变量
	 * @return 一次导函数的值
	 */
	public double getOneDerivative(double x) {
		return 2 * x - 3;
	}
 
	/**
	 * 二次导函数
	 * 
	 * @param x 变量
	 * @return 二次导函数的值
	 */
	public double getTwoDerivative(double x) {
		return 2;
	}
 
	/**
	 * 利用牛顿法求解
	 * 
	 * @return
	 */
	public double getNewtonMin() {
		double x = this.getOriginalX();
		double y = 0;
		double k = 1;
		// 更新公式
		while (k <= this.getMaxCycle()) {
			y = this.getOriginal(x);
			double one = this.getOneDerivative(x);
			if (Math.abs(one) <= e) {
				break;
			}
			double two = this.getTwoDerivative(x);
			x = x - one / two;
			k++;
		}
		return y;
	}




}

