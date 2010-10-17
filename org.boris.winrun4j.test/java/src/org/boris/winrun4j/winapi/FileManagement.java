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
import java.util.ArrayList;

import org.boris.winrun4j.Closure;
import org.boris.winrun4j.Native;
import org.boris.winrun4j.NativeHelper;
import org.boris.winrun4j.PInvoke.Callback;

public class FileManagement
{
    private static final long library = Native.loadLibrary("kernel32.dll");

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

    public static long createFile(String fileName, int dwDesiredAccess, int dwShareMode, long lpSecurityAttributes,
            int dwCreationDisposition, int dwFlagsAndAttributes, long hTemplateFile) {
        long lpFileName = NativeHelper.toNativeString(fileName, true);
        long handle = NativeHelper.call(library, "CreateFileW", lpFileName, dwDesiredAccess, dwShareMode,
                lpSecurityAttributes, dwCreationDisposition, dwFlagsAndAttributes, hTemplateFile);
        NativeHelper.free(lpFileName);
        return handle;
    }

    public static boolean moveFileEx(String existingName, String newName, int flags) {
        if (existingName == null || newName == null)
            throw new NullPointerException();
        long e = NativeHelper.toNativeString(existingName, true);
        long n = NativeHelper.toNativeString(newName, true);
        boolean res = NativeHelper.call(library, "MoveFileExW", e, n, flags) == 1;
        NativeHelper.free(e, n);
        return res;
    }

    public static String getCurrentDirectory() {
        long lpBuffer = Native.malloc(Shell32.MAX_PATHW);
        int count = (int) NativeHelper.call(library, "GetCurrentDirectoryW", Shell32.MAX_PATHW, lpBuffer);
        String res = null;
        if (count > 0)
            res = NativeHelper.getString(lpBuffer, Shell32.MAX_PATHW, true);
        NativeHelper.free(lpBuffer);
        return res;
    }

    public static long getFileSize(long hFile) {
        long lpFileSizeHigh = Native.malloc(4);
        long size = NativeHelper.call(library, "GetFileSize", hFile, lpFileSizeHigh);
        int fileSizeHigh = NativeHelper.getInt(lpFileSizeHigh);
        NativeHelper.free(lpFileSizeHigh);
        if (size == 0xffffffff && Kernel32.GetLastError() != 0)
            return -1;
        return ((long) fileSizeHigh) << 32 | size;
    }

    public static long findFirstFile(String fileName, WIN32_FIND_DATA findFileData) {
        if (findFileData == null)
            return 0;
        long lpFileName = NativeHelper.toNativeString(fileName, true);
        long lpFindFileData = Native.malloc(WIN32_FIND_DATA.SIZE);
        long handle = NativeHelper.call(library, "FindFirstFileW", lpFileName, lpFindFileData);
        if (handle != 0) {
            decode(lpFindFileData, findFileData);
        }
        NativeHelper.free(lpFileName, lpFindFileData);
        return handle;
    }

    public static long findFirstChangeNotification(String pathName, boolean bWatchSubtree, int dwNotifyFilter) {
        if (pathName == null)
            return 0;
        long lpPathName = NativeHelper.toNativeString(pathName, true);
        long handle = NativeHelper.call(library, "FindFirstChangeNotificationW", lpPathName, bWatchSubtree ? 1
                : 0, dwNotifyFilter);
        NativeHelper.free(lpPathName);
        return handle;
    }

    public static boolean findNextChangeNotification(long hChangeHandle) {
        return NativeHelper.call(library, "FindNextChangeNotification", hChangeHandle) != 0;
    }

    public static boolean findCloseChangeNotification(long hChangeHandle) {
        return NativeHelper.call(library, "FindCloseChangeNotification", hChangeHandle) != 0;
    }

    public static boolean findNextFile(long handle, WIN32_FIND_DATA findFileData) {
        long lpFindFileData = Native.malloc(WIN32_FIND_DATA.SIZE);
        boolean res = NativeHelper.call(library, "FindNextFileW", handle, lpFindFileData) != 0;
        if (res) {
            decode(lpFindFileData, findFileData);
        }
        NativeHelper.free(lpFindFileData);
        return res;
    }

    public static boolean findClose(long handle) {
        return NativeHelper.call(library, "FindClose", handle) != 0;
    }

    public static boolean readDirectoryChanges(long hDirectory, long lpBuffer, int dwBufferLength, boolean bWatchTree,
            int dwNotifyFilter, long lpOverlapped, Closure completionRoutine) {
        return NativeHelper.call(library, "ReadDirectoryChangesW", hDirectory, lpBuffer, dwBufferLength,
                bWatchTree ? 1 : 0, dwNotifyFilter, 0, lpOverlapped, completionRoutine.getPointer()) != 0;
    }

    public static boolean setCurrentDirectory(String pathName) {
        long lpPathName = NativeHelper.toNativeString(pathName, true);
        boolean res = NativeHelper.call(library, "SetCurrentDirectoryW", lpPathName) != 0;
        NativeHelper.free(lpPathName);
        return res;
    }

    private static void decode(long ptr, WIN32_FIND_DATA findFileData) {
        ByteBuffer bb = NativeHelper.getBuffer(ptr, WIN32_FIND_DATA.SIZE);
        findFileData.dwFileAttributes = bb.getInt();
        findFileData.ftCreationTime = decodeFileTime(bb);
        findFileData.ftLastAccessTime = decodeFileTime(bb);
        findFileData.ftLastWriteTime = decodeFileTime(bb);
        findFileData.nFileSizeHigh = bb.getInt();
        findFileData.nFileSizeLow = bb.getInt();
        findFileData.dwReserved0 = bb.getInt();
        findFileData.dwReserved1 = bb.getInt();
        byte[] cbfn = new byte[Shell32.MAX_PATHW];
        bb.get(cbfn);
        findFileData.cFileName = NativeHelper.getString(cbfn, true);
        byte[] cbaf = new byte[28];
        bb.get(cbaf);
        findFileData.cAlternateFileName = NativeHelper.getString(cbaf, true);
        if (findFileData.cAlternateFileName != null && findFileData.cAlternateFileName.length() == 0)
            findFileData.cAlternateFileName = null;
    }

    private static FILETIME decodeFileTime(ByteBuffer bb) {
        FILETIME res = new FILETIME();
        res.dwLowDateTime = bb.getInt();
        res.dwHighDateTime = bb.getInt();
        return res;
    }

    public static FILE_NOTIFY_INFORMATION[] decodeFileNotifyInformation(ByteBuffer bb) {
        ArrayList results = new ArrayList();
        int offset = 0;
        while ((offset = bb.getInt()) != 0) {
            FILE_NOTIFY_INFORMATION fni = new FILE_NOTIFY_INFORMATION();
            fni.action = bb.getInt();
            int len = bb.getInt();
            byte[] b = new byte[len];
            bb.get(b);
            fni.filename = NativeHelper.getString(b, true);
            bb.position(offset);
            results.add(fni);
        }
        return (FILE_NOTIFY_INFORMATION[]) results.toArray(new FILE_NOTIFY_INFORMATION[results.size()]);
    }

    public static abstract class FileNotifyInformationCallback implements Callback
    {
        protected final int callback(int stack) {
            ByteBuffer bb = NativeHelper.getBuffer(stack, 12);
            return fileIoCompletionRoutine(bb.getInt(), bb.getInt(), bb.getInt());
        }

        protected abstract int fileIoCompletionRoutine(int dwErrorCode, int dwNumberOfBytesTransferred,
                long lpOverlapped);
    }

    public static class WIN32_FIND_DATA
    {
        public static final int SIZE = 592;
        public int dwFileAttributes;
        public FILETIME ftCreationTime;
        public FILETIME ftLastAccessTime;
        public FILETIME ftLastWriteTime;
        public int nFileSizeHigh;
        public int nFileSizeLow;
        public int dwReserved0;
        public int dwReserved1;
        public String cFileName;
        public String cAlternateFileName;
    }

    public static class FILETIME
    {
        public int dwLowDateTime;
        public int dwHighDateTime;
    }

    public static class FILE_NOTIFY_INFORMATION
    {
        public int action;
        public String filename;
    }

    public static class OVERLAPPED
    {
        public int internal;
        public int internalHigh;
        public int offset;
        public int offstHigh;
    }
}
