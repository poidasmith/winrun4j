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

import org.boris.winrun4j.Closure;
import org.boris.winrun4j.Log;
import org.boris.winrun4j.winapi.Gdi32;
import org.boris.winrun4j.winapi.Kernel32;
import org.boris.winrun4j.winapi.User32;
import org.boris.winrun4j.winapi.User32.MSG;
import org.boris.winrun4j.winapi.User32.WNDCLASSEX;
import org.boris.winrun4j.winapi.User32.WindowProc;

public class WindowTest implements WindowProc
{
    public static void main(String[] args) throws Exception {
        WindowTest wt = new WindowTest();
        Closure callback = Closure.build(WindowProc.class, wt, true);
        registerWindow(callback);
        long hWnd = createWindow();
        if (hWnd == 0)
            Log.error("Unable to create window");
        User32.ShowWindow(hWnd, User32.SW_SHOW);
        User32.UpdateWindow(hWnd);

        MSG m = new MSG();
        while (User32.GetMessage(m, hWnd, 0, 0)) {
            User32.TranslateMessage(m);
            User32.DispatchMessage(m);
        }
    }

    private static long createWindow() {
        return User32.CreateWindowEx(0x80, "WinRun4J.Test", "WinRun4J.TestWindow", 0x80000000, 100, 100, 100, 100, 0,
                0, 0, 0);
    }

    private static void registerWindow(Closure callback) {
        WNDCLASSEX wcx = new WNDCLASSEX();
        wcx.style = User32.CS_BYTEALIGNCLIENT | User32.CS_BYTEALIGNWINDOW;
        wcx.lpfnWndProc = callback;
        wcx.cbClsExtra = 0;
        wcx.cbWndExtra = 30;
        wcx.hInstance = Kernel32.GetModuleHandle(null);
        wcx.hIcon = 0;
        wcx.hCursor = User32.LoadCursor(0, 32514);
        wcx.hbrBackground = Gdi32.getStockObject(1);
        wcx.menuName = null;
        wcx.className = "WinRun4J.Test";
        wcx.hIconSm = 0;
        if (!User32.RegisterClassEx(wcx)) {
            Log.error("Unable to create DDE Window class");
        }
    }

    public int windowProc(long hWnd, int uMsg, long wParam, long lParam) {
        return User32.DefWindowProc(hWnd, uMsg, wParam, lParam);
    }
}
