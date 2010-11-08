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
import org.boris.winrun4j.PInvoke.Callback;
import org.boris.winrun4j.PInvoke.DllImport;
import org.boris.winrun4j.PInvoke.IntPtr;

public class BindingExample3
{
    static {
        PInvoke.bind(BindingExample3.class, "user32");
    }

    public static void main(String[] args) throws Exception {
        IntPtr count = new IntPtr();

        WindowEnumProc callback = new WindowEnumProc() {
            public boolean windowEnum(long hWnd, IntPtr lParam) throws Exception {
                System.out.println(hWnd);
                System.out.println(lParam);
                lParam.value++;
                return true;
            }
        };

        EnumWindows(callback, count);

        System.out.println(count.value);
    }

    @DllImport
    public static native boolean EnumWindows(WindowEnumProc enumFunc, IntPtr lParam);

    public interface WindowEnumProc extends Callback
    {
        boolean windowEnum(long hWnd, IntPtr lParam) throws Exception;
    }
}
