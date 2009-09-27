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

import org.boris.winrun4j.Native;
import org.boris.winrun4j.NativeHelper;

public class DirectoryManagement
{
    public static final int MOVEFILE_REPLACE_EXISTING = 0x00000001;
    public static final int MOVEFILE_COPY_ALLOWED = 0x00000002;
    public static final int MOVEFILE_DELAY_UNTIL_REBOOT = 0x00000004;
    public static final int MOVEFILE_WRITE_THROUGH = 0x00000008;
    public static final int MOVEFILE_CREATE_HARDLINK = 0x00000010;
    public static final int MOVEFILE_FAIL_IF_NOT_TRACKABLE = 0x00000020;

    public static final int FILE_ACTION_ADDED = 1;
    public static final int FILE_ACTION_REMOVED = 2;
    public static final int FILE_ACTION_MODIFIED = 3;
    public static final int FILE_ACTION_RENAMED_OLD_NAME = 4;
    public static final int FILE_ACTION_RENAMED_NEW_NAME = 5;

    public static final int FILE_NOTIFY_CHANGE_FILE_NAME = 0x1;
    public static final int FILE_NOTIFY_CHANGE_DIR_NAME = 0x2;
    public static final int FILE_NOTIFY_CHANGE_ATTRIBUTES = 0x4;
    public static final int FILE_NOTIFY_CHANGE_SIZE = 0x8;
    public static final int FILE_NOTIFY_CHANGE_LAST_WRITE = 0x10;
    public static final int FILE_NOTIFY_CHANGE_LAST_ACCESS = 0x20;
    public static final int FILE_NOTIFY_CHANGE_CREATION = 0x40;
    public static final int FILE_NOTIFY_CHANGE_SECURITY = 0x100;

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
