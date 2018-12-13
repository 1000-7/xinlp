package segment;

import lucene.Atom;

import java.util.ArrayList;
import java.util.List;

public interface Segment {
    List<Atom> seg(String text);

    default List<Atom> strings2AtomList(String[] strings) {
        List<Atom> atoms = new ArrayList<>();
        int d = 0;
        for (String s : strings) {
            Atom atom = new Atom();
            atom.setContent(s);
            atom.setOffe(d);
            atom.setLen(s.length());
            atom.setChars(s.toCharArray());
            d += s.length();
            atoms.add(atom);
        }
        return atoms;
    }
}
