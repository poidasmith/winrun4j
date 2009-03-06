/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Peter Smith
 *******************************************************************************/
package org.boris.winrun4j.classloader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

public class ByteBufferInputStream extends InputStream
{
    private ByteBuffer bb;

    public ByteBufferInputStream(ByteBuffer bb) {
        this.bb = bb;
    }

    public int read() throws IOException {
        try {
            return bb.get() & Integer.MAX_VALUE;
        } catch (BufferUnderflowException e) {
            return -1;
        }
    }

    public int read(byte[] b) throws IOException {
        int len = b.length;
        if (len > bb.remaining()) {
            len = bb.remaining();
        }

        bb.get(b, 0, len);
        return len;
    }

    public int read(byte[] b, int off, int len) throws IOException {
        if (len > bb.remaining()) {
            len = bb.remaining();
        }

        bb.get(b, off, len);
        return len;
    }

    public long skip(long n) throws IOException {
        if (n > bb.remaining()) {
            n = bb.remaining();
        }
        bb.position((int) (bb.position() + n));
        return n;
    }

    public int available() throws IOException {
        return bb.remaining();
    }

    public boolean markSupported() {
        return false;
    }
}
