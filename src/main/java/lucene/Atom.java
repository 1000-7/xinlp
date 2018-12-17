package lucene;

import lombok.Data;

@Data
public class Atom {
    private String content;


    // 当前词的起始位置
    private int offe;
    private int len;
    private char[] chars;
}
