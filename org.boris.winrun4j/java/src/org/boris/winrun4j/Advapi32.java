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

public class Advapi32
{
    public static final long library = Native.loadLibrary("Advapi32");
    public static final long procCloseServiceHandle = Native.getProcAddress(library, "CloseServiceHandle");
    public static final long procCreateService = Native.getProcAddress(library, "CreateServiceW");
    public static final long procOpenSCManager = Native.getProcAddress(library, "OpenSCManagerW");

    public static boolean CloseServiceHandle(long service) {
        return NativeHelper.call(procCloseServiceHandle, service) != 0;
    }

    public static long CreateService(long scManager, String serviceName, String displayName, int desiredAccess,
            int serviceType, int startType, int errorControl, String binaryPathName, String[] loadOrderGroup,
            String[] dependencies, String serviceStartName, String password) {
        return 0;
    }

    public static SERVICE_STATUS ControlService(long service, int control) {
        return null;
    }

    public static boolean DeleteService(long service) {
        return false;
    }

    public static String GetServiceDisplayName(long scManager, String serviceName) {
        return null;
    }

    public static long OpenSCManager(String machineName, String databaseName, int desiredAccess) {
        long mnp = NativeHelper.toNativeString(machineName, true);
        long dnp = NativeHelper.toNativeString(databaseName, true);
        long res = NativeHelper.call(procOpenSCManager, mnp, dnp, desiredAccess);
        if (mnp != 0)
            Native.free(mnp);
        if (dnp != 0)
            Native.free(dnp);
        return res;
    }

    public static long OpenService(long scManager, String serviceName, int desiredAccess) {
        return 0;
    }

    public static boolean StartService(long service, String[] args) {
        return false;
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

    public class SERVICE_STATUS_PROCESS extends SERVICE_STATUS
    {
        public String serviceName;
        public String displayName;
        public int processId;
        public int serviceFlags;
    }

}
