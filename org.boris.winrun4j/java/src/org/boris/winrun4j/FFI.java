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

public class FFI
{
    public static final int ABI_SYSV = 1;
    public static final int ABI_STDCALL = 2;

    public static native int prepare(long cif, int abi, int nargs, long rtype, long atypes);

    public static native void call(long cif, long fn, long rvalue, long avalue);

    public static native long closureAlloc(long pCode);

    public static native int prepareClosure(long closure, long cif, long fun, long userData, long codeloc);

    public static native void closureFree(long ptr);

    public static long call(long lib, String function, long[] args) {
        long proc = Native.getProcAddress(lib, function);
        if (proc == 0)
            throw new RuntimeException("Invalid function: " + function);
        return call(proc, args);
    }

    public static long call(long proc, long[] args) {
        int sizeOfCif = 30;
        long cif = Native.malloc(sizeOfCif);
        long ffi_type_ptr = Native.malloc(12);
        ByteBuffer bb = NativeHelper.getBuffer(ffi_type_ptr, 12);
        bb.putInt(4);
        bb.putShort((short) 4);
        bb.putShort((short) 14);
        bb.putInt(0);
        long rtype = ffi_type_ptr;
        int argSize = (args.length + 1) * 4;
        long atypes = Native.malloc(argSize);
        ByteBuffer ab = NativeHelper.getBuffer(atypes, argSize);
        for (int i = 0; i < args.length; i++) {
            ab.putInt((int) ffi_type_ptr);
        }
        ab.putInt(0);
        int res = prepare(cif, FFI.ABI_STDCALL, args.length, rtype, atypes);
        if (res != 0) {
            throw new RuntimeException("Invalid FFI types for function");
        }
        long rvalue = Native.malloc(8);
        NativeHelper.zeroMemory(NativeHelper.getBuffer(rvalue, 8));
        long avalue = 0;
        long pvalue = 0;
        if (args.length > 0) {
            int size = args.length * 4;
            avalue = Native.malloc(size);
            pvalue = Native.malloc(size);
            ByteBuffer vb = NativeHelper.getBuffer(avalue, size);
            ByteBuffer pb = NativeHelper.getBuffer(pvalue, size);
            for (int i = 0; i < args.length; i++) {
                vb.putInt((int) args[i]);
                pb.putInt((int) (avalue + (i * 4)));
            }
        }
        call(cif, proc, rvalue, pvalue);
        ByteBuffer rb = NativeHelper.getBuffer(rvalue, 8);
        long result = rb.getLong();
        NativeHelper.free(cif, ffi_type_ptr, atypes, rvalue, avalue, pvalue);
        return result;
    }
}
