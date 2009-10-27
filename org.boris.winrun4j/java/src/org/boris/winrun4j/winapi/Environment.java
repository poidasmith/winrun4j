package org.boris.winrun4j.winapi;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Properties;

import org.boris.winrun4j.Native;
import org.boris.winrun4j.NativeHelper;

public class Environment
{
    private static final long library = Kernel32.library;

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

    public static OSVERSIONINFOEX getVersionEx() {
        long pOs = Native.malloc(156);
        ByteBuffer b = Native.fromPointer(pOs, 156).order(ByteOrder.LITTLE_ENDIAN);
        NativeHelper.zeroMemory(b);
        b.rewind();
        b.putInt(156); // set dwOSVersionInfoSize;
        long res = NativeHelper.call(library, "GetVersionExA", pOs);
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
        info.csdVersion = NativeHelper.getString(vs, false);
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
