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

import org.boris.winrun4j.Callback;
import org.boris.winrun4j.Hooks;
import org.boris.winrun4j.Kernel32;
import org.boris.winrun4j.Native;
import org.boris.winrun4j.Hooks.MOUSEHOOKSTRUCT;
import org.boris.winrun4j.Hooks.MouseProc;
import org.boris.winrun4j.Hooks.MouseProcCallback;

public class HooksTest
{
    public static void main(String[] args) throws Exception {
        MouseProcCallback mpc = new MouseProcCallback(new MouseProc() {
            public int cbMouseProc(int code, int id, MOUSEHOOKSTRUCT struc) {
                System.out.println(code);
                System.out.println(id);
                System.out.println(Reflection.toString(struc));
                return 0;
            }
        });
        Callback cb = new Callback() {
            protected int callback(int stack) {
                System.out.println(stack);
                return 0;
            }
        };
        long hook = Hooks.SetWindwsHookEx(Hooks.WH_MOUSE_LL, cb, Native.loadLibrary("jvm"), 0);
        System.out.println(hook);
        System.out.println(Kernel32.GetLastError());
        Thread.sleep(2000);
        Hooks.UnhookWindowsHookEx(hook);
    }
}
