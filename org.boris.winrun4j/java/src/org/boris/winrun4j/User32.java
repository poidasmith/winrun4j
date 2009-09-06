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
import java.nio.ByteOrder;

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

    public static long DdeIntialize(Callback callback, int afCmd) {
        long pid = Native.malloc(4);
        long res = NativeHelper.call(pid, callback.getPointer(), afCmd, 0);
        long pidInst = Native.fromPointer(pid, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
        Native.free(pid);
        return res == 0 ? 0 : pidInst;
    }

    public static boolean DdeUnitialize(long handle) {
        return NativeHelper.call(User32.procDdeUninitialize, handle) != 0;
    }
}
