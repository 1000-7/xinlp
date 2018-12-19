package mining.cluster;

import com.google.common.collect.HashMultimap;
import mining.tfidf.AllDocTfIdf;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;

public class KmeansCluster {
    public Map<Integer, Integer> cluster(HashMap<Integer, HashMap<Integer, Double>> idTfidfs, int k) {
        int fileLen = idTfidfs.size();
        Integer[] ids = new Integer[fileLen];

        idTfidfs.keySet().toArray(ids);
        HashMap<Integer, HashMap<Integer, Double>> meansMap = getInitPoint(idTfidfs, k);
        System.out.println(meansMap);
        //distance[i][j]记录点i到聚类中心j的距离
        double[][] distance = new double[fileLen][k];
        //记录所有点属于的聚类序号，初始化全部为0
        int[] assignMeans = new int[fileLen];
        //记录每个聚类的成员点序号
        HashMultimap<Integer, Integer> clusterMember = HashMultimap.create();
        int iterNum = 0;
        while (true) {
            System.out.println("Iteration No." + (iterNum++) + "----------------------");
            //计算每个点和每个聚类中心的距离
            for (int i = 0; i < fileLen; i++) {
                for (int j = 0; j < k; j++) {
                    distance[i][j] = getDistance(idTfidfs.get(i), meansMap.get(j));
                }
            }
            //找出每个点最近的聚类中心
            int[] nearestMeans = new int[fileLen];
            for (int i = 0; i < fileLen; i++) {
                nearestMeans[i] = findNearestMeans(distance, i);
            }
            //判断当前所有点属于的聚类序号是否已经全部是其离得最近的聚类，如果是或者达到最大的迭代次数，那么结束算法
            int okCount = 0;
            for (int i = 0; i < fileLen; i++) {
                if (nearestMeans[i] == assignMeans[i]) {
                    okCount++;
                }
            }
            if (okCount == fileLen || iterNum >= 10) {
                break;
            }
            System.out.println("okCount = " + okCount);
            //如果前面条件不满足，那么需要重新聚类再进行一次迭代，需要修改每个聚类的成员和每个点属于的聚类信息
            clusterMember.clear();
            for (int i = 0; i < fileLen; i++) {
                assignMeans[i] = nearestMeans[i];
                clusterMember.put(nearestMeans[i], i);
            }
            for (int i = 0; i < k; i++) {
                if (!clusterMember.containsKey(i)) {
                    clusterMember.put(i, 0);
                }
                HashMap<Integer, Double> newMean = computeNewMean(clusterMember.get(i), idTfidfs);
                meansMap.put(i, newMean);
            }
        }
        //8、形成聚类结果并且返回
        Map<Integer, Integer> resMap = new TreeMap<>();
        for (int i = 0; i < fileLen; i++) {
            resMap.put(i, assignMeans[i]);
        }
        return resMap;
    }

    private HashMap<Integer, Double> computeNewMean(Set<Integer> integers, HashMap<Integer, HashMap<Integer, Double>> idTfidfs) {
        int size = integers.size();
        HashMap<Integer, Double> oneMean = new HashMap<>();
        for (Integer i : integers) {
            HashMap<Integer, Double> oneFile = idTfidfs.get(i);
            for (Map.Entry<Integer, Double> oneTerm : oneFile.entrySet()) {
                int termKey = oneTerm.getKey();
                double termValue = oneTerm.getValue();
                if (!oneMean.containsKey(oneTerm.getKey())) {
                    oneMean.put(termKey, 0.0);
                }
                oneMean.put(termKey, oneMean.get(termKey) + (termValue / size));
            }
        }
        return oneMean;
    }

    private int findNearestMeans(double[][] distance, int m) {
        double minDist = Double.MAX_VALUE;
        int j = 0;
        for (int i = 0; i < distance[m].length; i++) {
            if (distance[m][i] < minDist) {
                minDist = distance[m][i];
                j = i;
            }
        }
        return j;
    }

    private double getDistance(HashMap<Integer, Double> map1, HashMap<Integer, Double> map2) {
        return computeCos(map1, map2);
    }

    private double computeCos(HashMap<Integer, Double> map1, HashMap<Integer, Double> map2) {
        double norm1 = getMapNorm(map1);
        double norm2 = getMapNorm(map2);
        AtomicReference<Double> mul = new AtomicReference<>(0.0);
        map1.forEach((k, v) -> {
            if (map2.containsKey(k)) {
                mul.updateAndGet(v1 -> v1 + v * map2.get(k));
            }
        });
        return mul.get() / (norm1 * norm2);
    }

    private double getMapNorm(HashMap<Integer, Double> map) {
        AtomicReference<Double> sum = new AtomicReference<>(0.0);
        map.forEach((k, v) -> sum.updateAndGet(v1 -> v1 + Math.pow(v, 2)));
        return Math.sqrt(sum.get());
    }

    private HashMap<Integer, HashMap<Integer, Double>> getInitPoint(HashMap<Integer, HashMap<Integer, Double>> idTfidfs, int k) {
        int count = 0;
        int i = 0;
        HashMap<Integer, HashMap<Integer, Double>> meansMap = new HashMap<>();
        for (Map.Entry<Integer, HashMap<Integer, Double>> entry : idTfidfs.entrySet()) {
            if (count == i * idTfidfs.size() / k) {
                meansMap.put(i, entry.getValue());
                i++;
            }
            count++;
        }
        return meansMap;
    }

    @Test
    public void test() {
        AllDocTfIdf allDocTfIdf = new AllDocTfIdf();
        HashMap<Integer, HashMap<Integer, Double>> idTfIDf = allDocTfIdf.loadAllDocTfIdf();
        KmeansCluster kmeansCluster = new KmeansCluster();
        kmeansCluster.cluster(idTfIDf, 20);
    }
}
