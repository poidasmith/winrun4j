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
import org.boris.winrun4j.Native;
import org.boris.winrun4j.NativeHelper;

public class Services
{
    static final long kernel32 = Native.loadLibrary("kernel32");
    static final long procOpenScManager = Native.getProcAddress(kernel32, "OpenSCManager");

    public static void main(String[] args) throws Exception {

    }

    public static long OpenSCManager(String machineName, String databaseName, int desiredAccess) {
        long mnp = NativeHelper.toNativeString(machineName, false);
        long dnp = NativeHelper.toNativeString(databaseName, false);
        long res = NativeHelper.call(procOpenScManager, mnp, dnp, desiredAccess);
        if (mnp != 0)
            Native.free(mnp);
        if (dnp != 0)
            Native.free(dnp);
        return res;
    }

    public static SERVICE_STATUS_PROCESS[] EnumServiceStatusEx(long scManager, int serviceType,
            int serviceState) {
        return null;
    }

    public static long RegisterServiceCtrlHandlerEx(String serviceName, Callback handlerProc,
            long context) {
        return 0;
    }

    public static boolean ChangeServiceConfig(long service, int serviceType, int startType,
            int errorControl, String binaryPathName, String loadOrderGroup, long tagId,
            String dependencies, String serviceStartName, String password, String displayName) {
        return false;
    }
}
