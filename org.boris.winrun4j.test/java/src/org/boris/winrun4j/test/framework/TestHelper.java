/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Peter Smith
 *******************************************************************************/
package org.boris.winrun4j.test.framework;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Random;

import junit.framework.TestCase;

import org.boris.winrun4j.Launcher;

public class TestHelper
{
    public static final File LAUNCHER = new File(
            "F:\\eclipse\\workspace\\WinRun4J\\build\\WinRun4J-Debug\\WinRun4J.exe");

    public static byte[] createRandomByteArray() {
        Random r = new Random();
        byte[] b = new byte[r.nextInt(50) + 1];
        r.nextBytes(b);
        return b;
    }

    public static void assertArrayEquals(Object arr1, Object arr2) {
        TestCase.assertNotNull("Array 1 is null", arr1);
        TestCase.assertNotNull("Array 2 is null", arr2);
        int len = Array.getLength(arr1);
        int len2 = Array.getLength(arr2);
        TestCase.assertEquals(len, len2);
        for (int i = 0; i < len; i++)
            TestCase.assertEquals("Element " + i + " not equal", Array.get(arr1, i), Array.get(arr2, i));
    }

    public static Launcher testcp(Launcher l) {
        return l.classpath("F:\\eclipse\\workspace\\org.boris.winrun4j.test\\bin").
                classpath("F:\\eclipse\\workspace\\org.boris.commons\\bin").
                classpath("F:\\eclipse\\workspace\\org.boris.winrun4j\\bin").
                classpath("F:\\eclipse\\platform3.5\\plugins\\org.junit*\\*.jar");
    }

    public static Launcher launcher() throws IOException {
        Launcher l = new Launcher();
        testcp(l);
        l.create(LAUNCHER);
        return l;
    }
}
