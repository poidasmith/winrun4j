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
import java.util.Properties;
import java.util.StringTokenizer;

import org.boris.winrun4j.winapi.Console;
import org.boris.winrun4j.winapi.Kernel32;
import org.boris.winrun4j.winapi.Services;
import org.boris.winrun4j.winapi.WinError;
import org.boris.winrun4j.winapi.Services.SERVICE_STATUS;
import org.boris.winrun4j.winapi.Services.SERVICE_TABLE_ENTRY;

public class NativeServiceHost
{
    public static void main(String[] args) {
        int res = 0;
        if (args != null && args.length == 1 && "-register".equals(args[0])) {
            res = registerService();
        } else if (args != null && args.length == 1 && "-unregister".equals(args[0])) {
            res = unregisterService();
        } else if (args != null && args.length > 0 && "-console".equals(args[0])) {
            String[] args1 = new String[args.length - 1];
            System.arraycopy(args, 1, args1, 0, args.length - 1);
            res = runConsole(args1);
        } else {
            res = runService();
        }
        System.exit(res);
    }

    private static int runService() {
        Service s = createService();
        if (s == null)
            return 1;

        ServiceRunner runner = new ServiceRunner(s);
        runner.startService();

        return 0;
    }

    private static int runConsole(String[] args) {
        final Service s = createService();
        Console.setConsoleCtrlHandler(new Console.HandlerRoutine() {
            public boolean handlerRoutine(int dwCtrlType) {
                switch (dwCtrlType) {
                case Console.CTRL_BREAK_EVENT:
                case Console.CTRL_C_EVENT:
                case Console.CTRL_CLOSE_EVENT:
                case Console.CTRL_SHUTDOWN_EVENT:
                case Console.CTRL_LOGOFF_EVENT:
                    try {
                        s.serviceRequest(Service.SERVICE_CONTROL_SHUTDOWN);
                        return true;
                    } catch (ServiceException e) {
                        Log.error(e);
                        // System.exit(1);
                    }
                    break;
                }
                return false;
            }
        }, true);
        try {
            return s.serviceMain(args);
        } catch (ServiceException e) {
            Log.error(e);
            return 1;
        }
    }

    private static Service createService() {
        String serviceCls = INI.getProperty(INI.SERVICE_CLASS);
        if (serviceCls == null) {
            Log.error("Service class not specified");
            return null;
        }

        try {
            return (Service) Class.forName(serviceCls).newInstance();
        } catch (Exception e) {
            Log.error("Could not create service class: " + serviceCls);
            Log.error(e);
            return null;
        }
    }

    private static int getControlsAccepted() {
        String controls = INI.getProperty(INI.SERVICE_CONTROLS);
        if (controls == null) {
            return Services.SERVICE_ACCEPT_STOP | Services.SERVICE_ACCEPT_SHUTDOWN;
        }

        int controlsAccepted = 0;
        StringTokenizer st = new StringTokenizer(controls, "|");
        while (st.hasMoreElements()) {
            String p = st.nextToken().toLowerCase().trim();
            if ("stop".equals(p)) {
                controlsAccepted |= Services.SERVICE_ACCEPT_STOP;
            } else if ("shutdown".equals(p)) {
                controlsAccepted |= Services.SERVICE_ACCEPT_SHUTDOWN;
            } else if ("pause".equals(p)) {
                controlsAccepted |= Services.SERVICE_ACCEPT_PAUSE_CONTINUE;
            } else if ("param".equals(p)) {
                controlsAccepted |= Services.SERVICE_ACCEPT_PARAMCHANGE;
            } else if ("netbind".equals(p)) {
                controlsAccepted |= Services.SERVICE_ACCEPT_NETBINDCHANGE;
            } else if ("hardware".equals(p)) {
                controlsAccepted |= Services.SERVICE_ACCEPT_HARDWAREPROFILECHANGE;
            } else if ("power".equals(p)) {
                controlsAccepted |= Services.SERVICE_ACCEPT_POWEREVENT;
            } else if ("session".equals(p)) {
                controlsAccepted |= Services.SERVICE_ACCEPT_SESSIONCHANGE;
            }
        }

        return controlsAccepted;
    }

    private static int unregisterService() {
        Log.info("Unregistering Service...");

        String serviceId = INI.getProperty(INI.SERVICE_ID);
        if (serviceId == null) {
            Log.error("Service ID not specified");
            return 1;
        }

        long h = Services.openSCManager(null, null, Services.SC_MANAGER_CREATE_SERVICE);
        if (h == 0) {
            int error = (int) Kernel32.getLastError();
            Log.error("Could not access service manager: " + error);
            return error;
        }
        long s = Services.openService(h, serviceId, Services.SC_MANAGER_ALL_ACCESS);
        if (s == 0) {
            int error = (int) Kernel32.getLastError();
            Log.error("Could not open service: " + error);
            return error;
        }

        return Services.deleteService(s) ? 0 : 1;
    }

    private static int registerService() {
        Properties props = INI.getProperties();
        Log.info("Registering Service...");

        String serviceId = props.getProperty(INI.SERVICE_ID);
        if (serviceId == null) {
            Log.error("Service ID not specified");
            return 1;
        }

        // Grab service name
        String name = props.getProperty(INI.SERVICE_NAME);
        if (name == null) {
            Log.error("Service name not specified");
            return 1;
        }

        // Grab service description
        String description = INI.getProperty(INI.SERVICE_DESCRIPTION);
        if (description == null) {
            Log.error("Service description not specified");
            return 1;
        }

        // Check for startup mode override
        int startupMode = Services.SERVICE_DEMAND_START;
        String startup = props.getProperty(INI.SERVICE_STARTUP);
        if (startup != null) {
            startup = startup.toLowerCase();
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

        // Check for extras
        String[] dependencies = INI.getNumberedEntries(props, INI.SERVICE_DEPENDENCY);
        String loadOrderGroup = props.getProperty(INI.SERVICE_LOAD_ORDER_GROUP);
        String user = props.getProperty(INI.SERVICE_USER);
        String pwd = props.getProperty(INI.SERVICE_PWD);

        String path = "\"" + Kernel32.getModuleFilename(0) + "\"";
        long h = Services.openSCManager(null, null, Services.SC_MANAGER_CREATE_SERVICE);
        if (h == 0) {
            int error = (int) Kernel32.getLastError();
            Log.error("Could not access service manager: " + error);
            return error;
        }

        long s = Services.createService(h, serviceId, name, Services.SERVICE_ALL_ACCESS,
                Services.SERVICE_WIN32_OWN_PROCESS, startupMode, Services.SERVICE_ERROR_NORMAL, path, loadOrderGroup,
                dependencies, user, pwd);
        if (s == 0) {
            int error = (int) Kernel32.getLastError();
            if (error == WinError.ERROR_SERVICE_EXISTS) {
                Log.warning("Service already exists");
            } else {
                Log.error("Could not create service: " + error);
            }
            return error;
        }
        Services.closeServiceHandle(s);
        Services.closeServiceHandle(h);

        // Add description
        RegistryKey sk = new RegistryKey(RegistryKey.HKEY_LOCAL_MACHINE, "System\\CurrentControlSet\\Services\\" +
                serviceId);
        sk.setString("Description", description);

        Log.info("Service regitration complete");

        return 0;
    }

    public static class ServiceRequestCallback extends Callback
    {
        private Service service;

        public ServiceRequestCallback(Service service) {
            this.service = service;
        }

        protected int callback(int stack) {
            try {
                return service.serviceRequest(NativeHelper.getInt(stack));
            } catch (ServiceException e) {
                Log.error(e);
                return 0;
            }
        }
    }

    public static class ServiceMainCallback extends Callback
    {
        private Service service;

        public ServiceMainCallback(Service service) {
            this.service = service;
        }

        protected int callback(int stack) {
            int argc = NativeHelper.getInt(stack);
            ByteBuffer bb = NativeHelper.getBuffer(stack + 4, argc * 4);
            String[] args = new String[argc];
            for (int i = 0; i < argc; i++) {
                long ptr = NativeHelper.getInt(bb.getInt());
                args[i] = NativeHelper.getString(ptr, 1024, true);
            }
            try {
                return service.serviceMain(args);
            } catch (ServiceException e) {
                Log.error(e);
                return -1;
            }
        }
    }

    public static class ServiceRunner implements Service
    {
        private final Service service;
        private final ServiceRequestCallback request;
        private final ServiceMainCallback serviceMain;
        private long statusHandle;
        private SERVICE_STATUS status = new SERVICE_STATUS();
        private String serviceId;

        public ServiceRunner(Service service) {
            this.service = service;
            this.request = new ServiceRequestCallback(this);
            this.serviceMain = new ServiceMainCallback(this);
        }

        public void startService() {
            serviceId = INI.getProperty(INI.SERVICE_ID);
            SERVICE_TABLE_ENTRY entry = new SERVICE_TABLE_ENTRY();
            entry.serviceName = serviceId;
            entry.serviceProc = serviceMain;
            SERVICE_TABLE_ENTRY[] entries = new SERVICE_TABLE_ENTRY[] { entry };

            if (!Services.startServiceCtrlDispatcher(entries)) {
                Log.error("Service control dispatcher error: " + Kernel32.getLastError());
            }
        }

        public int serviceMain(String[] args) throws ServiceException {
            statusHandle = Services.registerServiceCtrlHandler(serviceId, request);
            if (statusHandle == 0) {
                Log.error("Error registering service control handler: " + Kernel32.getLastError());
                return 1;
            }

            status.serviceType = Services.SERVICE_WIN32;
            status.currentState = Services.SERVICE_START_PENDING;
            status.controlsAccepted = getControlsAccepted();
            status.win32ExitCode = 0;
            status.serviceSpecificExitCode = 0;
            status.waitHint = 0;
            setServiceStatus(Services.SERVICE_RUNNING);
            try {
                service.serviceMain(args);
            } catch (Exception e) {
                Log.error(e);
            }
            setServiceStatus(Services.SERVICE_STOPPED);

            return 0;
        }

        public int serviceRequest(int control) throws ServiceException {
            switch (control) {
            case Services.SERVICE_CONTROL_PAUSE:
                setServiceStatus(Services.SERVICE_PAUSED);
                break;
            case Services.SERVICE_CONTROL_CONTINUE:
                setServiceStatus(Services.SERVICE_RUNNING);
                break;
            case Services.SERVICE_CONTROL_SHUTDOWN:
            case Services.SERVICE_CONTROL_STOP:
                setServiceStatus(Services.SERVICE_STOP_PENDING);
            }

            service.serviceRequest(control);

            return 0;
        }

        private void setServiceStatus(int currentState) {
            status.currentState = currentState;
            if (!Services.setServiceStatus(statusHandle, status)) {
                Log.error("Error in SetServiceStatus: " + Kernel32.getLastError());
            }
        }
    }
}
