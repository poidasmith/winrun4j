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

import org.boris.winrun4j.FFI;
import org.boris.winrun4j.Native;
import org.boris.winrun4j.NativeHelper;
import org.boris.winrun4j.FFI.CIF;
import org.boris.winrun4j.winapi.Console;
import org.boris.winrun4j.winapi.Kernel32;
import org.boris.winrun4j.winapi.User32;

public class FFIClosureTest
{
    public static void main2(String[] args) throws Exception {
        CIF cif = CIF.prepare(FFI.ABI_STDCALL, 1);
        Kernel32.debugBreak();
        FFIClosureTest test = new FFIClosureTest();
        long objectId = Native.newGlobalRef(test);
        long methodId = Native.getMethodId(FFIClosureTest.class, "callback", "(JJ)V", false);
        long handle = FFI.prepareClosure(cif.get(), objectId, methodId);
        long callback = NativeHelper.getInt(handle);
        Console.allocConsole();
        Console.setConsoleTitle("Testing Console");
        NativeHelper.call(Kernel32.library, "SetConsoleCtrlHandler", callback, 1);
        Thread.sleep(500000);
    }

    public static void main(String[] args) throws Exception {
        CIF cif = CIF.prepare(FFI.ABI_STDCALL, 2);
        // Kernel32.debugBreak();
        FFIClosureTest test = new FFIClosureTest();
        long objectId = Native.newGlobalRef(test);
        long methodId = Native.getMethodId(FFIClosureTest.class, "callback", "(JJ)V", false);
        long handle = FFI.prepareClosure(cif.get(), objectId, methodId);
        long callback = NativeHelper.getInt(handle);
        NativeHelper.call(User32.library, "EnumWindows", callback, 101010);
    }

    public void callback(long resp, long args) {
        printh(resp);
        printh(args);
        long parg0 = NativeHelper.getInt(args);
        long parg1 = NativeHelper.getInt(args + 4);
        printh(parg0);
        printh(parg1);
        // long parg2 = NativeHelper.getInt(args + 8);
        printh(NativeHelper.getInt(parg0));
        printh(NativeHelper.getInt(parg1));
        // System.out.println(NativeHelper.getInt(parg2));
        // System.out.println(NativeHelper.getInt(NativeHelper.getInt(parg1)));
        System.out.println();
        NativeHelper.setInt(resp, 1);
    }

    private void printh(long resp) {
        System.out.println(Long.toHexString(resp));
    }
}
