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

    public static final int DONT_RESOLVE_DLL_REFERENCES = 0x00000001;
    public static final int LOAD_LIBRARY_AS_DATAFILE = 0x00000002;
    public static final int LOAD_WITH_ALTERED_SEARCH_PATH = 0x00000008;
    public static final int LOAD_IGNORE_CODE_AUTHZ_LEVEL = 0x00000010;

    public static void debugBreak() {
        NativeHelper.call(library, "DebugBreak");
    }

    public static long loadLibraryEx(String filename, int dwFlags) {
        long lpFilename = NativeHelper.toNativeString(filename, true);
        long handle = NativeHelper.call(library, "LoadLibraryExW", lpFilename, 0, dwFlags);
        NativeHelper.free(lpFilename);
        return handle;
    }

    public static void setPriorityClass(long hProcess, int dwPriorityClass) {
        NativeHelper.call(library, "SetPriorityClass", hProcess, dwPriorityClass);
    }

    public static long getCurrentProcessId() {
        return NativeHelper.call(library, "GetCurrentProcessId");
    }

    public static long getCurrentThreadId() {
        return NativeHelper.call(library, "GetCurrentThreadId");
    }

    public static long getModuleHandle(String moduleName) {
        long lpModuleName = NativeHelper.toNativeString(moduleName, true);
        long res = NativeHelper.call(library, "GetModuleHandleW", lpModuleName);
        NativeHelper.free(lpModuleName);
        return res;
    }

    public static long getLastError() {
        return NativeHelper.call(library, "GetLastError");
    }

    public static long getTickCount() {
        return NativeHelper.call(library, "GetTickCount");
    }

    public static String getModuleFilename(long hModule) {
        long ptr = Native.malloc(Shell32.MAX_PATHW);
        NativeHelper.call(Kernel32.library, "GetModuleFileNameW", hModule, ptr, Shell32.MAX_PATHW);
        String res = NativeHelper.getString(ptr, Shell32.MAX_PATHW, true);
        Native.free(ptr);
        return res;
    }

    public static void closeHandle(long handle) {
        NativeHelper.call(Kernel32.library, "CloseHandle", handle);
    }

    public static long createToolhelp32Snapshot(int dwFlags, long th32ProcessID) {
        return NativeHelper.call(Kernel32.library, "CreateToolhelp32Snapshot", dwFlags, th32ProcessID);
    }

    public static boolean process32First(long hSnapshot, long lppe) {
        return NativeHelper.call(Kernel32.library, "Process32FirstW", hSnapshot, lppe) != 0;
    }

    public static boolean process32Next(long hSnapshot, long lppe) {
        return NativeHelper.call(Kernel32.library, "Process32NextW", hSnapshot, lppe) != 0;
    }

    public static long openProcess(int dwDesiredAccess, boolean bInheritHandle, long dwProcessId) {
        return NativeHelper.call(Kernel32.library, "OpenProcess", dwDesiredAccess, bInheritHandle ? 1 : 0, dwProcessId);
    }

    public static long waitForSingleObject(long handle, int milliseconds) {
        return NativeHelper.call(library, "WaitForSingleObject", handle, milliseconds);
    }

    public static long waitForSingleObjectEx(long hHandle, int dwMilliseconds, boolean bAlertable) {
        return NativeHelper.call(library, "WaitForSingleObjectEx", hHandle, dwMilliseconds, bAlertable ? 1 : 0);
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
