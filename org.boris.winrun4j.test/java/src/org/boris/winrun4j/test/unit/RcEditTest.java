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
import org.boris.winrun4j.test.framework.RCEDIT;
import org.boris.winrun4j.test.framework.TestHelper;
import org.junit.Test;

/**
 * Test setting of icon etc.., list resources.
 */
public class RcEditTest
{
    @Test
    public void test() throws Exception {
        Launcher l = TestHelper.launcher().create();
        File exe = l.getLauncher();
        String res = RCEDIT.clear(exe);
        res = RCEDIT.setMainIcon(exe, res("eclipse.ico"));
        assertTrue(res.contains("[info] OK"));
        res = RCEDIT.setINI(exe, res("service.ini"));
        assertTrue(res.contains("[info] OK"));
        res = RCEDIT.setSplash(exe, res("../resources/spinner1.gif"));
        assertTrue(res.contains("[info] OK"));
        res = RCEDIT.addJar(exe, new File("../org.boris.winrun4j.eclipse/launcher/WinRun4J.jar"));
        assertTrue(res.contains("[info] OK"));
        res = RCEDIT.list(exe);
        assertTrue(res.contains("Icon      \t0007"));
        assertTrue(res.contains("Group Icon\t0001"));
        assertTrue(res.contains("INI File"));
        assertTrue(res.contains("JAR File  \tWinRun4J.jar"));
        assertTrue(res.contains("Splash File"));
        res = RCEDIT.printINI(exe);
        assertTrue(res.contains("service.class=org.boris.winrun4j.test.ServiceTest"));
        assertTrue(res.contains("service.id=ServiceTest"));
        assertTrue(res.contains("service.name=WinRun4J Test Service"));
    }

    public static void main(String[] args) throws Exception {
        new RcEditTest().test();
    }

    private static File res(String res) {
        return new File("../WinRun4J/test/" + res);
    }
}
