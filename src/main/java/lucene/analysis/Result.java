package lucene.analysis;

import lombok.Data;
import lucene.simple.Atom;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.StringJoiner;

@Data
public class Result implements Iterable<Atom> {
    private LinkedList<Atom> atomList = null;

    public Result(LinkedList<Atom> atomList) {
        this.atomList = atomList;
    }

    @Override
    public Iterator<Atom> iterator() {
        return atomList.iterator();
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Result.class.getSimpleName() + "[", "]")
                .add("atomList=" + atomList)
                .toString();
    }
}
