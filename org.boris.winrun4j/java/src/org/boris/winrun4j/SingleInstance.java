package org.boris.winrun4j;

import java.util.Properties;

import org.boris.winrun4j.winapi.DDEML;
import org.boris.winrun4j.winapi.Kernel32;
import org.boris.winrun4j.winapi.PSAPI;
import org.boris.winrun4j.winapi.Pointer;
import org.boris.winrun4j.winapi.User32;
import org.boris.winrun4j.winapi.Kernel32.PROCESSENTRY32;
import org.boris.winrun4j.winapi.User32.WINDOWINFO;

public class SingleInstance
{
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
        long procId = Kernel32.GetCurrentProcessId();
        String moduleFile = Kernel32.GetModuleFilename(0);
        System.out.println(moduleFile + " " + procId);
        long handle = Kernel32.CreateToolhelp32Snapshot(2, 0);
        long lppe = Native.malloc(Kernel32.PROCESSENTRY32.SIZE);
        NativeHelper.setInt(lppe, Kernel32.PROCESSENTRY32.SIZE);
        PROCESSENTRY32 pe = new PROCESSENTRY32();
        if (Kernel32.Process32First(handle, lppe)) {
            Kernel32.decode(lppe, pe);
            long hProcess = Kernel32.OpenProcess(0x410, false, pe.th32ProcessID);
            String otherModule = PSAPI.GetModuleFilenameEx(hProcess, 0);
            Kernel32.CloseHandle(hProcess);
            if (procId != pe.th32ProcessID && moduleFile.equals(otherModule)) {
                System.out.println("Found other process");
                Native.free(lppe);
                return true;
            }
            User32.EnumWindows(enumWindowsProc, pe.th32ProcessID);
            while (Kernel32.Process32Next(handle, lppe)) {
                Kernel32.decode(lppe, pe);
                hProcess = Kernel32.OpenProcess(0x410, false, pe.th32ProcessID);
                otherModule = PSAPI.GetModuleFilenameEx(hProcess, 0);
                Kernel32.CloseHandle(hProcess);
                if (procId != pe.th32ProcessID && moduleFile.equals(otherModule)) {
                    System.out.println("Found other process");
                    Native.free(lppe);
                    return true;
                }
                User32.EnumWindows(enumWindowsProc, pe.th32ProcessID);
            }
        }
        Native.free(lppe);
        return false;
    }

    public static boolean NotifySingleInstance(Properties ini) {
        Pointer pidInst = new Pointer();
        if (DDEML.DdeInitialize(pidInst, null, 0, 0) != 0) {
            Log.warning("DDE failed to initialize");
        }

        String appName = ini.getProperty(INI.DDE_SERVER_NAME);
        String topic = ini.getProperty(INI.DDE_TOPIC);
        long hServer = DDEML.DdeCreateStringHandle(pidInst.ptr, appName == null ? "WinRun4J" : appName,
                DDEML.CP_WINANSI);
        long hTopic = DDEML.DdeCreateStringHandle(pidInst.ptr, topic == null ? "system" : topic, DDEML.CP_WINANSI);
        long conv = DDEML.DdeConnect(pidInst.ptr, hServer, hTopic, 0);
        if (conv != 0) {
            byte[] b = "WinRun4J.ACTIVATE".getBytes();
            long res = DDEML.DdeClientTransaction(b, b.length, conv, 0, 0, 0x4050, -1);
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

    public static int EnumWindowsProc(long hwnd, long lParam) {
        long procId = User32.GetWindowThreadProcessId(hwnd);
        if (lParam == procId) {
            WINDOWINFO wi = User32.GetWindowInfo(hwnd);
            if (wi != null && (wi.dwStyle & 0x10000000) != 0) {
                User32.SetForegroundWindow(hwnd);
                Log.warning("Single Instance Shutdown");
            }
        }
        return 0;
    }
}
