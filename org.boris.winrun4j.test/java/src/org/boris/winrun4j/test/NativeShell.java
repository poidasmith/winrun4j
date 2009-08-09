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
import org.boris.winrun4j.NativeHelper;
import org.boris.winrun4j.NativeStack;
import org.boris.winrun4j.Environment;

public class NativeShell
{
    public static void main(String[] args) throws Exception {
    }

    public static void testGetDriveType() {
        File[] rootDirs = Environment.getLogicalDrives();
        for (File f : rootDirs) {
            System.out.println(GetDriveType(f));
        }
    }

    public static int GetDriveType(File rootDir) {
        long buf = NativeHelper.toNativeString(rootDir);
        long handle = Native.loadLibrary("kernel32");
        long proc = Native.getProcAddress(handle, "GetDriveTypeA");
        NativeStack ns = new NativeStack();
        ns.addArg32(buf);
        long res = NativeHelper.call(proc, ns);
        if (buf != 0)
            Native.free(buf);
        return (int) res;
    }

    public static String GetTempPath() {
        return null;
    }
}
