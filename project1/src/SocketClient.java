import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;

public class SocketClient {

    final int port = 12235;
    final String host = "amlia.cs.washington.edu";

    public static void main(String [] args) throws IOException {
        SocketClient client = new SocketClient();
        client.stageA();
    }

    public void stageA() throws IOException {
        //BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
        DatagramSocket clientSocket = new DatagramSocket();
        InetAddress IPAddress = InetAddress.getByName(host);

        byte[] sendData;
        byte[] receiveData = new byte[1000];
        //String sentence = inFromUser.readLine();

        String sentence = "hello world\0";

        ByteBuffer byteBuffer = ByteBuffer.allocate(12 + sentence.length());

        byteBuffer.putInt(sentence.length());
        byteBuffer.putInt(0);
        byteBuffer.putShort((short) 2);
        byteBuffer.putShort((short) 500);
        byteBuffer.put(sentence.getBytes());
        //output.write(sentence.getBytes());

        sendData = byteBuffer.array();

        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
        while(true) {
            try {
                clientSocket.send(sendPacket);
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                clientSocket.receive(receivePacket);

                break;
            } catch(Exception e) {
                System.out.println(e.toString());
            }
        }

        ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(receiveData));
        int num = input.readInt();
        int len = input.readInt();
        int udpPort = input.readInt();
        int secretA = input.readInt();

        System.out.println("FROM SERVER:" + num);
        System.out.println("FROM SERVER:" + len);
        System.out.println("FROM SERVER:" + udpPort);
        System.out.println("FROM SERVER:" + secretA);
        clientSocket.close();
    }
}
