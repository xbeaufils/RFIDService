package cn.pda.serialport;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class MockOutputStream extends ByteArrayOutputStream {
    @Override
    public void close() throws IOException {
        super.close();
    }

    @Override
    public void write(byte[] b) throws IOException {
        super.write(b);
    }
}
