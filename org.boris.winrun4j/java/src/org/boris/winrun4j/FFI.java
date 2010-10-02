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

    public static final int FFI_TYPE_VOID = 0;
    public static final int FFI_TYPE_INT = 1;
    public static final int FFI_TYPE_FLOAT = 2;
    public static final int FFI_TYPE_DOUBLE = 3;
    public static final int FFI_TYPE_LONGDOUBLE = FFI_TYPE_DOUBLE;
    public static final int FFI_TYPE_UINT8 = 5;
    public static final int FFI_TYPE_SINT8 = 6;
    public static final int FFI_TYPE_UINT16 = 7;
    public static final int FFI_TYPE_SINT16 = 8;
    public static final int FFI_TYPE_UINT32 = 9;
    public static final int FFI_TYPE_SINT32 = 10;
    public static final int FFI_TYPE_UINT64 = 11;
    public static final int FFI_TYPE_SINT64 = 12;
    public static final int FFI_TYPE_STRUCT = 13;
    public static final int FFI_TYPE_POINTER = 14;

    public static native int prepare(long cif, int abi, int nargs, long rtype, long atypes);

    public static native void call(long cif, long fn, long rvalue, long avalue);

    public static native long prepareClosure(long cif, long objectId, long methodId);

    public static native void freeClosure(long closure);

    public static long call(long lib, String function, long[] args) {
        long proc = Native.getProcAddress(lib, function);
        if (proc == 0)
            throw new RuntimeException("Invalid function: " + function);
        return call(proc, args);
    }

    public static long call(long proc, long[] args) {
        CIF cif = CIF.prepare(ABI_STDCALL, args.length);
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
        call(cif.get(), proc, rvalue, pvalue);
        ByteBuffer rb = NativeHelper.getBuffer(rvalue, 8);
        long result = rb.getLong();
        NativeHelper.free(rvalue, avalue, pvalue);
        cif.destroy();
        return result;
    }

    public static class CIF
    {
        private long cif;
        private long ffi_type_ptr;
        private long atypes;

        private CIF() {
        }

        public static CIF prepare(int abi, int argc) {
            CIF c = new CIF();
            int sizeOfCif = 30;
            c.cif = Native.malloc(sizeOfCif);
            c.ffi_type_ptr = makeType(FFI_TYPE_POINTER, 4);
            int argSize = (argc + 1) * 4;
            c.atypes = Native.malloc(argSize);
            ByteBuffer ab = NativeHelper.getBuffer(c.atypes, argSize);
            for (int i = 0; i < argc; i++) {
                ab.putInt((int) c.ffi_type_ptr);
            }
            ab.putInt(0);
            int res = FFI.prepare(c.cif, abi, argc, c.ffi_type_ptr, c.atypes);
            if (res != 0) {
                NativeHelper.free(c.cif, c.ffi_type_ptr, c.atypes);
                throw new RuntimeException("Invalid FFI types for function");
            }

            return c;
        }

        private static long makeType(int type, int size) {
            long ffi_type = Native.malloc(12);
            ByteBuffer bb = NativeHelper.getBuffer(ffi_type, 12);
            bb.putInt(size);
            bb.putShort((short) size);
            bb.putShort((short) type);
            bb.putInt(0);
            return ffi_type;
        }

        public void destroy() {
            if (cif != 0) {
                NativeHelper.free(cif, ffi_type_ptr, atypes);
                cif = ffi_type_ptr = atypes = 0;
            }
        }

        public long get() {
            return cif;
        }
    }
}
