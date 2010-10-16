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

import org.boris.winrun4j.Delegate;
import org.boris.winrun4j.Log;
import org.boris.winrun4j.NativeHelper;
import org.boris.winrun4j.winapi.DDEML;

public class NativeDdeClientTest extends DDEML.DdeCallback
{
    public static void main(String[] args) throws Exception {
        Delegate cb = new NativeDdeClientTest();
        long pid = DDEML.initialize(cb, 0);
        if (pid == 0) {
            Log.error("Could not initialize DDE");
            return;
        }

        long hs = DDEML.createStringHandle(pid, "WinRun4J", DDEML.CP_WINUNICODE);
        long ht = DDEML.createStringHandle(pid, "system", DDEML.CP_WINUNICODE);
        long hc = DDEML.connect(pid, hs, ht, 0);
        if (hc == 0) {
            Log.error("Could not connect to server");
            cb.dispose();
            return;
        }

        byte[] b = NativeHelper.toBytes("Testing", true);
        long res = DDEML.clientTransaction(b, b.length, hc, 0, 0, DDEML.XTYP_EXECUTE, DDEML.TIMEOUT_ASYNC);
        if (res == 0) {
            Log.error("Failed to send notification");
            cb.dispose();
            return;
        }

        DDEML.uninitialize(pid);
        cb.dispose();
    }

    public long ddeCallback(int type, int fmt, long conv, long hsz1, long hsz2, long data, int data1, int data2) {
        System.out.println(type);
        return 0;
    }
}
