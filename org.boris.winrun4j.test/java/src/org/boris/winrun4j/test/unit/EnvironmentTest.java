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

import org.boris.commons.io.IO;
import org.boris.commons.io.ProcessResult;
import org.boris.winrun4j.Log;
import org.boris.winrun4j.PInvoke;
import org.boris.winrun4j.test.framework.TestHelper;
import org.boris.winrun4j.winapi.Environment;
import org.boris.winrun4j.winapi.Environment.OSVERSIONINFOEX;
import org.junit.Test;

public class EnvironmentTest
{
    @Test
    public void testVars() throws Exception {
        assertEquals(
                Environment.expandEnvironmentString("%TEMP%"),
                Environment.getEnvironmentVariable("TEMP"));
    }

    @Test
    public void testCommandLine() throws Exception {
        ProcessResult pr = TestHelper.launcher().main(getClass()).log(Log.Level.WARN).
                launch("-test ok now this is great", "ok", "great");
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
        assertEquals(version.csdVersion, "Service Pack 3");
        assertEquals(version.buildNumber, 2600);
        assertEquals(version.majorVersion, 5);
        assertEquals(version.minorVersion, 1);
        assertEquals(version.reserved, 0);
        assertEquals(version.productType, 1);
        assertEquals(version.servicePackMajor, 3);
        assertEquals(version.servicePackMinor, 0);
    }

    public static void main(String[] args) throws Exception {
        for (String arg : Environment.getCommandLine()) {
            System.out.println(arg);
        }
    }
}
