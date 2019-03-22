package lda;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LdaGibbsSampling {
    public static void main(String[] args) {
        Documents docs = new Documents();
        docs.readDocs(LDAConfig.DOCPATH);
        log.info("文章数量：" + docs.getDocs().size());
        log.info("单词数量：" + docs.getTermToIndexMap().size());
        Parameter parameter = Parameter.create(LDAConfig.PARAMPATH);
        LdaModel ldaModel = new LdaModel(parameter);
        log.info("模型初始化中");
        ldaModel.init(docs);
        log.info("模型训练中");
        ldaModel.inference(docs);
        log.info("模型打印");
        ldaModel.saveIteratedModel(100);
        log.info("大功告成");
        
    }
    
}
