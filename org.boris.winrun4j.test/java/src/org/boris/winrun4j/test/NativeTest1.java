/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Peter Smith
 *******************************************************************************/
package org.boris.winrun4j.test;

import java.io.File;

import org.boris.winrun4j.Native;
import org.boris.winrun4j.NativeStack;

public class NativeTest1
{
    public static void main(String[] args) throws Exception {
        // System.out.println(GetTickCount());
        GetLogicalDrives();
    }

    public static File[] GetLogicalDrives() {
        long handle = Native.loadLibrary("kernel32");
        long proc = Native.getProcAddress(handle, "GetLogicalDriveStrings");
        long buf = Native.malloc(1024);
        System.out.printf("Handle: %d\n", buf);
        int len = 1024;
        NativeStack s = new NativeStack();
        s.addArg32(len);
        s.addArg32(buf);
        byte[] b = s.toBytes();
        for (int i = 0; i < b.length; i++) {
            System.out.printf("%x ", b[i]);
        }
        System.out.println();
        DebugBreak();
        Native.intCall(proc, s);
        // ByteBuffer bb = Native.fromPointer(buf, 1024);
        // for (int i = 0; i < 1024; i++) {
        // System.out.printf("%x", bb.get());
        // }

        Native.free(buf);

        return null;
    }

    public static void DebugBreak() {
        long handle = Native.loadLibrary("kernel32");
        long proc = Native.getProcAddress(handle, "DebugBreak");
        Native.intCall(proc, null, 0);
    }

    public static long GetTickCount() {
        long handle = Native.loadLibrary("kernel32");
        long proc = Native.getProcAddress(handle, "GetTickCount");
        return Native.intCall(proc, null, 0);
    }
}
