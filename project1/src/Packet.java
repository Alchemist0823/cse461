import java.nio.ByteBuffer;

public class Packet {
    private int len;
    private int secret;
    private int step;
    private int studentNum;
    private ByteBuffer buffer;

    public Packet(byte[] receiveData) {
        buffer = ByteBuffer.allocate(receiveData.length);
        buffer.put(receiveData);
        len = buffer.getInt(0);
        secret = buffer.getInt(4);
        step = buffer.getShort(8);
        studentNum = buffer.getShort(10);
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }

    public int getLen() {
        return len;
    }

    public int getSecret() {
        return secret;
    }

    public int getStep() {
        return step;
    }

    public int getStudentNum() {
        return studentNum;
    }
}
