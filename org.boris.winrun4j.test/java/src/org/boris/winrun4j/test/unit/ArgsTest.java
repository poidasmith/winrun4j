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
import org.boris.winrun4j.test.framework.ArgsDumper;
import org.boris.winrun4j.test.framework.TestHelper;
import org.junit.Test;

public class ArgsTest
{
    @Test
    public void testArgs() throws Exception {
        Launcher l = TestHelper.launcher();
        l.main(ArgsDumper.class);
        l.vmarg("\"-Dp=23;23;23\"");
        l.arg("a").arg(" b").arg("\"  as asdf  \"");
        String out = TestHelper.run(l, "\"  \"", "adf", "--WinRun4J:ExecuteINI");
        System.out.println(out);
        assertTrue(out.contains("'a' 'b' '  as asdf' '  ' 'adf' '--WinRun4J:ExecuteINI'"));
    }
    
    public static void main(String[] args) throws Exception {
        new ArgsTest().testArgs();
    }
}
