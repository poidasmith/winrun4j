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

import java.io.File;

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
        l.workingDir(new File("."));
        l.classpath("data/jars/*.jar");
        l.classpath("data/jars/*.zip");
        l.log(Log.Level.WARN);
        String res = TestHelper.run(l);
        assertTrue(res.contains("Copy of dummy1"));
        assertTrue(res.contains("d2.jar"));
        assertTrue(res.contains("whatever"));
        assertTrue(res.contains("something with a really"));
    }

    public static void main(String[] args) throws Exception {
        for (String s : System.getProperty("java.class.path").split(";"))
            System.out.println(s);
    }
}
