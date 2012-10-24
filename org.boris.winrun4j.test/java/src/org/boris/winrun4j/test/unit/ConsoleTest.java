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

import org.boris.winrun4j.Launcher;
import org.boris.winrun4j.test.framework.ProcessResult;
import org.boris.winrun4j.test.framework.SystemExiter;
import org.boris.winrun4j.test.framework.TestHelper;
import org.junit.Test;

public class ConsoleTest
{
    @Test
    public void testReturnCode() throws Exception {
        Launcher l = TestHelper.launcher(true).main(SystemExiter.class);
        ProcessResult pr = TestHelper.start(l).waitFor();
        System.out.println(pr.getStdStr());
        assertEquals(17, pr.exitValue());
    }
}
