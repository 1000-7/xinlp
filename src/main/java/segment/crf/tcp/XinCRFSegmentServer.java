package segment.crf.tcp;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import lucene.Atom;
import segment.crf.XinCRFSegment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

/**
 * @Author unclewang
 * @Date 2018-12-12 14:15
 */
@Slf4j
public class XinCRFSegmentServer {

    static XinCRFSegment segment = new XinCRFSegment();


    public static void main(String[] args) throws IOException {

        ServerSocket ss = new ServerSocket(9428);
        while (true) {
            Socket s = ss.accept();
            new Thread(new ServerThread(s)).start();
        }
    }


    private static class ServerThread implements Runnable {
        Socket s = null;
        BufferedReader br = null;
        PrintStream ps = null;

        public ServerThread(Socket s) throws IOException {
            this.s = s;
            this.br = new BufferedReader(new InputStreamReader(s.getInputStream()));
            this.ps = new PrintStream(s.getOutputStream());
        }

        @Override
        public void run() {
            String content = null;
            while ((content = read()) != null) {
                List<Atom> atoms = segment.seg(content);
                try {
                    System.out.println(JSON.toJSONString(atoms));
                    ps.write(JSON.toJSONString(atoms).getBytes());
                    ps.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                br.close();
                ps.close();
                s.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        public String read() {
            try {
                String readContent = br.readLine();
                log.info("client请求，数据为：" + readContent);
                return readContent;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

    }
}
