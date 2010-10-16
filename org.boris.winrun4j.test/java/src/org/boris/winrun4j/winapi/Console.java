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

import org.boris.winrun4j.Native;
import org.boris.winrun4j.NativeHelper;
import org.boris.winrun4j.PInvoke.Callback;
import org.boris.winrun4j.PInvoke.Delegate;

public class Console
{
    public static final long library = Kernel32.library;

    public static final int CTRL_C_EVENT = 0;
    public static final int CTRL_BREAK_EVENT = 1;
    public static final int CTRL_CLOSE_EVENT = 2;
    public static final int CTRL_LOGOFF_EVENT = 5;
    public static final int CTRL_SHUTDOWN_EVENT = 6;

    public static boolean allocConsole() {
        return NativeHelper.call(library, "AllocConsole") != 0;
    }

    public static int getConsoleMode(long hConsole) {
        long lpMode = Native.malloc(4);
        boolean res = NativeHelper.call(library, "GetConsoleMode", hConsole, lpMode) != 0;
        int mode = NativeHelper.getInt(lpMode);
        NativeHelper.free(lpMode);
        return res ? mode : 0;
    }

    public static long getStdHandle(long nStdHandle) {
        return NativeHelper.call(library, "GetStdHandle", nStdHandle);
    }

    public static boolean setConsoleCtrlHandler(Delegate callback, boolean add) {
        long handler = callback == null ? 0 : callback.getPointer();
        return NativeHelper.call(library, "SetConsoleCtrlHandler", handler, add ? 1 : 0) != 0;
    }

    public static boolean setConsoleWindowInfo(long hConsoleOutput, boolean bAbsolute, SMALL_RECT rect) {
        if (rect == null)
            return false;
        long lpConsoleWindow = Native.malloc(8);
        ByteBuffer bb = NativeHelper.getBuffer(lpConsoleWindow, 8);
        bb.putShort((short) rect.Left);
        bb.putShort((short) rect.Top);
        bb.putShort((short) rect.Right);
        bb.putShort((short) rect.Bottom);
        boolean res = NativeHelper.call(library, "SetConsoleWindowInfo", hConsoleOutput, bAbsolute ? 1 : 0,
                lpConsoleWindow) != 0;
        NativeHelper.free(lpConsoleWindow);
        return res;
    }

    public static boolean setConsoleTitle(String consoleTitle) {
        long lpConsoleTitle = NativeHelper.toNativeString(consoleTitle, true);
        boolean res = NativeHelper.call(library, "SetConsoleTitleW", lpConsoleTitle) != 0;
        NativeHelper.free(lpConsoleTitle);
        return res;
    }

    public static boolean setConsoleTextAttribute(long hConsole, int wAttributes) {
        return NativeHelper.call(library, "SetConsoleTextAttribute", hConsole, wAttributes) != 0;
    }

    public static boolean setConsoleMode(long hConsole, int dwMode) {
        return NativeHelper.call(library, "SetConsoleMode", hConsole, dwMode) != 0;
    }

    public static boolean setStdHandle(long nStdHandle, long hHandle) {
        return NativeHelper.call(library, "SetStdHandle", nStdHandle, hHandle) != 0;
    }

    public static int writeConsole(long hConsoleOutput, String buffer) {
        long lpBuffer = NativeHelper.toNativeString(buffer, true);
        long lpNumberOfCharsWritten = Native.malloc(4);
        boolean res = NativeHelper.call(library, "WriteConsoleW", hConsoleOutput, lpBuffer, buffer.length(),
                lpNumberOfCharsWritten, 0) != 0;
        int numChars = NativeHelper.getInt(lpNumberOfCharsWritten);
        NativeHelper.free(lpBuffer, lpNumberOfCharsWritten);
        return res ? numChars : 0;
    }

    public static abstract class HandlerRoutine extends Callback
    {
        @Delegate
        public abstract boolean handlerRoutine(int dwCtrlType);
    }

    public static class SMALL_RECT
    {
        public int Left;
        public int Top;
        public int Right;
        public int Bottom;
    }
}
