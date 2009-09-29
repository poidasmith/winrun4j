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

import java.util.Properties;

import org.boris.winrun4j.winapi.User32;
import org.boris.winrun4j.winapi.User32.WNDCLASSEX;
import org.boris.winrun4j.winapi.User32.WindowProc;
import org.boris.winrun4j.winapi.User32.WindowProcCallback;

public class NativeDdeServer implements Runnable, WindowProc
{
    private Thread thread;
    private String server;
    private String topic;
    private String windowClass;
    private Callback mainWndProc;
    private long hWnd;

    public void initialize(String server, String topic, String windowClass) {
        this.server = server;
        this.topic = topic;
        this.windowClass = windowClass == null ? "WinRun4J.DDEWndClass"
                : windowClass;
        this.mainWndProc = new WindowProcCallback(this);
    }

    public boolean initialize(Properties ini) {
        if (Boolean.valueOf(ini.getProperty(INI.DDE_ENABLED)).booleanValue()) {
            initialize(ini.getProperty(INI.DDE_SERVER_NAME), ini
                    .getProperty(INI.DDE_TOPIC), ini
                    .getProperty(INI.DDE_WINDOW_CLASS));
            return true;
        }
        return false;
    }

    public void uninitialize() {
        mainWndProc.cleanup();
    }

    public void run() {
        registerWindow(0, mainWndProc);
        if (!registerDde()) {
            return;
        }

        this.hWnd = User32.CreateWindowEx(0, windowClass, "WinRun4J.DDEWindow",
                0, 0, 0, 0, 0, 0, 0, 0, 0);

        runMessageLoop(0);
    }

    public int windowProc(long hWnd, int uMsg, long wParam, long lParam) {
        return 0;
    }

    private boolean registerDde() {
        return false;
    }

    private void registerWindow(long hInstance, Callback mainWndProc) {
        WNDCLASSEX wcx = new WNDCLASSEX();
        wcx.style = User32.CS_BYTEALIGNCLIENT | User32.CS_BYTEALIGNWINDOW;
        wcx.lpfnWndProc = mainWndProc;
        wcx.cbClsExtra = 0;
        wcx.cbWndExtra = 30;
        wcx.hInstance = hInstance;
        wcx.hIcon = 0;
        wcx.hCursor = User32.LoadCursor(0, 32514);
        wcx.hbrBackground = User32.GetStockObject(1);
        wcx.menuName = null;
        wcx.className = windowClass;
        wcx.hIconSm = 0;
        User32.RegisterClassEx(wcx);
    }

    public static void runMessageLoop(long hWnd) {
        long pMsg = Native.malloc(28);
        while (User32.GetMessage(pMsg, hWnd, 0, 0)) {
            User32.TranslateMessage(pMsg);
            User32.DispatchMessage(pMsg);
        }
        NativeHelper.free(pMsg);
    }
}
