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

import java.util.Properties;

import org.boris.winrun4j.INI;
import org.boris.winrun4j.Log;
import org.boris.winrun4j.RegistryKey;
import org.boris.winrun4j.winapi.Kernel32;
import org.boris.winrun4j.winapi.Services;

public class NativeServiceTest
{
    public static void main(String[] args) throws Exception {
        if (args != null && args.length == 1) {
            if ("-register".equals(args[0])) {
                register(INI.getProperties());
                return;
            } else if ("-unregister".equals(args[0])) {
                unregister(INI.getProperties());
            } else if ("-console".equals(args[0])) {
                // Run as a simple console app
                new NativeServiceTest().serviceStart(args);
                return;
            }
        }
    }

    public void serviceCtrlHandler(int opCode) {
    }

    public void serviceStart(String[] args) {
    }

    public void initialize(Properties ini) {
    }

    public static int register(Properties ini) {
        String serviceId = ini.getProperty(INI.SERVICE_ID);
        if (serviceId == null) {
            Log.error("Service ID not specified");
            return 1;
        }

        String serviceName = ini.getProperty(INI.SERVICE_NAME);
        if (serviceName == null) {
            Log.error("Service name not specified");
            return 1;
        }

        String serviceDesc = ini.getProperty(INI.SERVICE_DESCRIPTION);
        if (serviceDesc == null) {
            Log.error("Service description not specified");
            return 1;
        }

        int startupMode = Services.SERVICE_DEMAND_START;
        String startup = ini.getProperty(INI.SERVICE_STARTUP);
        if (startup != null) {
            if (startup.equals("auto")) {
                startupMode = Services.SERVICE_AUTO_START;
                Log.info("Service startup mode: SERVICE_AUTO_START");
            } else if (startup.equals("boot")) {
                startupMode = Services.SERVICE_BOOT_START;
                Log.info("Service startup mode: SERVICE_BOOT_START");
            } else if (startup.equals("demand")) {
                startupMode = Services.SERVICE_DEMAND_START;
                Log.info("Service startup mode: SERVICE_DEMAND_START");
            } else if (startup.equals("disabled")) {
                startupMode = Services.SERVICE_DISABLED;
                Log.info("Service startup mode: SERVICE_DISABLED");
            } else if (startup.equals("system")) {
                startupMode = Services.SERVICE_SYSTEM_START;
                Log.info("Service startup mode: SERVICE_SYSTEM_START");
            } else {
                Log.warning("Unrecognized service startup mode: " + startup);
            }
        }

        String[] dependencies = INI.getNumberedEntries(ini, INI.SERVICE_DEPENDENCY);
        String loadOrderGroup = ini.getProperty(INI.SERVICE_LOAD_ORDER_GROUP);
        String user = ini.getProperty(INI.SERVICE_USER);
        String pwd = ini.getProperty(INI.SERVICE_PWD);
        String path = "\"" + Kernel32.GetModuleFilename(0) + "\"";

        long handle = Services.OpenSCManager(null, null, Services.SC_MANAGER_CREATE_SERVICE);
        if (handle == 0) {
            long err = Kernel32.GetLastError();
            Log.error("Could not access service control manager: " + err);
            return (int) err;
        }

        long s = Services.CreateService(handle, serviceId, serviceName, Services.SERVICE_ALL_ACCESS,
                Services.SERVICE_WIN32_OWN_PROCESS, startupMode, Services.SERVICE_ERROR_NORMAL, path, loadOrderGroup,
                dependencies, user, pwd);
        if (s == 0) {
            long err = Kernel32.GetLastError();
            if (err == 1073) { // SERVICE_ERROR_EXISTS
                Log.warning("Service already exists");
            } else {
                Log.error("Could not create service: " + err);
            }
        }
        Services.CloseServiceHandle(s);
        Services.CloseServiceHandle(handle);

        // Add description
        RegistryKey k = new RegistryKey(RegistryKey.HKEY_LOCAL_MACHINE, "System\\CurentControlSet\\Services\\" +
                serviceId);
        k.setString("Description", serviceDesc);

        return 0;
    }

    public static int unregister(Properties ini) {
        Log.info("Unregistering service...");
        String serviceId = ini.getProperty(INI.SERVICE_ID);
        if (serviceId == null) {
            Log.error("Service ID not specified");
            return 1;
        }

        long h = Services.OpenSCManager(null, null, Services.SC_MANAGER_CREATE_SERVICE);
        if (h == 0) {
            long error = Kernel32.GetLastError();
            Log.error("Could not access service manager: " + error);
            return (int) error;
        }
        long s = Services.OpenService(h, serviceId, Services.SC_MANAGER_ALL_ACCESS);
        if (s == 0) {
            long error = Kernel32.GetLastError();
            Log.error("Could not open service: " + error);
            return (int) error;
        }

        return Services.DeleteService(s) ? 0 : 1;
    }
}
