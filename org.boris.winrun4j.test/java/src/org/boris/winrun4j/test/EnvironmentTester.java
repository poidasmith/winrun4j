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

import org.boris.winrun4j.winapi.Environment;
import org.boris.winrun4j.winapi.Kernel32;
import org.boris.winrun4j.winapi.Kernel32.SYSTEM_INFO;
import org.boris.winrun4j.winapi.Shell32;

public class EnvironmentTester
{
    public static void main(String[] args) throws Exception {
        System.out.println("Logical Drives");
        File[] logicalDrives = Environment.getLogicalDrives();
        for (File f : logicalDrives) {
            System.out.println(f);
        }
        System.out.println();

        System.out.println("Special Folder Paths");
        for (int i = 0; i < 100; i++) {
            File f = Shell32.getFolderPath(i);
            if (f != null)
                System.out.printf("%x: %s\n", i, f);
        }
        System.out.println();

        System.out.println("Environment Variables");
        System.out.printf("TEMP: %s\n", Environment.getEnvironmentVariable("TEMP"));
        System.out.printf("TEMP2: %s\n", Environment.getEnvironmentVariable("TEMP2"));
        System.out.println(Environment.getEnvironmentVariables());
        System.out.println(Environment.expandEnvironmentString("temp: %TEMP%"));
        System.out.println();

        System.out.println("Command Line Args");
        String[] ags = Environment.getCommandLine();
        for (String a : ags) {
            System.out.println(a);
        }
        System.out.println();

        System.out.println("Version Info");
        System.out.println(Reflection.toString(Environment.getVersionEx()));
        System.out.println();

        System.out.println("Tick Count");
        for (int i = 0; i < 10; i++)
            System.out.println(Kernel32.GetTickCount());

        System.out.printf("Current Process Id: %d\n", Kernel32.GetCurrentProcessId());

        System.out.println(System.getProperty("user.dir"));

        System.out.println("System info");
        SYSTEM_INFO si = new SYSTEM_INFO();
        Kernel32.GetSystemInfo(si);
        Reflection.println(si);
    }
}
