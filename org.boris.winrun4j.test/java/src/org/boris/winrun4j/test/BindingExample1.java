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

import org.boris.winrun4j.PInvoke;
import org.boris.winrun4j.PInvoke.DllImport;
import org.boris.winrun4j.PInvoke.UIntPtr;

public class BindingExample1
{
    static {
        PInvoke.bind(BindingExample1.class);
    }

    public static void main(String[] args) throws Exception {
        StringBuilder name = new StringBuilder();
        UIntPtr size = new UIntPtr(100);
        if (GetComputerName(name, size)) {
            System.out.println(name);
        }
    }

    @DllImport("kernel32")
    public static native boolean GetComputerName(StringBuilder lpBuffer, UIntPtr lpnSize);

}
