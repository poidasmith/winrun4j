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

public class DirectoryManagement
{
    public static final int MOVEFILE_REPLACE_EXISTING = 0x00000001;
    public static final int MOVEFILE_COPY_ALLOWED = 0x00000002;
    public static final int MOVEFILE_DELAY_UNTIL_REBOOT = 0x00000004;
    public static final int MOVEFILE_WRITE_THROUGH = 0x00000008;
    public static final int MOVEFILE_CREATE_HARDLINK = 0x00000010;
    public static final int MOVEFILE_FAIL_IF_NOT_TRACKABLE = 0x00000020;

    private static long procMoveFileEx = Native.getProcAddress(Kernel32.library, "MoveFileExW");

    public static boolean MoveFileEx(String existingName, String newName, int flags) {
        if (existingName == null || newName == null)
            throw new NullPointerException();
        long e = NativeHelper.toNativeString(existingName, true);
        long n = NativeHelper.toNativeString(newName, true);
        boolean res = NativeHelper.call(procMoveFileEx, e, n, flags) == 1;
        Native.free(e);
        Native.free(n);
        return res;
    }
}
