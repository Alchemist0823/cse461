import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
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
            byteBuffer.putInt(rand.nextInt(10000) + 10000);
            byteBuffer.putInt(rand.nextInt(1000));

            byte[] responseData = byteBuffer.array();
            DatagramPacket response = new DatagramPacket(responseData, responseData.length,
                    packet.getAddress(), packet.getPort());
            try {
                socket.send(response);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}