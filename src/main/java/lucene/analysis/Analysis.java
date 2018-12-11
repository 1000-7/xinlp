package lucene.analysis;

import lucene.simple.Atom;

import java.io.IOException;
import java.io.Reader;
import java.util.LinkedList;

/**
 * @Author unclewang
 * @Date 2018-12-11 12:14
 */
public abstract class Analysis {
    /**
     * 用来记录偏移量
     */
    private int offe;
    private Reader reader;
    private LinkedList<Atom> atoms = new LinkedList<>();

    abstract Result segmemt(String s);

    public void resetContent(Reader reader) {
        this.offe = 0;
        this.reader = reader;
    }

    public Result parse() throws IOException {
        LinkedList<Atom> list = new LinkedList<>();
        Atom temp = null;
        while ((temp = next()) != null) {
            list.add(temp);
        }
        Result result = new Result(list);
        return result;
    }


    private Atom next() {
        Atom atom = null;
        if (!atoms.isEmpty()) {
            atom = atoms.poll();
            return atom;
        }
        return atom;
    }
}
