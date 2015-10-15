import java.nio.ByteBuffer;

public class Util {
    public static final int PORT = 12235;

    public static void putHeader(ByteBuffer buffer, int payloadLen, int secret, int step, int num) {
        buffer.putInt(payloadLen);
        buffer.putInt(secret);
        buffer.putShort((short) step);
        buffer.putShort((short) num);
    }
}
