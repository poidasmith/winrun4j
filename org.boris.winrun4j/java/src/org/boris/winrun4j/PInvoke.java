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

public class PInvoke
{
    public static void bind(Class clazz) {
        NativeBinder.bind(clazz);
    }

    public static void bind(Class clazz, String library) {
        NativeBinder.bind(clazz, library);
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
    public @interface Delegate {
        boolean wideChar() default true;
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface MarshalAs {
        int sizeConst() default 0;
    }

    public static class IntPtr
    {
        public int value;

        public IntPtr() {
        }

        public IntPtr(int value) {
            this.value = value;
        }
    }

    public static class UIntPtr extends IntPtr
    {
        public UIntPtr() {
        }

        public UIntPtr(int value) {
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
