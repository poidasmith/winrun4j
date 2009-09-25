package org.boris.winrun4j.test;

import java.lang.reflect.Field;

import org.boris.winrun4j.Native;

public class Structs
{
    public static long toNative(Object struct) {
        long ptr = Native.malloc(sizeOf(struct.getClass()));

        return ptr;
    }

    public static Object fromNative(long ptr, Class struct) {
        return null;
    }

    public static int sizeOf(Class structClass) {
        if (structClass == null)
            return 0;
        if (Object.class.equals(structClass))
            return 0;
        Field[] f = structClass.getDeclaredFields();
        int size = 0;
        for (int i = 0; i < f.length; i++) {
            Class t = f[i].getType();
            if (int.class.equals(t)) {
                size += 4;
            } else if (long.class.equals(t)) {
                size += 8;
            } else if (double.class.equals(t)) {
                size += 8;
            } else if (float.class.equals(t)) {
                size += 4;
            } else if (short.class.equals(t)) {
                size += 2;
            } else if (char.class.equals(t)) {
                size += 2;
            } else if (byte.class.equals(t)) {
                size += 2;
            } else if (boolean.class.equals(t)) {
                size += 2;
            } else {
                size += sizeOf(t);
            }
        }
        return size + sizeOf(structClass.getSuperclass());
    }
}
