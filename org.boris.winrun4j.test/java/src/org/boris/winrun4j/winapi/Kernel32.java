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
import org.boris.winrun4j.PInvoke.DllImport;
import org.boris.winrun4j.PInvoke.MarshalAs;
import org.boris.winrun4j.PInvoke.Struct;

public class Kernel32
{
    static {
        PInvoke.bind(Kernel32.class, "kernel32.dll");
    }

    public static final int DONT_RESOLVE_DLL_REFERENCES = 0x00000001;
    public static final int LOAD_LIBRARY_AS_DATAFILE = 0x00000002;
    public static final int LOAD_WITH_ALTERED_SEARCH_PATH = 0x00000008;
    public static final int LOAD_IGNORE_CODE_AUTHZ_LEVEL = 0x00000010;

    @DllImport
    public static native void DebugBreak();

    @DllImport
    public static native long LoadLibraryEx(String lpFilename, long hFile, int dwFlags);

    @DllImport
    public static native void SetPriorityClass(long hProcess, int dwPriorityClass);

    @DllImport
    public static native long GetCurrentProcessId();

    @DllImport
    public static native long GetCurrentThreadId();

    @DllImport
    public static native long GetModuleHandle(String moduleName);

    @DllImport
    public static native long GetLastError();

    @DllImport
    public static native long GetTickCount();

    @DllImport
    public static native long GetModuleFilename(long hModule, StringBuilder lpFilename, int nSize);

    @DllImport
    public static native void CloseHandle(long handle);

    @DllImport
    public static native long CreateToolhelp32Snapshot(int dwFlags, long th32ProcessID);

    @DllImport
    public static native boolean Process32First(long hSnapshot, PROCESSENTRY32 lppe);

    @DllImport
    public static native boolean Process32Next(long hSnapshot, PROCESSENTRY32 lppe);

    @DllImport
    public static native long OpenProcess(int dwDesiredAccess, boolean bInheritHandle, long dwProcessId);

    @DllImport
    public static native long WaitForSingleObject(long handle, int milliseconds);

    @DllImport
    public static native long WaitForSingleObjectEx(long hHandle, int dwMilliseconds, boolean bAlertable);

    @DllImport
    public static native void GetSystemInfo(SYSTEM_INFO si);

    public static class PROCESSENTRY32 implements Struct
    {
        private static int sizeOf = PInvoke.sizeOf(PROCESSENTRY32.class);

        public PROCESSENTRY32() {
            dwSize = sizeOf;
        }

        public int dwSize;
        public int cntUsage;
        public int th32ProcessID;
        public long th32DefaultHeapID;
        public int th32ModuleID;
        public int cntThreads;
        public int th32ParentProcessID;
        public int pcPriClassBase;
        public int dwFlags;
        @MarshalAs(sizeConst = 128)
        public String szExeFile;
    }

    public static class SYSTEM_INFO implements Struct
    {
        public int dwOemId;
        // TODO: union
        // public int wProcessorAchitecture;
        // public int wReserved;
        public int dwPageSize;
        public long lpMinimumApplicationAddress;
        public long lpMaximumApplicationAddress;
        public long dwActiveProcessorMask;
        public int dwNumberOfProcessor;
        public int dwProcessorType;
        public int dwAllocationGranularity;
        public short wProcessorLevel;
        public short wProcessorRevision;
    }
}
