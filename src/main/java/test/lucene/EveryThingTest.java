package test.lucene;

import lombok.Data;
import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.NlpAnalysis;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;

/**
 * @Author unclewang
 * @Date 2018-12-11 12:15
 */
public class EveryThingTest {
    @Test
    public void test() {
        List<Term> terms = NlpAnalysis.parse(
                "本文对半导体氧化物气体敏感材料的电导振荡特性加以研究分析，通过试验与理论分析得出气敏电导振荡的必要条件，并对电导振荡型气体敏感元件的原理、工艺技术和结构等进行分析说明。同时，对半导体氧化物气体敏感材料的常温气体敏感特性进行归纳总结，指出其优缺点和需要解决的问题。"
        ).getTerms();
        for (Term term : terms) {
            String word = term.getName(); //拿到词
            String natureStr = term.getNatureStr(); //拿到词性
            //if (expectedNature.contains(natureStr)) {
            System.out.print(word + " ");
            //}
        }
    }

    @Test
    public void testHashMap() {
        HashMap<String, String> stringStringHashMap = new HashMap<>();
        stringStringHashMap.put("1", "sadas");
        stringStringHashMap.put("1", "sas");
        System.out.println(stringStringHashMap.get("1"));
        stringStringHashMap.remove("1");
        System.out.println(stringStringHashMap.get("1"));
        System.out.println(stringStringHashMap.containsKey("1"));
    }
}

