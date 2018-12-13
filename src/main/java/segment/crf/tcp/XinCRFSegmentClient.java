package segment.crf.tcp;

import com.alibaba.fastjson.JSONObject;
import lucene.simple.Atom;
import org.junit.jupiter.api.Test;
import segment.Segment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.List;

/**
 * @Author unclewang
 * @Date 2018-12-12 14:24
 */
public class XinCRFSegmentClient implements Segment {

    @Override
    public List<Atom> seg(String text) {
        Socket socket = null;
        List<Atom> list = null;
        try {
            socket = new Socket("localhost", 9428);
            PrintStream ps = new PrintStream(socket.getOutputStream());
            ps.println(text);
            ps.flush();
            socket.shutdownOutput();
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            list = JSONObject.parseArray(br.readLine(), Atom.class);
            br.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Test
    public void test() {
        System.out.println(seg("你好"));
    }
}
