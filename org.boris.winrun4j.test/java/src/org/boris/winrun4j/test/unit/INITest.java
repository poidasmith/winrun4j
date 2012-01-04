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

import static org.junit.Assert.assertTrue;

import org.boris.winrun4j.Launcher;
import org.boris.winrun4j.test.framework.PrintEnvironment;
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
        l.vmarg("-Dtest=this");
        l.main(PrintEnvironment.class);
        String result = TestHelper.run(l);
        assertTrue(result.contains("arg.0=hello"));
        assertTrue(result.contains("vmarg.1=-Dtest=this"));
        assertTrue(result.contains("WinRun4J:module.ini="));
    }

    public static void main(String[] args) throws Exception {
        new INITest().testINI();
    }
}
