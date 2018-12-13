package test.hmm.baumwelch;
 
import java.util.ArrayList;
import java.util.List;

public class SegmentationUtils {
	/**
	 * 将字符串数组的每一个字符串中的字符直接转换为Unicode码
	 * @param strs 字符串数组
	 * @return Unicode值
	 */
	public static List<int[]> strs2int(String[] strs) {
		List<int[]> res = new ArrayList<>(strs.length);
		for(int i = 0; i < strs.length;i++) {
			int[] O = new int[strs[i].length()];
			for(int j = 0; j < strs[i].length();j++) {
				O[j] = strs[i].charAt(j);
			}
			res.add(O);
		}
		return res;
	}
	
	public static int[] str2int(String str) {
		return strs2int(new String[] {str}).get(0);
	}
	/**
	 * 根据预测结果解码
	 * BEMS 0123
	 * @param predict 预测结果
	 * @param sentence 句子
	 * @return
	 */
	public static String[] decode(int[] predict, String sentence) {
		List<String> res = new ArrayList<>();
		char[] chars = sentence.toCharArray();
		for(int i = 0; i < predict.length;i++) {
			if(predict[i] == 0 || predict[i] == 1) {
				int a = i;
				while(predict[i] != 2) {
					i++;
					if(i == predict.length) {
						break;
					}
				}
				int b = i;
				if(b == predict.length) {
					b--;
				}
				res.add(new String(chars,a,b-a+1));
			} else {
				res.add(new String(chars,i,1));
			}
		}
		String[] s = new String[res.size()];
		return res.toArray(s);
	}
}

