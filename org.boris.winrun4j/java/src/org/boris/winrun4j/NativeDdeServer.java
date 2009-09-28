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

import java.nio.ByteBuffer;
import java.util.Properties;

import org.boris.winrun4j.winapi.User32;
import org.boris.winrun4j.winapi.Hooks.POINT;

public class NativeDdeServer implements Runnable
{
    private Thread thread;

    public void initialize(String server, String topic, String windowClass) {
    }

    public boolean initialize(Properties ini) {
        if (Boolean.valueOf(ini.getProperty(INI.DDE_ENABLED)).booleanValue()) {
            initialize(ini.getProperty(INI.DDE_SERVER_NAME), ini.getProperty(INI.DDE_TOPIC), ini
                    .getProperty(INI.DDE_WINDOW_CLASS));
            return true;
        }
        return false;
    }

    public void uninitialize() {
    }

    public void run() {
    }

    public static long CreateWindowEx(int dwExStyle, String className, String windowName, int dwStyle, int x, int y,
            int width, int height, long parent, long menu, long hInstance, long lParam) {
        long lpClassName = NativeHelper.toNativeString(className, true);
        long lpWindowName = NativeHelper.toNativeString(windowName, true);
        long res = NativeHelper.call(User32.library, "CreateWindowExW", new long[] { dwExStyle, lpClassName, dwStyle,
                x, y, width, height, parent, menu, hInstance, lParam });
        NativeHelper.free(lpClassName, lpWindowName);
        return res;
    }

    public static MSG GetMessage(long hWnd, int wMsgFilterMin, int wMsgFilterMax) {
        long ptr = Native.malloc(28);
        long res = NativeHelper.call(User32.library, "GetMessage", ptr, hWnd, wMsgFilterMin, wMsgFilterMax);
        MSG m = null;
        if (res != 0) {
            m = new MSG();
            ByteBuffer bb = NativeHelper.getBuffer(ptr, 28);
            m.hWnd = bb.getInt();
            m.message = bb.getInt();
            m.wParam = bb.getInt();
            m.lParam = bb.getInt();
            m.time = bb.getInt();
            m.pt = new POINT();
            m.pt.x = bb.getInt();
            m.pt.y = bb.getInt();
        }
        NativeHelper.free(ptr);
        return m;
    }

    public static class MSG
    {
        long hWnd;
        int message;
        int wParam;
        int lParam;
        int time;
        POINT pt;
    }
}
