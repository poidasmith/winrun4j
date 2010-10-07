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
import org.boris.winrun4j.NativeHelper;
import org.boris.winrun4j.winapi.Kernel32;

public class PerfTest1
{
    public static void main(String[] args) {
        int count = 1000000;
        long n1 = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            getTickCountSlow();
        }
        long n2 = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            getTickCountFast();
        }
        long n3 = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            getTickCountFaster();
        }
        long n4 = System.currentTimeMillis();
        System.out.println(n2 - n1);
        System.out.println(n3 - n2);
        System.out.println(n4 - n3);
    }

    private static long procGetTickCount = Native.getProcAddress(Kernel32.library, "GetTickCount");

    private static long getTickCountFast() {
        return NativeHelper.call(procGetTickCount);
    }

    private static long getTickCountFaster() {
        // return NativeHelper.call(procGetTickCount, null, 0, 0);
        return 0;
    }

    private static long getTickCountSlow() {
        return NativeHelper.call(Kernel32.library, "GetTickCount");
    }
}
