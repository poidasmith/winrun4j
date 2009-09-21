package org.boris.winrun4j.test;

import java.nio.ByteBuffer;

import org.boris.winrun4j.Callback;
import org.boris.winrun4j.Kernel32;
import org.boris.winrun4j.Native;
import org.boris.winrun4j.NativeHelper;
import org.boris.winrun4j.Shell32;

public class SingleInstance
{
    private static final long psapi = Native.loadLibrary("psapi");

    public static void main(String[] args) throws Exception {
        long procId = GetCurrentProcessId();
        String moduleFile = GetModuleFilename(0);
        System.out.println(moduleFile + " " + procId);
        long handle = CreateToolhelp32Snapshot(2, 0);
        long lppe = Native.malloc(556);
        NativeHelper.setInt(lppe, 556);
        PROCESSENTRY32 pe = new PROCESSENTRY32();
        if (Process32First(handle, lppe)) {
            decode(lppe, pe);
            long hProcess = OpenProcess(0x410, false, pe.th32ProcessID);
            String otherModule = GetModuleFilenameEx(hProcess, 0);
            CloseHandle(hProcess);
            if (procId != pe.th32ProcessID && moduleFile.equals(otherModule)) {
                System.out.println("Found other process");
                return;
            }
            while (Process32Next(handle, lppe)) {
                decode(lppe, pe);
                hProcess = OpenProcess(0x410, false, pe.th32ProcessID);
                otherModule = GetModuleFilenameEx(hProcess, 0);
                CloseHandle(hProcess);
                if (procId != pe.th32ProcessID && moduleFile.equals(otherModule)) {
                    System.out.println("Found other process");
                    return;
                }
            }
        }
        Native.free(lppe);
    }

    public static long GetCurrentProcessId() {
        return NativeHelper.call(Kernel32.library, "GetCurrentProcessId");
    }

    public static long GetWindowThreadProcessId(long hwnd) {
        long ptr = Native.malloc(4);
        // NativeHelper.call(

        return 0;
    }

    public static String GetModuleFilename(long hModule) {
        long ptr = Native.malloc(Shell32.MAX_PATHW);
        NativeHelper.call(Kernel32.library, "GetModuleFileNameW", hModule, ptr, Shell32.MAX_PATHW);
        String res = NativeHelper.getString(ptr, Shell32.MAX_PATHW, true);
        Native.free(ptr);
        return res;
    }

    public static String GetModuleFilenameEx(long hProcess, long hModule) {
        long ptr = Native.malloc(Shell32.MAX_PATHW);
        NativeHelper.call(psapi, "GetModuleFileNameExW", hProcess, hModule, ptr, Shell32.MAX_PATHW);
        String res = NativeHelper.getString(ptr, Shell32.MAX_PATHW, true);
        Native.free(ptr);
        return res;
    }

    public static void CloseHandle(long handle) {
        NativeHelper.call(Kernel32.library, "CloseHandle", handle);
    }

    public static boolean EnumWindows(Callback proc, long lParam) {
        return NativeHelper.call(Kernel32.library, "EnumWindows", proc.getPointer(), lParam) != 0;
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

    public static long OpenProcess(int dwDesiredAccess, boolean bInheritHandle, int dwProcessId) {
        return NativeHelper.call(Kernel32.library, "OpenProcess", dwDesiredAccess, bInheritHandle ? 1 : 0, dwProcessId);
    }

    public static void decode(long ptr, PROCESSENTRY32 pe) {
        ByteBuffer bb = NativeHelper.getBuffer(ptr, 556);
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
