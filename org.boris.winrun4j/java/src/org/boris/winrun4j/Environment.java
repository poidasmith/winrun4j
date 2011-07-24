package org.boris.winrun4j;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Properties;

import org.boris.winrun4j.Native;
import org.boris.winrun4j.NativeHelper;
import org.boris.winrun4j.PInvoke;
import org.boris.winrun4j.PInvoke.DllImport;
import org.boris.winrun4j.PInvoke.MarshalAs;
import org.boris.winrun4j.PInvoke.Struct;
import org.boris.winrun4j.PInvoke.UIntPtr;

public class Environment
{
    static {
        PInvoke.bind(Environment.class, "kernel32.dll");
    }

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
        StringBuilder sb = new StringBuilder();
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

    public static String getEnv(String name) {
        StringBuilder sb = new StringBuilder();
        UIntPtr ptr = new UIntPtr(4096);
        int res = GetEnvironmentVariable(name, sb, ptr);
        if (res > 0)
            return sb.toString();
        else
            return null;
    }

    @DllImport("kernel32.dll")
    public static native int GetEnvironmentVariable(String lpName, StringBuilder lpBuffer, UIntPtr nsize);

    public static File[] getLogicalDrives() {
        int len = 1024;
        long buf = Native.malloc(len);
        long res = NativeHelper.call(library, "GetLogicalDriveStringsW", len, buf);
        ByteBuffer bb = NativeHelper.getBuffer(buf, (int) (res + 1) << 1);
        ArrayList drives = new ArrayList();
        StringBuilder sb = new StringBuilder();
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

    public static class OSVERSIONINFOEX implements Struct
    {
        public int sizeOf;
        public int majorVersion;
        public int minorVersion;
        public int buildNumber;
        public int platformId;
        @MarshalAs(sizeConst = 128)
        public String csdVersion;
        public short servicePackMajor;
        public short servicePackMinor;
        public short suiteMask;
        public byte productType;
        public byte reserved;
    }
}
