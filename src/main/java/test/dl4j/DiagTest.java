package test.dl4j;

import org.junit.jupiter.api.Test;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

public class DiagTest {
    @Test
    public void test() {
        INDArray A = Nd4j.create(new double[]{1, 2, 3});

        System.out.println(A.shapeInfoToString());
        INDArray B = Nd4j.diag(A);
        System.out.println(B.shapeInfoToString());
        System.out.println(B);

        System.err.println(B.getDouble(0, 0));
        INDArray Si = Nd4j.zeros(A.shape());
        System.out.println(Si.shapeInfoToString());
    }

    @Test
    public void test1() {
        INDArray A = Nd4j.create(new double[]{1, 2, 3, 4, 5});
        double[] doubles = A.toDoubleVector();
        for (int i = 0; i < doubles.length; i++) {
            if (i <= 2) {
                doubles[i] = doubles[i] * doubles[i];
            } else {
                doubles[i] = 0;
            }
        }
        INDArray Si = Nd4j.diag(Nd4j.create(doubles));
        INDArray S1 = Si.getColumns(0, 1, 2);

        System.out.println(Si);
        System.out.println(S1);
    }

}
