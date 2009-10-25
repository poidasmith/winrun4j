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
import java.nio.ByteOrder;
import java.util.Properties;

import org.boris.winrun4j.winapi.DDEML;
import org.boris.winrun4j.winapi.Gdi32;
import org.boris.winrun4j.winapi.Kernel32;
import org.boris.winrun4j.winapi.User32;
import org.boris.winrun4j.winapi.DDEML.DdeCallback;
import org.boris.winrun4j.winapi.User32.WNDCLASSEX;
import org.boris.winrun4j.winapi.User32.WindowProc;
import org.boris.winrun4j.winapi.User32.WindowProcCallback;

public class NativeDdeServer extends DdeCallback implements Runnable, WindowProc
{
    private Thread thread;
    private String server;
    private long hServerName;
    private String topic;
    private long hTopic;
    private String windowClass;
    private Callback mainWndProc;
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
            DDEML.freeStringHandle(pidInst, hServerName);
        if (hTopic != 0)
            DDEML.freeStringHandle(pidInst, hTopic);
        if (mainWndProc != null)
            mainWndProc.dispose();
        dispose();
        if (pidInst != 0)
            DDEML.uninitialize(pidInst);
        if (hWnd != 0)
            User32.destroyWindow(hWnd);
    }

    public void run() {
        long hInstance = Kernel32.getModuleHandle(null);
        registerWindow(hInstance, mainWndProc);
        if (!registerDde()) {
            uninitialize();
            return;
        }

        this.hWnd = User32.createWindowEx(0, windowClass, "WinRun4J.DDEWindow", 0, 0, 0, 0, 0, 0, 0, hInstance, 0);
        if (hWnd == 0) {
            Log.error("Unable to create DDE window: " + Kernel32.getLastError());
            uninitialize();
            return;
        }

        runMessageLoop(0);
    }

    public int windowProc(long hWnd, int uMsg, long wParam, long lParam) {
        System.out.println(hWnd + " " + uMsg + " " + wParam + " " + lParam);
        return User32.defWindowProc(hWnd, uMsg, wParam, lParam);
    }

    private boolean registerDde() {
        this.pidInst = DDEML.initialize(this, 0);
        if (pidInst == 0) {
            Log.error("Could not initialize DDE");
            return false;
        }
        this.hServerName = DDEML.createStringHandle(pidInst, server, DDEML.CP_WINUNICODE);
        this.hTopic = DDEML.createStringHandle(pidInst, topic, DDEML.CP_WINUNICODE);
        long res = DDEML.nameService(pidInst, hServerName, 0, DDEML.DNS_REGISTER);
        if (res == 0) {
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
        wcx.hCursor = User32.loadCursor(0, 32514);
        wcx.hbrBackground = Gdi32.getStockObject(1);
        wcx.menuName = null;
        wcx.className = windowClass;
        wcx.hIconSm = 0;
        if (!User32.registerClassEx(wcx)) {
            Log.error("Unable to create DDE Window class");
        }
    }

    public static void runMessageLoop(long hWnd) {
        long pMsg = Native.malloc(28);
        while (User32.getMessage(pMsg, hWnd, 0, 0)) {
            User32.translateMessage(pMsg);
            User32.dispatchMessage(pMsg);
        }
        NativeHelper.free(pMsg);
        Log.info("End of message loop: " + Thread.currentThread().getName());
    }

    public long ddeCallback(int uType, int fmt, long hConv, long hsz1, long hsz2, long hData, int data1, int data2) {
        System.out.println("DDECallback: " + uType);
        switch (uType) {
        case DDEML.XTYP_CONNECT:
            if (hsz2 == hServerName && hsz1 == hTopic)
                return 1;
            break;

        case DDEML.XTYP_EXECUTE:
            byte[] execute = new byte[520];
            int len = DDEML.getData(hData, execute, execute.length, 0);
            String exec = NativeHelper.getString(ByteBuffer.wrap(execute, 0, len).order(ByteOrder.LITTLE_ENDIAN), true);
            System.out.println("DDE: " + exec);
            return 1;
        }

        return 0;
    }
}
