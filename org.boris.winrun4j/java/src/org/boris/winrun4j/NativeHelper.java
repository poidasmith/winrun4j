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
import java.util.ArrayList;

public class NativeHelper
{
    public static final boolean IS_64 = false; // TODO
    public static final int PTR_SIZE = IS_64 ? 8 : 4;
    public static final int HANDLE_SIZE = 4;
    public static final int INT_SIZE = 4;
    public static final int DWORD_SIZE = 4;

    public static long call(long proc) {
        return call(proc, new long[0]);
    }

    public static long call(long proc, long arg1) {
        return call(proc, (new long[] { arg1 }));
    }

    public static long call(long proc, long arg1, long arg2) {
        return call(proc, new long[] { arg1, arg2 });
    }

    public static long call(long proc, long arg1, long arg2, long arg3) {
        return call(proc, new long[] { arg1, arg2, arg3 });
    }

    public static long call(long proc, long arg1, long arg2, long arg3, long arg4) {
        return call(proc, new long[] { arg1, arg2, arg3, arg4 });
    }

    public static long call(long proc, long arg1, long arg2, long arg3, long arg4, long arg5) {
        return call(proc, new long[] { arg1, arg2, arg3, arg4, arg5 });
    }

    public static long call(long proc, long arg1, long arg2, long arg3, long arg4, long arg5, long arg6) {
        return call(proc, new long[] { arg1, arg2, arg3, arg4, arg5, arg6 });
    }

    public static long call(long proc, long arg1, long arg2, long arg3, long arg4, long arg5, long arg6, long arg7) {
        return call(proc, new long[] { arg1, arg2, arg3, arg4, arg5, arg6, arg7 });
    }

    public static long call(long proc, long arg1, long arg2, long arg3, long arg4, long arg5, long arg6, long arg7,
            long arg8) {
        return call(proc, new long[] { arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8 });
    }

    public static long call(long proc, long arg1, long arg2, long arg3, long arg4, long arg5, long arg6, long arg7,
            long arg8, long arg9) {
        return call(proc, new long[] { arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9 });
    }

    public static long call(long proc, long arg1, long arg2, long arg3, long arg4, long arg5, long arg6, long arg7,
            long arg8, long arg9, long arg10) {
        return call(proc, new long[] { arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10 });
    }

    public static long call(long library, String fn) {
        return call(library, fn, new long[0]);
    }

    public static long call(long library, String fn, long arg1) {
        return call(library, fn, new long[] { arg1 });
    }

    public static long call(long library, String fn, long arg1, long arg2) {
        return call(library, fn, new long[] { arg1, arg2 });
    }

    public static long call(long library, String fn, long arg1, long arg2, long arg3) {
        return call(library, fn, new long[] { arg1, arg2, arg3 });
    }

    public static long call(long library, String fn, long arg1, long arg2, long arg3, long arg4) {
        return call(library, fn, new long[] { arg1, arg2, arg3, arg4 });
    }

    public static long call(long library, String fn, long arg1, long arg2, long arg3, long arg4, long arg5) {
        return call(library, fn, new long[] { arg1, arg2, arg3, arg4, arg5 });
    }

    public static long call(long library, String fn, long arg1, long arg2, long arg3, long arg4, long arg5, long arg6) {
        return call(library, fn, new long[] { arg1, arg2, arg3, arg4, arg5, arg6 });
    }

    public static long call(long library, String fn, long arg1, long arg2, long arg3, long arg4, long arg5, long arg6,
            long arg7) {
        return call(library, fn, new long[] { arg1, arg2, arg3, arg4, arg5, arg6, arg7 });
    }

    public static long call(long library, String fn, long arg1, long arg2, long arg3, long arg4, long arg5, long arg6,
            long arg7, long arg8) {
        return call(library, fn, new long[] { arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8 });
    }

    public static long call(long library, String fn, long arg1, long arg2, long arg3, long arg4, long arg5, long arg6,
            long arg7, long arg8, long arg9) {
        return call(library, fn, new long[] { arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9 });
    }

    public static long call(long library, String fn, long arg1, long arg2, long arg3, long arg4, long arg5, long arg6,
            long arg7, long arg8, long arg9, long arg10) {
        return call(library, fn, new long[] { arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10 });
    }

    public static long call(long library, String fn, long[] args) {
        return call(Native.getProcAddress(library, fn), args);
    }

    // public static long call(long library, String fn, NativeStack stack) {
    // return call(Native.getProcAddress(library, fn), stack);
    // }

    public static long call(long proc, long[] args) {
        return FFI.call(proc, args);
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

    public static void free(long ptr1, long ptr2, long ptr3, long ptr4, long ptr5) {
        free(new long[] { ptr1, ptr2, ptr3, ptr4, ptr5 });
    }

    public static void free(long ptr1, long ptr2, long ptr3, long ptr4, long ptr5, long ptr6) {
        free(new long[] { ptr1, ptr2, ptr3, ptr4, ptr5, ptr6 });
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
        ByteBuffer bb = getBuffer(ptr, (int) size);
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

    public static String getString(byte[] buffer, boolean wideChar) {
        if (buffer == null)
            return null;
        return getString(ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN), wideChar);
    }

    public static byte[] toBytes(String str, boolean wideChar) {
        int len = str.length() + 1;
        if (wideChar)
            len <<= 1;
        byte[] buf = new byte[len];
        ByteBuffer bb = ByteBuffer.wrap(buf).order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < str.length(); i++) {
            if (wideChar)
                bb.putChar(str.charAt(i));
            else
                bb.put((byte) str.charAt(i));
        }
        if (wideChar)
            bb.putChar((char) 0);
        else
            bb.put((byte) 0);
        return buf;
    }

    public static void zeroMemory(ByteBuffer b) {
        while (b.hasRemaining())
            b.put((byte) 0);
    }

    public static void setInt(long ptr, int value) {
        getBuffer(ptr, 4).putInt(value);
    }

    public static String[] getMultiString(ByteBuffer bb, boolean wideChar) {
        ArrayList strs = new ArrayList();
        StringBuffer sb = new StringBuffer();
        while (true) {
            char c = wideChar ? bb.getChar() : (char) bb.get();
            if (c == 0) {
                if (sb.length() == 0) {
                    break;
                } else {
                    strs.add(sb.toString());
                    sb.setLength(0);
                }
            } else {
                sb.append(c);
            }
        }
        return (String[]) strs.toArray(new String[strs.size()]);
    }

    public static long toNative(byte[] b, int offset, int len) {
        long ptr = Native.malloc(len);
        ByteBuffer bb = getBuffer(ptr, len);
        bb.put(b, offset, len);
        return ptr;
    }
}
