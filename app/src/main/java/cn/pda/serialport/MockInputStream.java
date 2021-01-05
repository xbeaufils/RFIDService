package cn.pda.serialport;

import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;

public class MockInputStream extends ByteArrayInputStream {
    private byte[] initialArray; // = { 0x02,0x30,0x31,0x30,0x30,0x30,0x30,0x30,0x30,0x30,0x30,0x43,0x39,0x30,0x30,0x30,0x30,0x30,0x30,0x30,0x30,0x30,0x30,0x30,0x30,0x30,0x30,0x7B,(byte)0x84,0x03 };

    public MockInputStream(byte[] buf) {
        super(buf);
        this.initialArray = Arrays.copyOf(buf, buf.length);
        Log.d("MOCK", "MockInputStream: " + this.initialArray.length);
    }

    @Override
    public synchronized int available() {
        return 30;
        //return super.available();
    }

    @Override
    public void close() throws IOException {
        super.close();
    }

    @Override
    public int read(byte[] b) throws IOException {
        Log.d("MOCK", "read: " +  initialArray.length + " buffer size " + b.length);
        for (int i = 0; i < initialArray.length; i++) {
            android.util.Log.d("MOCK", "read: " +  initialArray[i]);
            b[i] = initialArray[i];
        //b = Arrays.copyOf(initialArray, 30);
        //System.arraycopy(b, 0, initialArray, 0, 30);
        return initialArray.length;
        //return super.read(b);
    }
}
