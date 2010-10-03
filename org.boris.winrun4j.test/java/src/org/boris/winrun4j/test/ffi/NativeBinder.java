/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Peter Smith
 *******************************************************************************/
package org.boris.winrun4j.test.ffi;

import java.lang.reflect.Method;

public class NativeBinder
{
    public static void bind(Class clazz) {
        bind(clazz, null);
    }

    public static void bind(Class clazz, String library) {
        Method[] methods = clazz.getMethods();
        for (Method m : methods) {
        }
    }
}
