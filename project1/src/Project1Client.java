import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;

public class Project1Client {
    final static String HOST = "amlia.cs.washington.edu";
    final static int STUDENT_NUM = 500;

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
    private byte cD;

    private int secretD;

    public static void main(String [] args) throws IOException {
        Project1Client client = new Project1Client();
        client.stageA();
        client.stageB();
        client.stageD(client.stageC());
    }

    public Project1Client() throws UnknownHostException {
        IPAddress = InetAddress.getByName(HOST);
    }

    private DatagramSocket connectUDP(int port) throws UnknownHostException, SocketException {
        DatagramSocket clientSocket = new DatagramSocket();
        clientSocket.connect(IPAddress, port);
        clientSocket.setSoTimeout(1000);
        return clientSocket;
    }

    private Socket connectTCP(int port) throws IOException {
        Socket clientSocket = new Socket(HOST, port);
        return clientSocket;
    }

    public void stageA() throws IOException {
        DatagramSocket clientSocket = connectUDP(Util.PORT);
        byte[] sendData;
        byte[] receiveData = new byte[100];
        //String sentence = inFromUser.readLine();

        String sentence = "hello world\0";

        ByteBuffer byteBuffer = ByteBuffer.allocate(12 + sentence.length());
        Util.putHeader(byteBuffer, sentence.length(), 0, 1, STUDENT_NUM);
        byteBuffer.put(sentence.getBytes());
        //output.write(sentence.getBytes());

        sendData = byteBuffer.array();

        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, Util.PORT);
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
        numB = results.getInt(12);
        lenB = results.getInt(16);
        portB = results.getInt(20);
        secretA = results.getInt(24);

        System.out.println("numB:" + numB);
        System.out.println("lenB:" + lenB);
        System.out.println("portB:" + portB);
        System.out.println("secretA:" + secretA);

        clientSocket.close();
    }

    public static int alignBytes(int num) {
        return ((num + 3) / 4) * 4;
    }

    public void stageB() throws IOException {
        DatagramSocket clientSocket = connectUDP(portB);

        for (int i = 0; i < numB; i ++) {

            while (true) {
                try {
                    int lenAligned = alignBytes(lenB + 4);
                    ByteBuffer byteBuffer = ByteBuffer.allocate(12 + lenAligned);
                    Util.putHeader(byteBuffer, 4 + lenB, secretA, 1, STUDENT_NUM);
                    byteBuffer.putInt(i);

                    byte[] sendData = byteBuffer.array();
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, portB);
                    clientSocket.send(sendPacket);
                    byte[] receiveData = new byte[100];
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

                    System.out.println("Receive " + i);
                    clientSocket.receive(receivePacket);

                    ByteBuffer results = ByteBuffer.allocate(100);
                    results.put(receiveData);
                    int acked = results.getInt(12);

                    if (acked == i)
                        break;
                } catch(Exception e) {
                    System.out.println(e.toString());
                }
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
        System.out.println("secretB:" + psecret);

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

    public Socket stageC() throws IOException {
        Socket clientSocket = connectTCP(portC);
        byte[] receiveData = readBytes(clientSocket, 28);

        ByteBuffer results = ByteBuffer.allocate(100);
        results.put(receiveData);
        numD = results.getInt(12);
        lenD = results.getInt(16);
        secretC = results.getInt(20);
        cD = results.get(24);

        System.out.println("numD:" + numD);
        System.out.println("lenD:" + lenD);
        System.out.println("secretC:" + secretC);
        System.out.println("cD:" + cD);

        return clientSocket;
    }

    public void stageD(Socket clientSocket) throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocate(12 + alignBytes(lenD));
        Util.putHeader(byteBuffer, lenD, secretC, 1, STUDENT_NUM);
        for (int i = 0; i < lenD; i ++) {
            byteBuffer.put(i + 12, cD);
        }
        byte[] sendData = byteBuffer.array();

        for (int i = 0; i < numD; i ++) {
            DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());
            dos.write(sendData, 0, sendData.length);
            dos.flush();
        }
        byte[] receiveData = readBytes(clientSocket, 12 + 4);
        ByteBuffer results = ByteBuffer.allocate(100);
        results.put(receiveData);
        secretD = results.getInt(12);

        System.out.println("secretD: " + secretD);

        clientSocket.close();
    }
}
