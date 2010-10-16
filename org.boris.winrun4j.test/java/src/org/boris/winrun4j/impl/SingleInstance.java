package org.boris.winrun4j.impl;

import java.util.Properties;

import org.boris.winrun4j.Delegate;
import org.boris.winrun4j.INI;
import org.boris.winrun4j.Log;
import org.boris.winrun4j.Native;
import org.boris.winrun4j.NativeHelper;
import org.boris.winrun4j.winapi.DDEML;
import org.boris.winrun4j.winapi.Kernel32;
import org.boris.winrun4j.winapi.PSAPI;
import org.boris.winrun4j.winapi.User32;
import org.boris.winrun4j.winapi.Kernel32.PROCESSENTRY32;
import org.boris.winrun4j.winapi.User32.WINDOWINFO;

public class SingleInstance extends DDEML.DdeCallback
{
    public static boolean check(Properties ini) throws Exception {
        String singleInstance = ini.getProperty(INI.SINGLE_INSTANCE);
        if (singleInstance == null)
            return false;

        Delegate enumWindowsProc = new Delegate() {
            protected long callback(long stack) {
                return enumWindowsProc(NativeHelper.getInt(stack), NativeHelper.getInt(stack + 4));
            }
        };
        long procId = Kernel32.getCurrentProcessId();
        String moduleFile = Kernel32.getModuleFilename(0);
        System.out.println(moduleFile + " " + procId);
        long handle = Kernel32.createToolhelp32Snapshot(2, 0);
        long lppe = Native.malloc(Kernel32.PROCESSENTRY32.SIZE);
        NativeHelper.setInt(lppe, Kernel32.PROCESSENTRY32.SIZE);
        PROCESSENTRY32 pe = new PROCESSENTRY32();
        if (Kernel32.process32First(handle, lppe)) {
            Kernel32.decode(lppe, pe);
            long hProcess = Kernel32.openProcess(0x410, false, pe.th32ProcessID);
            String otherModule = PSAPI.getModuleFilenameEx(hProcess, 0);
            Kernel32.closeHandle(hProcess);
            if (procId != pe.th32ProcessID && moduleFile.equals(otherModule)) {
                Log.info("Found other process");
                Native.free(lppe);
                return true;
            }
            User32.enumWindows(enumWindowsProc, pe.th32ProcessID);
            while (Kernel32.process32Next(handle, lppe)) {
                Kernel32.decode(lppe, pe);
                hProcess = Kernel32.openProcess(0x410, false, pe.th32ProcessID);
                otherModule = PSAPI.getModuleFilenameEx(hProcess, 0);
                Kernel32.closeHandle(hProcess);
                if (procId != pe.th32ProcessID && moduleFile.equals(otherModule)) {
                    Log.info("Found other process");
                    Native.free(lppe);
                    return true;
                }
                User32.enumWindows(enumWindowsProc, pe.th32ProcessID);
            }
        }
        Native.free(lppe);
        return false;
    }

    public static boolean notify(String appName, String topic) {
        Delegate cb = new SingleInstance();
        long pidInst = DDEML.initialize(cb, 0);
        if (pidInst == 0) {
            Log.warning("DDE failed to initialize");
        }

        long hServer = DDEML.createStringHandle(pidInst, appName, DDEML.CP_WINUNICODE);
        long hTopic = DDEML.createStringHandle(pidInst, topic, DDEML.CP_WINUNICODE);
        long conv = DDEML.connect(pidInst, hServer, hTopic, 0);
        if (conv == 0) {
            Log.error("Unable to create DDE conversation");
            cb.dispose();
            return false;
        }

        byte[] b = NativeHelper.toBytes("WinRun4J.ACTIVATE", true);
        long res = DDEML.clientTransaction(b, b.length, conv, 0, 0, 0x4050, -1);
        if (res == 0) {
            Log.error("Failed to send DDE single instance notification");
            cb.dispose();
            return false;
        }

        DDEML.uninitialize(pidInst);
        cb.dispose();

        return true;
    }

    public static boolean notify(Properties ini) {
        return notify(ini.getProperty(INI.DDE_SERVER_NAME), ini.getProperty(INI.DDE_TOPIC));
    }

    private static int enumWindowsProc(long hwnd, long lParam) {
        long procId = User32.getWindowThreadProcessId(hwnd);
        if (lParam == procId) {
            WINDOWINFO wi = User32.getWindowInfo(hwnd);
            if (wi != null && (wi.dwStyle & 0x10000000) != 0) {
                User32.setForegroundWindow(hwnd);
                Log.warning("Single Instance Shutdown");
            }
        }
        return 0;
    }

    public long ddeCallback(int type, int fmt, long conv, long hsz1, long hsz2, long data, int data1, int data2) {
        System.out.println(type);
        return 0;
    }
}
