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
import org.boris.winrun4j.Log;
import org.boris.winrun4j.Native;
import org.boris.winrun4j.test.framework.TestHelper;
import org.junit.Test;

/**
 * Test logging, memory setting, console setting...
 */
public class LauncherFunctionsTest
{
    @Test
    public void testLogging() throws Exception {
        Launcher l = TestHelper.launcher().main(LauncherFunctionsTest.class);
        String result = TestHelper.run(l);
        assertTrue(result.contains("[err] error test"));
        assertTrue(result.contains("[info] info test"));
        assertTrue(result.contains("[warn] warn test"));
    }

    @Test
    public void testMaxMemory() throws Exception {
        if (Native.IS_64) {
        } else {
            Launcher l = TestHelper.launcher().main(LauncherFunctionsTest.class).heapMax(100);
            TestHelper.run(l);
        }
    }

    public static void main(String[] args) throws Exception {
        Log.error("error test");
        Log.info("info test");
        Log.warning("warn test");
    }
}
