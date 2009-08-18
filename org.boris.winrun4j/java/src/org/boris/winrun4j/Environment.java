/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Peter Smith
 *******************************************************************************/
package org.boris.winrun4j;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Properties;

public class Environment
{
    private static long kernel32 = Native.loadLibrary("kernel32");
    private static long shell32 = Native.loadLibrary("shell32");
    private static long procGetLogicalDrive = Native.getProcAddress(kernel32,
            "GetLogicalDriveStringsA");
    private static long procGetFolderPath = Native.getProcAddress(shell32, "SHGetFolderPathA");
    private static long procGetEnvVar = Native.getProcAddress(kernel32, "GetEnvironmentVariableA");
    private static long procGetEnvStrings = Native.getProcAddress(kernel32,
            "GetEnvironmentStringsA");
    private static long procFreeEnvStrings = Native.getProcAddress(kernel32,
            "FreeEnvironmentStringsA");
    private static long procExpandEnvStrings = Native.getProcAddress(kernel32,
            "ExpandEnvironmentStringsA");
    private static long procGetCommandLine = Native.getProcAddress(kernel32, "GetCommandLineA");
    private static long procGetTickCount = Native.getProcAddress(kernel32, "GetTickCount");
    private static long procDebugBreak = Native.getProcAddress(kernel32, "DebugBreak");
    private static long getCurrentProcId = Native.getProcAddress(kernel32, "GetCurrentProcessId");

    public static File[] getLogicalDrives() {
        int len = 1024;
        long buf = Native.malloc(len);
        long res = NativeHelper.call(procGetLogicalDrive, len, buf);
        ByteBuffer bb = Native.fromPointer(buf, res + 1);
        ArrayList drives = new ArrayList();
        StringBuffer sb = new StringBuffer();
        while (true) {
            char c = (char) bb.get();
            if (c == 0) {
                if (sb.length() == 0) {
                    break;
                } else {
                    drives.add(new File(sb.toString()));
                    sb.setLength(0);
                }
            } else {
                sb.append(c);
            }
        }
        Native.free(buf);
        return (File[]) drives.toArray(new File[drives.size()]);
    }

    public static File getFolderPath(int type) {
        long buf = Native.malloc(Native.MAX_PATH);
        NativeHelper.call(procGetFolderPath, 0, type, 0, 0, buf);
        String res = NativeHelper.getString(buf, Native.MAX_PATH, false);
        Native.free(buf);
        if (res == null || res.length() == 0)
            return null;
        return new File(res);
    }

    public static String getEnvironmentVariable(String var) {
        if (var == null)
            return null;
        long buf = NativeHelper.toNativeString(var, false);
        long rbuf = Native.malloc(4096);
        long res = NativeHelper.call(procGetEnvVar, buf, rbuf, 4096);
        if (res == 0)
            return null;
        if (res > 4096)
            return null;
        String str = NativeHelper.getString(rbuf, 4096, false);
        Native.free(buf);
        Native.free(rbuf);
        return str;
    }

    public static Properties getEnvironmentVariables() {
        long buf = NativeHelper.call(procGetEnvStrings);
        ByteBuffer bb = Native.fromPointer(buf, 32767);
        Properties p = new Properties();
        while (true) {
            String s = NativeHelper.getString(bb, false);
            if (s == null || s.length() == 0)
                break;
            int idx = s.indexOf('=');
            p.put(s.substring(0, idx), s.substring(idx + 1));
        }
        NativeHelper.call(procFreeEnvStrings, buf);
        return p;
    }

    public static String expandEnvironmentString(String var) {
        if (var == null)
            return null;
        long str = NativeHelper.toNativeString(var, false);
        long buf = Native.malloc(4096);
        long res = NativeHelper.call(procExpandEnvStrings, str, buf, 4096);
        String rs = null;
        if (res > 0 && res <= 4096) {
            rs = NativeHelper.getString(buf, 4096, false);
        }
        Native.free(str);
        Native.free(buf);
        return rs;
    }

    public static String[] getCommandLineArgs() {
        long res = Native.call(procGetCommandLine, null, 0);
        String s = NativeHelper.getString(res, 1024, false);
        boolean inQuote = false;
        ArrayList args = new ArrayList();
        StringBuffer sb = new StringBuffer();
        int len = s.length();
        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            if (c == '"') {
                inQuote = !inQuote;
            }
            if (c == ' ') {
                if (inQuote) {
                    sb.append(c);
                } else {
                    args.add(sb.toString());
                    sb.setLength(0);
                }
            } else {
                sb.append(c);
            }
        }
        if (sb.length() > 0)
            args.add(sb.toString());

        return (String[]) args.toArray(new String[args.size()]);
    }

    public static OSVersionInfo getVersionInfo() {
        return null;
    }

    public static long getTickCount() {
        return NativeHelper.call(procGetTickCount);
    }

    public static long getCurrentProcessId() {
        return NativeHelper.call(getCurrentProcId);
    }

    public static void debugBreak() {
        NativeHelper.call(procDebugBreak);
    }
}
