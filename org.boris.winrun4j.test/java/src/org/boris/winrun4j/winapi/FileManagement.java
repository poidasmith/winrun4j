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

import org.boris.winrun4j.Closure;
import org.boris.winrun4j.PInvoke;
import org.boris.winrun4j.PInvoke.Callback;
import org.boris.winrun4j.PInvoke.DllImport;
import org.boris.winrun4j.PInvoke.IntPtr;
import org.boris.winrun4j.PInvoke.MarshalAs;
import org.boris.winrun4j.PInvoke.Struct;

public class FileManagement
{
    static {
        PInvoke.bind(FileManagement.class, "kernel32");
    }

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

    @DllImport
    public static native long CreateFile(String lpFileName, int dwDesiredAccess,
            int dwShareMode, long lpSecurityAttributes,
            int dwCreationDisposition, int dwFlagsAndAttributes, long hTemplateFile);

    @DllImport
    public static native boolean MoveFileEx(String existingName, String newName, int flags);

    @DllImport
    public static native int GetCurrentDirectory(int nBufferLength, StringBuilder lpBuffer);

    @DllImport
    public static native long GetFileSize(long hFile, IntPtr lpFileSizeHigh);

    @DllImport
    public static native long FindFirstFile(String fileName, WIN32_FIND_DATA findFileData);

    @DllImport
    public static native long FindFirstChangeNotification(String pathName, boolean bWatchSubtree, int dwNotifyFilter);

    @DllImport
    public static native boolean FindNextChangeNotification(long hChangeHandle);

    @DllImport
    public static native boolean FindCloseChangeNotification(long hChangeHandle);

    @DllImport
    public static native boolean FindNextFile(long handle, WIN32_FIND_DATA findFileData);

    @DllImport
    public static native boolean FindClose(long handle);

    @DllImport
    public static native boolean ReadDirectoryChanges(long hDirectory, long lpBuffer, int dwBufferLength,
            boolean bWatchTree,
            int dwNotifyFilter, long lpOverlapped, Closure completionRoutine);

    @DllImport
    public static native boolean SetCurrentDirectory(String pathName);

    public interface FileNotifyInformationCallback extends Callback
    {
        int fileIoCompletionRoutine(int dwErrorCode, int dwNumberOfBytesTransferred,
                long lpOverlapped);
    }

    public static class WIN32_FIND_DATA implements Struct
    {
        public int dwFileAttributes;
        public FILETIME ftCreationTime;
        public FILETIME ftLastAccessTime;
        public FILETIME ftLastWriteTime;
        public int nFileSizeHigh;
        public int nFileSizeLow;
        public int dwReserved0;
        public int dwReserved1;
        @MarshalAs(sizeConst = 256)
        public String cFileName;
        @MarshalAs(sizeConst = 256)
        public String cAlternateFileName;
    }

    public static class FILETIME implements Struct
    {
        public int dwLowDateTime;
        public int dwHighDateTime;
    }

    public static class FILE_NOTIFY_INFORMATION implements Struct
    {
        public int action;
        @MarshalAs(sizeConst = 256)
        public String filename;
    }

    public static class OVERLAPPED implements Struct
    {
        public int internal;
        public int internalHigh;
        public int offset;
        public int offstHigh;
    }
}
