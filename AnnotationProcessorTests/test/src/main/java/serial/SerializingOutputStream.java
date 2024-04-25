package serial;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class SerializingOutputStream extends ByteArrayOutputStream {

    SerializingOutputStream() { super(); }
    SerializingOutputStream(int length) { super(length); }

    void writeInt(int val) {
        byte[] buf = new byte[4];

        for(int i = 4; i != 0; i--) {
            buf[i-1] = (byte) (val & 0xFF);
            val <<= 8;
        }
        writeBytes(buf);
    }

    void writeLong(long val) {
        byte[] buf = new byte[8];

        for(int i = 8; i != 0; i--) {
            buf[i-1] = (byte) (val & 0xFF);
            val <<= 8;
        }
        writeBytes(buf);
    }

    void writeDouble(double val) {
        ByteBuffer bb = ByteBuffer.allocate(8);
        bb.putDouble(val);
        writeBytes(bb.array());
    }

    void writeFloat(float val) {
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.putFloat(val);
        writeBytes(bb.array());
    }

    void writeString(String s){
        writeInt(s.length());
        writeBytes(s.getBytes());
    }


}
