package project2;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Date;

public class NonConnectHttpProxy {

    private static final int TCP_PORT = 46103;

    public static void main(String[] args) {

        try {
            ServerSocket listenSocket = new ServerSocket(TCP_PORT);
            System.out.println(new Date() + " - Proxy listening on 0.0.0.0:" + TCP_PORT);

            while(true) {
                Socket clientSocket = listenSocket.accept();
                //DataInputStream dis = new DataInputStream(clientSocket.getInputStream());

                new Thread(new NonConnectProxyConnection(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}
