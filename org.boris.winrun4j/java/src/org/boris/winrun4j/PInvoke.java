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

public class PInvoke
{
    public static void bind(Class clazz) {
        NativeBinder.bind(clazz);
    }

    public static void bind(Class clazz, String library) {
        NativeBinder.bind(clazz, library);
    }

    public static int sizeOf(Class struct) {
        if (struct == null)
            return 0;
        if (!Struct.class.isAssignableFrom(struct))
            return 0;
        int size = 0;
        Field[] fields = struct.getFields();
        for (int i = 0; i < fields.length; i++) {
            Field f = fields[i];
            if (!Modifier.isStatic(f.getModifiers()) && Modifier.isPublic(f.getModifiers())) {
                switch (NativeBinder.getArgType(f.getDeclaringClass())) {
                case NativeBinder.ARG_BOOL:
                    break;
                }
            }
        }
        return size;
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
    }

    public static class UIntPtr extends IntPtr
    {
        public UIntPtr() {
        }

        public UIntPtr(long value) {
            this.value = value;
        }
    }

    public interface Callback
    {
    }

    public interface Struct
    {
    }
}
