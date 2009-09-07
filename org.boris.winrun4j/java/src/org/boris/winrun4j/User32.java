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

import java.nio.ByteBuffer;

public class User32
{
    public static final long library = Native.loadLibrary("user32");
    public static final long procDdeAbandonTransaction = Native.getProcAddress(library, "DdeAbandonTransaction");
    public static final long procDdeAccessData = Native.getProcAddress(library, "DdeAccessData");
    public static final long procDdeAddData = Native.getProcAddress(library, "DdeAddData");
    public static final long procDdeCallback = Native.getProcAddress(library, "DdeCallback");
    public static final long procDdeClientTransaction = Native.getProcAddress(library, "DdeClientTransaction");
    public static final long procDdeCmpStringHandles = Native.getProcAddress(library, "DdeCmpStringHandles");
    public static final long procDdeConnect = Native.getProcAddress(library, "DdeConnect");
    public static final long procDdeConnectList = Native.getProcAddress(library, "DdeConnectList");
    public static final long procDdeCreateDataHandle = Native.getProcAddress(library, "DdeCreateDataHandle");
    public static final long procDdeCreateStringHandle = Native.getProcAddress(library, "DdeCreateStringHandle");
    public static final long procDdeDisconnect = Native.getProcAddress(library, "DdeDisconnect");
    public static final long procDdeDisconnectList = Native.getProcAddress(library, "DdeDisconnectList");
    public static final long procDdeEnableCallback = Native.getProcAddress(library, "DdeEnableCallback");
    public static final long procDdeFreeDataHandle = Native.getProcAddress(library, "DdeFreeDataHandle");
    public static final long procDdeGetData = Native.getProcAddress(library, "DdeGetData");
    public static final long procDdeGetLastError = Native.getProcAddress(library, "DdeGetLastError");
    public static final long procDdeImpersonateClient = Native.getProcAddress(library, "DdeImpersonateClient");
    public static final long procDdeIntialize = Native.getProcAddress(library, "DdeIntialize");
    public static final long procDdeKeepStringHandle = Native.getProcAddress(library, "DdeAbandonTransaction");
    public static final long procDdeNameService = Native.getProcAddress(library, "DdeNameService");
    public static final long procDdePostAdvise = Native.getProcAddress(library, "DdePostAdvise");
    public static final long procDdeQueryConvInfo = Native.getProcAddress(library, "DdeQueryConvInfo");
    public static final long procDdeQueryNextServer = Native.getProcAddress(library, "DdeQueryNextServer");
    public static final long procDdeQueryString = Native.getProcAddress(library, "DdeQueryString");
    public static final long procDdeReconnect = Native.getProcAddress(library, "DdeReconnect");
    public static final long procDdeSetUserHandle = Native.getProcAddress(library, "DdeSetUserHandle");
    public static final long procDdeUnaccessData = Native.getProcAddress(library, "DdeUnaccessData");
    public static final long procDdeUninitialize = Native.getProcAddress(library, "DdeUninitialize");

    public static boolean DdeAbandonTransaction(long handle, long conversation, long transaction) {
        return NativeHelper.call(procDdeAbandonTransaction, handle, conversation, transaction) != 0;
    }

    public static ByteBuffer DdeAccessData(long data) {
        long ptr = Native.malloc(4);
        long pb = NativeHelper.call(procDdeAccessData, data, ptr);
        long size = NativeHelper.getInt(ptr);
        Native.free(ptr);
        return pb == 0 ? null : Native.fromPointer(pb, size);
    }

    public static long DdeAddData(long data, byte[] buffer, int len, int offset) {
        return 0;
    }

    public static long DdeCallback(int type, int fmt, long conversation, long hsz1, long hsz2, long hdata,
            long dwData1, long dwData2) {
        return NativeHelper.call(procDdeCallback, new long[] { type, fmt, conversation, hsz1, hsz2, hdata, dwData1,
                dwData2 });
    }

    public static long DdeClientTransaction(byte[] data, int len, long conversation, long hszItem, int fmt, int type,
            int timeout) {
        return 0;
    }

    public static int DdeCmpStringHandles(long hsz1, long hsz2) {
        return (int) NativeHelper.call(procDdeCmpStringHandles, hsz1, hsz2);
    }

    public static long DdeConnect(long idInst, String service, String topic, CONVCONTEXT context) {
        return 0;
    }

    public static long DdeConnectList(long idInst, String service, String topic, long convLst, CONVCONTEXT conext) {
        return 0;
    }

    public static long DdeCreateDataHandle(long idInst, byte[] data, int len, int offset, String name, int format,
            int afCmd) {
        return 0;
    }

    public static long DdeCreateStringHandle(long idInst, String str, int codePage) {
        return 0;
    }

    public static boolean DdeDisconnect(long conversation) {
        return false;
    }

    public static boolean DdeDisconnectList(long convList) {
        return false;
    }

    public static boolean DdeEnableCallback(long idInst, long conv, int cmd) {
        return false;
    }

    public static boolean DdeFreeDataHandle(long data) {
        return false;
    }

    public static boolean DdeFreeStringHandle(long idInst, long hsz) {
        return false;
    }

    public static int DdeGetData(long data, byte[] buffer, int len, int offset) {
        return 0;
    }

    public static long DdeGetLastError(long idInst) {
        return NativeHelper.call(procDdeGetLastError, idInst);
    }

    public static boolean DdeImpersonateClient(long conv) {
        return NativeHelper.call(procDdeImpersonateClient, conv) != 0;
    }

    public static long DdeInitialize(Callback callback, int afCmd) {
        long pid = Native.malloc(4);
        long res = NativeHelper.call(pid, callback.getPointer(), afCmd, 0);
        long pidInst = NativeHelper.getInt(pid);
        Native.free(pid);
        return res == 0 ? 0 : pidInst;
    }

    public static boolean DdeKeepStringHandle(long idInst, long hsz) {
        return NativeHelper.call(procDdeKeepStringHandle, idInst, hsz) != 0;
    }

    public static long DdeNameService(long idInst, long hsz1, long hsz2, int afCmd) {
        return NativeHelper.call(procDdeNameService, idInst, hsz1, hsz2, afCmd);
    }

    public static boolean DdePostAdvise(long idInst, long hszTopic, long hszItem) {
        return NativeHelper.call(procDdePostAdvise, idInst, hszTopic, hszItem) != 0;
    }

    public static int DdeQueryConvInfo(long conv, long idTransaction, long convInfo) {
        return (int) NativeHelper.call(procDdeQueryConvInfo, conv, idTransaction, convInfo);
    }

    public static long DdeQueryNextServer(long convList, long convPrev) {
        return NativeHelper.call(procDdeQueryNextServer, convList, convPrev);
    }

    public static int DdeQueryString(long idInst, long hsz, StringBuffer buffer, int codePage) {
        return 0;
    }

    public static long DdeReconnect(long conv) {
        return NativeHelper.call(procDdeReconnect, conv);
    }

    public static boolean DdeSetUserHandle(long conv, int id, int user) {
        return NativeHelper.call(procDdeSetUserHandle, conv, id, user) != 0;
    }

    public static boolean DdeUnaccessData(long data) {
        return NativeHelper.call(procDdeUnaccessData, data) != 0;
    }

    public static boolean DdeUninitialize(long handle) {
        return NativeHelper.call(User32.procDdeUninitialize, handle) != 0;
    }

    public static class CONVCONTEXT
    {
        int cb;
        int flags;
        int countryId;
        int codePage;
        int langId;
        int security;
        int qos;
    }
}
