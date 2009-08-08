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
import java.nio.ByteBuffer;
import java.util.ArrayList;

import org.boris.winrun4j.Native;
import org.boris.winrun4j.NativeStack;

public class NativeTest1
{
    public static void main(String[] args) throws Exception {
        // System.out.println(GetTickCount());
        // testMemory();
        // testLogicalDrives();
    }

    public static void testMemory() {
        long ptr = Native.malloc(10);
        ByteBuffer bb = Native.fromPointer(ptr, 10);
        for (int i = 0; i < 10; i++) {
            bb.put((byte) i);
        }
        bb.position(0);
        for (int i = 0; i < 10; i++) {
            System.out.printf("%x ", bb.get());
        }
        System.out.println();
        Native.free(ptr);
    }

    public static void testLogicalDrives() {
        File[] drives = GetLogicalDrives();
        for (int i = 0; i < drives.length; i++) {
            System.out.println(drives[i]);
        }
    }

    public static long intCall(long handle, NativeStack stack) {
        byte[] b = stack.toBytes();
        return Native.call(handle, b, b.length);
    }

    public static File[] GetLogicalDrives() {
        long handle = Native.loadLibrary("kernel32");
        long proc = Native.getProcAddress(handle, "GetLogicalDriveStringsA");
        int len = 1024;
        long buf = Native.malloc(len);
        NativeStack s = new NativeStack();
        s.addArg32(len);
        s.addArg32(buf);
        long res = intCall(proc, s);
        ByteBuffer bb = Native.fromPointer(buf, res + 1);
        ArrayList drives = new ArrayList();
        StringBuffer sb = new StringBuffer();
        while (true) {
            char c = (char) bb.get();
            if (c == 0) {
                if (sb.length() == 0) {
                    break;
                } else {
                    drives.add(new File(sb.toString()));
                    sb.setLength(0);
                }
            } else {
                sb.append(c);
            }
        }
        Native.free(buf);
        return (File[]) drives.toArray(new File[drives.size()]);
    }

    public static String toString(ByteBuffer bb) {
        StringBuffer sb = new StringBuffer();
        while (true) {
            char c = (char) bb.get();
            if (c == 0)
                break;
            else
                sb.append(c);
        }
        return sb.toString();
    }

    public static String toWideString(ByteBuffer bb) {
        StringBuffer sb = new StringBuffer();
        while (true) {
            char c = bb.getChar();
            if (c == 0)
                break;
            else
                sb.append(c);
        }
        return sb.toString();
    }

    public static void DebugBreak() {
        long handle = Native.loadLibrary("kernel32");
        long proc = Native.getProcAddress(handle, "DebugBreak");
        Native.call(proc, null, 0);
    }

    public static long GetTickCount() {
        long handle = Native.loadLibrary("kernel32");
        long proc = Native.getProcAddress(handle, "GetTickCount");
        return Native.call(proc, null, 0);
    }
}
