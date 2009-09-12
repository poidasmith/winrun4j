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

public class Advapi32
{
    public static final long library = Native.loadLibrary("Advapi32");
    public static final long procChangeServiceConfig = Native.getProcAddress(library, "ChangeServiceConfigW");
    public static final long procChangeServiceConfig2 = Native.getProcAddress(library, "ChangeServiceConfig2W");
    public static final long procCloseServiceHandle = Native.getProcAddress(library, "CloseServiceHandle");
    public static final long procCreateService = Native.getProcAddress(library, "CreateServiceW");
    public static final long procDeleteService = Native.getProcAddress(library, "DeleteService");
    public static final long procEnumDependentServices = Native.getProcAddress(library, "EnumDependentServicesW");
    public static final long procEnumServicesStatus = Native.getProcAddress(library, "EnumServicesStatusW");
    public static final long procEnumServicesStatusEx = Native.getProcAddress(library, "EnumServicesStatusExW");
    public static final long procGetServiceDisplayName = Native.getProcAddress(library, "GetServiceDisplayNameW");
    public static final long procGetServiceKeyName = Native.getProcAddress(library, "GetServiceKeyNameW");
    public static final long procLockServiceDatabase = Native.getProcAddress(library, "LockServiceDatabase");
    public static final long procNotifyBootConfigStatus = Native.getProcAddress(library, "NotifyBootConfigStatus");
    public static final long procOpenSCManager = Native.getProcAddress(library, "OpenSCManagerW");
    public static final long procOpenService = Native.getProcAddress(library, "OpenServiceW");
    public static final long procQueryServiceConfig = Native.getProcAddress(library, "QueryServiceConfigW");
    public static final long procQueryServiceConfig2 = Native.getProcAddress(library, "QueryServiceConfig2W");
    public static final long procQueryServiceLockStatus = Native.getProcAddress(library, "QueryServiceLockStatusW");
    public static final long procQueryServiceStatus = Native.getProcAddress(library, "QueryServiceStatus");
    public static final long procQueryServiceStatusEx = Native.getProcAddress(library, "QueryServiceStatusEx");
    public static final long procRegisterServiceCtrlHandler = Native.getProcAddress(library,
            "RegisterServiceCtrlHandlerW");
    public static final long procRegisterServiceCtrlHandlerEx = Native.getProcAddress(library,
            "RegisterServiceCtrlHandlerExW");
    public static final long procSetServiceBits = Native.getProcAddress(library, "SetServiceBits");
    public static final long procSetServiceStatus = Native.getProcAddress(library, "ServiceServiceStatus");
    public static final long procStartService = Native.getProcAddress(library, "StartServiceW");
    public static final long procStartServiceCtrlDispatcher = Native.getProcAddress(library,
            "StartServiceCtrlDispatcherW");
    public static final long procUnlockServiceDatabase = Native.getProcAddress(library, "UnlockServiceDatabase");

    public static final int SC_MANAGER_ALL_ACCESS = 0xF003F;

    public static boolean ChangeServiceConfig(long service, int serviceType, int startType, int errorControl,
            String binaryPathName, String[] loadOrderGroup, String[] dependencies, String serviceStartName,
            String password, String displayName) {
        return false;
    }

    public static boolean CloseServiceHandle(long service) {
        return NativeHelper.call(procCloseServiceHandle, service) != 0;
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
        return NativeHelper.call(procDeleteService, service) != 0;
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
        return null;
    }

    public static String GetServiceKeyName(long scManager, String displayName) {
        return null;
    }

    public static long LockServiceDatabase(long scManager) {
        return 0;
    }

    public static boolean NotifyBootConfigStatus(boolean bootAcceptable) {
        return false;
    }

    public static long NotifyServiceStatusChange(long service, int notifyMask, SERVICE_NOTIFY notify) {
        return 0;
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
