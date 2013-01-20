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

import java.nio.ByteBuffer;

import org.boris.winrun4j.FFI;
import org.boris.winrun4j.Native;
import org.boris.winrun4j.NativeHelper;

public class FFITest
{
    public static void main(String[] args) throws Exception {
        autoTest();
    }

    public static void autoTest() throws Exception {
        long lib = Native.loadLibrary("kernel32");
        long proc = Native.getProcAddress(lib, "GetProcAddress");
        long procStr = NativeHelper.toNativeString("GetProcAddress", false);
        long proc2 = callV(lib, "GetProcAddress", lib, procStr);
        NativeHelper.free(procStr);
        System.out.println(proc);
        System.out.println(proc2);
    }

    public static void manualTest() throws Exception {
        long lib = Native.loadLibrary("kernel32");
        long proc = Native.getProcAddress(lib, "GetProcAddress");
        System.out.println(proc);

        int sizeOfCif = 30;
        long cif = Native.malloc(sizeOfCif);
        long ffi_type_ptr = Native.malloc(12);
        ByteBuffer bb = NativeHelper.getBuffer(ffi_type_ptr, 12);
        bb.putInt(4);
        bb.putShort((short) 4);
        bb.putShort((short) 14);
        bb.putInt(0);
        long rtype = ffi_type_ptr;
        long atypes = Native.malloc(12);
        ByteBuffer ab = NativeHelper.getBuffer(atypes, 12);
        ab.putInt((int) ffi_type_ptr);
        ab.putInt((int) ffi_type_ptr);
        ab.putInt(0);

        int res = FFI.prepare(cif, FFI.ABI_STDCALL, 2, rtype, atypes);
        System.out.println(res);

        long rvalue = Native.malloc(8);
        long avalue = Native.malloc(8);
        ByteBuffer vb = NativeHelper.getBuffer(avalue, 8);
        vb.putInt((int) lib);
        long procStr = NativeHelper.toNativeString("GetProcAddress", false);
        vb.putInt((int) procStr);
        long pvalue = Native.malloc(8);
        ByteBuffer pb = NativeHelper.getBuffer(pvalue, 8);
        pb.putInt((int) avalue);
        pb.putInt((int) (avalue + 4));
        FFI.call(cif, proc, rvalue, pvalue);

        ByteBuffer rb = NativeHelper.getBuffer(rvalue, 8);
        long res2 = rb.getLong();
        System.out.println(res2);
    }

    public static long callV(long lib, String function, long... args) {
        return FFI.call(lib, function, args);
    }
}
