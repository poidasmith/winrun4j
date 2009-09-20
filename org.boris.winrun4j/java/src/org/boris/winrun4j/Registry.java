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
        NativeHelper.call(library, "RegCreateKey", key, str, ptr);
        long h = NativeHelper.getInt(ptr);
        NativeHelper.free(ptr, str);
        return h;
    }

    public static class SECURITY_ATTRIBUTES
    {
    }
}
