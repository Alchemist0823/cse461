import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

public class Util {
    public static final int PORT = 12235;

    public static void putHeader(ByteBuffer buffer, int payloadLen, int secret, int step, int num) {
        buffer.putInt(payloadLen);
        buffer.putInt(secret);
        buffer.putShort((short) step);
        buffer.putShort((short) num);
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
}
