package segment.bilstmcrf;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import lombok.extern.slf4j.Slf4j;
import lucene.simple.Atom;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;
import segment.Segment;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

/**
 * @Author unclewang
 * @Date 2018-12-12 12:58
 * BiLSTM+CRF分词，http访问
 */
@Slf4j
public class BLCSegment implements Segment {
    public static final String IP = "192.168.1.104";

    @Override
    public List<Atom> seg(String text) {
        try {
            JSONArray jsonArray = getTokens(text);
            char[] chars = text.toCharArray();
            assert jsonArray.size() == chars.length;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < jsonArray.size(); i++) {
                sb.append(chars[i]);
                if ("S".equals(jsonArray.get(i).toString()) || "E".equals(jsonArray.get(i).toString())) {
                    sb.append("\t");
                }
            }
            String[] strings = sb.toString().trim().split("[\t\n]");
            return strings2AtomList(strings);
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }
        log.error("BiLSTM+CRF分词出问题了，赶快过来看看");
        return null;
    }

    public JSONArray getTokens(String sent) throws URISyntaxException, IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        ResponseHandler<String> responseHandler = (response) -> {
            int status = response.getStatusLine().getStatusCode();
            if (status >= 200 && status < 300) {
                HttpEntity entity = response.getEntity();
                return entity != null ? EntityUtils.toString(entity, "UTF-8")
                        : null;
            } else if (status == 404) {
                return "404";
            } else {
                System.out.println("Error occured, statusLine : " + status);
                throw new ClientProtocolException(
                        "Unexpected response status: " + status);
            }
        };

        URI uri = new URIBuilder()
                .setScheme("http")
                .setHost(IP)
                .setPort(9006)
                .setPath("/predict")
                .setParameter("sent", sent)
                .build();
        HttpGet get = new HttpGet(uri);
        get.setHeader("Accept-Encoding", "gzip,deflate,sdch");
        String tokens = httpClient.execute(get, responseHandler);
        System.out.println(tokens);
        return JSON.parseArray(tokens);
    }

    @Test
    public void test() {
        seg("碰到的一个问题 - 小橙子宝贝 - 博客园");
    }

}
