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
        res = RCEDIT.setMainIcon(exe, res("EclipseFile.ico"));
        boolean tres = res.contains("[info] OK");
        if(!tres)
            System.out.println(res);
        assertTrue(tres);
        res = RCEDIT.setINI(exe, res("service.ini"));
        assertTrue(res.contains("[info] OK"));
        res = RCEDIT.setSplash(exe, res("SplashScreen.gif"));
        assertTrue(res.contains("[info] OK"));
        res = RCEDIT.addJar(exe, res("junit-4.10.jar"));
        assertTrue(res.contains("[info] OK"));
        res = RCEDIT.setManifest(exe, res("WinRun4J.exe.manifest"));
        assertTrue(res.contains("[info] OK"));
        res = RCEDIT.list(exe);
        assertTrue(res.contains("Icon      \t0003"));
        assertTrue(res.contains("Group Icon\t0001"));
        assertTrue(res.contains("INI File"));
        assertTrue(res.contains("JAR File  \tjunit-4.10.jar"));
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
        String[] locations = {"../WinRun4J/test/", ".", "docs", "lib", "../WinRun4J/" };
        for(String location : locations) {
            File f = new File(new File(location), res);
            if(f.exists())
                return f;
        }
        
        return new File(res);
    }
}
