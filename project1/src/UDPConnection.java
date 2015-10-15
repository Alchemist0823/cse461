import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Random;

public class UDPConnection implements Runnable {

    DatagramSocket socket = null;
    DatagramPacket packet = null;

    public UDPConnection(DatagramSocket socket, DatagramPacket packet) {
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
                resBuffer.putInt(rand.nextInt(10000) + 10000);
                resBuffer.putInt(rand.nextInt(1000));

                responseData = resBuffer.array();

                DatagramPacket responseB2 = new DatagramPacket(responseData, responseData.length,
                        address, clientPortB);

                socketB.send(responseB2);

            } catch (SocketException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}