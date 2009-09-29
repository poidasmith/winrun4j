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
        this.windowClass = windowClass == null ? "WinRun4J.DDEWndClass" : windowClass;
        this.mainWndProc = new WindowProcCallback(this);
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
        mainWndProc.cleanup();
    }

    public void run() {
        registerWindow(0, mainWndProc);
        if (!registerDde()) {
            return;
        }

        this.hWnd = CreateWindowEx(0, windowClass, "WinRun4J.DDEWindow", 0, 0, 0, 0, 0, 0, 0, 0, 0);

        runMessageLoop(0);
    }

    public int windowProc(long hWnd, int uMsg, long wParam, long lParam) {
        return 0;
    }

    private boolean registerDde() {
        return false;
    }

    public static final int CS_VREDRAW = 0x0001;
    public static final int CS_HREDRAW = 0x0002;
    public static final int CS_DBLCLKS = 0x0008;
    public static final int CS_OWNDC = 0x0020;
    public static final int CS_CLASSDC = 0x0040;
    public static final int CS_PARENTDC = 0x0080;
    public static final int CS_NOCLOSE = 0x0200;
    public static final int CS_SAVEBITS = 0x0800;
    public static final int CS_BYTEALIGNCLIENT = 0x1000;
    public static final int CS_BYTEALIGNWINDOW = 0x2000;
    public static final int CS_GLOBALCLASS = 0x4000;

    private void registerWindow(long hInstance, Callback mainWndProc) {
        WNDCLASSEX wcx = new WNDCLASSEX();
        wcx.style = CS_BYTEALIGNCLIENT | CS_BYTEALIGNWINDOW;
        wcx.lpfnWndProc = mainWndProc;
        wcx.cbClsExtra = 0;
        wcx.cbWndExtra = 30;
        wcx.hInstance = hInstance;
        wcx.hIcon = 0;
        wcx.hCursor = LoadCursor(0, 32514);
        wcx.hbrBackground = GetStockObject(1);
        wcx.menuName = null;
        wcx.className = windowClass;
        wcx.hIconSm = 0;
        RegisterClassEx(wcx);
    }

    public static long LoadCursor(long hInstance, long lpCursorName) {
        return NativeHelper.call(User32.library, "LoadCursor", hInstance, lpCursorName);
    }

    public static long GetStockObject(int fnObject) {
        return NativeHelper.call(User32.library, "GetStockObject", fnObject);
    }

    public static void runMessageLoop(long hWnd) {
        long pMsg = Native.malloc(28);
        while (GetMessage(pMsg, hWnd, 0, 0)) {
            TranslateMessage(pMsg);
            DispatchMessage(pMsg);
        }
        NativeHelper.free(pMsg);
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
            decode(ptr, m);
        }
        NativeHelper.free(ptr);
        return m;
    }

    public static boolean GetMessage(long pMsg, long hWnd, int wMsgFilterMin, int wMsgFilterMax) {
        return NativeHelper.call(User32.library, "GetMessage", pMsg, hWnd, wMsgFilterMin, wMsgFilterMax) != 0;
    }

    public static boolean TranslateMessage(MSG m) {
        long ptr = Native.malloc(28);
        encode(ptr, m);
        boolean res = NativeHelper.call(User32.library, "TranslateMessage", ptr) != 0;
        decode(ptr, m);
        NativeHelper.free(ptr);
        return res;
    }

    public static boolean TranslateMessage(long pMsg) {
        return NativeHelper.call(User32.library, "TranslateMessage", pMsg) != 0;
    }

    public static boolean DispatchMessage(MSG m) {
        long ptr = Native.malloc(28);
        encode(ptr, m);
        boolean res = NativeHelper.call(User32.library, "DispatchMessage", ptr) != 0;
        decode(ptr, m);
        NativeHelper.free(ptr);
        return res;
    }

    public static boolean DispatchMessage(long pMsg) {
        return NativeHelper.call(User32.library, "DispatchMessage", pMsg) != 0;
    }

    public static boolean RegisterClassEx(WNDCLASSEX wcx) {
        long ptr = Native.malloc(WNDCLASSEX.SIZE);
        long lpMenuName = NativeHelper.toNativeString(wcx.menuName, true);
        long lpClassName = NativeHelper.toNativeString(wcx.className, true);
        ByteBuffer bb = NativeHelper.getBuffer(ptr, WNDCLASSEX.SIZE);
        bb.putInt(wcx.style);
        bb.putInt((int) wcx.lpfnWndProc.getPointer());
        bb.putInt(wcx.cbClsExtra);
        bb.putInt(wcx.cbWndExtra);
        bb.putInt((int) wcx.hInstance);
        bb.putInt((int) wcx.hIcon);
        bb.putInt((int) wcx.hCursor);
        bb.putInt((int) wcx.hbrBackground);
        bb.putInt((int) lpMenuName);
        bb.putInt((int) lpClassName);
        boolean res = NativeHelper.call(User32.library, "RegisterClassExW", ptr) != 0;
        NativeHelper.free(ptr, lpMenuName, lpClassName);
        return res;
    }

    public static void decode(long ptr, MSG m) {
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

    public static void encode(long ptr, MSG m) {
        ByteBuffer bb = NativeHelper.getBuffer(ptr, 28);
        bb.putInt((int) m.hWnd);
        bb.putInt(m.message);
        bb.putInt(m.wParam);
        bb.putInt(m.lParam);
        bb.putInt(m.time);
        bb.putInt(m.pt.x);
        bb.putInt(m.pt.y);
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

    public static class WNDCLASSEX
    {
        public static final int SIZE = 48;
        public int cbSize = SIZE;
        public int style;
        public Callback lpfnWndProc;
        public int cbClsExtra;
        public int cbWndExtra;
        public long hInstance;
        public long hIcon;
        public long hCursor;
        public long hbrBackground;
        public String menuName;
        public String className;
        public long hIconSm;
    }
}
