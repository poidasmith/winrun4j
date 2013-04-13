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

import java.io.File;
import java.io.FileWriter;
import java.io.StringReader;

import org.boris.winrun4j.Launcher;
import org.boris.winrun4j.test.framework.IO;
import org.boris.winrun4j.test.framework.PrintEnvironment;
import org.boris.winrun4j.test.framework.ProcessResult;
import org.boris.winrun4j.test.framework.RCEDIT;
import org.boris.winrun4j.test.framework.TestHelper;
import org.junit.Test;

/**
 * Test InI keys match expected values.
 */
public class INITest
{
    @Test
    public void testINI() throws Exception {
        Launcher l = TestHelper.launcher();
        l.arg("hello");
        l.main(PrintEnvironment.class);
        String result = TestHelper.run(l, "-Dtest=this", "-Xms128M");
        assertTrue(result.contains("arg.1=hello"));
        assertTrue(result.contains("vmarg.1=-Dtest=this"));
        assertTrue(result.contains("WinRun4J:module.ini="));
    }

    /**
     * Bounds checking large INIs
     */
    public void testLargeINI() throws Exception {
        Launcher l = TestHelper.launcher().create();
        for (int i = 0; i < 100; i++)
            l.vmarg("-Dx=testing.large.ini");
        l.main(PrintEnvironment.class);
        String ini = l.toString();
        File exe = l.getLauncher();
        File inif = File.createTempFile("ini", ".ini");
        IO.copy(new StringReader(ini), new FileWriter(inif), true);
        RCEDIT.setINI(exe, inif);
        String result = RCEDIT.printINI(exe);
        assertTrue(result.contains("vmarg.101"));
        ProcessResult pr = TestHelper.start(l).waitFor();
        assertEquals(pr.exitValue(), 0);
    }

    public static void main(String[] args) throws Exception {
        new INITest().testINI();
        new INITest().testLargeINI();
    }
}
