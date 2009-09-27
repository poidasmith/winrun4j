package org.boris.winrun4j.winapi;

import org.boris.winrun4j.Native;

public class IPHelper
{
    private static final long iphlpapi = Native.loadLibrary("iphlpapi");

    public static final Object GetExtendedTcpTable(boolean order, int ulAf) {
        return null;
    }
}
