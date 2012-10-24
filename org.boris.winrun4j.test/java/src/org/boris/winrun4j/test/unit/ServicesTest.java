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
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.boris.winrun4j.Launcher;
import org.boris.winrun4j.RegistryKey;
import org.boris.winrun4j.test.framework.ProcessResult;
import org.boris.winrun4j.test.framework.TestHelper;
import org.boris.winrun4j.test.framework.Threads;
import org.boris.winrun4j.winapi.Services;
import org.boris.winrun4j.winapi.Services.ENUM_SERVICE_STATUS_PROCESS;
import org.junit.Test;

public class ServicesTest
{
    public static void main(String[] args) throws Exception {
        new ServicesTest().testRegistration();
        new ServicesTest().testService();
    }

    /**
     * Register and unregister a service.
     */
    @Test
    public void testRegistration() throws Exception {
        Launcher l = BasicService.launcher();

        // Register service
        ProcessResult res = TestHelper.start(l, "--WinRun4J:UnregisterService");
        res.waitFor();
        res = TestHelper.start(l, "--WinRun4J:RegisterService");
        res.waitFor();
        assertEquals(0, res.exitValue());
        assertTrue(res.getStdOut().indexOf("[info] Registering Service...") != -1);

        // Check that the registry keys are setup correctly
        RegistryKey rk = new RegistryKey(RegistryKey.HKEY_LOCAL_MACHINE, "SYSTEM\\CurrentControlSet\\Services\\" +
                BasicService.class.getSimpleName());
        assertTrue(rk.exists());
        assertEquals("Basic Service", rk.getString("DisplayName"));
        assertEquals("A test service for winrun4j", rk.getString("Description"));
        String[] s = rk.getMultiString("DependOnService");
        assertEquals(s.length, 1);
        assertEquals("Tcpip", s[0]);
        assertEquals(2, rk.getDoubleWord("Start", -1));

        // Unregister service and check that the registry keys have gone
        res = TestHelper.start(l, "--WinRun4J:UnregisterService").waitFor();
        assertEquals(0, res.exitValue());
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

        // Setup launcher args
        File temp = File.createTempFile("serviceTest", ".txt");
        String[] args = new String[] { temp.getAbsolutePath(), "-testArg1", "-print",
                "\"This is some sort of test for service args\"" };
        for (String arg : args)
            l.arg(arg);

        // Register service
        ProcessResult res = new ProcessResult(l.launch("--WinRun4J:RegisterService")).waitFor();
        assertEquals(0, res.exitValue());
        assertTrue(res.getStdOut().indexOf("[info] Registering Service...") != -1);

        // Open handle to service control manager
        long scm = Services.openSCManager(null, null, Services.SC_MANAGER_ALL_ACCESS);
        assertFalse(scm == 0);

        // Check that our service is there
        assertEquals("Basic Service", Services.getServiceDisplayName(scm, "BasicService"));

        // Start the service
        long service = Services.openService(scm, "BasicService", Services.SC_MANAGER_ALL_ACCESS);
        assertTrue(service != 0);
        String[] extraArgs = new String[] { "extra1", "extras2!!!" };
        Services.startService(service, extraArgs);

        // Wait for service to start
        Threads.sleepQuietly(2000);

        // Check that the service is running
        ENUM_SERVICE_STATUS_PROCESS ss = Services.getServiceStatus(scm, "BasicService");
        assertTrue(ss != null);
        assertEquals(Services.SERVICE_RUNNING, ss.currentState);

        // Stop service and
        Services.controlService(service, Services.SERVICE_CONTROL_STOP);

        // Wait for service to stop
        Threads.sleepQuietly(2000);

        // Check that it stopped
        ss = Services.getServiceStatus(scm, "BasicService");
        assertTrue(ss != null);
        assertEquals(Services.SERVICE_STOPPED, ss.currentState);

        // Check args are passed in correctly
        BufferedReader br = new BufferedReader(new FileReader(temp));
        for (String arg : args)
            assertEquals(stripOffQuotes(arg), br.readLine());
        for (String arg : extraArgs)
            assertEquals(stripOffQuotes(arg), br.readLine());

        // Unregister service
        res = new ProcessResult(l.launch("--WinRun4J:UnregisterService")).waitFor();
        String out = res.getStdStr();
        assertEquals(0, res.exitValue());
        assertTrue(out.indexOf("[info] Unregistering Service...") != -1);
    }

    @Test
    public void testServiceApi() throws Exception {
    }

    private String stripOffQuotes(String arg) {
        if (arg.startsWith("\""))
            arg = arg.substring(1);
        if (arg.endsWith("\""))
            arg = arg.substring(0, arg.length() - 1);
        return arg;
    }
}
