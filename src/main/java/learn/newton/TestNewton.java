package learn.newton;
/**
 * @Author unclewang
 * @Date 2018-11-27 00:45
 * https://blog.csdn.net/google19890102/article/details/41087931
 */
public class TestNewton {
	public static void main(String args[]) {
		NewtonMethod newton = new NewtonMethod(0, 0.00001, 100);
		System.out.println("基本牛顿法求解：" + newton.getNewtonMin());
 
		GlobalNewtonMethod gNewton = new GlobalNewtonMethod(0, 0.55, 0.4,
				0.00001, 100);
		System.out.println("全局牛顿法求解：" + gNewton.getGlobalNewtonMin());
	}
}