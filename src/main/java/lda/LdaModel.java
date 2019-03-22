package lda;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Data
@Slf4j
public class LdaModel {
    int V, K, M;//词表长度, 主题数量, 文档数量
    int[][] docs;//第m个文档第n个单词的词表索引
    int[][] z;// 第m个文档第n个单词的所在主题
    float alpha; //文档-主题 狄利克雷先验参数
    float beta; //主题-单词 狄利克雷先验参数
    int[][] nmk;//每个文档下不同主题的个数, 文档长度*主题个数
    int[][] nkt;//每个主题下不同词的个数，主题个数*词表长度
    int[] nmkSum;//第k个文档单词的个数
    int[] nktSum;//第k个主题单词的个数
    double[][] phi;//每个主题下所有单词的概率分布，主题个数*词表长度
    double[][] theta;//每个文档下所有主题的概率分布，文档长度*主题个数
    int iterations;//迭代次数
    int saveStep;//每隔几步保存一次
    int beginSaveIters;//从哪一次开始保存
    
    public LdaModel(Parameter parameter) {
        this.alpha = parameter.getAlpha();
        this.beta = parameter.getBeta();
        this.iterations = parameter.getIteration();
        this.K = parameter.getTopicNum();
        this.saveStep = parameter.getSaveStep();
        this.beginSaveIters = parameter.getBeginSaveIters();
    }
    
    public void init(Documents docSet) {
        this.M = docSet.getDocs().size();
        this.V = docSet.getTermToIndexMap().size();
        this.nmk = new int[M][K];
        this.nkt = new int[K][V];
        this.nmkSum = new int[M];
        this.nktSum = new int[K];
        this.phi = new double[K][V];
        this.theta = new double[M][K];
        this.docs = new int[M][];
        
        for (int m = 0; m < M; m++) {
            int n = docSet.getDocs().get(m).getDocWords().length;
            docs[m] = new int[n];
            System.arraycopy(docSet.getDocs().get(m).getDocWords(), 0, docs[m], 0, n);
        }
        
        this.z = new int[M][];
        for (int i = 0; i < M; i++) {
            int n = docSet.getDocs().get(i).getDocWords().length;
            z[i] = new int[n];
            for (int j = 0; j < n; j++) {
                int k = (int) (Math.random() * K);
                z[i][j] = k;
                nmk[i][k]++;
                nkt[k][docs[i][j]]++;
                nktSum[k]++;
            }
            nmkSum[i] = n;
        }
    }
    
    public void inference(Documents docSet) {
        if (iterations < saveStep + beginSaveIters) {
            System.err.println("Error: the number of iterations should be larger than " + (saveStep + beginSaveIters));
            System.exit(0);
        }
        
        for (int i = 0; i < iterations; i++) {
            log.info("迭代次数：" + i);
            if ((i >= beginSaveIters) && (((i - beginSaveIters) % saveStep) == 0)) {
                log.info("当前迭代次数为{},保存模型", i);
                updateEstimatedParameters();
                saveIteratedModel(i);
            }
            
            for (int m = 0; m < M; m++) {
                int N = docSet.getDocs().get(m).getDocWords().length;
                for (int n = 0; n < N; n++) {
                    // Sample from p(z_i|z_-i, w)
                    int newTopic = sampleTopicZ(m, n);
                    z[m][n] = newTopic;
                }
            }
        }
    }
    
    /**
     * 吉布斯采样
     *
     * @param m
     * @param n
     * @return
     */
    private int sampleTopicZ(int m, int n) {
        //删除 w_{m,n}
        int oldTopic = z[m][n];
        nmk[m][oldTopic]--;
        nkt[oldTopic][docs[m][n]]--;
        nmkSum[m]--;
        nktSum[oldTopic]--;
        
        //计算 p(z_i = k|z_-i, w)
        double[] p = new double[K];
        for (int k = 0; k < K; k++) {
            p[k] = (nkt[k][docs[m][n]] + beta) / (nktSum[k] + V * beta) * (nmk[m][k] + alpha) / (nmkSum[m] + K * alpha);
        }
        
        for (int k = 1; k < K; k++) {
            p[k] += p[k - 1];
        }
        double u = Math.random() * p[K - 1];
        int newTopic;
        for (newTopic = 0; newTopic < K; newTopic++) {
            if (u < p[newTopic]) {
                break;
            }
        }
        
        // w_{m, n} 换成新topic
        nmk[m][newTopic]++;
        nkt[newTopic][docs[m][n]]++;
        nmkSum[m]++;
        nktSum[newTopic]++;
        return newTopic;
    }
    
    public void saveIteratedModel(int i) {
        String resPath = LDAConfig.RESPATH;
        String modelPath = resPath + "lda_" + i;
        ArrayList<String> lines = new ArrayList<String>();
        lines.add("alpha = " + alpha);
        lines.add("beta = " + beta);
        lines.add("topicNum = " + K);
        lines.add("docNum = " + M);
        lines.add("termNum = " + V);
        lines.add("iterations = " + iterations);
        lines.add("saveStep = " + saveStep);
        lines.add("beginSaveIters = " + beginSaveIters);
        try {
            FileUtils.writeLines(new File(modelPath + ".params"), lines);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        writeMatrix(phi, modelPath + ".phi");
        writeMatrix(theta, modelPath + ".theta");
        writeDocTopic(docs, z, modelPath + ".all");
        writeTopWords(modelPath + ".topicNwords");
    }
    
    private void writeTopWords(String path) {
        int topNum = 20;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
            for (int i = 0; i < K; i++) {
                List<Integer> words = new ArrayList<>();
                for (int j = 0; j < V; j++) {
                    words.add(j);
                }
                double[] phii = phi[i];
                words.sort((o1, o2) -> {
                    double minus = phii[o2] - phii[o1];
                    if (minus == 0) {
                        return 0;
                    }
                    return minus > 0 ? 1 : -1;
                });
                writer.write("topic " + i + "\t:\t");
                for (int t = 0; t < topNum; t++) {
                    writer.write(Documents.getIndexToTermList().get(words.get(t)) + "=" + phi[i][words.get(t)] + "\t");
                }
                writer.write("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    
    private void writeMatrix(double[][] matrix, String path) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
            for (int i = 0; i < matrix.length; i++) {
                for (int j = 0; j < matrix[0].length; j++) {
                    writer.write(matrix[i][j] + "\t");
                }
                writer.write("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void writeDocTopic(int[][] doc, int[][] topic, String path) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
            for (int i = 0; i < doc.length; i++) {
                for (int j = 0; j < doc[i].length; j++) {
                    writer.write(doc[i][j] + ":" + topic[i][j] + ":" + Documents.getIndexToTermList().get(j) + "\t");
                }
                writer.write("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    
    private void updateEstimatedParameters() {
        for (int m = 0; m < M; m++) {
            for (int k = 0; k < K; k++) {
                theta[m][k] = (nmk[m][k] + alpha) / (nmkSum[m] + K * alpha);
            }
        }
        
        for (int k = 0; k < K; k++) {
            for (int t = 0; t < V; t++) {
                phi[k][t] = (nkt[k][t] + beta) / (nktSum[k] + V * beta);
            }
        }
    }
}
