package org.boris.winrun4j.test;

import java.nio.ByteBuffer;
import java.util.Properties;

import org.boris.winrun4j.Callback;
import org.boris.winrun4j.DDEML;
import org.boris.winrun4j.INI;
import org.boris.winrun4j.Kernel32;
import org.boris.winrun4j.Log;
import org.boris.winrun4j.Native;
import org.boris.winrun4j.NativeHelper;
import org.boris.winrun4j.Pointer;
import org.boris.winrun4j.Shell32;

public class SingleInstance
{
    private static final long psapi = Native.loadLibrary("psapi");
    private static final long user32 = Native.loadLibrary("user32");

    public static void main(String[] args) throws Exception {
    }

    public static boolean check(Properties ini) throws Exception {
        String singleInstance = ini.getProperty(INI.SINGLE_INSTANCE);
        if (singleInstance == null)
            return false;

        Callback enumWindowsProc = new Callback() {
            protected int callback(int stack) {
                return EnumWindowsProc(NativeHelper.getInt(stack + 8), NativeHelper.getInt(stack + 12));
            }
        };
        long procId = GetCurrentProcessId();
        String moduleFile = GetModuleFilename(0);
        System.out.println(moduleFile + " " + procId);
        long handle = CreateToolhelp32Snapshot(2, 0);
        long lppe = Native.malloc(PROCESSENTRY32.SIZE);
        NativeHelper.setInt(lppe, PROCESSENTRY32.SIZE);
        PROCESSENTRY32 pe = new PROCESSENTRY32();
        if (Process32First(handle, lppe)) {
            decode(lppe, pe);
            long hProcess = OpenProcess(0x410, false, pe.th32ProcessID);
            String otherModule = GetModuleFilenameEx(hProcess, 0);
            CloseHandle(hProcess);
            if (procId != pe.th32ProcessID && moduleFile.equals(otherModule)) {
                System.out.println("Found other process");
                Native.free(lppe);
                return true;
            }
            EnumWindows(enumWindowsProc, pe.th32ProcessID);
            while (Process32Next(handle, lppe)) {
                decode(lppe, pe);
                hProcess = OpenProcess(0x410, false, pe.th32ProcessID);
                otherModule = GetModuleFilenameEx(hProcess, 0);
                CloseHandle(hProcess);
                if (procId != pe.th32ProcessID && moduleFile.equals(otherModule)) {
                    System.out.println("Found other process");
                    Native.free(lppe);
                    return true;
                }
                EnumWindows(enumWindowsProc, pe.th32ProcessID);
            }
        }
        Native.free(lppe);
        return false;
    }

    public static boolean NotifySingleInstance(Properties ini) {
        Pointer pidInst = new Pointer();
        if (DdeInitialize(pidInst, null, 0, 0) != 0) {
            Log.warning("DDE failed to initialize");
        }

        String appName = ini.getProperty(INI.DDE_SERVER_NAME);
        String topic = ini.getProperty(INI.DDE_TOPIC);
        long hServer = DdeCreateStringHandle(pidInst.ptr, appName == null ? "WinRun4J" : appName, DDEML.CP_WINANSI);
        long hTopic = DdeCreateStringHandle(pidInst.ptr, topic == null ? "system" : topic, DDEML.CP_WINANSI);
        long conv = DdeConnect(pidInst.ptr, hServer, hTopic, 0);
        if (conv != 0) {
            byte[] b = "WinRun4J.ACTIVATE".getBytes();
            long res = DdeClientTransaction(b, b.length, conv, 0, 0, 0x4050, -1);
            if (res == 0) {
                Log.error("Failed to send DDE single instance notification");
                return false;
            }
        } else {
            Log.error("Unable to create DDE conversation");
            return false;
        }

        return true;
    }

    public static long DdeInitialize(Pointer pidInst, Callback callback, int afCmd, int ulRes) {
        long ptr = Native.malloc(4);
        long res = NativeHelper.call(user32, "DdeInitialize", ptr, callback == null ? 0 : callback.getPointer(), afCmd,
                ulRes);
        pidInst.ptr = NativeHelper.getInt(ptr);
        Native.free(ptr);
        return res;
    }

    public static boolean DdeUninitialize(long handle) {
        return NativeHelper.call(user32, "DdeUninitialize", handle) != 0;
    }

    public static long DdeCreateStringHandle(long pidInst, String str, int codePage) {
        long ptr = NativeHelper.toNativeString(str, codePage == DDEML.CP_WINUNICODE);
        long res = NativeHelper.call(user32, "DdeCreateStringHandle", pidInst, ptr, codePage);
        Native.free(ptr);
        return res;
    }

    public static long DdeConnect(long pidInst, long server, long topic, long context) {
        return NativeHelper.call(user32, "DdeConnect", pidInst, server, topic, context);
    }

    public static long DdeClientTransaction(byte[] data, int len, long conv, long hszItem, int fmt, int type,
            int timeout) {
        long ptr = Native.malloc(len);
        ByteBuffer bb = Native.fromPointer(ptr, len);
        bb.put(data, 0, len);
        long res = NativeHelper.call(user32, "DdeClientTransaction", ptr, len, conv, hszItem, fmt, type, timeout, 0);
        Native.free(ptr);
        return res;
    }

    public static WINDOWINFO GetWindowInfo(long hwnd) {
        long ptr = Native.malloc(WINDOWINFO.SIZE);
        NativeHelper.setInt(ptr, WINDOWINFO.SIZE);
        boolean res = NativeHelper.call(user32, "GetWindowInfo", hwnd, ptr) != 0;
        WINDOWINFO wi = null;
        if (res) {
            wi = new WINDOWINFO();
            decode(ptr, wi);
        }
        Native.free(ptr);
        return wi;
    }

    public static int EnumWindowsProc(long hwnd, long lParam) {
        long procId = GetWindowThreadProcessId(hwnd);
        if (lParam == procId) {
            WINDOWINFO wi = GetWindowInfo(hwnd);
            if (wi != null && (wi.dwStyle & 0x10000000) != 0) {
                SetForegroundWindow(hwnd);
                Log.warning("Single Instance Shutdown");
            }
        }
        return 0;
    }

    public static void SetForegroundWindow(long hwnd) {
        NativeHelper.call(user32, "SetForegroundWindow", hwnd);
    }

    public static long GetCurrentProcessId() {
        return NativeHelper.call(Kernel32.library, "GetCurrentProcessId");
    }

    public static int GetWindowThreadProcessId(long hwnd) {
        long ptr = Native.malloc(4);
        NativeHelper.call(user32, "GetWindowThreadProcessId", hwnd, ptr);
        int res = NativeHelper.getInt(ptr);
        Native.free(ptr);
        return res;
    }

    public static String GetModuleFilename(long hModule) {
        long ptr = Native.malloc(Shell32.MAX_PATHW);
        NativeHelper.call(Kernel32.library, "GetModuleFileNameW", hModule, ptr, Shell32.MAX_PATHW);
        String res = NativeHelper.getString(ptr, Shell32.MAX_PATHW, true);
        Native.free(ptr);
        return res;
    }

    public static String GetModuleFilenameEx(long hProcess, int hModule) {
        long ptr = Native.malloc(Shell32.MAX_PATHW);
        NativeHelper.call(psapi, "GetModuleFileNameExW", hProcess, hModule, ptr, Shell32.MAX_PATHW);
        String res = NativeHelper.getString(ptr, Shell32.MAX_PATHW, true);
        Native.free(ptr);
        return res;
    }

    public static void CloseHandle(long handle) {
        NativeHelper.call(Kernel32.library, "CloseHandle", handle);
    }

    public static boolean EnumWindows(Callback proc, int lParam) {
        return NativeHelper.call(user32, "EnumWindows", proc.getPointer(), lParam) != 0;
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
