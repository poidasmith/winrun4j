package org.boris.winrun4j.winapi;

import org.boris.winrun4j.Native;
import org.boris.winrun4j.NativeHelper;

public class PSAPI
{
    public static final long library = Native.loadLibrary("psapi");

    public static String getModuleFilenameEx(long hProcess, int hModule) {
        long ptr = Native.malloc(Shell32.MAX_PATHW);
        NativeHelper.call(library, "GetModuleFileNameExW", hProcess, hModule,
                ptr, Shell32.MAX_PATHW);
        String res = NativeHelper.getString(ptr, Shell32.MAX_PATHW, true);
        Native.free(ptr);
        return res;
    }
}
