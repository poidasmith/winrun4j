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
    // Service Type
    public static final int SERVICE_FILE_SYSTEM_DRIVER = 0x2;
    public static final int SERVICE_KERNEL_DRIVER = 0x3;
    public static final int SERVICE_WIN32_OWN_PROCESS = 0x10;
    public static final int SERVICE_WIN32_SHARE_PROCESS = 0x20;
    public static final int SERVICE_WIN32 = 0x30;

    // Service Start Type
    public static final int SERVICE_AUTO_START = 0x2;
    public static final int SERVICE_BOOT_START = 0x0;
    public static final int SERVICE_DEMAND_START = 0x3;
    public static final int SERVICE_DISABLED = 0x4;
    public static final int SERVICE_SYSTEM_START = 0x1;

    // Service Error Control
    public static final int SERVICE_ERROR_CRITICAL = 0x3;
    public static final int SERVICE_ERROR_IGNORE = 0x0;
    public static final int SERVICE_ERROR_NORMAL = 0x1;
    public static final int SERVICE_ERROR_SEVERE = 0x2;

    // Service Config Actions
    public static final int SC_ACTION_NONE = 0x0;
    public static final int SC_ACTION_REBOOT = 0x2;
    public static final int SC_ACTION_RESTART = 0x1;
    public static final int SC_ACTION_RUN_COMMAND = 0x3;

    // Service Current State
    public static final int SERVICE_CONTINUE_PENDING = 0x5;
    public static final int SERVICE_PAUSE_PENDING = 0x6;
    public static final int SERVICE_PAUSED = 0x7;
    public static final int SERVICE_RUNNING = 0x4;
    public static final int SERVICE_START_PENDING = 0x2;
    public static final int SERVICE_STOP_PENDING = 0x3;
    public static final int SERVICE_STOPPED = 0x1;

    // Service Access Type
    public static final int SERVICE_QUERY_CONFIG = 0x0001;
    public static final int SERVICE_CHANGE_CONFIG = 0x0002;
    public static final int SERVICE_QUERY_STATUS = 0x0004;
    public static final int SERVICE_ENUMERATE_DEPENDENTS = 0x0008;
    public static final int SERVICE_START = 0x0010;
    public static final int SERVICE_STOP = 0x0020;
    public static final int SERVICE_PAUSE_CONTINUE = 0x0040;
    public static final int SERVICE_INTERROGATE = 0x0080;
    public static final int SERVICE_USER_DEFINED_CONTROL = 0x0100;
    public static final int SERVICE_ALL_ACCESS = 0x01ff;

    public static boolean ChangeServiceConfig(long service, int serviceType, int startType, int errorControl,
            String binaryPathName, String loadOrderGroup, String[] dependencies, String serviceStartName,
            String password, String displayName) {
        long pathPtr = NativeHelper.toNativeString(binaryPathName, true);
        long loadPtr = NativeHelper.toNativeString(loadOrderGroup, true);
        long depPtr = NativeHelper.toMultiString(dependencies, true);
        long starPtr = NativeHelper.toNativeString(serviceStartName, true);
        long passPtr = NativeHelper.toNativeString(password, true);
        long dispPtr = NativeHelper.toNativeString(displayName, true);
        boolean res = NativeHelper.call(Advapi32.procChangeServiceConfig, new long[] { service, serviceType, startType,
                errorControl, pathPtr, loadPtr, depPtr, starPtr, passPtr, dispPtr }) != 0;
        NativeHelper.free(new long[] { pathPtr, loadPtr, depPtr, passPtr, dispPtr });
        return res;
    }

    public static boolean ChangeServiceConfig2(long service, int infoLevel, SERVICE_CONFIG_BASE config) {
        return false;
    }

    public static boolean CloseServiceHandle(long service) {
        return NativeHelper.call(Advapi32.procCloseServiceHandle, service) != 0;
    }

    public static SERVICE_STATUS ControlService(long service, int control) {
        long ptr = Native.malloc(28);
        boolean res = NativeHelper.call(service, control, ptr) != 0;
        SERVICE_STATUS status = null;
        if (res) {
            status = new SERVICE_STATUS();
            ByteBuffer bb = NativeHelper.getBuffer(ptr, 28);
            status.serviceType = bb.getInt();
            status.currentState = bb.getInt();
            status.controlsAccepted = bb.getInt();
            status.win32ExitCode = bb.getInt();
            status.serviceSpecificExitCode = bb.getInt();
            status.checkPoint = bb.getInt();
            status.waitHint = bb.getInt();
        }
        NativeHelper.free(ptr);
        return status;
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
        long handle = NativeHelper.call(Advapi32.procCreateService, new long[] { scManager, snPtr, dnPtr,
                desiredAccess, serviceType, startType, errorControl, bpPtr, loPtr, depPtr, ssPtr, psPtr });
        NativeHelper.free(new long[] { snPtr, dnPtr, bpPtr, loPtr, ssPtr, psPtr });
        return handle;
    }

    public static boolean DeleteService(long service) {
        return NativeHelper.call(Advapi32.procDeleteService, service) != 0;
    }

    public static ENUM_SERVICE_STATUS[] EnumDependentServices(long service, int serviceState) {
        long ptrBytesNeeded = Native.malloc(4);
        long ptrNumServices = Native.malloc(4);
        boolean res = NativeHelper.call(Advapi32.procEnumDependentServices, service, serviceState, 0, 0,
                ptrBytesNeeded, ptrNumServices) == 0;
        ENUM_SERVICE_STATUS[] deps = null;
        long ptr = 0;
        if (res) {
            int size = NativeHelper.getInt(ptrBytesNeeded);
            if (size > 0) {
                ptr = Native.malloc(size);
                ptrNumServices = Native.malloc(4);
                res = NativeHelper.call(Advapi32.procEnumDependentServices, service, serviceState, ptr, size,
                        ptrBytesNeeded, ptrNumServices) != 0;
                if (res) {
                    int numServices = NativeHelper.getInt(ptrNumServices);
                    deps = decodeEnum(ptr, size, numServices);
                }
            }
        }
        NativeHelper.free(ptrBytesNeeded, ptrNumServices, ptr);
        return deps;
    }

    public static ENUM_SERVICE_STATUS[] EnumServiceStatus(long scManager, int serviceType, int serviceState) {
        long ptrBytesNeeded = Native.malloc(4);
        long ptrNumServices = Native.malloc(4);
        long ptr = Native.malloc(1024);
        boolean res = NativeHelper.call(Advapi32.procEnumServicesStatus, scManager, serviceType, serviceState, ptr,
                1024, ptrBytesNeeded, ptrNumServices, 0) == 0;
        int size = NativeHelper.getInt(ptrBytesNeeded);
        ENUM_SERVICE_STATUS[] stats = null;
        if (res && size > 0) {
            ptr = Native.malloc(size);
            res = NativeHelper.call(Advapi32.procEnumServicesStatus, scManager, serviceType, serviceState, ptr, size,
                    ptrBytesNeeded, ptrNumServices, 0) == 0;
            if (res) {
                int numServices = NativeHelper.getInt(ptrNumServices);
                stats = decodeEnum(ptr, size, numServices);
            }
        }
        NativeHelper.free(ptrBytesNeeded, ptrNumServices, ptr);
        return stats;
    }

    public static ENUM_SERVICE_STATUS_PROCESS[] EnumServiceStatusEx(long scManager, int serviceType, int serviceState,
            String groupName) {
        long ptrBytesNeeded = Native.malloc(4);
        long ptrGroupName = NativeHelper.toNativeString(groupName, true);
        long ptrNumServices = Native.malloc(4);
        boolean res = NativeHelper.call(Advapi32.procEnumServicesStatusEx, scManager, 0, serviceType, serviceState, 0,
                0, ptrBytesNeeded, ptrNumServices, 0, ptrGroupName) == 0;
        ENUM_SERVICE_STATUS_PROCESS[] stats = null;
        long ptr = 0;
        int size = NativeHelper.getInt(ptrBytesNeeded);
        if (res && size > 0) {
            ptr = Native.malloc(size);
            res = NativeHelper.call(Advapi32.procEnumServicesStatusEx, scManager, 0, serviceType, serviceState, ptr,
                    size, ptrBytesNeeded, ptrNumServices, 0, ptrGroupName) != 0;
            if (res) {
                int numServices = NativeHelper.getInt(ptrNumServices);
                stats = decodeEnumProc(ptr, size, numServices);
            }
        }
        NativeHelper.free(ptrBytesNeeded, ptrNumServices, ptr);
        return stats;
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
        NativeHelper.free(new long[] { snPtr, dnPtr, szPtr });
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
        NativeHelper.free(new long[] { snPtr, dnPtr, szPtr });
        return serviceName;
    }

    public static long LockServiceDatabase(long scManager) {
        return NativeHelper.call(Advapi32.procLockServiceDatabase, scManager);
    }

    public static boolean NotifyBootConfigStatus(boolean bootAcceptable) {
        return NativeHelper.call(Advapi32.procNotifyBootConfigStatus, bootAcceptable ? 1 : 0) != 0;
    }

    public static long NotifyServiceStatusChange(long service, int notifyMask, SERVICE_NOTIFY notify) {
        long ptr = 0;
        if (notify != null) {

        }
        long handle = NativeHelper.call(Advapi32.procNotifyServiceStatusChange, service, notifyMask, ptr);
        NativeHelper.free(ptr);
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
        long ptr = NativeHelper.toNativeString(serviceName, true);
        long res = NativeHelper.call(Advapi32.procOpenService, scManager, ptr, desiredAccess);
        NativeHelper.free(ptr);
        return res;
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
        return NativeHelper.call(Advapi32.procSetServiceBits, serviceStatus, serviceBits, setBitsOn ? 1 : 0,
                updateImmediately ? 1 : 0) != 0;
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

    public static abstract class SERVICE_CONFIG_BASE
    {
    }

    public static class SERVICE_DELAYED_AUTO_START extends SERVICE_CONFIG_BASE
    {
        public boolean delayedAutoStart;
    }

    public static class SERVICE_DESCRIPTION extends SERVICE_CONFIG_BASE
    {
        public String description;
    }

    public static class SERVICE_FAILURE_ACTIONS extends SERVICE_CONFIG_BASE
    {
        public int resetPeriod;
        public String rebootMsg;
        public String command;
        public SC_ACTION[] actions;
    }

    public static class SERVICE_FAILURE_ACTIONS_FLAG extends SERVICE_CONFIG_BASE
    {
        public boolean failureActionsOnNonCrashFailures;
    }

    public static class SERVICE_PREFERRED_NODE_INFO extends SERVICE_CONFIG_BASE
    {
        public int preferredNode;
        public boolean delete;
    }

    public static class SERVICE_PRESHUTDOWN_INFO extends SERVICE_CONFIG_BASE
    {
        public int preshutdownTimeout;
    }

    public static class SEVICE_REQUIRED_PRIVELEGES_INFO extends SERVICE_CONFIG_BASE
    {
        public String requiredPriveleges;
    }

    public static class SERVICE_SID_INFO extends SERVICE_CONFIG_BASE
    {
        public String serviceSidType;
    }

    public static class SERVICE_TRIGGER_INFO extends SERVICE_CONFIG_BASE
    {
        public String description;
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
        public int version = 2;
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

    public static class SC_ACTION
    {
        public int type;
        public int delay;
    }

    public static class SERVICE_TRIGGER
    {
        public int triggerType;
        public int action;
        public Advapi32.GUID triggerSubType;
        public TRIGGER_SPECIFIC_DATA_ITEM[] dataItems;
    }

    public static class TRIGGER_SPECIFIC_DATA_ITEM
    {
        public int dataType;
        public byte[] data;
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

    private static ENUM_SERVICE_STATUS[] decodeEnum(long ptr, int size, int numServices) {
        ENUM_SERVICE_STATUS[] deps = new ENUM_SERVICE_STATUS[numServices];
        ByteBuffer bb = NativeHelper.getBuffer(ptr, size);
        for (int i = 0; i < numServices; i++) {
            ENUM_SERVICE_STATUS ess = new ENUM_SERVICE_STATUS();
            ess.serviceName = NativeHelper.getString(bb.getInt(), 1024, true);
            ess.displayName = NativeHelper.getString(bb.getInt(), 1024, true);
            decodeStatus(bb, ess);
            deps[i] = ess;
        }
        return deps;
    }

    private static ENUM_SERVICE_STATUS_PROCESS[] decodeEnumProc(long ptr, int size, int numServices) {
        ENUM_SERVICE_STATUS_PROCESS[] deps = new ENUM_SERVICE_STATUS_PROCESS[numServices];
        ByteBuffer bb = NativeHelper.getBuffer(ptr, size);
        for (int i = 0; i < numServices; i++) {
            ENUM_SERVICE_STATUS_PROCESS ess = new ENUM_SERVICE_STATUS_PROCESS();
            ess.serviceName = NativeHelper.getString(bb.getInt(), 1024, true);
            ess.displayName = NativeHelper.getString(bb.getInt(), 1024, true);
            decodeStatusProcess(bb, ess);
            deps[i] = ess;
        }
        return deps;
    }

    private static void decodeStatusProcess(ByteBuffer bb, SERVICE_STATUS_PROCESS s) {
        decodeStatus(bb, s);
        s.processId = bb.getInt();
        s.serviceFlags = bb.getInt();
    }

    private static void decodeStatus(ByteBuffer bb, SERVICE_STATUS s) {
        s.serviceType = bb.getInt();
        s.currentState = bb.getInt();
        s.controlsAccepted = bb.getInt();
        s.win32ExitCode = bb.getInt();
        s.serviceSpecificExitCode = bb.getInt();
        s.checkPoint = bb.getInt();
        s.waitHint = bb.getInt();
    }
}
