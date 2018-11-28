package learn.newton;
 
/**
 * @Author unclewang
 * @Date 2018-11-26 16:21
 */
public class GlobalNewtonMethod {
	private double originalX;
	private double delta;
	private double sigma;
	private double e;
	private double maxCycle;
 
	public GlobalNewtonMethod(double originalX, double delta, double sigma,
			double e, double maxCycle) {
		this.setOriginalX(originalX);
		this.setDelta(delta);
		this.setSigma(sigma);
		this.setE(e);
		this.setMaxCycle(maxCycle);
	}
 
	public double getOriginalX() {
		return originalX;
	}
 
	public void setOriginalX(double originalX) {
		this.originalX = originalX;
	}
 
	public double getDelta() {
		return delta;
	}
 
	public void setDelta(double delta) {
		this.delta = delta;
	}
 
	public double getSigma() {
		return sigma;
	}
 
	public void setSigma(double sigma) {
		this.sigma = sigma;
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
	public double getGlobalNewtonMin() {
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
			double dk = -one / two;// 搜索的方向
			double m = 0;
			double mk = 0;
			while (m < 20) {
				double left = this.getOriginal(x + Math.pow(this.getDelta(), m)
						* dk);
				double right = this.getOriginal(x) + this.getSigma()
						* Math.pow(this.getDelta(), m)
						* this.getOneDerivative(x) * dk;
				if (left <= right) {
					mk = m;
					break;
				}
				m++;
			}
			x = x + Math.pow(this.getDelta(), mk)*dk;
			k++;
		}
		return y;
	}
}

