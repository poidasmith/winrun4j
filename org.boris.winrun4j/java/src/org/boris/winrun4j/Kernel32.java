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
    static long kernel32 = Native.loadLibrary("kernel32");
    static long procGetLogicalDrive = Native.getProcAddress(kernel32, "GetLogicalDriveStringsA");
    static long procGetEnvVar = Native.getProcAddress(kernel32, "GetEnvironmentVariableA");
    static long procGetEnvStrings = Native.getProcAddress(kernel32, "GetEnvironmentStringsA");
    static long procFreeEnvStrings = Native.getProcAddress(kernel32, "FreeEnvironmentStringsA");
    static long procExpandEnvStrings = Native.getProcAddress(kernel32, "ExpandEnvironmentStringsA");
    static long procGetCommandLine = Native.getProcAddress(kernel32, "GetCommandLineA");
    static long procGetTickCount = Native.getProcAddress(kernel32, "GetTickCount");
    static long procDebugBreak = Native.getProcAddress(kernel32, "DebugBreak");
    static long procGetCurrentProcessId = Native.getProcAddress(kernel32, "GetCurrentProcessId");
    static long procGetVersionEx = Native.getProcAddress(kernel32, "GetVersionExA");
    static long procGetLastError = Native.getProcAddress(kernel32, "GetLastError");

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

    public static OSVersionInfo getVersionEx() {
        long pOs = Native.malloc(156);
        ByteBuffer b = Native.fromPointer(pOs, 156);
        b = b.order(ByteOrder.LITTLE_ENDIAN);
        NativeHelper.zeroMemory(b);
        b.rewind();
        b.putInt(156); // set dwOSVersionInfoSize;
        long res = NativeHelper.call(procGetVersionEx, pOs);
        if (res != 0) {
            int maj = b.getInt();
            int min = b.getInt();
            int build = b.getInt();
            int platform = b.getInt();
            byte[] vs = new byte[128];
            b.get(vs);
            String ver = NativeHelper.toString(vs);
            int serveMaj = b.getShort();
            int serveMin = b.getShort();
            int suite = b.getShort();
            int prod = b.get();
            int reserved = b.get();
            Native.free(pOs);
            return new OSVersionInfo(maj, min, build, platform, serveMaj, serveMin, suite, prod,
                    reserved, ver);
        }
        Native.free(pOs);
        System.out.println(getLastError());
        return null;
    }

    public static long getLastError() {
        return NativeHelper.call(procGetLastError);
    }

    public static long getTickCount() {
        return NativeHelper.call(procGetTickCount);
    }

    public static long getCurrentProcessId() {
        return NativeHelper.call(procGetCurrentProcessId);
    }

    public static void debugBreak() {
        NativeHelper.call(procDebugBreak);
    }
}
