package lucene.analysis;

import lucene.simple.Atom;
import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.util.LinkedList;

/**
 * @Author unclewang
 * @Date 2018-12-11 12:12
 */
public class HmmAnalysis extends Analysis {

    private Reader readerreader;

    public HmmAnalysis() {

    }

    @Override
    public Result segmemt(String text) {
        LinkedList<Atom> atoms = new LinkedList<>();
        Result result = new Result(atoms);
        return result;
    }

    @Test
    public void test() {
        System.out.println(segmemt("这件衣服真好看"));
    }
}
