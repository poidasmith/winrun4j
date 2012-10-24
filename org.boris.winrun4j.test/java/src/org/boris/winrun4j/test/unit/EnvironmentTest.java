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
import static org.junit.Assert.assertTrue;

import java.io.StringReader;

import org.boris.winrun4j.Log;
import org.boris.winrun4j.PInvoke;
import org.boris.winrun4j.test.framework.IO;
import org.boris.winrun4j.test.framework.ProcessResult;
import org.boris.winrun4j.test.framework.TestHelper;
import org.boris.winrun4j.winapi.Environment;
import org.boris.winrun4j.winapi.Environment.OSVERSIONINFOEX;
import org.junit.Test;

public class EnvironmentTest
{
    @Test
    public void testVars() throws Exception {
        assertEquals(Environment.expandEnvironmentString("%TEMP%"), Environment.getEnv("TEMP"));
    }

    @Test
    public void testCommandLine() throws Exception {
        ProcessResult pr = new ProcessResult(TestHelper.launcher().main(getClass()).log(Log.Level.WARN).launch(
                "-test ok now this is great", "ok", "great"));
        pr.waitFor();
        String[] lines = IO.readLines(new StringReader(pr.getStdOut()));
        assertEquals(lines.length, 4);
        assertEquals(lines[1], "\"-test ok now this is great\"");
        assertEquals(lines[2], "ok");
        assertEquals(lines[3], "great");
    }

    @Test
    public void testOsVersion() throws Exception {
        OSVERSIONINFOEX version = new OSVERSIONINFOEX();
        version.sizeOf = PInvoke.sizeOf(OSVERSIONINFOEX.class, true);
        boolean res = Environment.GetVersionEx(version);
        assertTrue(res);
        assertTrue(version.csdVersion != null);
        assertTrue(version.csdVersion.startsWith("Service Pack"));
        assertTrue(version.buildNumber != 0);
        assertEquals(version.majorVersion, 5);
        assertTrue(version.minorVersion >= 0);
        assertTrue(version.reserved == 0 || version.reserved == 30);
        assertEquals(version.productType, 1);
        assertTrue(version.servicePackMajor == 3 || version.servicePackMajor == 2);
        assertEquals(version.servicePackMinor, 0);
    }

    public static void main(String[] args) throws Exception {
        for (String arg : Environment.getCommandLine()) {
            System.out.println(arg);
        }
    }
}
