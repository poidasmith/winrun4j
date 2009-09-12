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

}
