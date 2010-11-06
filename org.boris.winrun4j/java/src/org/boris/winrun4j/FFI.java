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
    private static final boolean is64 = Native.IS_64;

    public static final int ABI_SYSV = 1;
    public static final int ABI_STDCALL = 2;
    public static final int ABI_WIN64 = 1;

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
        CIF cif = CIF.prepare(is64 ? ABI_WIN64 : ABI_STDCALL, args.length);
        long rvalue = Native.malloc(8);
        long avalue = 0;
        long pvalue = 0;
        if (args.length > 0) {
            int size = args.length * NativeHelper.PTR_SIZE;
            avalue = Native.malloc(size);
            pvalue = Native.malloc(size);
            ByteBuffer vb = NativeHelper.getBuffer(avalue, size);
            ByteBuffer pb = NativeHelper.getBuffer(pvalue, size);
            for (int i = 0; i < args.length; i++) {
                if (is64) {
                    vb.putLong(args[i]);
                    pb.putLong(avalue + (i * NativeHelper.PTR_SIZE));
                } else {
                    vb.putInt((int) args[i]);
                    pb.putInt((int) (avalue + (i * NativeHelper.PTR_SIZE)));
                }
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
        private long[] ffi_types;
        private long return_type;
        private long atypes;

        private CIF() {
        }

        public static CIF prepare(int abi, int argc) {
            int[] types = new int[argc];
            for (int i = 0; i < argc; i++)
                types[i] = FFI_TYPE_POINTER;
            return prepare(abi, types);
        }

        public static CIF prepare(int abi, int[] types) {
            CIF c = new CIF();
            int sizeOfCif = 30;
            c.cif = Native.malloc(sizeOfCif);
            c.ffi_types = new long[types.length];
            for (int i = 0; i < types.length; i++)
                c.ffi_types[i] = makeType(types[i]);
            c.return_type = makeType(FFI_TYPE_POINTER);
            int argSize = (types.length + 1) * NativeHelper.PTR_SIZE;
            c.atypes = Native.malloc(argSize);
            ByteBuffer ab = NativeHelper.getBuffer(c.atypes, argSize);
            for (int i = 0; i < types.length; i++) {
                if (is64)
                    ab.putLong(c.ffi_types[i]);
                else
                    ab.putInt((int) c.ffi_types[i]);
            }
            if (is64)
                ab.putLong(0);
            else
                ab.putInt(0);
            int res = FFI.prepare(c.cif, abi, types.length, c.return_type, c.atypes);
            if (res != 0) {
                NativeHelper.free(c.cif, c.return_type, c.atypes);
                NativeHelper.free(c.ffi_types);
                throw new RuntimeException("Invalid FFI types for function");
            }

            return c;
        }

        private static long makeType(int type) {
            long ffi_type = Native.malloc(24);
            int size = 0;
            switch (type) {
            case FFI_TYPE_POINTER:
                size = NativeHelper.PTR_SIZE;
                break;
            case FFI_TYPE_DOUBLE:
            case FFI_TYPE_SINT64:
                size = 8;
                break;
            }
            ByteBuffer bb = NativeHelper.getBuffer(ffi_type, 24);
            if (is64)
                bb.putLong(size);
            else
                bb.putInt(size);
            bb.putShort((short) size);
            bb.putShort((short) type);
            return ffi_type;
        }

        public void destroy() {
            if (cif != 0) {
                NativeHelper.free(cif, return_type, atypes);
                NativeHelper.free(ffi_types);
                cif = return_type = atypes = 0;
            }
        }

        public long get() {
            return cif;
        }
    }
}
