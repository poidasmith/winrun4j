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

    public static long toNativeString(Object o) {
        if (o == null)
            return 0;

        byte[] b = o.toString().getBytes();
        int len = b.length + 1;
        long buf = Native.malloc(len);
        ByteBuffer bb = Native.fromPointer(buf, len);
        bb.put(b);
        bb.put((byte) 0);
        return buf;
    }

    public static long call(long proc, NativeStack stack) {
        byte[] b = null;
        if (stack != null)
            b = stack.toBytes();
        int len = b != null ? b.length : 0;
        return Native.call(proc, b, len);
    }
}
