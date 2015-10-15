import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
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

            byteBuffer.putInt(rand.nextInt(10) + 5);
            byteBuffer.putInt(rand.nextInt(20) + 10);
            int portB = rand.nextInt(10000) + 10000;
            byteBuffer.putInt(portB);
            byteBuffer.putInt(rand.nextInt(1000));

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
                while(true) {
                    DatagramPacket packet = new DatagramPacket(data, data.length);
                    socketB.receive(packet);

                    byte[] dataB = this.packet.getData();
                    Packet contentB = new Packet(dataB);
                    int pid = contentB.getBuffer().getInt(12);

                    if (rand.nextFloat() < 0.8f) {
                        ByteBuffer resBuffer = ByteBuffer.allocate(12 + 4);
                        Util.putHeader(resBuffer, 4, contentB.getSecret(), 1, content.getStudentNum());
                        resBuffer.putInt(pid);

                        responseData = resBuffer.array();

                        DatagramPacket responseB = new DatagramPacket(responseData, responseData.length,
                                packet.getAddress(), packet.getPort());

                        socketB.send(responseB);
                    }
                }

            } catch (SocketException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}