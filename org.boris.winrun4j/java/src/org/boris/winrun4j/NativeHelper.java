/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Peter Smith
 *******************************************************************************/
package org.boris.winrun4j;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

public class NativeHelper
{
    public static String getString(long ptr, int size, boolean wideChar) {
        ByteBuffer bb = Native.fromPointer(ptr, size);
        return getString(bb, wideChar);
    }

    public static String getString(ByteBuffer bb, boolean wideChar) {
        StringBuffer sb = new StringBuffer();
        while (true) {
            char c = wideChar ? bb.getChar() : (char) bb.get();
            if (c == 0)
                break;
            else
                sb.append(c);
        }
        return sb.toString();
    }

    public static long toNativeString(Object o, boolean wideChar) {
        if (o == null)
            return 0;

        byte[] b;
        try {
            b = o.toString().getBytes(wideChar ? "UTF-16" : "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        int len = b.length + 1;
        long buf = Native.malloc(len);
        ByteBuffer bb = Native.fromPointer(buf, len);
        bb.put(b);
        bb.put((byte) 0);
        return buf;
    }

    public static long call(long proc, NativeStack stack) {
        if (proc == 0)
            throw new NullPointerException("Invalid procedure address");
        byte[] b = null;
        if (stack != null)
            b = stack.toBytes();
        int len = b != null ? b.length : 0;
        return Native.call(proc, b, len);
    }

    public static long call(long proc) {
        return call(proc, (NativeStack) null);
    }

    public static long call(long proc, long[] args) {
        return call(proc, new NativeStack(args));
    }

    public static long call(long proc, long arg1) {
        return call(proc, new NativeStack(new long[] { arg1 }));
    }

    public static long call(long proc, long arg1, long arg2) {
        return call(proc, new NativeStack(new long[] { arg1, arg2 }));
    }

    public static long call(long proc, long arg1, long arg2, long arg3) {
        return call(proc, new NativeStack(new long[] { arg1, arg2, arg3 }));
    }

    public static long call(long proc, long arg1, long arg2, long arg3, long arg4) {
        return call(proc, new NativeStack(new long[] { arg1, arg2, arg3, arg4 }));
    }

    public static long call(long proc, long arg1, long arg2, long arg3, long arg4, long arg5) {
        return call(proc, new NativeStack(new long[] { arg1, arg2, arg3, arg4, arg5 }));
    }

    public static long call(long proc, long arg1, long arg2, long arg3, long arg4, long arg5,
            long arg6) {
        return call(proc, new NativeStack(new long[] { arg1, arg2, arg3, arg4, arg5, arg6 }));
    }

    public static long call(long proc, long arg1, long arg2, long arg3, long arg4, long arg5,
            long arg6, long arg7) {
        return call(proc, new NativeStack(new long[] { arg1, arg2, arg3, arg4, arg5, arg6, arg7 }));
    }
}
