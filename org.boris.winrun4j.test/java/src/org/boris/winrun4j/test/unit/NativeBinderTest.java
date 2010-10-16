/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Peter Smith
 *******************************************************************************/
package org.boris.winrun4j.test.unit;

import java.lang.reflect.Method;

import org.boris.winrun4j.NativeBinder;
import org.boris.winrun4j.winapi.Console;

public class NativeBinderTest
{
    public static void main(String[] args) throws Exception {
        Method[] m = Console.class.getMethods();
        for (Method mm : m) {
            System.out.println(mm.getName() + " - " + NativeBinder.generateSig(mm));
        }
    }
}
