package org.boris.winrun4j.winapi;

import java.nio.ByteBuffer;

import org.boris.winrun4j.Native;
import org.boris.winrun4j.NativeHelper;

public class IPHelper
{
    public static final long library = Native.loadLibrary("iphlpapi");

    public static final int TCP_TABLE_BASIC_LISTENER = 0;
    public static final int TCP_TABLE_BASIC_CONNECTIONS = 1;
    public static final int TCP_TABLE_BASIC_ALL = 2;
    public static final int TCP_TABLE_OWNER_PID_LISTENER = 3;
    public static final int TCP_TABLE_OWNER_PID_CONNECTIONS = 4;
    public static final int TCP_TABLE_OWNER_PID_ALL = 5;
    public static final int TCP_TABLE_OWNER_MODULE_LISTENER = 6;
    public static final int TCP_TABLE_OWNER_MODULE_CONNECTIONS = 7;
    public static final int TCP_TABLE_OWNER_MODULE_ALL = 8;

    public static final int AF_INET = 2;
    public static final int AF_INET6 = 0;

    public static final MIB_TCPROW[] getExtendedTcpTable(boolean order, int ulAf, int tableClass) {
        if (tableClass > 5)
            return null;
        long pdwSize = Native.malloc(4);
        NativeHelper.setInt(pdwSize, 0);
        long res = NativeHelper.call(library, "GetExtendedTcpTable", 0, pdwSize, order ? 1 : 0, ulAf, tableClass, 0);
        if (res == WinError.ERROR_INSUFFICIENT_BUFFER) {
            int size = NativeHelper.getInt(pdwSize);
            long pTcpTable = Native.malloc(size);
            res = NativeHelper.call(library, "GetExtendedTcpTable", pTcpTable, pdwSize, order ? 1 : 0, ulAf,
                    tableClass, 0);
            if (res != WinError.NO_ERROR) {
                NativeHelper.free(pdwSize, pTcpTable);
                return null;
            }

            ByteBuffer bb = NativeHelper.getBuffer(pTcpTable, size);
            int count = bb.getInt();
            MIB_TCPROW[] row = new MIB_TCPROW[count];
            for (int i = 0; i < count; i++) {
                row[i] = decodeRow(bb, tableClass);
            }
            NativeHelper.free(pdwSize, pTcpTable);
            return row;
        }

        NativeHelper.free(pdwSize);
        return null;
    }

    private static MIB_TCPROW decodeRow(ByteBuffer bb, int tableClass) {
        MIB_TCPROW r = tableClass < 3 ? new MIB_TCPROW() : new MIB_TCPROW_OWNER_PID();
        r.dwState = bb.getInt();
        r.dwLocalAddr = bb.getInt();
        r.dwLocalPort = bb.getInt();
        r.dwRemoteAddr = bb.getInt();
        r.dwRemotePort = bb.getInt();
        if (tableClass > 2) {
            ((MIB_TCPROW_OWNER_PID) r).dwOwningPid = bb.getInt();
        }
        return r;
    }

    public static class MIB_TCPROW
    {
        public int dwState;
        public int dwLocalAddr;
        public int dwLocalPort;
        public int dwRemoteAddr;
        public int dwRemotePort;
    }

    public static class MIB_TCPROW_OWNER_PID extends MIB_TCPROW
    {
        public int dwOwningPid;
    }
}
