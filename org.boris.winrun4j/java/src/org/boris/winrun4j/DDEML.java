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

public class DDEML
{
    private static final long library = User32.library;

    public static final int CP_WINANSI = 1004;
    public static final int CP_WINUNICODE = 1200;
    
    public static boolean DdeAbandonTransaction(long handle, long conversation, long transaction) {
        return NativeHelper.call(library, "DdeAbandonTransaction", handle, conversation, transaction) != 0;
    }

    public static ByteBuffer DdeAccessData(long data) {
        long ptr = Native.malloc(4);
        long pb = NativeHelper.call(library, "DdeAccessData", data, ptr);
        long size = NativeHelper.getInt(ptr);
        Native.free(ptr);
        return pb == 0 ? null : Native.fromPointer(pb, size);
    }

    public static long DdeAddData(long data, byte[] buffer, int len, int offset) {
        long ptr = Native.malloc(len);
        ByteBuffer bb = Native.fromPointer(ptr, len);
        bb.put(buffer, 0, len);
        long res = NativeHelper.call(library, "DdeAddData", data, ptr, len, offset);
        Native.free(ptr);
        return res;
    }

    public static long DdeClientTransaction(byte[] data, int len, long conv, long hszItem, int fmt, int type,
            int timeout) {
        long ptr = Native.malloc(len);
        ByteBuffer bb = Native.fromPointer(ptr, len);
        bb.put(data, 0, len);
        long res = NativeHelper.call(library, "DdeClientTransaction", ptr, len, conv, hszItem, fmt, type, timeout, 0);
        Native.free(ptr);
        return res;
    }

    public static int DdeCmpStringHandles(long hsz1, long hsz2) {
        return (int) NativeHelper.call(library, "DdeCmpStringHandles", hsz1, hsz2);
    }

    public static long DdeConnect(long idInst, String service, String topic, CONVCONTEXT context) {
        long hszService = DdeCreateStringHandle(idInst, service, CP_WINUNICODE);
        long hszTopic = DdeCreateStringHandle(idInst, topic, CP_WINUNICODE);
        long ptr = context == null ? 0 : context.toNative();
        long res = NativeHelper.call(library, "DdeConnect", idInst, hszService, hszTopic, ptr);
        DdeFreeStringHandle(idInst, hszService);
        DdeFreeStringHandle(idInst, hszTopic);
        if (ptr != 0)
            Native.free(ptr);
        return res;
    }

    public static long DdeConnectList(long idInst, String service, String topic, long convList, CONVCONTEXT context) {
        long hszService = DdeCreateStringHandle(idInst, service, CP_WINUNICODE);
        long hszTopic = DdeCreateStringHandle(idInst, topic, CP_WINUNICODE);
        long ptr = context == null ? 0 : context.toNative();
        long res = NativeHelper.call(library, "DdeConnectList", idInst, hszService, hszTopic, convList, ptr);
        DdeFreeStringHandle(idInst, hszService);
        DdeFreeStringHandle(idInst, hszTopic);
        if (ptr != 0)
            Native.free(ptr);
        return res;
    }

    public static long DdeCreateDataHandle(long idInst, byte[] data, int len, int offset, long hszItem, int fmt,
            int afCmd) {
        long ptr = Native.malloc(len);
        ByteBuffer bb = Native.fromPointer(ptr, len);
        bb.put(data, 0, len);
        long res = NativeHelper.call(library, "DdeCreateDataHandle", idInst, ptr, len, offset, hszItem, fmt, afCmd);
        Native.free(ptr);
        return res;
    }

    public static long DdeCreateStringHandle(long idInst, String str, int codePage) {
        boolean wide = true;
        if (codePage == CP_WINANSI) {
            wide = false;
        }
        long ptr = NativeHelper.toNativeString(str, wide);
        long res = NativeHelper.call(library, "DdeCreateStringHandle", idInst, ptr, codePage);
        Native.free(ptr);
        return res;
    }

    public static boolean DdeDisconnect(long conv) {
        return NativeHelper.call(library, "DdeDisconnect", conv) != 0;
    }

    public static boolean DdeDisconnectList(long convList) {
        return NativeHelper.call(library, "DdeDisconnectList", convList) != 0;
    }

    public static boolean DdeEnableCallback(long idInst, long conv, int cmd) {
        return NativeHelper.call(library, "DdeEnableCallback", idInst, conv, cmd) != 0;
    }

    public static boolean DdeFreeDataHandle(long data) {
        return NativeHelper.call(library, "DdeFreeDataHandle", data) != 0;
    }

    public static boolean DdeFreeStringHandle(long idInst, long hsz) {
        return NativeHelper.call(library, "DdeFreeStringHandle", idInst, hsz) != 0;
    }

    public static int DdeGetData(long data, byte[] buffer, int len, int offset) {
        long ptr = 0;
        if (buffer != null) {
            ptr = Native.malloc(len);
        }
        int res = (int) NativeHelper.call(library, "DdeGetData", data, ptr, len, offset);
        if (buffer != null) {
            ByteBuffer bb = Native.fromPointer(ptr, len);
            bb.get(buffer, 0, len);
            Native.free(ptr);
        }
        return res;
    }

    public static long DdeGetLastError(long idInst) {
        return NativeHelper.call(library, "DdeGetLastError", idInst);
    }

    public static boolean DdeImpersonateClient(long conv) {
        return NativeHelper.call(library, "DdeImpersonateClient", conv) != 0;
    }

    public static long DdeInitialize(DdeCallback callback, int afCmd) {
        return DdeInitialize(new DdeCallbackImpl(callback), afCmd);
    }

    public static long DdeInitialize(Callback callback, int afCmd) {
        long pid = Native.malloc(4);
        long res = NativeHelper.call(pid, callback.getPointer(), afCmd, 0);
        long pidInst = NativeHelper.getInt(pid);
        Native.free(pid);
        return res == 0 ? 0 : pidInst;
    }

    public static boolean DdeKeepStringHandle(long idInst, long hsz) {
        return NativeHelper.call(library, "DdeKeepStringHandle", idInst, hsz) != 0;
    }

    public static long DdeNameService(long idInst, long hsz1, long hsz2, int afCmd) {
        return NativeHelper.call(library, "DdeNameService", idInst, hsz1, hsz2, afCmd);
    }

    public static boolean DdePostAdvise(long idInst, long hszTopic, long hszItem) {
        return NativeHelper.call(library, "DdePostAdvise", idInst, hszTopic, hszItem) != 0;
    }

    public static int DdeQueryConvInfo(long conv, long idTransaction, long convInfo) {
        return (int) NativeHelper.call(library, "DdeQueryConvInfo", conv, idTransaction, convInfo);
    }

    public static long DdeQueryNextServer(long convList, long convPrev) {
        return NativeHelper.call(library, "DdeQueryNextServer", convList, convPrev);
    }

    public static int DdeQueryString(long idInst, long hsz, StringBuffer buffer, int len, int codePage) {
        long ptr = 0;
        if (buffer != null) {
            ptr = Native.malloc(len);
        }
        int res = (int) NativeHelper.call(library, "DdeQueryString", idInst, hsz, ptr, len, codePage);
        if (buffer != null) {
            ByteBuffer bb = NativeHelper.getBuffer(ptr, len);
            buffer.append(NativeHelper.getString(bb, codePage == CP_WINUNICODE));
            Native.free(ptr);
        }
        return res;
    }

    public static String DdeQueryString(long idInst, long hsz, int len) {
        StringBuffer sb = new StringBuffer();
        DdeQueryString(idInst, hsz, sb, len, CP_WINUNICODE);
        return sb.toString();
    }

    public static long DdeReconnect(long conv) {
        return NativeHelper.call(library, "DdeReconnect", conv);
    }

    public static boolean DdeSetUserHandle(long conv, int id, int user) {
        return NativeHelper.call(library, "DdeSetUserHandle", conv, id, user) != 0;
    }

    public static boolean DdeUnaccessData(long data) {
        return NativeHelper.call(library, "DdeUnaccessData", data) != 0;
    }

    public static boolean DdeUninitialize(long handle) {
        return NativeHelper.call(library, "DdeUninitialize", handle) != 0;
    }

    public static class CONVCONTEXT
    {
        int flags;
        int countryId;
        int codePage;
        int langId;
        int security;
        int qos;

        public long toNative() {
            long ptr = Native.malloc(28);
            ByteBuffer bb = NativeHelper.getBuffer(ptr, 28);
            bb.putInt(28);
            bb.putInt(flags);
            bb.putInt(countryId);
            bb.putInt(codePage);
            bb.putInt(langId);
            bb.putInt(security);
            bb.putInt(qos);
            return ptr;
        }
    }

    public interface DdeCallback
    {
        long callback(int type, int fmt, long conv, long hsz1, long hsz2, long data, int data1, int data2);
    }

    public static class DdeCallbackImpl extends Callback
    {
        private DdeCallback callback;

        public DdeCallbackImpl(DdeCallback callback) {
            this.callback = callback;
        }

        protected int callback(int stack) {
            ByteBuffer bb = Native.fromPointer(stack + 8, 32).order(ByteOrder.LITTLE_ENDIAN);
            return (int) callback.callback(bb.getInt(), bb.getInt(), bb.getInt(), bb.getInt(), bb.getInt(),
                    bb.getInt(), bb.getInt(), bb.getInt());
        }
    }
}
