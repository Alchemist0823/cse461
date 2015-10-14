import java.net.DatagramPacket;
import java.net.Socket;

public class UDPConnection implements Runnable {

    Socket socket = null;
    DatagramPacket packet = null;

    public UDPConnection(Socket socket, DatagramPacket packet) {
        this.socket = socket;
        this.packet = packet;
    }

    public void run() {
        //byte[] data = makeResponse(); // code not shown
        /*DatagramPacket response = new DatagramPacket(data, data.length,
                packet.getAddress(), packet.getPort());*/
        //socket.send(response);
    }
}