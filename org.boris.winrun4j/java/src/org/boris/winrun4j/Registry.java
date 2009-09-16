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

public class Registry
{
    public static final long library = User32.library;
    public static final long procRegCloseKey = Native.getProcAddress(library, "RegCloseKey");
    public static final long procRegConnectRegistry = Native.getProcAddress(library, "RegConnectRegistry");
    public static final long procRegCreateKey = Native.getProcAddress(library, "RegCreateKey");
    public static final long procRegCreateKeyEx = Native.getProcAddress(library, "RegCrateKeyEx");
    public static final long procRegDeleteKey = Native.getProcAddress(library, "RegDeleteKey");
    public static final long procRegDeleteValue = Native.getProcAddress(library, "RegDeleteValue");
    public static final long procRegEnumKey = Native.getProcAddress(library, "RegEnumKey");
    public static final long procRegEnumKeyEx = Native.getProcAddress(library, "RegEnumKeyEx");
    public static final long procRegEnumValue = Native.getProcAddress(library, "RegEnumValue");
    public static final long procRegFlushKey = Native.getProcAddress(library, "RegFlushKey");
    public static final long procRegGetKeySecurity = Native.getProcAddress(library, "RegGetKeySecurity");
    public static final long procRegLoadKey = Native.getProcAddress(library, "RegLoadKey");
    public static final long procRegNotifyChangeKeyValue = Native.getProcAddress(library, "RegNotifyChangeKeyValue");
    public static final long procRegOpenKey = Native.getProcAddress(library, "RegOpenKey");
    public static final long procRegOpenKeyEx = Native.getProcAddress(library, "RegOpenKeyEx");
    public static final long procRegQueryInfoKey = Native.getProcAddress(library, "RegQueryInfoKey");
    public static final long procRegQueryMultipleValues = Native.getProcAddress(library, "RegQueryMultipleValues");
    public static final long procRegQueryValue = Native.getProcAddress(library, "RegQueryValue");
    public static final long procRegQueryValueEx = Native.getProcAddress(library, "RegQueryValueEx");
    public static final long procRegReplaceKey = Native.getProcAddress(library, "RegReplaceKey");
    public static final long procRegRestoreKey = Native.getProcAddress(library, "RegRestoreKey");
    public static final long procRegSaveKey = Native.getProcAddress(library, "RegSaveKey");
    public static final long procRegSetKeySecurity = Native.getProcAddress(library, "RegSetKeySecurity");
    public static final long procRegSetValue = Native.getProcAddress(library, "RegSetValue");
    public static final long procRegSetValueEx = Native.getProcAddress(library, "RegCloseKey");
    public static final long procRegUnLoadKey = Native.getProcAddress(library, "RegUnLoadKey");

    public static long RegCloseKey(long key) {
        return NativeHelper.call(procRegCloseKey, key);
    }

    public static long RegConnectRegistry(String machineName, long key) {
        long ptr = Native.malloc(4);
        long str = NativeHelper.toNativeString(machineName, true);
        NativeHelper.call(procRegConnectRegistry, str, key, ptr);
        long h = NativeHelper.getInt(ptr);
        NativeHelper.free(ptr, str);
        return h;
    }

    public static long RegCreateKey(long key, String subKey) {
        long ptr = Native.malloc(4);
        long str = NativeHelper.toNativeString(subKey, true);
        NativeHelper.call(procRegCreateKey, key, str, ptr);
        long h = NativeHelper.getInt(ptr);
        NativeHelper.free(ptr, str);
        return h;
    }

    public static class SECURITY_ATTRIBUTES
    {
    }
}
