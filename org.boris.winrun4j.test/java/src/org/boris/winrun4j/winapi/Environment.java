package org.boris.winrun4j.winapi;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Properties;

import org.boris.winrun4j.Native;
import org.boris.winrun4j.NativeHelper;
import org.boris.winrun4j.PInvoke.DllImport;
import org.boris.winrun4j.PInvoke.MarshalAs;

public class Environment
{
    private static final long library = Native.loadLibrary("kernel32.dll");

    public static String expandEnvironmentString(String var) {
        if (var == null)
            return null;
        long str = NativeHelper.toNativeString(var, true);
        long buf = Native.malloc(4096);
        long res = NativeHelper.call(library, "ExpandEnvironmentStringsW", str, buf, 4096);
        String rs = null;
        if (res > 0 && res <= 4096) {
            rs = NativeHelper.getString(buf, 4096, true);
        }
        Native.free(str);
        Native.free(buf);
        return rs;
    }

    public static String[] getCommandLine() {
        long res = NativeHelper.call(library, "GetCommandLineW");
        String s = NativeHelper.getString(res, 1024, true);
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

    public static Properties getEnvironmentVariables() {
        long buf = NativeHelper.call(library, "GetEnvironmentStringsW");
        ByteBuffer bb = NativeHelper.getBuffer(buf, 32767);
        Properties p = new Properties();
        while (true) {
            String s = NativeHelper.getString(bb, true);
            if (s == null || s.length() == 0)
                break;
            int idx = s.indexOf('=');
            p.put(s.substring(0, idx), s.substring(idx + 1));
        }
        NativeHelper.call(library, "FreeEnvironmentStringsW", buf);
        return p;
    }

    public static String getEnvironmentVariable(String var) {
        if (var == null)
            return null;
        long buf = NativeHelper.toNativeString(var, true);
        long rbuf = Native.malloc(4096);
        long res = NativeHelper.call(library, "GetEnvironmentVariableW", buf, rbuf, 4096);
        if (res == 0)
            return null;
        if (res > 4096)
            return null;
        String str = NativeHelper.getString(rbuf, 4096, true);
        Native.free(buf);
        Native.free(rbuf);
        return str;
    }

    public static File[] getLogicalDrives() {
        int len = 1024;
        long buf = Native.malloc(len);
        long res = NativeHelper.call(library, "GetLogicalDriveStringsW", len, buf);
        ByteBuffer bb = NativeHelper.getBuffer(buf, (int) (res + 1) << 1);
        ArrayList drives = new ArrayList();
        StringBuffer sb = new StringBuffer();
        while (true) {
            char c = (char) bb.getChar();
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

    @DllImport("kernel32.dll")
    public static native boolean GetVersionEx(OSVERSIONINFOEX version);

    public static class OSVERSIONINFOEX
    {
        public int dwOSVersionInfoSize;
        public int dwMajorVersion;
        public int dwMinorVersion;
        public int dwBuildNumber;
        public int dwPlatformId;
        @MarshalAs(sizeConst = 128)
        public String szCSDVersion;
        public short wServicePackMajor;
        public short wServicePackMinor;
        public short wSuiteMask;
        public byte wProductType;
        public byte wReserved;
    }
}
