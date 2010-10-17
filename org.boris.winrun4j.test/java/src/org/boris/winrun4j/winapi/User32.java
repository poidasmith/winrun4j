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

import org.boris.winrun4j.Closure;
import org.boris.winrun4j.PInvoke;
import org.boris.winrun4j.PInvoke.Callback;
import org.boris.winrun4j.PInvoke.DllImport;
import org.boris.winrun4j.PInvoke.Struct;

public class User32
{
    static {
        PInvoke.bind(User32.class, "user32.dll");
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

    @DllImport
    public static native long CreateWindowEx(int dwExStyle, String className, String windowName, int dwStyle, int x,
            int y,
            int width, int height, long parent, long menu, long hInstance, long lParam);

    @DllImport
    public static native void DestroyWindow(long hWnd);

    @DllImport
    public static native long LoadCursor(long hInstance, long lpCursorName);

    @DllImport
    public static native void SetForegroundWindow(long hwnd);

    @DllImport
    public static native boolean ShowWindow(long hWnd, int nCmdShow);

    @DllImport
    public static native boolean UpdateWindow(long hWnd);

    @DllImport
    public static native long GetWindowThreadProcessId(long hwnd);

    @DllImport
    public static native boolean EnumWindows(Closure proc, int lParam);

    @DllImport
    public static native int getWindowInfo(long hwnd, WINDOWINFO info);

    @DllImport
    public static native int DefWindowProc(long hWnd, int uMsg, long wParam, long lParam);

    public interface WindowProc extends Callback
    {
        public int windowProc(long hWnd, int uMsg, long wParam, long lParam);
    }

    @DllImport
    public static native boolean GetMessage(MSG m, long hWnd, int wMsgFilterMin, int wMsgFilterMax);

    @DllImport
    public static native boolean TranslateMessage(MSG m);

    @DllImport
    public static native boolean DispatchMessage(MSG m);

    @DllImport
    public static native boolean registerClassEx(WNDCLASSEX wcx);

    public static class POINT implements Struct
    {
        public int x;
        public int y;
    }

    public static class MSG implements Struct
    {
        long hWnd;
        int message;
        long wParam;
        long lParam;
        int time;
        POINT pt;
    }

    public static class WNDCLASSEX implements Struct
    {
        public int style;
        public Closure lpfnWndProc;
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

    public static class RECT implements Struct
    {
        public int left;
        public int top;
        public int right;
        public int bottom;
    }

    public static class WINDOWINFO implements Struct
    {
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
