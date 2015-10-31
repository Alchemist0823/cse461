import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

public class NonConnectHttpProxy {

    private static final int TCP_PORT = 46103;

    public static void main(String[] args) {

        try {
            ServerSocket listenSocket = new ServerSocket(TCP_PORT);
            System.out.println("server start tcp listening... ... ...");

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
