package test.socket;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

@Slf4j
public class SocketTest {

    @Test
    public void simpleClient() throws IOException {
        Socket socket = new Socket("localhost", 10000);
        PrintStream ps = new PrintStream(socket.getOutputStream());
        ps.println("你好吗");
        BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String line = br.readLine();
        System.out.println("来自服务器：" + line);
        ps.close();
        br.close();
        socket.close();
    }

    @Test
    public void simpleServer() throws IOException {
        ServerSocket serverSocket = new ServerSocket(10000);
        while (true) {
            Socket s = serverSocket.accept();
            BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
            System.out.println("来自客户端：" + br.readLine());
            log.info("来自客户端访问:" + s.getInetAddress());
            PrintStream ps = new PrintStream(s.getOutputStream());
            ps.println("零零落落");
            ps.close();
            s.close();
        }
    }
}
