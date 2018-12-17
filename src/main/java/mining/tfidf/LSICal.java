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

    public float[][] transformMatrix(HashMap<Integer, HashMap<Integer, Double>> idTfIDf) {
        BiMap<Integer, Integer> termIdVocabularyId = null;
        try {
            termIdVocabularyId = AllDocTfIdf.loadTermid();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        BiMap<Integer, Integer> vocabularyIdTermId = termIdVocabularyId.inverse();
        log.info("正在生成" + idTfIDf.size() + "*" + termIdVocabularyId.size() + "的矩阵");
        float[][] docTermMatrix = new float[idTfIDf.size()][termIdVocabularyId.size()];
        for (Map.Entry<Integer, HashMap<Integer, Double>> entry : idTfIDf.entrySet()) {
            int docId = entry.getKey();
            for (Map.Entry<Integer, Double> termEntry : entry.getValue().entrySet()) {
                int vocaId = termEntry.getKey();
                double value = termEntry.getValue();
                int termId = vocabularyIdTermId.get(vocaId);
                docTermMatrix[docId][termId] = (float) value;
            }
        }
        return docTermMatrix;
    }


    /**
     * @param docTermMatrix
     * @return 对于奇异值, 它跟我们特征分解中的特征值类似，在奇异值矩阵中也是按照从大到小排列，而且奇异值的减少特别的快，在很多情况下，前10%甚至1%的奇异值的和就占了全部的奇异值之和的99%以上的比例。也就是说，我们也可以用最大的k个的奇异值和对应的左右奇异向量来近似描述矩阵。也就是说：
     * Am×n=Um×mΣm×nVTn×n≈Um×kΣk×kVTk×n
     */
    private float[][] svd(float[][] docTermMatrix) {
        log.info("开始进行SVD分解，得到Doc与Doc之间的关系");
//        Matrix docTermM = new Matrix(docTermMatrix);
        //因为行的size要大于列的size，所以进行转置
//        SingularValueDecomposition s = docTermM.transpose().svd();
        INDArray A = Nd4j.create(docTermMatrix);
        log.info("SVD计算结束");
        //因为是doc和doc的相似度，所以
//        Matrix U = s.getU();
//        System.out.println(U.toString());
//        Matrix V = s.getV();
//        System.out.println(V.toString());
//        Matrix S = s.getS();
//        System.out.println(S.toString());
        int nRows = A.rows();
        int nColumns = A.columns();
        System.out.println("A: " + A);


        INDArray S = Nd4j.zeros(1, nRows);
        INDArray U = Nd4j.zeros(nRows, nRows);
        INDArray V = Nd4j.zeros(nColumns, nColumns);
        Nd4j.getBlasWrapper().lapack().gesvd(A, S, U, V);

        System.out.println("\n S:" + S);
        System.out.println("\n U:" + U);
        System.out.println("\n V:" + V);
        return docTermMatrix;
    }

    public static void main(String[] args) {
        AllDocTfIdf allDocTfIdf = new AllDocTfIdf();
        HashMap<Integer, HashMap<Integer, Double>> idTfIDf = allDocTfIdf.loadAllDocTfIdf();
        LSICal lsiCal = new LSICal();
        lsiCal.svd(lsiCal.transformMatrix(idTfIDf));

    }
}
