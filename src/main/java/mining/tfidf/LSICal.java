package mining.tfidf;

import Jama.Matrix;
import Jama.SingularValueDecomposition;
import com.google.common.collect.BiMap;
import lombok.extern.slf4j.Slf4j;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class LSICal {
    private BiMap<Integer, Integer> termIdVocabularyId;

    public double[][] transformMatrix(HashMap<Integer, HashMap<Integer, Double>> idTfIDf) {
        BiMap<Integer, Integer> termIdVocabularyId = null;
        try {
            termIdVocabularyId = AllDocTfIdf.loadTermid();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        BiMap<Integer, Integer> vocabularyIdTermId = termIdVocabularyId.inverse();
        log.info("正在生成" + idTfIDf.size() + "*" + termIdVocabularyId.size() + "的矩阵");
        double[][] docTermMatrix = new double[idTfIDf.size()][termIdVocabularyId.size()];
        for (Map.Entry<Integer, HashMap<Integer, Double>> entry : idTfIDf.entrySet()) {
            int docId = entry.getKey();
            for (Map.Entry<Integer, Double> termEntry : entry.getValue().entrySet()) {
                int vocaId = termEntry.getKey();
                double value = termEntry.getValue();
                int termId = vocabularyIdTermId.get(vocaId);
                docTermMatrix[docId][termId] = value;
            }
        }
        return docTermMatrix;
    }

    private double[][] svd(double[][] docTermMatrix) {
        log.info("开始进行SVD分解，得到Doc与Doc之间的关系");
        Matrix docTermM = new Matrix(docTermMatrix);
        //因为行的size要大于列的size，所以进行转置，原来想得到的是V，
        SingularValueDecomposition s = docTermM.transpose().svd();
        log.info("SVD计算结束");
        Matrix U = s.getU();
        System.out.println(U.toString());
        Matrix V = s.getV();
        System.out.println(V.toString());
        Matrix S = s.getS();
        System.out.println(S.toString());
        return docTermM.getArray();
    }

    public static void main(String[] args) {
        AllDocTfIdf allDocTfIdf = new AllDocTfIdf();
        HashMap<Integer, HashMap<Integer, Double>> idTfIDf = allDocTfIdf.loadAllDocTfIdf();
        LSICal lsiCal = new LSICal();
        lsiCal.svd(lsiCal.transformMatrix(idTfIDf));

    }
}
