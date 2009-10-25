/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Peter Smith
 *******************************************************************************/
package org.boris.winrun4j.test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

import org.boris.winrun4j.winapi.IPHelper;
import org.boris.winrun4j.winapi.ToolHelper;
import org.boris.winrun4j.winapi.IPHelper.MIB_TCPROW;
import org.boris.winrun4j.winapi.IPHelper.MIB_TCPROW_OWNER_PID;
import org.boris.winrun4j.winapi.Kernel32.PROCESSENTRY32;

public class IPHelperTest
{
    public static void main(String[] args) throws Exception {
        MIB_TCPROW[] r = IPHelper
                .getExtendedTcpTable(true, IPHelper.AF_INET, 5);
        PROCESSENTRY32[] procs = ToolHelper.createProcessSnaphost();
        Map m = mapPids(procs);

        for (int i = 0; i < r.length; i++) {
            MIB_TCPROW_OWNER_PID rr = (MIB_TCPROW_OWNER_PID) r[i];
            PROCESSENTRY32 p = (PROCESSENTRY32) m.get(rr.dwOwningPid);
            if (p != null) {
                System.out.print(p.szExeFile);
                System.out.print(" [");
            }
            System.out.print(formatAddress(r[i].dwLocalAddr) + ":" +
                    r[i].dwLocalPort);
            System.out.print(" - ");
            System.out.print(formatAddress(r[i].dwRemoteAddr) + ":" +
                    r[i].dwRemotePort);
            System.out.print("] ");
            System.out.print(formatState(r[i].dwState));
            System.out.println();
        }
    }

    private static String formatState(int dwState) {
        switch (dwState) {
        case 1:
            return "closed";
        case 2:
            return "listen";
        case 3:
            return "syn_sent";
        case 4:
            return "syn_rcvd";
        case 5:
            return "estab";
        case 6:
            return "fin_wait1";
        case 7:
            return "fin_wait2";
        case 8:
            return "close_wait";
        case 9:
            return "last_ack";
        case 10:
            return "time_wait";
        case 11:
            return "delete_tcb";
        }
        return null;
    }

    public static Map mapPids(PROCESSENTRY32[] procs) {
        Map m = new HashMap();
        for (int i = 0; i < procs.length; i++) {
            m.put(procs[i].th32ProcessID, procs[i]);
        }
        return m;
    }

    public static String formatAddress(int ip) {
        byte[] b = new byte[4];
        ByteBuffer bb = ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN);
        bb.putInt(ip);
        return toInt(b[0]) + "." + toInt(b[1]) + "." + toInt(b[2]) + "." +
                toInt(b[3]);
    }

    public static int toInt(byte b) {
        return ((int) b) & 0xff;
    }
}
