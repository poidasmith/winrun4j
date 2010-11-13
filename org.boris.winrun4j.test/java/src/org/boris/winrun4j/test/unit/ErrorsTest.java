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

import java.util.Map;

import org.boris.winrun4j.INI;
import org.boris.winrun4j.Launcher;
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
        l.main(ErrorsTest.class);
        // l.vmVersion(null, null, "10.5.2");
        System.out.println(TestHelper.run(l));
    }

    public static void main(String[] args) throws Exception {
        Map<String, String> p = INI.getProperties();
        for (String k : p.keySet()) {
            System.out.print(k);
            System.out.print("=");
            System.out.print(p.get((String) k));
            System.out.print("\n");
        }
        new ErrorsTest().testVMVersion();
    }
}
