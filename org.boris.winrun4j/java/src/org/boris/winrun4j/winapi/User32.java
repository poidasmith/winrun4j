/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Peter Smith
 *******************************************************************************/
package org.boris.winrun4j.winapi;

import java.nio.ByteBuffer;

import org.boris.winrun4j.Callback;
import org.boris.winrun4j.Native;
import org.boris.winrun4j.NativeHelper;

public class User32
{
    public static final long library = Native.loadLibrary("user32");
    private static final boolean is64 = NativeHelper.IS_64;

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

    public static final int SW_HIDE = 0;
    public static final int SW_SHOWNORMAL = 1;
    public static final int SW_NORMAL = 1;
    public static final int SW_SHOWMINIMIZED = 2;
    public static final int SW_SHOWMAXIMIZED = 3;
    public static final int SW_MAXIMIZE = 3;
    public static final int SW_SHOWNOACTIVATE = 4;
    public static final int SW_SHOW = 5;
    public static final int SW_MINIMIZE = 6;
    public static final int SW_SHOWMINNOACTIVE = 7;
    public static final int SW_SHOWNA = 8;
    public static final int SW_RESTORE = 9;
    public static final int SW_SHOWDEFAULT = 10;
    public static final int SW_FORCEMINIMIZE = 11;
    public static final int SW_MAX = 11;

    public static long createWindowEx(int dwExStyle, String className, String windowName, int dwStyle, int x, int y,
            int width, int height, long parent, long menu, long hInstance, long lParam) {
        long lpClassName = NativeHelper.toNativeString(className, true);
        long lpWindowName = NativeHelper.toNativeString(windowName, true);
        long res = NativeHelper.call(library, "CreateWindowExW", new long[] { dwExStyle, lpClassName, lpWindowName,
                dwStyle, x, y, width, height, parent, menu, hInstance, lParam });
        NativeHelper.free(lpClassName, lpWindowName);
        return res;
    }

    public static void destroyWindow(long hWnd) {
        NativeHelper.call(library, "DestroyWindow", hWnd);
    }

    public static long loadCursor(long hInstance, long lpCursorName) {
        return NativeHelper.call(library, "LoadCursorW", hInstance, lpCursorName);
    }

    public static void setForegroundWindow(long hwnd) {
        NativeHelper.call(library, "SetForegroundWindow", hwnd);
    }

    public static boolean showWindow(long hWnd, int nCmdShow) {
        return NativeHelper.call(library, "ShowWindow", hWnd, nCmdShow) != 0;
    }

    public static boolean updateWindow(long hWnd) {
        return NativeHelper.call(library, "UpdateWindow", hWnd) != 0;
    }

    public static int getWindowThreadProcessId(long hwnd) {
        long ptr = Native.malloc(4);
        NativeHelper.call(library, "GetWindowThreadProcessId", hwnd, ptr);
        int res = NativeHelper.getInt(ptr);
        Native.free(ptr);
        return res;
    }

    public static boolean enumWindows(Callback proc, int lParam) {
        return NativeHelper.call(library, "EnumWindows", proc.getPointer(), lParam) != 0;
    }

    public static WINDOWINFO getWindowInfo(long hwnd) {
        long ptr = Native.malloc(WINDOWINFO.SIZE);
        NativeHelper.setInt(ptr, WINDOWINFO.SIZE);
        boolean res = NativeHelper.call(library, "GetWindowInfo", hwnd, ptr) != 0;
        WINDOWINFO wi = null;
        if (res) {
            wi = new WINDOWINFO();
            decode(ptr, wi);
        }
        Native.free(ptr);
        return wi;
    }

    public static int defWindowProc(long hWnd, int uMsg, long wParam, long lParam) {
        return (int) NativeHelper.call(library, "DefWindowProcW", hWnd, uMsg, wParam, lParam);
    }

    public interface WindowProc
    {
        public int windowProc(long hWnd, int uMsg, long wParam, long lParam);
    }

    public static class WindowProcCallback extends Callback
    {
        private WindowProc callback;

        public WindowProcCallback(WindowProc callback) {
            this.callback = callback;
        }

        protected int callback(int stack) {
            ByteBuffer bb = NativeHelper.getBuffer(stack, 16);
            return callback.windowProc(bb.getInt(), bb.getInt(), bb.getInt(), bb.getInt());
        }
    }

    public static MSG getMessage(long hWnd, int wMsgFilterMin, int wMsgFilterMax) {
        long ptr = Native.malloc(28);
        long res = NativeHelper.call(library, "GetMessage", ptr, hWnd, wMsgFilterMin, wMsgFilterMax);
        MSG m = null;
        if (res != 0) {
            m = new MSG();
            decode(ptr, m);
        }
        NativeHelper.free(ptr);
        return m;
    }

    private static long pGetMessage = Native.getProcAddress(library, "GetMessageW");
    private static long pTranslateMessage = Native.getProcAddress(library, "TranslateMessage");
    private static long pDispatchMessage = Native.getProcAddress(library, "DispatchMessageW");

    public static boolean getMessage(long pMsg, long hWnd, int wMsgFilterMin, int wMsgFilterMax) {
        return NativeHelper.call(pGetMessage, pMsg, hWnd, wMsgFilterMin, wMsgFilterMax) != 0;
    }

    public static boolean translateMessage(MSG m) {
        long ptr = Native.malloc(28);
        encode(ptr, m);
        boolean res = NativeHelper.call(pTranslateMessage, ptr) != 0;
        decode(ptr, m);
        NativeHelper.free(ptr);
        return res;
    }

    public static boolean translateMessage(long pMsg) {
        return NativeHelper.call(pTranslateMessage, pMsg) != 0;
    }

    public static boolean dispatchMessage(MSG m) {
        long ptr = Native.malloc(28);
        encode(ptr, m);
        boolean res = NativeHelper.call(pDispatchMessage, ptr) != 0;
        decode(ptr, m);
        NativeHelper.free(ptr);
        return res;
    }

    public static boolean dispatchMessage(long pMsg) {
        return NativeHelper.call(pDispatchMessage, pMsg) != 0;
    }

    public static boolean registerClassEx(WNDCLASSEX wcx) {
        long ptr = Native.malloc(WNDCLASSEX.SIZE);
        long lpMenuName = NativeHelper.toNativeString(wcx.menuName, true);
        long lpClassName = NativeHelper.toNativeString(wcx.className, true);
        ByteBuffer bb = NativeHelper.getBuffer(ptr, WNDCLASSEX.SIZE);
        bb.putInt(wcx.cbSize);
        bb.putInt(wcx.style);
        if (is64)
            bb.putLong(wcx.lpfnWndProc.getPointer());
        else
            bb.putInt((int) wcx.lpfnWndProc.getPointer());
        bb.putInt(wcx.cbClsExtra);
        bb.putInt(wcx.cbWndExtra);
        if (is64) {
            bb.putLong(wcx.hInstance);
            bb.putLong(wcx.hIcon);
            bb.putLong(wcx.hCursor);
            bb.putLong(wcx.hbrBackground);
            bb.putLong(lpMenuName);
            bb.putLong(lpClassName);
            bb.putLong(wcx.hIconSm);
        } else {
            bb.putInt((int) wcx.hInstance);
            bb.putInt((int) wcx.hIcon);
            bb.putInt((int) wcx.hCursor);
            bb.putInt((int) wcx.hbrBackground);
            bb.putInt((int) lpMenuName);
            bb.putInt((int) lpClassName);
            bb.putInt((int) wcx.hIconSm);
        }
        boolean res = NativeHelper.call(library, "RegisterClassExW", ptr) != 0;
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
        bb.putInt((int) m.wParam);
        bb.putInt((int) m.lParam);
        bb.putInt(m.time);
        bb.putInt(m.pt.x);
        bb.putInt(m.pt.y);
    }

    public static class POINT
    {
        public int x;
        public int y;
    }

    public static class MSG
    {
        public static final int SIZE = is64 ? 48 : 28;
        long hWnd;
        int message;
        long wParam;
        long lParam;
        int time;
        POINT pt;
    }

    public static class WNDCLASSEX
    {
        public static final int SIZE = is64 ? 80 : 48;
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

    public static void decode(long ptr, WINDOWINFO wi) {
        ByteBuffer bb = NativeHelper.getBuffer(ptr, WINDOWINFO.SIZE);
        wi.cbSize = bb.getInt();
        wi.rcWindow = new RECT();
        wi.rcWindow.left = bb.getInt();
        wi.rcWindow.top = bb.getInt();
        wi.rcWindow.right = bb.getInt();
        wi.rcWindow.bottom = bb.getInt();
        wi.rcClient = new RECT();
        wi.rcClient.left = bb.getInt();
        wi.rcClient.top = bb.getInt();
        wi.rcClient.right = bb.getInt();
        wi.rcClient.bottom = bb.getInt();
        wi.dwStyle = bb.getInt();
        wi.dwExStyle = bb.getInt();
        wi.dwWindowStatus = bb.getInt();
        wi.cxWindowBorders = bb.getInt();
        wi.cyWindowBorders = bb.getInt();
        wi.intWindowType = bb.getShort();
        wi.wCreatorVersion = bb.getShort();
    }

    public static class RECT
    {
        public static final int SIZE = 16;
        public int left;
        public int top;
        public int right;
        public int bottom;
    }

    public static class WINDOWINFO
    {
        public static final int SIZE = 60;
        public int cbSize;
        public RECT rcWindow;
        public RECT rcClient;
        public int dwStyle;
        public int dwExStyle;
        public int dwWindowStatus;
        public int cxWindowBorders;
        public int cyWindowBorders;
        public short intWindowType;
        public short wCreatorVersion;
    }
}
