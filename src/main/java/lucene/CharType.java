package lucene;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

public class CharType implements Serializable {
    /**
     * 单字节
     */
    public static final byte CT_SINGLE = 0;

    /**
     * 分隔符"!,.?()[]{}+= 各种稀奇古怪的符号
     */
    public static final byte CT_DELIMITER = 1;

    /**
     * 中文字符
     */
    public static final byte CT_CHINESE = 2;

    /**
     * 字母
     */
    public static final byte CT_LETTER = 3;

    /**
     * 数字
     */
    public static final byte CT_NUM = 4;

    /**
     * 序号
     */
    public static final byte CT_INDEX = 5;

    /**
     * 中文数字
     */
    public static final byte CT_CNUM = 6;

    /**
     * 其他
     */
    public static final byte CT_OTHER = 12;
    private static byte[] charType;

    static {
        charType = new byte[65536];
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(System.getProperty("user.dir") + "/src/main/resources/chartype/chartype.bin"));
            ois.read(charType);
            ois.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static byte get(char c) {
        return charType[(int) c];
    }
}
