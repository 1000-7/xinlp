package learn.crf;

import org.junit.jupiter.api.Test;
import segment.crf.XinTable;


public class TestTable {
    @Test
    public void test() {
        XinTable table = new XinTable();
        table.v = new String[][]{
                {"商", "?"},
                {"品", "?"},
                {"和", "?"},
                {"服", "?"},
                {"务", "?"},
        };
        System.out.println(table.get(9, 0));
    }

    @Test
    public void test1() {
        System.out.println(-7 & 1);
    }
}
