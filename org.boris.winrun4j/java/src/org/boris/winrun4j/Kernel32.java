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
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Properties;

public class Kernel32
{
    public static final long library = Native.loadLibrary("kernel32");
    public static final long procDebugBreak = Native.getProcAddress(library, "DebugBreak");
    public static final long procExpandEnvStrings = Native.getProcAddress(library, "ExpandEnvironmentStringsA");
    public static final long procFreeEnvStrings = Native.getProcAddress(library, "FreeEnvironmentStringsA");
    public static final long procGetCommandLine = Native.getProcAddress(library, "GetCommandLineA");
    public static final long procGetCurrentProcessId = Native.getProcAddress(library, "GetCurrentProcessId");
    public static final long procGetCurrentThreadId = Native.getProcAddress(library, "GetCurrentThreadId");
    public static final long procGetEnvStrings = Native.getProcAddress(library, "GetEnvironmentStringsA");
    public static final long procGetEnvVar = Native.getProcAddress(library, "GetEnvironmentVariableA");
    public static final long procGetLastError = Native.getProcAddress(library, "GetLastError");
    public static final long procGetLogicalDrive = Native.getProcAddress(library, "GetLogicalDriveStringsA");
    public static final long procGetTickCount = Native.getProcAddress(library, "GetTickCount");
    public static final long procGetVersionEx = Native.getProcAddress(library, "GetVersionExA");
    public static final long procGlobalAlloc = Native.getProcAddress(library, "GlobalAlloc");
    public static final long procGlobalFree = Native.getProcAddress(library, "GlobalFree");
    public static final long procOpenScManager = Native.getProcAddress(library, "OpenSCManager");

    public static void DebugBreak() {
        NativeHelper.call(procDebugBreak);
    }

    public static String ExpandEnvironmentString(String var) {
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

    public static String[] GetCommandLine() {
        long res = NativeHelper.call(procGetCommandLine);
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

    public static long GetCurrentProcessId() {
        return NativeHelper.call(procGetCurrentProcessId);
    }

    public static long GetCurrentThreadId() {
        return NativeHelper.call(procGetCurrentThreadId);
    }

    public static String GetEnvironmentVariable(String var) {
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

    public static Properties GetEnvironmentVariables() {
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

    public static long GetLastError() {
        return NativeHelper.call(procGetLastError);
    }

    public static File[] GetLogicalDrives() {
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

    public static long GetTickCount() {
        return NativeHelper.call(procGetTickCount);
    }

    public static OSVERSIONINFOEX GetVersionEx() {
        long pOs = Native.malloc(156);
        ByteBuffer b = Native.fromPointer(pOs, 156).order(ByteOrder.LITTLE_ENDIAN);
        NativeHelper.zeroMemory(b);
        b.rewind();
        b.putInt(156); // set dwOSVersionInfoSize;
        long res = NativeHelper.call(procGetVersionEx, pOs);
        if (res == 0) {
            Native.free(pOs);
            return null;
        }

        OSVERSIONINFOEX info = new OSVERSIONINFOEX();
        info.majorVersion = b.getInt();
        info.minorVersion = b.getInt();
        info.buildNumber = b.getInt();
        info.platformId = b.getInt();
        byte[] vs = new byte[128];
        b.get(vs);
        info.csdVersion = NativeHelper.toString(vs);
        info.servicePackMajor = b.getShort();
        info.servicePackMinor = b.getShort();
        info.suiteMask = b.getShort();
        info.productType = b.get();
        info.reserved = b.get();
        Native.free(pOs);
        return info;
    }

    public static class OSVERSIONINFOEX
    {
        public int buildNumber;
        public String csdVersion;
        public int majorVersion;
        public int minorVersion;
        public int platformId;
        public int productType;
        public int reserved;
        public int servicePackMajor;
        public int servicePackMinor;
        public int suiteMask;
    }
}
