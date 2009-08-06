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

/**
 * General JNI utilities for windows. Use at own risk.
 */
public class Native
{
    //
    // 1. LoadLibrary
    // 2. FreeLibrary
    // 3. Bind to a function (get proc address)
    // 4. Malloc/free.
    // 5. Memset (copy in data to memory).
    // 6. Access memory as ByteBuffer
    // 7. Call a function (pass in byte array as stack), return int?
    //

    public static native long loadLibrary(String filename);

    public static native void freeLibrary(long handle);

    public static native long getProcAddress(long handle, String name);

    public static native long malloc(int size);

    public static native void free(long handle);

    public static native void memcpy(long handle, byte[] buf, int size);

    public static native ByteBuffer fromPointer(long handle, long size);

    public static long intCall(long handle, NativeStack stack) {
        byte[] b = stack.toBytes();
        return intCall(handle, b, b.length);
    }

    public static native long intCall(long handle, byte[] stack, int size);

    public static native double doubleCall(long handle, byte[] stack, int size);
}
