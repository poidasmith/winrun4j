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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Reflection
{
    public static String toString(Object o) {
        return toString(o, false);
    }

    public static String toString(Object o, boolean includeSuper) {
        if (o == null)
            return "null";
        StringBuilder sb = new StringBuilder();
        Field[] fields = null;
        if (includeSuper) {
            ArrayList l = new ArrayList();
            Class c = o.getClass();
            while (c != null && c != Object.class) {
                List l2 = new ArrayList();
                Field[] f = c.getDeclaredFields();
                for (int i = 0; i < f.length; i++) {
                    if (!Modifier.isStatic(f[i].getModifiers())) {
                        l2.add(f[i]);
                    }
                }
                Collections.reverse(l2);
                l.addAll(l2);
                c = c.getSuperclass();
            }
            Collections.reverse(l);
            fields = (Field[]) l.toArray(new Field[l.size()]);
        } else {
            fields = o.getClass().getDeclaredFields();
        }
        for (Field f : fields) {
            if (Modifier.isStatic(f.getModifiers()))
                continue;
            f.setAccessible(true);
            sb.append(f.getName());
            sb.append(": ");
            try {
                Object val = f.get(o);
                if (val instanceof Integer) {
                    sb.append(f.get(o));
                    sb.append(" (0x");
                    sb.append(Integer.toHexString(((Integer) val).intValue()));
                    sb.append(")");
                } else if (val instanceof Long) {
                    sb.append(f.get(o));
                    sb.append(" (0x");
                    sb.append(Long.toHexString(((Long) val).longValue()));
                    sb.append(")");
                } else if (val != null && val.getClass().isArray()) {
                    if (val instanceof int[]) {
                        int[] arr = (int[]) val;
                        for (int i = 0; i < arr.length && i < 10; i++) {
                            if (i != 0)
                                sb.append(", ");
                            sb.append(arr[i]);
                        }
                    } else if (val instanceof byte[]) {
                        byte[] arr = (byte[]) val;
                        for (int i = 0; i < arr.length && i < 10; i++) {
                            if (i != 0)
                                sb.append(", ");
                            sb.append(Integer.toHexString(arr[i] & 0xff));
                        }
                    } else {
                        Object[] arr = (Object[]) val;
                        for (int i = 0; i < arr.length && i < 10; i++) {
                            if (i != 0)
                                sb.append(", ");
                            sb.append(arr[i]);
                        }
                    }
                } else {
                    sb.append(f.get(o));
                }
            } catch (Exception e) {
                sb.append(e.getMessage());
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    public static String getConstantName(Class clazz, int value) throws Exception {
        Field[] fields = clazz.getDeclaredFields();
        Integer valObj = new Integer(value);
        for (int i = 0; i < fields.length; i++) {
            Field f = fields[i];
            if (Modifier.isStatic(f.getModifiers()) && Modifier.isPublic(f.getModifiers())) {
                if (f.get(null).equals(valObj)) {
                    return f.getName();
                }
            }
        }

        return null;
    }

    public static void println(Object value) {
        System.out.println(toString(value));
    }

    public static void println(Object value, boolean includeSuper) {
        System.out.println(toString(value, includeSuper));
    }

    public static void printArray(Object[] arr, boolean includeSuper) {
        if (arr != null) {
            for (int i = 0; i < arr.length; i++) {
                println(arr[i], includeSuper);
            }
        }
    }
}
