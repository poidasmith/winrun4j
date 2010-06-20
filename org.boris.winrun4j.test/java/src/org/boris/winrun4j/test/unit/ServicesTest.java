/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Peter Smith
 *******************************************************************************/
package org.boris.winrun4j.test.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.boris.commons.io.ProcessResult;
import org.boris.winrun4j.RegistryKey;
import org.boris.winrun4j.test.framework.Launcher;
import org.boris.winrun4j.winapi.Services;
import org.boris.winrun4j.winapi.Services.ENUM_SERVICE_STATUS_PROCESS;
import org.boris.winrun4j.winapi.Services.SERVICE_STATUS;
import org.junit.Test;

public class ServicesTest
{
    /**
     * Register and unregister a service.
     */
    @Test
    public void testRegistration() throws Exception {
        Launcher l = BasicService.launcher();

        // Register service
        ProcessResult res = l.launch("--WinRun4J:RegisterService");
        res.waitFor();
        assertTrue(res.getStdOut().indexOf("[info] Registering Service...") != -1);

        // Check that the registry keys are setup correctly
        RegistryKey rk = new RegistryKey(RegistryKey.HKEY_LOCAL_MACHINE,
                "SYSTEM\\CurrentControlSet\\Services\\" + BasicService.class.getSimpleName());
        assertTrue(rk.exists());
        assertEquals("Basic Service", rk.getString("DisplayName"));
        assertEquals("A test service for winrun4j", rk.getString("Description"));
        String[] s = rk.getMultiString("DependOnService");
        assertEquals(s.length, 2);
        assertEquals("Tcpip", s[0]);
        assertEquals("Bonjour", s[1]);
        assertEquals(2, rk.getDoubleWord("Start", -1));

        // Unregister service and check that the registry keys have gone
        res = l.launch("--WinRun4J:UnregisterService").waitFor();
        assertTrue(res.getStdOut().indexOf("[info] Unregistering Service...") != -1);
        Thread.sleep(100); // race condition on registry clear out
        assertFalse(rk.exists());
    }

    /**
     * Register a service, start it up, stop it and then unregister it.
     */
    @Test
    public void testService() throws Exception {
        Launcher l = BasicService.launcher();

        // Register service
        ProcessResult res = l.launch("--WinRun4J:RegisterService").waitFor();
        assertTrue(res.getStdOut().indexOf("[info] Registering Service...") != -1);

        // Open handle to service control manager
        long scm = Services.openSCManager(null, null, Services.SC_MANAGER_ALL_ACCESS);
        assertFalse(scm == 0);

        // Check that our service is there
        assertEquals("Basic Service", Services.getServiceDisplayName(scm, "BasicService"));

        // Start the service
        long service = Services.openService(scm, "BasicService", Services.SC_MANAGER_ALL_ACCESS);
        assertTrue(service != 0);
        Services.startService(service, null);

        // Check that the service is running
        ENUM_SERVICE_STATUS_PROCESS ss = Services.getServiceStatus(scm, "BasicService");
        assertTrue(ss != null);
        assertEquals(ss.currentState, Services.SERVICE_STATE_ACTIVE);

        // Stop service and check that it stopped
        SERVICE_STATUS status = Services.controlService(service, Services.SERVICE_CONTROL_SHUTDOWN);
        assertNotNull(status);
        assertEquals(status.currentState, Services.SERVICE_STOPPED);

        // Unregister service
        res = l.launch("--WinRun4J:UnregisterService").waitFor();
        assertTrue(res.getStdOut().indexOf("[info] Unregistering Service...") != -1);
    }

    @Test
    public void testServiceApi() throws Exception {
    }
}
