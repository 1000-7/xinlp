package mining.cluster;

import mining.tfidf.AllDocTfIdf;

import java.util.HashMap;

public class ClusterApp {
    public static void main(String[] args) {
        AllDocTfIdf allDocTfIdf = new AllDocTfIdf();
        HashMap<Integer, HashMap<Integer, Double>> idTfIDf = allDocTfIdf.loadAllDocTfIdf();
        KmeansCluster kmeansCluster = new KmeansCluster();
        System.out.println(kmeansCluster.cluster(idTfIDf, 10));
    }
}
