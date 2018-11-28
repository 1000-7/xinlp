package learn.crf;

import org.junit.jupiter.api.Test;
import segment.crf.DoubleArrayTrie;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TestDAT {
    @Test
    public void test() {
        List<String> strings = new ArrayList<>();
        strings.add("一举一动");
        strings.add("一举成名");
        strings.add("一举成名天下知");
        strings.add("万能");
        strings.add("万能胶");
        Set<Character> charset = new HashSet<Character>();
        for (String s : strings) {
            for (Character c : s.toCharArray()) {
                charset.add(c);
            }
        }
        String infoCharsetValue = "";
        String infoCharsetCode = "";
        for (Character c : charset) {
            infoCharsetValue += c + "\t\t";
            infoCharsetCode += (int) c + "\t";
        }
        infoCharsetValue += '\n';
        infoCharsetCode += '\n';
        System.out.print(infoCharsetValue);
        System.out.print(infoCharsetCode);

        DoubleArrayTrie dat = new DoubleArrayTrie();
        dat.build(strings);
        int i = dat.exactMatchSearch("一举成名天下知");
        System.out.println(i);
        System.out.println(strings.get(i));
        List<Integer> integerList = dat.commonPrefixSearch("一举成名天下知");
        for (int index : integerList) {
            System.out.println(strings.get(index));
        }

    }
}
