package segment.crf;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author unclewang
 * @Date 2018-11-27 11:46
 */
@Log
@Data
public class XinCRFModel implements Serializable {

    static final long serialVersionUID = 5167607155517042691L;


    String filePath;

    /**
     * 标签和id的相互转换
     */
    Map<String, Integer> tag2id;
    /**
     * id转标签
     */
    protected String[] id2tag;
    /**
     * 特征函数
     */
    DoubleArrayTrie<FeatureFunction> featureFunctionTrie;
    /**
     * 特征模板,特征模版是为了傻瓜式的生成特征函数
     */
    List<FeatureTemplate> featureTemplateList;
    /**
     * tag的二元转移矩阵，适用于BiGram Feature
     */
    protected Double[][] matrix;

    private static class SingletonHolder {
        private static final XinCRFModel INSTANCE = new XinCRFModel();
    }

    public static XinCRFModel getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private XinCRFModel() {
        this.featureFunctionTrie = new DoubleArrayTrie<>();
        init();
    }

    private void init() {
        loadTxt();
    }

    public void loadTxt() {
        log.info("模型开始初始化");
        filePath = XinCRFConfig.getFilePath();

        log.info("开始读取模型文件");
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(this.filePath))))) {
            //model前四行
            log.info(br.readLine());
            log.info(br.readLine());
            log.info(br.readLine());
            log.info(br.readLine());
            //然后空一行
            br.readLine();
            log.info("开始读取隐藏状态信息");
            String line;
            int id = 0;
            tag2id = new HashMap<>();
            while ((line = br.readLine()).length() != 0) {
                tag2id.put(line, id++);
            }
            id2tag = new String[tag2id.size()];
            for (Map.Entry<String, Integer> entry : tag2id.entrySet()) {
                id2tag[entry.getValue()] = entry.getKey();
            }
            log.info("开始读取特征模版信息");
            featureTemplateList = new LinkedList<>();
            //不用空行了
            while ((line = br.readLine()).length() != 0) {
                if ("B".equals(line)) {
                    matrix = new Double[id2tag.length][id2tag.length];
                } else {
                    FeatureTemplate featureTemplate = FeatureTemplate.create(line);
                    featureTemplateList.add(featureTemplate);
                }
            }
            // 单独写个B是忽略观测序列，只为前后状态生成转移函数。
            // 如果有B，有一行关于B的内容，直接跳过
            // 其实应该可以定义B模版，但是一般都是单独写个B，这个B就相当于Hmm的状态转移矩阵
            if (matrix != null) {
                br.readLine();
            }
            // 构建trie树的时候用
            TreeMap<String, FeatureFunction> featureFunctionMap = new TreeMap<>();
            List<FeatureFunction> featureFunctionList = new LinkedList<FeatureFunction>();
            log.info("开始读取特征函数信息");
            while ((line = br.readLine()).length() != 0) {
                String[] args = line.split(" ", 2);
                char[] charArray = args[1].toCharArray();
                FeatureFunction ff = new FeatureFunction(charArray, id2tag.length);
                featureFunctionMap.put(args[1], ff);
                featureFunctionList.add(ff);
            }

            if (matrix != null) {
                log.info("开始读取B的转移参数");
                for (int i = 0; i < id2tag.length; i++) {
                    for (int j = 0; j < id2tag.length; j++) {
                        matrix[i][j] = Double.parseDouble(br.readLine());
                    }
                }
            }
            log.info("开始读取特征函数的参数");
            for (FeatureFunction featureFunction : featureFunctionList) {
                for (int i = 0; i < id2tag.length; i++) {
                    featureFunction.w[i] = Double.parseDouble(br.readLine());
                }
            }
            if (br.readLine() != null) {
                log.warning("文本有存留，模型可能会有问题");
            } else {
                log.info("模型文件:" + filePath + " 读取成功");
            }

            log.info("特征函数变成双数组tire存储");
            featureFunctionTrie.build(featureFunctionMap);
            log.info("模型创建成功");
            save(XinCRFConfig.getModelPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //存取对象到二进制文件中
    //TODO
    public void saveBin(String outFilePath) {
        try (DataOutputStream oos = new DataOutputStream(new FileOutputStream(new File(outFilePath)))) {
            oos.writeInt(id2tag.length);
            for (String tag : id2tag) {
                oos.writeUTF(tag);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //存取对象到模型文件里
    public void save(String outFilePath) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(outFilePath)))) {
            oos.writeObject(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //读取对象的模型文件
    public static XinCRFModel load(String filePath) {

        long start = System.currentTimeMillis();
        log.info("开始读取CRFModel");
        XinCRFModel xinCRFModel = null;
        try (ObjectInputStream oos = new ObjectInputStream(new FileInputStream(new File(filePath)))) {
            xinCRFModel = (XinCRFModel) oos.readObject();

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        if (xinCRFModel != null) {
            log.info("读取CRFModel完毕，共耗费时间" + (System.currentTimeMillis() - start) + "ms");
        } else {
            log.warning("读取CRFModel出错，耗时" + (System.currentTimeMillis() - start) + "ms");
        }

        return xinCRFModel;
    }


    public static void main(String[] args) {
//        XinCRFModel xinCRFModel = XinCRFModel.load("xincrf.model");
        XinCRFModel xinCRFModel = XinCRFModel.getInstance();
        System.out.println(xinCRFModel.tag2id);
        System.out.println(xinCRFModel.featureTemplateList);
    }
}

class FeatureTemplate implements Serializable {
    static final long serialVersionUID = 5167607155517042691L;

    /**
     * 用来解析模板的正则表达式
     */
    static final Pattern PATTERN = Pattern.compile("%x\\[(-?\\d*),(\\d*)]");
    String template;
    /**
     * 每个部分%x[-2,0]的位移，其中int[0]储存第一个数（-2），int[1]储存第二个数（0）
     * 特征选取的行是相对的，列数绝对的
     * 一般选取相对行前后m行，选取n-1列（假设语料总共有n列）
     * 特征表示方法为：%x[行，列]，行列的初始位置都为0。
     */
    ArrayList<int[]> offsetList;
    List<String> delimiterList;

    public FeatureTemplate() {
    }

    public static FeatureTemplate create(String template) {
        FeatureTemplate featureTemplate = new FeatureTemplate();
        featureTemplate.delimiterList = new LinkedList<>();
        featureTemplate.offsetList = new ArrayList<>(3);
        featureTemplate.template = template;
        Matcher matcher = PATTERN.matcher(template);
        int start = 0;
        while (matcher.find()) {
            featureTemplate.delimiterList.add(template.substring(start, matcher.start()));
            start = matcher.end();
            featureTemplate.offsetList.add(new int[]{Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2))});
        }
        return featureTemplate;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FeatureTemplate{");
        sb.append("template='").append(template).append('\'');
        sb.append(", offsetList=[");
        offsetList.forEach(s -> {
            for (int a : s) {
                sb.append(a).append(",");
            }
        });
        sb.append("], delimiterList=").append(delimiterList);
        sb.append('}');
        return sb.toString();
    }

    @Test
    public void test() {
        FeatureTemplate featureTemplate = FeatureTemplate.create("U06:%x[-1,0]/%x[1,0]");
        System.out.println(featureTemplate);
    }

    /**
     * 生成具体的特征函数
     * 比如当前this模版是  U06:%x[-1,0]/%x[1,0]
     * 前一个字后一个字
     * 碰见"科技的具体表现"
     * 当观测序列到"的"的时候，应该生成下面的这个：
     * U06:技/具
     * 这个找到之后就好弄了，因为featureFuntion的treemap可以找到 U06:技/具 对应的四种状态的参数
     */
    public char[] generateParameter(XinTable table, int current) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (String d : delimiterList) {
            sb.append(d);
            int[] offset = offsetList.get(i++);
            sb.append(table.get(current + offset[0], offset[1]));
        }

        char[] o = new char[sb.length()];
        sb.getChars(0, sb.length(), o, 0);

        return o;
    }
}

@Getter
@Setter
class FeatureFunction implements Serializable {
    static final long serialVersionUID = 5167607155517042691L;

    /**
     * 环境参数
     */
    char[] o;

    /**
     * 权值，按照index对应于tag的id
     */
    double[] w;

    public FeatureFunction(char[] o, int size) {
        this.o = o;
        this.w = new double[size];
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FeatureFunction{");
        sb.append("o=").append(Arrays.toString(o));
        sb.append(", w=").append(Arrays.toString(w));
        sb.append('}');
        return sb.toString();
    }
}
