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

import junit.framework.TestCase;

import org.boris.commons.io.ProcessResult;
import org.boris.winrun4j.RegistryKey;
import org.boris.winrun4j.test.framework.Launcher;
import org.junit.Test;

public class ServicesTest extends TestCase
{
    @Test
    public void testRegistration() throws Exception {
        Launcher l = BasicService.launcher();
        ProcessResult res = l.launch("--WinRun4J:RegisterService");
        res.waitFor();
        assertTrue(res.getStdOut().indexOf("[info] Registering Service...") != -1);
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
        res = l.launch("--WinRun4J:UnregisterService").waitFor();
        assertTrue(res.getStdOut().indexOf("[info] Unregistering Service...") != -1);
        Thread.sleep(10); // race condition on registry clear out
        assertFalse(rk.exists());
    }
}
