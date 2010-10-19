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

    public static int sizeOf(Class struct) {
        NativeStruct ns = NativeStruct.fromClass(struct);
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
        private Field[] fields;
        private int[] fieldTypes;
        private Map<Field, NativeStruct> childStructs = new HashMap();
        private int size;

        public static NativeStruct fromClass(Class struct) {
            if (struct == null)
                return null;
            if (!Struct.class.isAssignableFrom(struct))
                return null;
            NativeStruct ns = new NativeStruct();
            ns.parse(struct);
            return ns;
        }

        public void parse(Class struct) {
            Field[] fields = struct.getFields();
            List<Field> fieldList = new ArrayList();
            List<Integer> fieldTypes = new ArrayList();
            int size = 0;
            for (int i = 0; i < fields.length; i++) {
                Field f = fields[i];
                if (!Modifier.isStatic(f.getModifiers()) &&
                        Modifier.isPublic(f.getModifiers())) {
                    int ft = NativeBinder.getArgType(f.getType());
                    fieldList.add(f);
                    fieldTypes.add(ft);
                    if (ft == NativeBinder.ARG_STRUCT_PTR)
                        childStructs.put(f, fromClass(f.getType()));
                    size += sizeOf(ft, f);
                }
            }

            this.fields = fieldList.toArray(new Field[0]);
            this.fieldTypes = new int[fields.length];
            for (int i = 0; i < this.fieldTypes.length; i++)
                this.fieldTypes[i] = fieldTypes.get(i);
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
                size += nativeSize;
                break;
            case NativeBinder.ARG_STRING:
                MarshalAs ma = field.getAnnotation(MarshalAs.class);
                if (ma == null) {
                    throw new RuntimeException("Invalid struct definition at: " + field.getName());
                }
                size += ma.sizeConst(); // FIXME: widechar
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
            ByteBuffer bb = NativeHelper.getBuffer(ptr, size);
            for (int i = 0; i < fieldTypes.length; i++) {
                toNative(obj, fieldTypes[i], fields[i], bb);
            }
            return ptr;
        }

        private void toNative(Object obj, int fieldType, Field field, ByteBuffer bb) throws IllegalArgumentException,
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
            case NativeBinder.ARG_LONG:
                long l = field.getLong(obj);
                if (is64)
                    bb.putLong(l);
                else
                    bb.putInt((int) l);
                break;
            case NativeBinder.ARG_STRING:

            }
        }

        public void fromNative(long ptr, Object obj) throws IllegalArgumentException, IllegalAccessException {
            ByteBuffer bb = NativeHelper.getBuffer(ptr, size);
            fromNative(bb, obj);
        }

        private void fromNative(ByteBuffer bb, Object obj) throws IllegalArgumentException, IllegalAccessException {
            for (int i = 0; i < fieldTypes.length; i++) {
                fromNative(bb, fieldTypes[i], fields[i], obj);
            }
        }

        private void fromNative(ByteBuffer bb, int fieldType, Field field, Object obj) throws IllegalArgumentException,
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
            }
        }
    }
}
