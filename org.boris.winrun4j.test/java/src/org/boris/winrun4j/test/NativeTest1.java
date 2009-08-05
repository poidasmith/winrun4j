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

import org.boris.winrun4j.Native;

public class NativeTest1
{
    public static void main(String[] args) throws Exception {
        System.out.println(GetTickCount());
    }

    public static long GetTickCount() {
        long handle = Native.loadLibrary("kernel32");
        long proc = Native.getProcAddress(handle, "GetTickCount");
        return Native.intCall(proc, null, 0);
    }
}
