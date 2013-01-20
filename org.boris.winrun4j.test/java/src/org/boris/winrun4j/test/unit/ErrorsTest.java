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
import org.boris.winrun4j.Native;
import org.boris.winrun4j.test.framework.PrintEnvironment;
import org.boris.winrun4j.test.framework.TestHelper;
import org.junit.Test;

/**
 * Test error conditions, VM cannot load. unspecified main. etc..
 */
public class ErrorsTest
{
    @Test
    public void testVMVersion() throws Exception {
        Launcher l = TestHelper.launcher();
        l.main(PrintEnvironment.class);
        l.vmVersion(null, null, "10.5.2");
        l.showErrorPopup(false);
        assertTrue(TestHelper.run(l).contains("[err] Failed to find Java VM"));
        l.errorMessages("Could not find a matching VM version", null);
        l.create();
        String res = TestHelper.run(l);
        assertTrue(res.contains("[err] Could not find a matching VM version"));
        l = TestHelper.launcher().main("Unknown").showErrorPopup(false);
        assertTrue(TestHelper.run(l).contains("[err] Could not find or initialize main class"));
        if (!Native.IS_64) {
            l.vmarg("-Xmx45G");
            l.errorMessages(null, "VM not start");
            l.create();
            assertTrue(TestHelper.run(l).contains("[err] VM not start"));
        }
    }

    public static void main(String[] args) throws Exception {
        new ErrorsTest().testVMVersion();
    }
}
