package segment.crf.tcp;

import com.alibaba.fastjson.JSONObject;
import lucene.simple.Atom;

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
public class XinCRFSegmentClient {
    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("localhost", 9428);
        PrintStream ps = new PrintStream(socket.getOutputStream());
        ps.println("我过的很开心");
        ps.flush();
        socket.shutdownOutput();
        BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        List<Atom> list = JSONObject.parseArray(br.readLine(), Atom.class);
        System.out.println(list);

        br.close();
        socket.close();
    }

}
