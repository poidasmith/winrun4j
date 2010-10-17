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

import org.boris.winrun4j.PInvoke.DllImport;
import org.boris.winrun4j.PInvoke.IntPtr;
import org.boris.winrun4j.PInvoke.Out;
import org.boris.winrun4j.PInvoke.Struct;
import org.boris.winrun4j.PInvoke.UIntPtr;

public class Registry
{
    static {
        PInvoke.bind(Registry.class, "advapi32.dll");
    }

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

    @DllImport(entryPoint = "RegCloseKey")
    public static native int closeKey(long hKey);

    @DllImport(entryPoint = "RegCreateKeyW")
    public static native int createKey(long hKey, String lpSubKey, @Out UIntPtr phkResult);

    @DllImport(entryPoint = "RegDeleteKeyW")
    public static native long deleteKey(long hKey, String subKey);

    @DllImport(entryPoint = "RegDeleteValueW")
    public static native long deleteValue(long hKey, String valueName);

    @DllImport(entryPoint = "RegOpenKeyExW")
    public static native int openKeyEx(long hKey, String subKey, int options, long samDesired);

    @DllImport(entryPoint = "RegEnumKeyExW")
    public static native int enumKeyEx(
            long hkey,
            int index,
            StringBuilder lpName,
            UIntPtr lpcbName,
            long reserved,
            long lpClass,
            long lpcbClass,
            FILETIME lpftLastWriteTime);

    @DllImport(entryPoint = "RegEnumValue")
    public static native int enumValue(
            long hKey,
            int index,
            StringBuilder lpValueName,
            UIntPtr lpcValueName,
            long lpReserved,
            UIntPtr lpType,
            byte[] lpData,
            IntPtr lpcbData);

    @DllImport(entryPoint = "RegQueryValueEx")
    public static native int queryValueEx(long hKey, String valueName, int valueType);

    @DllImport(entryPoint = "RegQueryValueEx")
    public static native long queryValueType(long hKey, String valueName);

    @DllImport(entryPoint = "RegQueryInfoKey")
    public static native int queryInfoKey(
            long hKey,
            StringBuilder lpClass,
            UIntPtr lpcClass,
            long lpReserved,
            UIntPtr lpcSubKeys,
            UIntPtr lpcMaxSubKeyLen,
            UIntPtr lpcMaxClassLen,
            UIntPtr lpcValues,
            UIntPtr lpcMaxValueNameLen,
            UIntPtr lpcMaxValueLen,
            UIntPtr lpcbSecurityDescriptor,
            FILETIME lpftLastWriteTime);

    @DllImport(entryPoint = "RegSetValueEx")
    public static native long setValueEx(long hKey, String valueName, int type, byte[] data, int offset, int len);

    public static class FILETIME implements Struct
    {
        public int dwLowDateTime;
        public int dwHighDateTime;
    }
}
