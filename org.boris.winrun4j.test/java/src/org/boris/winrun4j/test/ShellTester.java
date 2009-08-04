/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Peter Smith
 *******************************************************************************/
package org.boris.winrun4j.test;

import java.io.File;

import org.boris.winrun4j.Shell;

public class ShellTester implements Runnable
{
    public static void main(String[] args) throws Exception {
        System.out.println("Logical Drives");
        File[] logicalDrives = Shell.getLogicalDrives();
        for (File f : logicalDrives) {
            System.out.println(f);
        }
        System.out.println();

        System.out.println("Special Folder Paths");
        for (int i = 0; i < 100; i++) {
            File f = Shell.getFolderPath(i);
            if (f != null)
                System.out.printf("%x: %s\n", i, f);
        }
        System.out.println();

        System.out.println("Environment Variables");
        System.out.printf("TEMP: %s\n", Shell.getEnvironmentVariable("TEMP"));
        System.out.printf("TEMP2: %s\n", Shell.getEnvironmentVariable("TEMP2"));
        System.out.println(Shell.getEnvironmentVariables());
        System.out.println(Shell.expandEnvironmentString("%TEMP%"));
        System.out.println();

        System.out.println("Command Line Args");
        String[] ags = Shell.getCommandLineArgs();
        for (String a : ags) {
            System.out.println(a);
        }
        System.out.println();

        System.out.println("Version Info");
        System.out.println(Reflection.toString(Shell.getVersionInfo()));
        System.out.println();

        System.out.println("Tick Count");
        for (int i = 0; i < 10; i++)
            System.out.println(Shell.getTickCount());

        System.out.println(System.getProperty("user.dir"));

        Runtime.getRuntime().addShutdownHook(new Thread(new ShellTester()));
    }

    public void run() {
        System.out.println("Shutting down...");
    }
}
