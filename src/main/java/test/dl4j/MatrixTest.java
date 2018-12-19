package test.dl4j;

import org.junit.jupiter.api.Test;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

public class MatrixTest {
    @Test
    public void testMatrix() {
        int nRows = 2;
        int nColumns = 2;
// Create INDArray of zeros
        INDArray zeros = Nd4j.zeros(nRows, nColumns);
// Create one of all ones
        INDArray ones = Nd4j.ones(nRows, nColumns);
//hstack
        INDArray hstack = Nd4j.hstack(ones, zeros);
        System.out.println("### HSTACK ####");
        System.out.println(hstack);
    }

    @Test
    public void testSVD() {
        int nRows = 1;
        int nColumns = 1;

        double[][] vals = {{1, 1, 1, 0, 0}, {2, 2, 2, 0, 0}, {1, 1, 1, 0, 0}, {5, 5, 5, 0, 0}, {0, 0, 0, 2, 2}, {0, 0, 0, 3, 3}, {0, 0, 0, 1, 1}};
        INDArray A = Nd4j.create(vals);
        nRows = A.rows();
        nColumns = A.columns();
        System.out.println("A: " + A);


        INDArray S = Nd4j.zeros(1, nRows);
        INDArray U = Nd4j.zeros(nRows, nRows);
        INDArray V = Nd4j.zeros(nColumns, nColumns);
        Nd4j.getBlasWrapper().lapack().gesvd(A, S, U, V);

        System.out.println("\n S:" + S);
        System.out.println("\n U:" + U);
        System.out.println("\n V:" + V);
    }

    @Test
    public void testmm() {
        double[][] vals = {{1, 1, 1, 0, 0}, {2, 2, 2, 0, 0}, {1, 1, 1, 0, 0}, {5, 5, 5, 0, 0}, {0, 0, 0, 2, 2}, {0, 0, 0, 3, 3}, {0, 0, 0, 1, 1}};
        INDArray A = Nd4j.create(vals);
        long m = A.rows();
        long n = A.columns();
        INDArray mean = A.mean(0);
        A.subiRowVector(mean);
        System.out.println(A);
        // The prepare SVD results, we'll decomp A to UxSxV'
        INDArray s = Nd4j.create(m < n ? m : n);
        INDArray VT = Nd4j.create(n, n, 'f');

        // Note - we don't care about U
        Nd4j.getBlasWrapper().lapack().gesvd(A, s, null, VT);
        System.out.println("\n S:" + s);
        System.out.println("\n V:" + VT);
    }

    @Test
    public void test() {
        System.out.println(System.getProperty("java.io.tmpdir"));
    }
}
