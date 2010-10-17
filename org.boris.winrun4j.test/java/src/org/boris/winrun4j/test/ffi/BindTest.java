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

import org.boris.winrun4j.Closure;
import org.boris.winrun4j.PInvoke;
import org.boris.winrun4j.PInvoke.Callback;
import org.boris.winrun4j.PInvoke.DllImport;
import org.boris.winrun4j.PInvoke.IntPtr;
import org.boris.winrun4j.PInvoke.UIntPtr;

public class BindTest
{
    static {
        PInvoke.bind(BindTest.class);
    }

    public static void main(String[] args) throws Exception {
        Closure c = Closure.build(WindowEnumProc.class, new WindowEnumProc() {
            public boolean windowEnum(IntPtr hWnd, IntPtr lParam) {
                return false;
            }
        });
    }

    public static void main2(String[] args) throws Exception {
        // System.out.println(GetCurrentProcessId());

        StringBuilder userName = new StringBuilder();
        // Kernel32.debugBreak();
        UIntPtr len = new UIntPtr(100);
        // Test1(344551122, sb, len);
        for (int i = 0; i < 100; i++) {
            len.value = 100;
            System.out.println(GetUserName(userName, len));
            System.out.println(userName);
            System.out.println(len.value);
            System.out.println(GetCurrentProcessId());
        }

        StringBuilder var = new StringBuilder();
        len.value = 100;
        System.out.println(GetEnvironmentVariable("TEMP", var, len));
        System.out.println(var);
        System.out.println(len.value);
    }

    public interface WindowEnumProc extends Callback
    {
        boolean windowEnum(IntPtr hWnd, IntPtr lParam);
    }

    @DllImport(lib = "kernel32.dll", entryPoint = "GetCurrentProcessId")
    public static native int Test1(int i, StringBuilder sb, UIntPtr p2);

    @DllImport("kernel32.dll")
    public static native int GetCurrentProcessId();

    @DllImport(lib = "advapi32.dll", setLastError = true)
    public static native boolean GetUserName(StringBuilder sb, UIntPtr length);

    @DllImport("user32.dll")
    public static native boolean EnumWindows(WindowEnumProc enumFunc, IntPtr lParam);

    @DllImport("user32.dll")
    public static native int GetWindowText(long hWnd, StringBuilder lpString, int nMaxCount);

    @DllImport("kernel32.dll")
    public static native int GetEnvironmentVariable(String lpName, StringBuilder lpBuffer, UIntPtr nSize);
}
