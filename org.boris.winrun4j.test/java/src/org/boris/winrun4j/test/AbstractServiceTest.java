/*******************************************************************************
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Peter Smith
 * This program and the accompanying materials
 *******************************************************************************/
package org.boris.winrun4j.test;

import org.boris.winrun4j.Native;
import org.boris.winrun4j.NativeHelper;

public class AbstractServiceTest
{
    private static long advapi32 = Native.loadLibrary("advapi32");
    private static long kernel32 = Native.loadLibrary("kernel32");

    public static boolean CloseServiceHandle(long handle) {
        return NativeHelper.call(advapi32, "CloseServiceHandle", handle) != 0;
    }

    public static long CreateService(long scManager, String serviceName, String displayName, int desiredAccess,
            int serviceType, int startType, int errorControl, String binaryPathName, String loadOrderGroup,
            String[] dependencies, String serviceStartName, String password) {
        long snPtr = NativeHelper.toNativeString(serviceName, true);
        long dnPtr = NativeHelper.toNativeString(displayName, true);
        long bpPtr = NativeHelper.toNativeString(binaryPathName, true);
        long loPtr = NativeHelper.toNativeString(loadOrderGroup, true);
        long depPtr = NativeHelper.toMultiString(dependencies, true);
        long ssPtr = NativeHelper.toNativeString(serviceStartName, true);
        long psPtr = NativeHelper.toNativeString(password, true);
        long handle = NativeHelper.call(advapi32, "CreateService", new long[] { scManager, snPtr, dnPtr, desiredAccess,
                serviceType, startType, errorControl, bpPtr, loPtr, depPtr, ssPtr, psPtr });
        NativeHelper.free(new long[] { snPtr, dnPtr, bpPtr, loPtr, ssPtr, psPtr });
        return handle;
    }

    public static boolean DeleteService(long service) {
        return NativeHelper.call(advapi32, "DeleteService", service) != 0;
    }

    public static long OpenSCManager(String machineName, String databaseName, int desiredAccess) {
        long mnp = NativeHelper.toNativeString(machineName, true);
        long dnp = NativeHelper.toNativeString(databaseName, true);
        long res = NativeHelper.call(advapi32, "OpenSCManager", mnp, dnp, desiredAccess);
        if (mnp != 0)
            Native.free(mnp);
        if (dnp != 0)
            Native.free(dnp);
        return res;
    }

    public static long OpenService(long scManager, String serviceName, int desiredAccess) {
        long ptr = NativeHelper.toNativeString(serviceName, true);
        long res = NativeHelper.call(advapi32, "OpenService", scManager, ptr, desiredAccess);
        NativeHelper.free(ptr);
        return res;
    }

    public static boolean SetServiceStatus(long serviceStatus, SERVICE_STATUS status) {
        return false;
    }

    public static int GetLastError() {
        return (int) NativeHelper.call(kernel32, "GetLastError");
    }

    public static class SERVICE_STATUS
    {
        public int serviceType;
        public int currentState;
        public int controlsAccepted;
        public int win32ExitCode;
        public int serviceSpecificExitCode;
        public int checkPoint;
        public int waitHint;
    }
}
