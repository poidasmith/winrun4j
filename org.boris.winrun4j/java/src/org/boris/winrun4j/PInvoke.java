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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PInvoke
{
    private static boolean is64 = Native.IS_64;

    public static void bind(Class clazz) {
        NativeBinder.bind(clazz);
    }

    public static void bind(Class clazz, String library) {
        NativeBinder.bind(clazz, library);
    }

    public static int sizeOf(Class struct, boolean wideChar) {
        NativeStruct ns = NativeStruct.fromClass(struct, wideChar);
        return ns == null ? 0 : ns.sizeOf();
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface DllImport {
        String value() default "";

        String lib() default "";

        String entryPoint() default "";

        boolean wideChar() default true;

        boolean setLastError() default false;

        boolean internal() default false;
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface Out {}

    @Retention(RetentionPolicy.RUNTIME)
    public @interface Delegate {}

    @Retention(RetentionPolicy.RUNTIME)
    public @interface MarshalAs {
        int sizeConst() default 0;

        boolean isPointer() default false;
    }

    public static class IntPtr
    {
        public long value;

        public IntPtr() {
        }

        public IntPtr(long value) {
            this.value = value;
        }

        public int intValue() {
            return (int) value;
        }

        public String toString() {
            return Long.toString(value);
        }
    }

    public static class UIntPtr extends IntPtr
    {
        public UIntPtr() {
        }

        public UIntPtr(long value) {
            this.value = value;
        }
    }

    public static class ByteArrayBuilder
    {
        private byte[] array;

        public void set(byte[] array) {
            this.array = array;
        }

        public byte[] toArray() {
            return array;
        }
    }

    public interface Callback
    {
    }

    public interface Struct
    {
    }

    public interface Union
    {
    }

    public static class NativeStruct
    {
        private boolean wideChar;
        private Field[] fields;
        private int[] fieldTypes;
        private int[] fieldSizes;
        private Map<Field, NativeStruct> childStructs = new HashMap();
        private int size;

        public NativeStruct(boolean wideChar) {
            this.wideChar = wideChar;
        }

        public static NativeStruct fromClass(Class struct, boolean wideChar) {
            if (struct == null)
                return null;
            if (!Struct.class.isAssignableFrom(struct))
                throw new RuntimeException("Invalid class used as struct: " + struct.getSimpleName());
            NativeStruct ns = new NativeStruct(wideChar);
            ns.parse(struct);
            return ns;
        }

        public void parse(Class struct) {
            Field[] fields = struct.getFields();
            List<Field> fieldList = new ArrayList();
            List<Integer> fieldTypes = new ArrayList();
            List<Integer> fieldSizes = new ArrayList();
            int size = 0;
            for (int i = 0; i < fields.length; i++) {
                Field f = fields[i];
                if (!Modifier.isStatic(f.getModifiers()) &&
                        Modifier.isPublic(f.getModifiers())) {
                    int ft = NativeBinder.getArgType(f.getType(), struct.getSimpleName());
                    if (ft == NativeBinder.ARG_STRING) {
                        MarshalAs ma = f.getAnnotation(MarshalAs.class);
                        if (ma == null) {
                            ft = NativeBinder.ARG_STRING_PTR;
                        }
                    }
                    fieldList.add(f);
                    fieldTypes.add(ft);
                    if (ft == NativeBinder.ARG_STRUCT_PTR)
                        childStructs.put(f, fromClass(f.getType(), wideChar));
                    int sz = sizeOf(ft, f);
                    fieldSizes.add(sz);
                    size += sz;
                }
            }

            this.fields = fieldList.toArray(new Field[0]);
            this.fieldTypes = new int[fields.length];
            this.fieldSizes = new int[fields.length];
            for (int i = 0; i < this.fields.length; i++) {
                this.fieldTypes[i] = fieldTypes.get(i);
                this.fieldSizes[i] = fieldSizes.get(i);
            }
            this.size = size;
        }

        private int sizeOf(int fieldType, Field field) {
            int nativeSize = Native.IS_64 ? 8 : 4;
            int size = 0;
            switch (fieldType) {
            case NativeBinder.ARG_BOOL:
            case NativeBinder.ARG_BYTE:
                size += 1;
                break;
            case NativeBinder.ARG_SHORT:
                size += 2;
                break;
            case NativeBinder.ARG_INT:
                size += 4;
                break;
            case NativeBinder.ARG_INT_PTR:
            case NativeBinder.ARG_LONG:
            case NativeBinder.ARG_UINT_PTR:
            case NativeBinder.ARG_RAW_CLOSURE:
            case NativeBinder.ARG_STRING_PTR:
                size += nativeSize;
                break;
            case NativeBinder.ARG_STRING:
                MarshalAs ma = field.getAnnotation(MarshalAs.class);
                if (ma == null) {
                    throw new RuntimeException("Invalid string arg type: " + field.getName());
                } else {
                    size += ma.sizeConst();
                    if (wideChar)
                        size <<= 1;
                }
                break;
            case NativeBinder.ARG_STRING_BUILDER:
                throw new RuntimeException("StringBuilder not supported in structs - " + field.getName());
            case NativeBinder.ARG_STRUCT_PTR:
                size += childStructs.get(field).sizeOf();
                break;
            default:
                throw new RuntimeException("Unsupported struct type: " + field.getName());
            }

            return size;
        }

        public int sizeOf() {
            return size;
        }

        public long toNative(Object obj) throws IllegalArgumentException, IllegalAccessException {
            long ptr = Native.malloc(size);
            toNative(ptr, obj);
            return ptr;
        }

        public void toNative(long ptr, Object obj) throws IllegalArgumentException, IllegalAccessException {
            ByteBuffer bb = NativeHelper.getBuffer(ptr, size);
            for (int i = 0; i < fieldTypes.length; i++) {
                toNative(obj, fieldTypes[i], fieldSizes[i], fields[i], bb);
            }
        }

        private void toNative(Object obj, int fieldType, int fieldSize, Field field, ByteBuffer bb)
                throws IllegalArgumentException,
                IllegalAccessException {
            switch (fieldType) {
            case NativeBinder.ARG_BOOL:
                boolean b = field.getBoolean(obj);
                bb.put(b ? (byte) 1 : (byte) 0);
                break;
            case NativeBinder.ARG_BYTE:
                bb.put(field.getByte(obj));
                break;
            case NativeBinder.ARG_INT:
                bb.putInt(field.getInt(obj));
                break;
            case NativeBinder.ARG_SHORT:
                bb.putShort(field.getShort(obj));
                break;
            case NativeBinder.ARG_RAW_CLOSURE:
                Closure cl = (Closure) field.get(obj);
                if (is64)
                    bb.putLong(cl.getPointer());
                else
                    bb.putInt((int) (cl.getPointer() & 0xffffff));
                break;
            case NativeBinder.ARG_LONG:
                long l = field.getLong(obj);
                if (is64)
                    bb.putLong(l);
                else
                    bb.putInt((int) (l & 0xffffff));
                break;
            case NativeBinder.ARG_STRING:
                String s = (String) field.get(obj);
                int bytesWritten = 0;
                if (s != null) {
                    if (wideChar) {
                        char[] c = s.toCharArray();
                        for (int i = 0; i < c.length; i++) {
                            if (bytesWritten >= fieldSize)
                                break;
                            bb.putChar(c[i]);
                            bytesWritten += 2;
                        }
                    } else {
                        byte[] bs = s.getBytes();
                        for (int i = 0; i < bs.length; i++) {
                            if (bytesWritten >= fieldSize)
                                break;
                            bb.put(bs[i]);
                            bytesWritten++;
                        }
                    }
                }
                for (int i = bytesWritten; i < fieldSize; i++)
                    bb.put((byte) 0);
            }
        }

        public void fromNative(long ptr, Object obj) throws IllegalArgumentException, IllegalAccessException {
            ByteBuffer bb = NativeHelper.getBuffer(ptr, size);
            fromNative(bb, obj);
        }

        private void fromNative(ByteBuffer bb, Object obj) throws IllegalArgumentException, IllegalAccessException {
            for (int i = 0; i < fieldTypes.length; i++) {
                fromNative(bb, fieldTypes[i], fieldSizes[i], fields[i], obj);
            }
        }

        private void fromNative(ByteBuffer bb, int fieldType, int fieldSize, Field field, Object obj)
                throws IllegalArgumentException,
                IllegalAccessException {
            switch (fieldType) {
            case NativeBinder.ARG_BOOL:
                field.set(obj, bb.get() != 0);
                break;
            case NativeBinder.ARG_BYTE:
                field.set(obj, bb.get());
                break;
            case NativeBinder.ARG_INT:
                field.set(obj, bb.getInt());
                break;
            case NativeBinder.ARG_LONG:
                field.set(obj, is64 ? bb.getLong() : bb.getInt());
                break;
            case NativeBinder.ARG_SHORT:
                field.set(obj, bb.getShort());
                break;
            case NativeBinder.ARG_STRING:
                byte[] b = new byte[fieldSize];
                bb.get(b);
                field.set(obj, NativeHelper.getString(b, wideChar));
                break;
            }
        }
    }
}
