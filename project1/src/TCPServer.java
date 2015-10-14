import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServer {
    public static void main (String args[])
    {
        try{
            int serverPort = Util.PORT;
            ServerSocket listenSocket = new ServerSocket(serverPort);

            System.out.println("server start listening... ... ...");

            while(true) {
                Socket clientSocket = listenSocket.accept();
                TCPConnection connection = new TCPConnection(clientSocket);
            }
        }
        catch(IOException e) {
            System.out.println("Listen :"+e.getMessage());}
    }
}