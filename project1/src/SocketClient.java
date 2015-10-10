import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;

public class SocketClient {

    final int PORT = 12235;
    final String HOST = "amlia.cs.washington.edu";

    InetAddress IPAddress;

    private int numB;
    private int lenB;
    private int portB;
    private int secretA;

    private int portC;
    private int secretB;

    private int secretC;
    private int numD;
    private int lenD;
    private char cD;

    private int secretD;

    public static void main(String [] args) throws IOException {
        SocketClient client = new SocketClient();
        client.stageA();
    }

    public SocketClient() throws UnknownHostException {
        IPAddress = InetAddress.getByName(HOST);
    }

    private DatagramSocket connectUDP(int port) throws UnknownHostException, SocketException {
        DatagramSocket clientSocket = new DatagramSocket();
        clientSocket.connect(IPAddress, port);
        clientSocket.setSoTimeout(500);
        return clientSocket;
    }

    private Socket connectTCP(int port) throws IOException {
        Socket clientSocket = new Socket(HOST, port);
        return clientSocket;
    }

    public void putHeader(ByteBuffer buffer, int secret, int step, int payloadLen) {
        buffer.putInt(payloadLen);
        buffer.putInt(secret);
        buffer.putShort((short) step);
        buffer.putShort((short) 500);
    }


    public void stageA() throws IOException {
        DatagramSocket clientSocket = connectUDP(PORT);
        byte[] sendData;
        byte[] receiveData = new byte[100];
        //String sentence = inFromUser.readLine();

        String sentence = "hello world\0";

        ByteBuffer byteBuffer = ByteBuffer.allocate(12 + sentence.length());
        putHeader(byteBuffer, 0, 1, sentence.length());
        byteBuffer.put(sentence.getBytes());
        //output.write(sentence.getBytes());

        sendData = byteBuffer.array();

        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, PORT);
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

        ByteBuffer results = ByteBuffer.allocate(100);
        results.put(receiveData);
        int num = results.getInt(12);
        int len = results.getInt(16);
        int port = results.getInt(20);
        int psecret = results.getInt(24);

        System.out.println("Num:" + num);
        System.out.println("len:" + len);
        System.out.println("PORT:" + port);
        System.out.println("secret:" + psecret);

        numB = num;
        lenB = len;
        portB = port;
        secretA = psecret;

        clientSocket.close();
    }


    public void stageB() throws IOException {
        DatagramSocket clientSocket = connectUDP(portB);

        for (int i = 0; i < numB; i ++) {

            while (true) {
                ByteBuffer byteBuffer = ByteBuffer.allocate(12 + 4 + lenB);

                putHeader(byteBuffer, secretA, 1, 4 + lenB);
                byteBuffer.putInt(i);
                byteBuffer.put(new byte[lenB]);


                byte[] sendData = byteBuffer.array();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, portB);
                clientSocket.send(sendPacket);
                byte[] receiveData = new byte[100];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                clientSocket.receive(receivePacket);

                ByteBuffer results = ByteBuffer.allocate(100);
                results.put(receiveData);
                int acked = results.getInt(12);

                if (acked == i)
                    break;
            }
        }


        byte[] receiveData = new byte[100];
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        clientSocket.receive(receivePacket);

        ByteBuffer results = ByteBuffer.allocate(100);
        results.put(receiveData);
        int tcpPort = results.getInt(12);
        int psecret = results.getInt(16);

        portC = tcpPort;
        secretB = psecret;

        System.out.println("tcpPort:" + tcpPort);
        System.out.println("psecert:" + psecret);

        clientSocket.close();
    }

    public static byte[] readBytes(Socket socket, int len) throws IOException {
        InputStream in = socket.getInputStream();
        DataInputStream dis = new DataInputStream(in);
        byte[] data = new byte[len];
        try {
            dis.readFully(data);
        } catch(Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
        }
        return data;
    }

    public void stageC() throws IOException {
        Socket clientSocket = connectTCP(portC);
        ByteBuffer byteBuffer = ByteBuffer.allocate(12);
        putHeader(byteBuffer, secretB, 1, 0);
        byte[] sendData = byteBuffer.array();
        clientSocket.getOutputStream().write(sendData);
        byte[] receiveData = readBytes(clientSocket, 13 + 12);

        ByteBuffer results = ByteBuffer.allocate(25);
        results.put(receiveData);
        numD = results.getInt(12);
        lenD = results.getInt(16);
        secretC = results.getInt(20);
        cD = results.getChar(24);

        clientSocket.close();
    }

    public void stageD() throws IOException {
        Socket clientSocket = connectTCP(portC);
        for (int i = 0; i < numD; i ++) {
            ByteBuffer byteBuffer = ByteBuffer.allocate(12 + 4 + lenB);
            putHeader(byteBuffer, secretC, 1, 4 + lenB);
            byteBuffer.putInt(i);
            for (int j = 0; j < lenD; j ++)
                byteBuffer.putChar(cD);
             byte[] sendData = byteBuffer.array();
            clientSocket.getOutputStream().write(sendData);

        }
        byte[] receiveData = readBytes(clientSocket, 12 + 4);
        ByteBuffer results = ByteBuffer.allocate(16);
        results.put(receiveData);
        secretD = results.getInt(12);

        System.out.println("secretD: " + secretD);

        clientSocket.close();
    }
}
