package lda;

import lombok.Data;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Data
public class Documents {
    private ArrayList<Doc> docs;
    private static ArrayList<String> indexToTermList;
    private Map<String, Integer> termToIndexMap;
    private Map<String, Integer> termCountMap;
    
    public Documents() {
        docs = new ArrayList<>();
        termToIndexMap = new HashMap<>();
        indexToTermList = new ArrayList<>();
        termCountMap = new HashMap<>();
    }
    
    public static ArrayList<String> getIndexToTermList() {
        return indexToTermList;
    }
    
    public void readDocs(String docsPath) {
        for (File docFile : Objects.requireNonNull(new File(docsPath).listFiles())) {
            Doc doc = null;
            try {
                doc = Doc.create(docFile.getAbsolutePath(), termToIndexMap, indexToTermList, termCountMap);
            } catch (IOException e) {
                e.printStackTrace();
            }
            docs.add(doc);
        }
    }
}

@Data
class Doc {
    private String docName;
    private int[] docWords;
    private static final Pattern PATTERN = Pattern.compile(".*[a-zA-Z]+.*");
    private static final Pattern CHINESE_PATTERN = Pattern.compile(".*[a-zA-Z]+.*");
    
    public static Doc create(String absolutePath, Map<String, Integer> termToIndexMap, ArrayList<String> indexToTermList, Map<String, Integer> termCountMap) throws IOException {
        Doc doc = new Doc();
        
        doc.docName = absolutePath;
        List<String> docLines = FileUtils.readLines(new File(absolutePath), "UTF8");
        ArrayList<String> words = new ArrayList<>();
        docLines.forEach(line -> {
            StringTokenizer strTok = new StringTokenizer(line);
            while (strTok.hasMoreTokens()) {
                String token = strTok.nextToken().replace(".", "").replace(">", "").replace("<", "");
                words.add(token.toLowerCase().trim());
            }
        });
        List<String> collect = words.stream().filter(s -> isNoiseWord(s, CHINESE_PATTERN)).collect(Collectors.toList());
        doc.docWords = new int[collect.size()];
        for (int i = 0; i < collect.size(); i++) {
            String word = collect.get(i);
            if (!termToIndexMap.containsKey(word)) {
                int newIndex = termToIndexMap.size();
                termToIndexMap.put(word, newIndex);
                indexToTermList.add(word);
                termCountMap.put(word, 1);
                doc.docWords[i] = newIndex;
            } else {
                doc.docWords[i] = termToIndexMap.get(word);
                termCountMap.put(word, termCountMap.get(word) + 1);
            }
        }
        collect.clear();
        return doc;
    }
    
    
    public static boolean isNoiseWord(String string, Pattern pattern) {
        string = string.toLowerCase().trim();
        Matcher m = pattern.matcher(string);
        // filter @xxx and URL
        if (string.matches(".*www\\..*") || string.matches(".*\\.com.*") ||
                string.matches(".*http:.*")) {
            return true;
        }
        return !m.matches();
    }
    
    @Test
    public void test() {
        String path = "/Users/unclewang/Idea_Projects/xinlp/src/main/resources/lda/doc";
        Documents docSet = new Documents();
        docSet.readDocs(path);
    }
}
