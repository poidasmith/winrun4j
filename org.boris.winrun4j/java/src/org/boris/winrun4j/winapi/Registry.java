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

import java.nio.ByteBuffer;

import org.boris.winrun4j.Native;
import org.boris.winrun4j.NativeHelper;

public class Registry
{
    private static final long library = Native.loadLibrary("advapi32");

    // Value Types
    public static final int REG_NONE = 0;
    public static final int REG_SZ = 1;
    public static final int REG_EXPAND_SZ = 2;
    public static final int REG_BINARY = 3;
    public static final int REG_DWORD = 4;
    public static final int REG_DWORD_LITTLE_ENDIAN = 4;
    public static final int REG_DWORD_BIG_ENDIAN = 5;
    public static final int REG_LINK = 6;
    public static final int REG_MULTI_SZ = 7;
    public static final int REG_RESOURCE_LIST = 8;
    public static final int REG_FULL_RESOURCE_DESCRIPTOR = 9;
    public static final int REG_RESOURCE_REQUIREMENTS_LIST = 10;
    public static final int REG_QWORD = 11;
    public static final int REG_QWORD_LITTLE_ENDIAN = 11;

    public static long RegCloseKey(long key) {
        return NativeHelper.call(library, "RegCloseKey", key);
    }

    public static long RegConnectRegistry(String machineName, long key) {
        long ptr = Native.malloc(4);
        long str = NativeHelper.toNativeString(machineName, true);
        NativeHelper.call(library, "ConnectRegistry", str, key, ptr);
        long h = NativeHelper.getInt(ptr);
        NativeHelper.free(ptr, str);
        return h;
    }

    public static long RegCreateKey(long key, String subKey) {
        long ptr = Native.malloc(4);
        long str = NativeHelper.toNativeString(subKey, true);
        NativeHelper.call(library, "RegCreateKeyW", key, str, ptr);
        long h = NativeHelper.getInt(ptr);
        NativeHelper.free(ptr, str);
        return h;
    }

    public static long RegDeleteKey(long hKey, String subKey) {
        long lpSubKey = NativeHelper.toNativeString(subKey, true);
        long res = NativeHelper.call(library, "RegDeleteKeyW", hKey, lpSubKey);
        NativeHelper.free(lpSubKey);
        return res;
    }

    public static long RegDeleteValue(long hKey, String valueName) {
        long lpValueName = NativeHelper.toNativeString(valueName, true);
        long res = NativeHelper.call(library, "RegDeleteValueW", hKey, lpValueName);
        NativeHelper.free(lpValueName);
        return res;
    }

    public static long RegOpenKeyEx(long hKey, String subKey, int options, long samDesired) {
        long phKey = Native.malloc(4);
        long lpSubKey = NativeHelper.toNativeString(subKey, true);
        long res = NativeHelper.call(library, "RegOpenKeyExW", hKey, lpSubKey, options, samDesired, phKey);
        long key = res == 0 ? NativeHelper.getInt(phKey) : 0;
        NativeHelper.free(phKey, lpSubKey);
        return key;
    }

    public static String RegEnumKeyEx(long hKey, int index) {
        long lpName = Native.malloc(520);
        long lpcName = Native.malloc(4);
        NativeHelper.setInt(lpcName, 520);
        long res = NativeHelper.call(library, "RegEnumKeyExW", hKey, index, lpName, lpcName, 0, 0, 0, 0);
        String key = null;
        if (res == 0) {
            key = NativeHelper.getString(lpName, 520, true);
        }
        NativeHelper.free(lpName, lpcName);
        return key;
    }

    public static String RegEnumValue(long hKey, int index) {
        long lpValueName = Native.malloc(520);
        long lpcchValueName = Native.malloc(4);
        NativeHelper.setInt(lpcchValueName, 520);
        long res = NativeHelper.call(library, "RegEnumValueW", hKey, index, lpValueName, lpcchValueName, 0, 0, 0, 0);
        String val = null;
        if (res == 0) {
            val = NativeHelper.getString(lpValueName, 520, true);
        }
        NativeHelper.free(lpValueName, lpcchValueName);
        return val;
    }

    public static byte[] RegQueryValueEx(long hKey, String valueName) {
        long lpValueName = NativeHelper.toNativeString(valueName, true);
        long lpcbData = Native.malloc(4);
        long res = NativeHelper.call(library, "RegQueryValueExW", hKey, lpValueName, 0, 0, 0, lpcbData);
        byte[] b = null;
        if (res == 0) {
            long lpData = Native.malloc(NativeHelper.getInt(lpcbData));
            res = NativeHelper.call(library, "RegQueryValueExW", hKey, lpValueName, 0, 0, lpData, lpcbData);
            if (res == 0) {
                b = new byte[NativeHelper.getInt(lpcbData)];
                ByteBuffer bb = NativeHelper.getBuffer(lpData, b.length);
                bb.get(b);
            }
            NativeHelper.free(lpData);
        }
        NativeHelper.free(lpValueName, lpcbData);
        return b;
    }

    public static long RegQueryValueType(long hKey, String valueName) {
        long lpValueName = NativeHelper.toNativeString(valueName, true);
        long lpType = Native.malloc(4);
        long res = NativeHelper.call(library, "RegQueryValueExW", hKey, lpValueName, 0, lpType, 0, 0);
        long type = 0;
        if (res == 0) {
            type = NativeHelper.getInt(lpType);
        }
        NativeHelper.free(lpValueName, lpType);
        return type;
    }

    public static long RegSetValueEx(long hKey, String valueName, int type, byte[] data, int offset, int len) {
        long lpValueName = NativeHelper.toNativeString(valueName, true);
        long lpData = NativeHelper.toNative(data, offset, len);
        long res = NativeHelper.call(library, "RegSetValueExW", hKey, lpValueName, 0, type, lpData, len);
        NativeHelper.free(lpValueName, lpData);
        return res;
    }

    public static QUERY_INFO RegQueryInfoKey(long hKey) {
        long lpClass = Native.malloc(520);
        long lpcClass = Native.malloc(4);
        NativeHelper.setInt(lpcClass, 520);
        long lpcSubKeys = Native.malloc(4);
        long lpcMaxSubKeyLen = Native.malloc(4);
        long lpcMaxClassLen = Native.malloc(4);
        long lpcValues = Native.malloc(4);
        long lpcMaxValueNameLen = Native.malloc(4);
        long lpcMaxValueLen = Native.malloc(4);
        long lpftLastWriteTime = Native.malloc(8);

        long res = NativeHelper.call(library, "RegQueryInfoKeyW", new long[] { hKey, lpClass, lpcClass, 0, lpcSubKeys,
                lpcMaxSubKeyLen, lpcMaxClassLen, lpcValues, lpcMaxValueNameLen, lpcMaxValueLen, 0, lpftLastWriteTime });

        QUERY_INFO info = null;
        if (res == 0) {
            info = new QUERY_INFO();
            info.keyClass = NativeHelper.getString(lpClass, 512, true);
            info.subKeyCount = NativeHelper.getInt(lpcSubKeys);
            info.maxSubKeyLen = NativeHelper.getInt(lpcMaxSubKeyLen);
            info.valueCount = NativeHelper.getInt(lpcValues);
            info.maxValueNameLen = NativeHelper.getInt(lpcMaxValueNameLen);
            info.maxValueLen = NativeHelper.getInt(lpcMaxValueLen);
            info.fileTime = new FILETIME();
            info.fileTime.dwLowDateTime = NativeHelper.getInt(lpftLastWriteTime);
            info.fileTime.dwHighDateTime = NativeHelper.getInt(lpftLastWriteTime + 4);
        }

        NativeHelper.free(new long[] { lpClass, lpcClass, lpcSubKeys, lpcMaxSubKeyLen, lpcMaxClassLen, lpcValues,
                lpcMaxValueNameLen, lpcMaxValueLen, lpftLastWriteTime });

        return info;
    }

    public static class QUERY_INFO
    {
        public String keyClass;
        public int subKeyCount;
        public int maxSubKeyLen;
        public int valueCount;
        public int maxValueNameLen;
        public int maxValueLen;
        public FILETIME fileTime;
    }

    public static class FILETIME
    {
        public int dwLowDateTime;
        public int dwHighDateTime;
    }
}
