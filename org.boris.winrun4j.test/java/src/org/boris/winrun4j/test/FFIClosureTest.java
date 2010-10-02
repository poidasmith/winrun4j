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

public class FFIClosureTest
{
    public static void main(String[] args) throws Exception {
        CIF cif = CIF.prepare(FFI.ABI_STDCALL, 1);
        FFIClosureTest test = new FFIClosureTest();
        long objectId = Native.newGlobalRef(test);
        long methodId = Native.getMethodId(FFIClosureTest.class, "callback", "(JJ)V", false);
        long callback = FFI.prepareClosure(cif.get(), objectId, methodId);
        Console.allocConsole();
        Console.setConsoleTitle("Testing Console");
        NativeHelper.call(Kernel32.library, "SetConsoleCtrlHandler", callback, 1);
        Thread.sleep(500000);
    }

    public void callback(long resp, long args) {
        System.out.println(resp);
        System.out.println(args);
    }
}
