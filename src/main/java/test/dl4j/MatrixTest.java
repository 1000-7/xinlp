package test.dl4j;

import org.junit.jupiter.api.Test;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

public class MatrixTest {
    @Test
    public void test() {
        int nRows = 2;
        int nColumns = 2;
// Create INDArray of zeros
        INDArray zeros = Nd4j.zeros(nRows, nColumns, nColumns);
// Create one of all ones
        INDArray ones = Nd4j.ones(nRows, nColumns, nColumns);
//hstack
        INDArray hstack = Nd4j.hstack(ones, zeros);
        System.out.println("### HSTACK ####");
        System.out.println(hstack);
    }
}
