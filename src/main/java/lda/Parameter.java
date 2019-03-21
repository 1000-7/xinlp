package lda;


import lombok.Data;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Data
public class Parameter {
    private float alpha = 0.5f;
    private float beta = 0.1f;
    private int topicNum = 100;
    private int iteration = 100;
    private int saveStep = 10;
    private int beginSaveIters = 50;
    
    public static Parameter create(String parameterFile) {
        // TODO Auto-generated method stub
        List<String> paramLines = null;
        try {
            paramLines = FileUtils.readLines(new File(parameterFile), "UTF8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Parameter parameter = new Parameter();
        
        for (String line : Objects.requireNonNull(paramLines)) {
            String[] lineParts = line.split("\t");
            switch (parameters.valueOf(lineParts[0])) {
                case alpha:
                    parameter.alpha = Float.valueOf(lineParts[1]);
                    break;
                case beta:
                    parameter.beta = Float.valueOf(lineParts[1]);
                    break;
                case topicNum:
                    parameter.topicNum = Integer.valueOf(lineParts[1]);
                    break;
                case iteration:
                    parameter.iteration = Integer.valueOf(lineParts[1]);
                    break;
                case saveStep:
                    parameter.saveStep = Integer.valueOf(lineParts[1]);
                    break;
                case beginSaveIters:
                    parameter.beginSaveIters = Integer.valueOf(lineParts[1]);
                    break;
                default:
                    break;
            }
        }
        return parameter;
    }
    
    public enum parameters {
        alpha, beta, topicNum, iteration, saveStep, beginSaveIters;
    }
}