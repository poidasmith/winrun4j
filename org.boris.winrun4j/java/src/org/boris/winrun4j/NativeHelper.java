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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class NativeHelper
{
    public static long call(long proc) {
        return call(proc, (NativeStack) null);
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

    public static long call(long proc, long arg1, long arg2, long arg3, long arg4, long arg5, long arg6) {
        return call(proc, new NativeStack(new long[] { arg1, arg2, arg3, arg4, arg5, arg6 }));
    }

    public static long call(long proc, long arg1, long arg2, long arg3, long arg4, long arg5, long arg6, long arg7) {
        return call(proc, new NativeStack(new long[] { arg1, arg2, arg3, arg4, arg5, arg6, arg7 }));
    }

    public static long call(long proc, long arg1, long arg2, long arg3, long arg4, long arg5, long arg6, long arg7,
            long arg8) {
        return call(proc, new NativeStack(new long[] { arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8 }));
    }

    public static long call(long proc, long arg1, long arg2, long arg3, long arg4, long arg5, long arg6, long arg7,
            long arg8, long arg9) {
        return call(proc, new NativeStack(new long[] { arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9 }));
    }

    public static long call(long proc, long arg1, long arg2, long arg3, long arg4, long arg5, long arg6, long arg7,
            long arg8, long arg9, long arg10) {
        return call(proc, new NativeStack(new long[] { arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10 }));
    }

    public static long call(long proc, long[] args) {
        return call(proc, new NativeStack(args));
    }

    public static long call(long proc, NativeStack stack) {
        if (proc == 0)
            throw new NullPointerException("Invalid procedure address");
        byte[] b = null;
        if (stack != null)
            b = stack.toBytes();
        int len = b != null ? b.length : 0;
        return Native.call(proc, b, len, 0);
    }

    public static void free(long ptr) {
        if (ptr != 0)
            Native.free(ptr);
    }

    public static void free(long ptr1, long ptr2) {
        free(new long[] { ptr1, ptr2 });
    }

    public static void free(long ptr1, long ptr2, long ptr3) {
        free(new long[] { ptr1, ptr2, ptr3 });
    }

    public static void free(long ptr1, long ptr2, long ptr3, long ptr4) {
        free(new long[] { ptr1, ptr2, ptr3, ptr4 });
    }

    public static void free(long[] ptrs) {
        for (int i = 0; i < ptrs.length; i++) {
            Native.free(ptrs[i]);
        }
    }

    public static ByteBuffer getBuffer(long ptr, int size) {
        return Native.fromPointer(ptr, size).order(ByteOrder.LITTLE_ENDIAN);
    }

    public static int getInt(long ptr) {
        return Native.fromPointer(ptr, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
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

    public static String getString(long ptr, long size, boolean wideChar) {
        ByteBuffer bb = Native.fromPointer(ptr, size);
        return getString(bb, wideChar);
    }

    public static long toMultiString(String[] strs, boolean wideChar) {
        if (strs == null || strs.length == 0)
            return 0;

        int size = wideChar ? 2 : 1;
        for (int i = 0; i < strs.length; i++) {
            int len = strs[i].length() + 1;
            if (wideChar)
                len <<= 1;
            size += len;
        }

        long ptr = Native.malloc(size);
        ByteBuffer bb = NativeHelper.getBuffer(ptr, size);
        for (int i = 0; i < strs.length; i++) {
            if (wideChar) {
                char[] c = strs[i].toCharArray();
                for (int j = 0; j < c.length; j++) {
                    bb.putChar(c[j]);
                }
            } else {
                byte[] b = strs[i].getBytes();
                for (int j = 0; j < b.length; j++) {
                    bb.put(b[j]);
                }
            }
        }

        if (wideChar)
            bb.putShort((short) 0);
        else
            bb.put((byte) 0);

        return ptr;
    }

    public static long toNativeString(Object o, boolean wideChar) {
        if (o == null)
            return 0;

        String s = o.toString();
        if (wideChar) {
            char[] c = s.toCharArray();
            int len = c.length * 2 + 2;
            long buf = Native.malloc(len);
            ByteBuffer bb = NativeHelper.getBuffer(buf, len);
            for (int i = 0; i < c.length; i++) {
                bb.putChar(c[i]);
            }
            bb.putChar((char) 0);
            return buf;
        } else {
            byte[] b;
            b = s.getBytes();
            int len = b.length + 1;
            long buf = Native.malloc(len);
            ByteBuffer bb = Native.fromPointer(buf, len);
            bb.put(b);
            bb.put((byte) 0);
            return buf;
        }
    }

    public static String toString(byte[] buffer) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < buffer.length; i++) {
            if (buffer[i] == 0)
                break;
            sb.append((char) buffer[i]);
        }
        return sb.toString();
    }

    public static void zeroMemory(ByteBuffer b) {
        while (b.hasRemaining())
            b.put((byte) 0);
    }
}
