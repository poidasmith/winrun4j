package org.boris.winrun4j.test.framework;

import java.io.IOException;
import java.io.OutputStream;

public class TeeOutputStream extends OutputStream
{
    private OutputStream[] streams;

    public TeeOutputStream(OutputStream... streams) {
        this.streams = streams;
    }

    public void write(int b) throws IOException {
        for (OutputStream os : streams)
            os.write(b);
    }

    public void write(byte[] data, int offset, int length)
            throws IOException {
        for (OutputStream os : streams)
            os.write(data, offset, length);
    }

    public void flush() throws IOException {
        for (OutputStream os : streams)
            os.flush();
    }

    public void close() throws IOException {
        for (OutputStream os : streams)
            os.close();
    }
}