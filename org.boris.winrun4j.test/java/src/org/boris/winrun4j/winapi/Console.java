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

import org.boris.winrun4j.PInvoke;
import org.boris.winrun4j.PInvoke.Callback;
import org.boris.winrun4j.PInvoke.DllImport;
import org.boris.winrun4j.PInvoke.Struct;
import org.boris.winrun4j.PInvoke.UIntPtr;

public class Console
{
    static {
        PInvoke.bind(Console.class, "kernel32.dll");
    }

    public static final int CTRL_C_EVENT = 0;
    public static final int CTRL_BREAK_EVENT = 1;
    public static final int CTRL_CLOSE_EVENT = 2;
    public static final int CTRL_LOGOFF_EVENT = 5;
    public static final int CTRL_SHUTDOWN_EVENT = 6;

    @DllImport
    public static native boolean AllocConsole();

    @DllImport
    public static native boolean GetConsoleMode(long hConsole, UIntPtr lpMode);

    @DllImport
    public static native long GetStdHandle(int nStdHandle);

    public interface HandlerRoutine extends Callback
    {
        boolean handlerRoutine(int dwCtrlType);
    }

    @DllImport
    public static native boolean SetConsoleCtrlHandler(long handler, boolean add);

    @DllImport
    public static native boolean SetConsoleWindowInfo(long hConsoleOutput, boolean bAbsolute, SMALL_RECT rect);

    @DllImport
    public static native boolean SetConsoleTitle(String consoleTitle);

    @DllImport
    public static native boolean SetConsoleTextAttribute(long hConsole, int wAttributes);

    @DllImport
    public static native boolean SetConsoleMode(long hConsole, int dwMode);

    @DllImport
    public static native boolean SetStdHandle(long nStdHandle, long hHandle);

    public static int WriteConsole(long hConsoleOutput, String buffer) {
        return WriteConsole(hConsoleOutput, buffer, buffer == null ? 0 : buffer.length(), null, null);
    }

    @DllImport
    public static native int WriteConsole(long hConsoleOutput, String buffer, int nNumberOfCharsToWrite,
            UIntPtr lpNumberOfCharsWritten, UIntPtr lpReserved);

    public static class SMALL_RECT implements Struct
    {
        public int Left;
        public int Top;
        public int Right;
        public int Bottom;
    }
}
