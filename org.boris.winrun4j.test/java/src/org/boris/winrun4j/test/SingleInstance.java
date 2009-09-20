package org.boris.winrun4j.test;

import org.boris.winrun4j.Kernel32;
import org.boris.winrun4j.Native;
import org.boris.winrun4j.NativeHelper;
import org.boris.winrun4j.Shell32;

public class SingleInstance
{
    public static void main(String[] args) throws Exception {
        long procId = GetCurrentProcessId();
        String moduleFile = GetModuleFilename(0);
        System.out.println(moduleFile + " " + procId);
    }

    public static long GetCurrentProcessId() {
        return NativeHelper.call(Kernel32.library, "GetCurrentProcessId");
    }

    public static String GetModuleFilename(long hModule) {
        long ptr = Native.malloc(Shell32.MAX_PATHW);
        NativeHelper.call(Kernel32.library, "GetModuleFileNameW", hModule, ptr,
                Shell32.MAX_PATHW);
        String res = NativeHelper.getString(ptr, Shell32.MAX_PATHW, true);
        Native.free(ptr);
        return res;
    }

    public static long CreateToolhelp32Snapshot(int dwFlags, long th32ProcessID) {
        return NativeHelper.call(Kernel32.library, "CreateToolhelp32Snapshot",
                dwFlags, th32ProcessID);
    }

    public boolean Process32First(long hSnapshot, long lppe) {
        return false;
    }

    public boolean Process32Next(long hSnapshot, long lppe) {
        return false;
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
        public int szExeFile;
    }
}
