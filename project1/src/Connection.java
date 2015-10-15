import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Random;

public class Connection implements Runnable {

    DatagramSocket socket = null;
    DatagramPacket packet = null;

    public Connection(DatagramSocket socket, DatagramPacket packet) {
        this.socket = socket;
        this.packet = packet;
    }

    public void run() {
        byte[] data = this.packet.getData();
        Random rand = new Random();
        Packet content = new Packet(data);

        String str = new String(data, 12, content.getLen());
        if (content.getSecret() == 0 && content.getStep() == 1 && str.equals("hello world\0")) {

            ByteBuffer byteBuffer = ByteBuffer.allocate(12 + 16);
            Util.putHeader(byteBuffer, 16, content.getSecret(), 2, content.getStudentNum());

            int num = rand.nextInt(10) + 5;
            byteBuffer.putInt(num);
            byteBuffer.putInt(rand.nextInt(20) + 10);
            int portB = rand.nextInt(10000) + 10000;
            byteBuffer.putInt(portB);
            int secretA = rand.nextInt(1000);
            byteBuffer.putInt(secretA);

            byte[] responseData = byteBuffer.array();
            DatagramPacket response = new DatagramPacket(responseData, responseData.length,
                    packet.getAddress(), packet.getPort());
            try {
                socket.send(response);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                DatagramSocket socketB = new DatagramSocket(portB);
                InetAddress address = null;
                int clientPortB = 0;
         

                for(int i = 0; i < num; i ++) {
                    DatagramPacket packet = new DatagramPacket(data, data.length);
                    socketB.receive(packet);

                    byte[] dataB = this.packet.getData();
                    Packet contentB = new Packet(dataB);

                    if (rand.nextFloat() < 0.8f && secretA == contentB.getSecret()) {
                        int pid = contentB.getBuffer().getInt(12);

                        ByteBuffer resBuffer = ByteBuffer.allocate(12 + 4);
                        Util.putHeader(resBuffer, 4, contentB.getSecret(), 1, content.getStudentNum());
                        resBuffer.putInt(pid);

                        responseData = resBuffer.array();

                        address = packet.getAddress();
                        clientPortB = packet.getPort();
                        DatagramPacket responseB = new DatagramPacket(responseData, responseData.length,
                                address, clientPortB);

                        socketB.send(responseB);
                    } else {
                        i --;
                    }
                }
                System.out.println("b2");

                ByteBuffer resBuffer = ByteBuffer.allocate(12 + 8);
                Util.putHeader(resBuffer, 8, secretA, 2, content.getStudentNum());

                int tcpPort = rand.nextInt(10000) + 10000;
                resBuffer.putInt(tcpPort);
                int secretB = rand.nextInt(1000);
                resBuffer.putInt(secretB);

                responseData = resBuffer.array();

                DatagramPacket responseB2 = new DatagramPacket(responseData, responseData.length,
                        address, clientPortB);


                // Stage C
                ServerSocket listenSocket = new ServerSocket(tcpPort);
                System.out.println("server start tcp listening... ... ...");
                
                socketB.send(responseB2);
                socketB.close();

                Socket clientSocket = listenSocket.accept();

                ByteBuffer resCBuffer = ByteBuffer.allocate(12 + 16);
                Util.putHeader(resCBuffer, 13, secretB, 2, content.getStudentNum());

                int num2 = rand.nextInt(10) + 5;
                resCBuffer.putInt(num2);
                int len2 = rand.nextInt(20) + 10;
                resCBuffer.putInt(len2);
                int secretC = rand.nextInt(1000);
                resCBuffer.putInt(secretC);
                byte c = (byte) (rand.nextInt(10));
                resCBuffer.put(c);


                DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());
                byte[] sendData = resCBuffer.array();
                dos.write(sendData, 0, sendData.length);
                dos.flush();

                // Stage D
                for (int i = 0; i < num2; i ++) {
                    byte[] resDdata = Util.readBytes(clientSocket, 12 + Util.alignBytes(len2));
                    Packet contentD = new Packet(resDdata);
                    if (contentD.getSecret() == secretC && contentD.getLen() == len2) {
                        boolean t = true;
                        for (int j = 12; j < 12 + len2; j ++) {
                            if (resDdata[j] != c) {
                                t = false;
                                break;
                            }
                        }
                        if (!t) {
                            i--;
                        }
                    } else {
                        i --;
                    }
                }

                ByteBuffer resDBuffer = ByteBuffer.allocate(12 + 4);
                Util.putHeader(resDBuffer, 4, secretC, 2, content.getStudentNum());
                int secretD = rand.nextInt(1000);
                resDBuffer.putInt(secretD);

                sendData = resDBuffer.array();
                dos.write(sendData, 0, sendData.length);
                dos.flush();

                listenSocket.close();

            } catch (SocketException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}