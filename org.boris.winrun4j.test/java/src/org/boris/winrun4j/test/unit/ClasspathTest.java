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

import org.boris.winrun4j.Launcher;
import org.boris.winrun4j.Log;
import org.boris.winrun4j.test.framework.TestHelper;
import org.junit.Test;

public class ClasspathTest
{
    @Test
    public void testLongClassPath() throws Exception {
        Launcher l = TestHelper.launcher();
        l.main(ClasspathTest.class);
        l.classpath("F:/downloads/*.jar");
        l.log(Log.Level.WARN);
        String res = l.launch().waitFor().getStdOut();
        System.out.println(res);
    }

    public static void main(String[] args) throws Exception {
        for (String s : System.getProperty("java.class.path").split(";"))
            System.out.println(s);
    }
}
