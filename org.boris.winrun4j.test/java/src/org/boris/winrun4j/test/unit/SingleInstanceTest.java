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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.boris.commons.io.ProcessResult;
import org.boris.commons.lang.Threads;
import org.boris.winrun4j.Launcher;
import org.boris.winrun4j.test.framework.TestHelper;
import org.junit.Test;

public class SingleInstanceTest
{
    @Test
    public void testProcess() throws Exception {
        Launcher l = create();
        ProcessResult res = TestHelper.start(l, "process");
        Threads.sleepQuietly(100);
        ProcessResult res2 = TestHelper.start(l, "process").waitFor();
        assertTrue(res2.getStdStr().contains("Single Instance Shutdown"));
        assertFalse(res2.isActive());
        res.destroy();
        res2.destroy();
    }

    private static Launcher create() throws IOException {
        Launcher l = TestHelper.launcher();
        l.main(SingleInstanceRunner.class);
        l.singleInstance("process");
        l.dde(true, null);
        return l;
    }

    public static void main(String[] args) throws Exception {
        new SingleInstanceTest().testProcess();
    }
}
