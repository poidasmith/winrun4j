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

import org.boris.commons.io.ProcessResult;
import org.boris.winrun4j.Launcher;
import org.boris.winrun4j.Native;

public class TestHelper
{
    public static final File BASE_PATH = new File("..").getAbsoluteFile();

    public static final File LAUNCHER = Native.IS_64 ? new File(BASE_PATH,
            "WinRun4J\\build\\WinRun4J-Debug-x64\\WinRun4J.exe") : new File(BASE_PATH,
            "WinRun4J\\build\\WinRun4J-Debug\\WinRun4J.exe");
    public static final File LAUNCHER_CONSOLE = Native.IS_64 ? new File(BASE_PATH,
            "WinRun4J\\build\\WinRun4J-Debug-x64 - Console\\WinRun4J.exe") : new File(BASE_PATH,
            "WinRun4J\\build\\WinRun4J-Debug - Console\\WinRun4J.exe");

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

    private static Launcher testcp(Launcher l) {
        return l.classpath(BASE_PATH + "\\org.boris.winrun4j.test\\bin").classpath(
                BASE_PATH + "\\org.boris.commons\\bin").classpath(BASE_PATH + "\\org.boris.winrun4j\\bin").classpath(
                BASE_PATH + "\\org.boris.winrun4j.x64test\\bin").classpath(
                BASE_PATH + "\\..\\platform3.5\\plugins\\org.junit*\\*.jar");
    }

    public static Launcher launcher(boolean console) throws IOException {
        Launcher l = new Launcher(console ? LAUNCHER_CONSOLE : LAUNCHER);
        testcp(l);
        return l;
    }

    public static Launcher launcher() throws IOException {
        return launcher(false).showErrorPopup(false);
    }

    public static String run(Launcher l, String... args) throws Exception {
        l.create();
        ProcessResult pr = new ProcessResult(l.launch(args));
        return pr.waitFor().getStdStr();
    }
}
