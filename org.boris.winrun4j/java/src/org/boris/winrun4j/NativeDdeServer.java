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

import org.boris.winrun4j.winapi.DDEML;
import org.boris.winrun4j.winapi.Gdi32;
import org.boris.winrun4j.winapi.Kernel32;
import org.boris.winrun4j.winapi.User32;
import org.boris.winrun4j.winapi.DDEML.DdeCallback;
import org.boris.winrun4j.winapi.User32.WNDCLASSEX;
import org.boris.winrun4j.winapi.User32.WindowProc;
import org.boris.winrun4j.winapi.User32.WindowProcCallback;

public class NativeDdeServer implements Runnable, WindowProc, DdeCallback
{
    private Thread thread;
    private String server;
    private long hServerName;
    private String topic;
    private long hTopic;
    private String windowClass;
    private Callback mainWndProc;
    private Callback ddeProc;
    private long hWnd;
    private long pidInst;

    public void initialize(String server) {
        initialize(server, "system", "WinRun4J.DDEWndClass");
    }

    public void initialize() {
        initialize("WinRun4J");
    }

    public void initialize(String server, String topic, String windowClass) {
        Log.info("Initializing DDE...");
        this.server = server;
        this.topic = topic;
        this.windowClass = windowClass == null ? "WinRun4J.DDEWndClass" : windowClass;
        this.mainWndProc = new WindowProcCallback(this);
        thread = new Thread(this, "DDE Callback Thread");
        thread.setDaemon(true);
        thread.start();
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
        if (hServerName != 0)
            DDEML.DdeFreeStringHandle(pidInst, hServerName);
        if (hTopic != 0)
            DDEML.DdeFreeStringHandle(pidInst, hTopic);
        if (mainWndProc != null)
            mainWndProc.cleanup();
        if (ddeProc != null)
            ddeProc.cleanup();
        if (pidInst != 0)
            DDEML.DdeUninitialize(pidInst);
        if (hWnd != 0)
            User32.DestroyWindow(hWnd);
    }

    public void run() {
        long hInstance = Kernel32.GetModuleHandle(null);
        registerWindow(hInstance, mainWndProc);
        if (!registerDde()) {
            uninitialize();
            return;
        }

        this.hWnd = User32.CreateWindowEx(0, windowClass, "WinRun4J.DDEWindow", 0, 0, 0, 0, 0, 0, 0, hInstance, 0);
        if (hWnd == 0) {
            Log.error("Unable to create DDE window: " + Kernel32.GetLastError());
            uninitialize();
            return;
        }

        runMessageLoop(0);
    }

    public int windowProc(long hWnd, int uMsg, long wParam, long lParam) {
        System.out.println(hWnd);
        return User32.DefWindowProc(hWnd, uMsg, wParam, lParam);
    }

    private boolean registerDde() {
        this.ddeProc = new DDEML.DdeCallbackImpl(this);
        this.pidInst = DDEML.DdeInitialize(ddeProc, 0);
        if (pidInst == 0) {
            Log.error("Could not initialize DDE");
            return false;
        }
        this.hServerName = DDEML.DdeCreateStringHandle(pidInst, server, DDEML.CP_WINUNICODE);
        this.hTopic = DDEML.DdeCreateStringHandle(pidInst, topic, DDEML.CP_WINUNICODE);
        long res = DDEML.DdeNameService(pidInst, hServerName, hTopic, DDEML.DNS_REGISTER);
        if (res != 0) {
            Log.error("Could not create name service");
            return false;
        }
        return true;
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
        wcx.hbrBackground = Gdi32.GetStockObject(1);
        wcx.menuName = null;
        wcx.className = windowClass;
        wcx.hIconSm = 0;
        if (!User32.RegisterClassEx(wcx)) {
            Log.error("Unable to create DDE Window class");
        }
    }

    public static void runMessageLoop(long hWnd) {
        long pMsg = Native.malloc(28);
        while (User32.GetMessage(pMsg, hWnd, 0, 0)) {
            User32.TranslateMessage(pMsg);
            User32.DispatchMessage(pMsg);
        }
        NativeHelper.free(pMsg);
        Log.info("End of message loop: " + Thread.currentThread().getName());
    }

    public long ddeCallback(int uType, int fmt, long hConv, long hsz1, long hsz2, long hData, int data1, int data2) {
        switch (uType) {
        case DDEML.XTYP_CONNECT:
            if (hsz2 == hServerName && hsz1 == hTopic)
                return 1;
            break;

        case DDEML.XTYP_EXECUTE:
            byte[] execute = new byte[520];
            DDEML.DdeGetData(hData, execute, execute.length, 0);
            String exec = new String(execute);
            System.out.println("DDE: " + exec);
            return 1;
        }

        return 0;
    }
}
