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

import org.boris.commons.io.ProcessResult;
import org.boris.winrun4j.test.framework.Launcher;
import org.junit.Test;

public class SingleInstanceTest
{
    @Test
    public void testProcess() throws Exception {
        Launcher l = create().create();
        ProcessResult res = l.launch("process");
        ProcessResult res2 = l.launch("process").waitFor();
        assertTrue(res2.getStdStr().contains("Single Instance Shutdown"));
        assertFalse(res2.isActive());
        res.destroy();
        res2.destroy();
    }

    private static Launcher create() {
        Launcher l = new Launcher();
        l.main(SingleInstanceRunner.class);
        l.testcp();
        l.singleInstance("process");
        l.dde(true, null);
        return l;
    }
}
