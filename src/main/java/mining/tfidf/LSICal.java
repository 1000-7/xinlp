package mining.tfidf;

import com.google.common.collect.BiMap;
import lombok.extern.slf4j.Slf4j;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class LSICal {
    private BiMap<Integer, Integer> termIdVocabularyId;
    //18288数量太大，电脑吃不消
    private int docNum = 10;
    private double featurePercent = 10;

    public double[][] transformMatrix(HashMap<Integer, HashMap<Integer, Double>> idTfIDf) {
        BiMap<Integer, Integer> termIdVocabularyId = null;
        try {
            termIdVocabularyId = AllDocTfIdf.loadTermid();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        BiMap<Integer, Integer> vocabularyIdTermId = termIdVocabularyId.inverse();
        log.info("正在生成" + docNum + "*" + termIdVocabularyId.size() + "的矩阵");
        double[][] docTermMatrix = new double[docNum][termIdVocabularyId.size()];
        for (Map.Entry<Integer, HashMap<Integer, Double>> entry : idTfIDf.entrySet()) {
            int docId = entry.getKey();
            if (docId >= docNum) {
                break;
            }
            for (Map.Entry<Integer, Double> termEntry : entry.getValue().entrySet()) {
                int vocaId = termEntry.getKey();
                double value = termEntry.getValue();
                int termId = vocabularyIdTermId.get(vocaId);
                docTermMatrix[docId][termId] = value;
            }

        }
        return docTermMatrix;
    }


    /**
     * @param docTermMatrix
     * @return 对于奇异值, 它跟我们特征分解中的特征值类似，在奇异值矩阵中也是按照从大到小排列，而且奇异值的减少特别的快，在很多情况下，前10%甚至1%的奇异值的和就占了全部的奇异值之和的99%以上的比例。也就是说，我们也可以用最大的k个的奇异值和对应的左右奇异向量来近似描述矩阵。也就是说：
     * Am×n=Um×mΣm×nVTn×n≈Um×kΣk×kVTk×n
     */
    private double[][] svd(double[][] docTermMatrix) {
//        docTermMatrix = new double[][]{{1, 1, 1, 0, 0}, {2, 2, 2, 0, 0}, {1, 1, 1, 0, 0}, {5, 5, 5, 0, 0}, {0, 0, 0, 2, 2}, {0, 0, 0, 3, 3}, {0, 0, 0, 1, 1}};
        log.info("开始进行SVD分解，得到Doc与Doc之间的关系");
        INDArray A = Nd4j.create(docTermMatrix);
        if (A.rows() < A.columns()) {
            A = A.transpose();
        }
        int nRows = A.rows();
        int nColumns = A.columns();
        System.out.println(A + "\n");
        //左奇异矩阵U可以用于行数的压缩。相对的，右奇异矩阵V可以用于列数即特征维度的压缩,PCA降维
        INDArray vt = Nd4j.zeros(nColumns, nColumns);
        INDArray s = Nd4j.zeros(1, nColumns);
        INDArray u = Nd4j.zeros(nRows, nRows);
        Nd4j.getBlasWrapper().lapack().gesvd(A, s, u, vt);
        System.out.println(A);
        INDArray sigma = Nd4j.diag(s);
        INDArray temp = Nd4j.zeros(nRows - nColumns, nColumns);
        INDArray vsigam = Nd4j.vstack(sigma, temp);
        System.out.println("--------------------");
//        System.out.println("E:" + u.mmul(u.transpose()));
        System.out.println("E:" + vt.transpose().mmul(vt));
        System.out.println("--------------------");
        System.out.println("A" + A);
        System.out.println(u.mmul(vsigam.mmul(vt)));
        System.out.println("--------------------");

        INDArray docSim = vt.transpose().mmul(vsigam.transpose().mmul(vsigam.mmul(vt)));
        log.info("SVD计算结束");
        System.out.println("\n S:" + s);
        System.out.println("\n VT:" + vt);
        System.out.println("\n docSim:\n" + docSim);
        System.out.println("\n original:\n" + A.transpose().mmul(A));
        System.out.println("\n original:\n" + A.mmul(A.transpose()));
        return docTermMatrix;
    }

    public static void main(String[] args) {
        AllDocTfIdf allDocTfIdf = new AllDocTfIdf();
        HashMap<Integer, HashMap<Integer, Double>> idTfIDf = allDocTfIdf.loadAllDocTfIdf();
        LSICal lsiCal = new LSICal();
        lsiCal.svd(lsiCal.transformMatrix(idTfIDf));

    }
}
