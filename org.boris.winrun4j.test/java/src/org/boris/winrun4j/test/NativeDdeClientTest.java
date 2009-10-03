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

import org.boris.winrun4j.Callback;
import org.boris.winrun4j.Log;
import org.boris.winrun4j.winapi.DDEML;

public class NativeDdeClientTest
{
    public static void main(String[] args) throws Exception {
        long pid = DDEML.DdeInitialize((Callback) null, 0);
        if (pid == 0) {
            Log.error("Could not initialize DDE");
            return;
        }

        long hs = DDEML.DdeCreateStringHandle(pid, "WinRun4J", DDEML.CP_WINUNICODE);
        long ht = DDEML.DdeCreateStringHandle(pid, "system", DDEML.CP_WINUNICODE);
        long hc = DDEML.DdeConnect(pid, hs, ht, 0);
        if (hc == 0) {
            Log.error("Could not connect to server");
            return;
        }

        byte[] b = "Testing".getBytes();
        long res = DDEML.DdeClientTransaction(b, b.length, hc, 0, 0, DDEML.XTYP_EXECUTE, DDEML.TIMEOUT_ASYNC);
        if (res == 0) {
            Log.error("Failed to send notification");
            return;
        }

        DDEML.DdeUninitialize(pid);
    }
}
