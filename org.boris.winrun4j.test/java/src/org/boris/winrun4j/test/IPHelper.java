package org.boris.winrun4j.test;

import org.boris.winrun4j.Native;

public class IPHelper
{
    public static final long library = Native.loadLibrary("iphlpapi");
    
    public static final Object GetExtendedTcpTable(boolean order, int ulAf) {
        return null;
    }
}
