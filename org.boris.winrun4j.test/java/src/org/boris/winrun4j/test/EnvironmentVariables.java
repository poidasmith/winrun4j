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

public class EnvironmentVariables
{
    static {
        PInvoke.bind(EnvironmentVariables.class, "kernel32.dll");
    }
    
    @DllImport("kernel32.dll")
    public static native int GetEnvironmentVariable(String lpName, StringBuilder lpBuffer, UIntPtr nsize);

    @DllImport("kernel32.dll")
    public static native boolean SetEnvironmentVariable(String lpName, String lpValue);

    public static void main(String[] args) {
        StringBuilder sb = new StringBuilder();
        UIntPtr uip = new UIntPtr(4096);
        GetEnvironmentVariable("PATH", sb, uip);
        String path = sb.toString();
        System.out.println(path);
        // Modify path now
        path += ";../mypath/etc";
        SetEnvironmentVariable("PATH", path);
        GetEnvironmentVariable("PATH", sb, uip);
        System.out.println(sb.toString());
    }
}
