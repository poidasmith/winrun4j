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

public class Kernel32
{
    public static final long library = Native.loadLibrary("kernel32");

    public static void DebugBreak() {
        NativeHelper.call(library, "DebugBreak");
    }

    public static void SetPriorityClass(long hProcess, int dwPriorityClass) {
        NativeHelper.call(library, "SetPriorityClass", hProcess, dwPriorityClass);
    }

    public static long GetCurrentProcessId() {
        return NativeHelper.call(library, "GetCurrentProcessId");
    }

    public static long GetCurrentThreadId() {
        return NativeHelper.call(library, "GetCurrentThreadId");
    }

    public static long GetModuleHandle(String moduleName) {
        long lpModuleName = NativeHelper.toNativeString(moduleName, true);
        long res = NativeHelper.call(library, "GetModuleHandleW", lpModuleName);
        NativeHelper.free(lpModuleName);
        return res;
    }

    public static long GetLastError() {
        return NativeHelper.call(library, "GetLastError");
    }

    public static long GetTickCount() {
        return NativeHelper.call(library, "GetTickCount");
    }

    public static String GetModuleFilename(long hModule) {
        long ptr = Native.malloc(Shell32.MAX_PATHW);
        NativeHelper.call(Kernel32.library, "GetModuleFileNameW", hModule, ptr, Shell32.MAX_PATHW);
        String res = NativeHelper.getString(ptr, Shell32.MAX_PATHW, true);
        Native.free(ptr);
        return res;
    }

    public static void CloseHandle(long handle) {
        NativeHelper.call(Kernel32.library, "CloseHandle", handle);
    }

    public static long CreateToolhelp32Snapshot(int dwFlags, long th32ProcessID) {
        return NativeHelper.call(Kernel32.library, "CreateToolhelp32Snapshot", dwFlags, th32ProcessID);
    }

    public static boolean Process32First(long hSnapshot, long lppe) {
        return NativeHelper.call(Kernel32.library, "Process32FirstW", hSnapshot, lppe) != 0;
    }

    public static boolean Process32Next(long hSnapshot, long lppe) {
        return NativeHelper.call(Kernel32.library, "Process32NextW", hSnapshot, lppe) != 0;
    }

    public static long OpenProcess(int dwDesiredAccess, boolean bInheritHandle, long dwProcessId) {
        return NativeHelper.call(Kernel32.library, "OpenProcess", dwDesiredAccess, bInheritHandle ? 1 : 0, dwProcessId);
    }

    public static void decode(long ptr, PROCESSENTRY32 pe) {
        ByteBuffer bb = NativeHelper.getBuffer(ptr, PROCESSENTRY32.SIZE);
        pe.dwSize = bb.getInt();
        pe.cntUsage = bb.getInt();
        pe.th32ProcessID = bb.getInt();
        pe.th32DefaultHeapID = bb.getInt();
        pe.th32ModuleID = bb.getInt();
        pe.cntThreads = bb.getInt();
        pe.th32ParentProcessID = bb.getInt();
        pe.pcPriClassBase = bb.getInt();
        pe.dwFlags = bb.getInt();
        pe.szExeFile = NativeHelper.getString(bb, true);
    }

    public static class PROCESSENTRY32
    {
        public static final int SIZE = 556;
        public int dwSize;
        public int cntUsage;
        public int th32ProcessID;
        public int th32DefaultHeapID;
        public int th32ModuleID;
        public int cntThreads;
        public int th32ParentProcessID;
        public int pcPriClassBase;
        public int dwFlags;
        public String szExeFile;
    }
}
