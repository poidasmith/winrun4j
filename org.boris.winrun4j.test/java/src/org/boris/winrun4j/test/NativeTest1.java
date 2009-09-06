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

import org.boris.winrun4j.Kernel32;
import org.boris.winrun4j.Native;

public class NativeTest1
{
    public static void main(String[] args) throws Exception {
        // System.out.println(GetTickCount());
        // testMemory();
        // testLogicalDrives();
    }

    public static void callWithDouble() throws Exception {

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
        File[] drives = Kernel32.GetLogicalDrives();
        for (int i = 0; i < drives.length; i++) {
            System.out.println(drives[i]);
        }
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

}
