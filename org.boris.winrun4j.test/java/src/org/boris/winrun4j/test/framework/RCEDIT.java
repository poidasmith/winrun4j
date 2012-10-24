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

public class RCEDIT
{
    public static String setMainIcon(File exe, File icon) throws Exception {
        return run("/I", exe, icon);
    }

    public static String addIcon(File exe, File icon) throws Exception {
        return run("/A", exe, icon);
    }

    public static String setINI(File exe, File ini) throws Exception {
        return run("/N", exe, ini);
    }

    public static String setSplash(File exe, File splash) throws Exception {
        return run("/S", exe, splash);
    }

    public static String addHTML(File exe, File html) throws Exception {
        return run("/H", exe, html);
    }

    public static String addJar(File exe, File jar) throws Exception {
        return run("/J", exe, jar);
    }

    public static String setManifest(File exe, File manifest) throws Exception {
        return run("/M", exe, manifest);
    }

    public static String clear(File exe) throws Exception {
        return run("/C", exe, null);
    }

    public static String list(File exe) throws Exception {
        return run("/L", exe, null);
    }

    public static String printINI(File exe) throws Exception {
        return run("/P", exe, null);
    }

    public static String script(File exe, File script) throws Exception {
        return run("/P", exe, script);
    }

    public static String run(String option, File exe, File resource) throws Exception {
        return start(option, exe, resource).waitFor().getStdStr();
    }

    public static ProcessResult start(String option, File exe, File resource) throws IOException {
        return start(option, exe.getAbsolutePath(), resource == null ? null : resource.getAbsolutePath());
    }

    public static ProcessResult start(String option, String exe, String resource) throws IOException {
        String[] args = new String[resource == null ? 3 : 4];
        args[0] = TestHelper.RCEDIT.getAbsolutePath();
        args[1] = option;
        args[2] = exe;
        if (resource != null)
            args[3] = resource;
        Process p = Runtime.getRuntime().exec(args);
        return new ProcessResult(p);
    }
}
