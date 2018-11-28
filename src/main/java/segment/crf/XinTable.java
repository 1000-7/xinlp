package segment.crf;

/**
 * @Author unclewang
 * @Date 2018-11-27 19:39
 * 句子转换成二维数组，方便存token
 */
public class XinTable {
    public String[][] v;
    static final String HEAD = "_B";


    /**
     * 获取表中某一个元素
     *
     * @param x
     * @param y
     * @return
     */
    public String get(int x, int y) {
        if (x < 0) {
            return HEAD + x;
        }
        if (x >= v.length) {
            return HEAD + "+" + (x - v.length + 1);
        }

        return v[x][y];
    }

    public void setLast(int x, String t) {
        v[x][v[x].length - 1] = t;
    }

    public int size() {
        return v.length;
    }

    public String[][] getV() {
        return v;
    }

    public void setV(String[][] v) {
        this.v = v;
    }

    @Override
    public String toString() {
        if (v == null) {
            return "null";
        }
        final StringBuilder sb = new StringBuilder(v.length * v[0].length * 2);
        for (String[] line : v) {
            for (String element : line) {
                sb.append(element).append('\t');
            }
            sb.append('\n');
        }
        return sb.toString();
    }
}
