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

public class Services
{
    public static boolean ChangeServiceConfig(long service, int serviceType, int startType, int errorControl,
            String binaryPathName, String[] loadOrderGroup, String[] dependencies, String serviceStartName,
            String password, String displayName) {
        return false;
    }

    public static boolean CloseServiceHandle(long service) {
        return NativeHelper.call(Advapi32.procCloseServiceHandle, service) != 0;
    }

    public static SERVICE_STATUS ControlService(long service, int control) {
        return null;
    }

    public static long CreateService(long scManager, String serviceName, String displayName, int desiredAccess,
            int serviceType, int startType, int errorControl, String binaryPathName, String[] loadOrderGroup,
            String[] dependencies, String serviceStartName, String password) {
        return 0;
    }

    public static boolean DeleteService(long service) {
        return NativeHelper.call(Advapi32.procDeleteService, service) != 0;
    }

    public static ENUM_SERVICE_STATUS[] EnumDependentServices(long service, int serviceState) {
        return null;
    }

    public static ENUM_SERVICE_STATUS[] EnumServiceStatus(long scManager, int serviceType, int serviceState) {
        return null;
    }

    public static ENUM_SERVICE_STATUS_PROCESS[] EnumServiceStatusEx(long scManager, int serviceType, int serviceState,
            String groupName) {
        return null;
    }

    public static String GetServiceDisplayName(long scManager, String serviceName) {
        if (serviceName == null)
            return null;
        long snPtr = NativeHelper.toNativeString(serviceName, true);
        long dnPtr = Native.malloc(1024);
        long szPtr = Native.malloc(4);
        NativeHelper.getBuffer(szPtr, 4).putInt(1024);
        boolean res = NativeHelper.call(Advapi32.procGetServiceDisplayName, scManager, snPtr, dnPtr, szPtr) != 0;
        String displayName = null;
        if (res) {
            displayName = NativeHelper.getString(dnPtr, 1024, true);
        }
        Native.free(dnPtr);
        Native.free(szPtr);
        return displayName;
    }

    public static String GetServiceKeyName(long scManager, String displayName) {
        if (displayName == null)
            return null;
        long snPtr = NativeHelper.toNativeString(displayName, true);
        long dnPtr = Native.malloc(1024);
        long szPtr = Native.malloc(4);
        NativeHelper.getBuffer(szPtr, 4).putInt(1024);
        boolean res = NativeHelper.call(Advapi32.procGetServiceKeyName, scManager, snPtr, dnPtr, szPtr) != 0;
        String serviceName = null;
        if (res) {
            displayName = NativeHelper.getString(dnPtr, 1024, true);
        }
        Native.free(dnPtr);
        Native.free(szPtr);
        return serviceName;
    }

    public static long LockServiceDatabase(long scManager) {
        return NativeHelper.call(Advapi32.procLockServiceDatabase, scManager);
    }

    public static boolean NotifyBootConfigStatus(boolean bootAcceptable) {
        return NativeHelper.call(Advapi32.procNotifyBootConfigStatus, bootAcceptable ? 1 : 0) != 0;
    }

    public static long NotifyServiceStatusChange(long service, int notifyMask, SERVICE_NOTIFY notify) {
        return 0;
    }

    public static long OpenSCManager(String machineName, String databaseName, int desiredAccess) {
        long mnp = NativeHelper.toNativeString(machineName, true);
        long dnp = NativeHelper.toNativeString(databaseName, true);
        long res = NativeHelper.call(Advapi32.procOpenSCManager, mnp, dnp, desiredAccess);
        if (mnp != 0)
            Native.free(mnp);
        if (dnp != 0)
            Native.free(dnp);
        return res;
    }

    public static long OpenService(long scManager, String serviceName, int desiredAccess) {
        return 0;
    }

    public static QUERY_SERVICE_CONFIG QueryServiceConfig(long service) {
        return null;
    }

    public static QUERY_SERVICE_CONFIG2 QueryServiceConfig2(long service, int infoLevel) {
        return null;
    }

    public static QUERY_SERVICE_LOCK_STATUS QueryServiceLockStatus(long service) {
        return null;
    }

    public static SERVICE_STATUS QueryServiceStatus(long service) {
        return null;
    }

    public static SERVICE_STATUS_PROCESS QueryServiceStatusEx(long service) {
        return null;
    }

    public static long RegisterServiceCtrlHandler(String serviceName, Callback handler) {
        return 0;
    }

    public static long RegisterServiceCtrlHandleEx(String serviceName, Callback handler, long context) {
        return 0;
    }

    public static boolean SetServiceBits(long serviceStatus, int serviceBits, boolean setBitsOn,
            boolean updateImmediately) {
        return false;
    }

    public static boolean SetServiceStatus(long serviceStatus, SERVICE_STATUS status) {
        return false;
    }

    public static boolean StartService(long service, String[] args) {
        return false;
    }

    public static boolean StartServiceCtrlDispatcher(SERVICE_TABLE_ENTRY[] entries) {
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

    public static class SERVICE_STATUS_PROCESS extends SERVICE_STATUS
    {
        public int processId;
        public int serviceFlags;
    }

    public static class ENUM_SERVICE_STATUS extends SERVICE_STATUS
    {
        public String serviceName;
        public String displayName;
    }

    public static class ENUM_SERVICE_STATUS_PROCESS extends SERVICE_STATUS_PROCESS
    {
        public String serviceName;
        public String displayName;
    }

    public static class SERVICE_NOTIFY
    {
        public int version;
        public Callback notifyCallback;
        public long context;
        public int notificationStatus;
        public SERVICE_STATUS_PROCESS serviceStatus;
        public String[] serviceNames;
    }

    public static class QUERY_SERVICE_CONFIG
    {
    }

    public static abstract class QUERY_SERVICE_CONFIG2
    {
    }

    public static class QUERY_SERVICE_LOCK_STATUS
    {
    }

    public static class SERVICE_TABLE_ENTRY
    {
        public String serviceName;
        public Callback serviceProc;
    }

    public interface ServiceMain
    {
        void serviceMain(String[] args);
    }

    public static class ServiceMainCallback extends Callback
    {
        private ServiceMain callback;

        public ServiceMainCallback(ServiceMain callback) {
            this.callback = callback;
        }

        protected int callback(int stack) {
            int argc = NativeHelper.getInt(stack + 8);
            ByteBuffer bb = NativeHelper.getBuffer(stack + 12, argc * 4);
            String[] args = new String[argc];
            for (int i = 0; i < argc; i++) {
                long ptr = NativeHelper.getInt(bb.getInt());
                args[i] = NativeHelper.getString(ptr, 1024, true);
            }
            callback.serviceMain(args);
            return 0;
        }
    }
}
