package org.boris.winrun4j.test;

import java.lang.reflect.Field;

public class Structs
{
    public static long toNative(Object struct) {
        return 0;
    }

    public static int sizeOf(Class structClass) {
        Field[] f = structClass.getDeclaredFields();
        int size = 0;
        for (int i = 0; i < f.length; i++) {
            if (int.class.equals(f[i].getDeclaringClass())) {
                size += 4;
            }
        }
        return size;
    }

}
