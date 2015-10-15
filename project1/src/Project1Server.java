import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class Project1Server {
    public static void main (String args[])
    {
        try{
            int serverPort = Util.PORT;
            DatagramSocket serverSocket = new DatagramSocket(serverPort);

            System.out.println("server start listening... ... ...");

            while(true) {
                byte[] receiveData = new byte[65536];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);

                new Thread(new Connection(serverSocket, receivePacket)).start();
            }
        } catch(IOException e) {
            System.out.println("Listen :"+e.getMessage());}
    }
}