package learn.crf;

import org.junit.jupiter.api.Test;

import java.util.TreeMap;

/**
 * @Author unclewang
 * @Date 2018-11-27 14:31
 */
public class TreeMapTest {
    @Test
    public void test() {
        TreeMap<Integer, String> treeMap = new TreeMap();
        treeMap.put(11, "safs");
        treeMap.put(31, "safs");
        treeMap.put(211, "safs");
        treeMap.put(12, "safs");
        System.out.println(treeMap);
    }

    @Test
    public void test1() {

    }
}
