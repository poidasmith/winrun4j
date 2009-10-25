/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Peter Smith
 *******************************************************************************/
package org.boris.winrun4j;

import org.boris.winrun4j.winapi.ResourceEntry;
import org.boris.winrun4j.winapi.Resources;

public class ResourceEditor
{
    public static int mainc(String[] args, boolean is64) {
        if (args == null || args.length == 0) {
            printUsage(false);
            return 1;
        }

        boolean ok = true;

        if (strcmp(args[1], "/I") == 0) {
            if (args.length != 4)
                return printUsage(is64);
            String exeFile = args[2];
            String iconFile = args[3];
            ok = setIcon(exeFile, iconFile);
        } else if (strcmp(args[1], "/A") == 0) {
            if (args.length != 4)
                return printUsage(is64);
            String exeFile = args[2];
            String iconFile = args[3];
            ok = addIcon(exeFile, iconFile);
        } else if (strcmp(args[1], "/N") == 0) {
            if (args.length != 4)
                return printUsage(is64);
            String exeFile = args[2];
            String iniFile = args[3];
            ok = setINI(exeFile, iniFile);
        } else if (strcmp(args[1], "/J") == 0) {
            if (args.length != 4)
                return printUsage(is64);
            String exeFile = args[2];
            String jarFile = args[3];
            ok = addJar(exeFile, jarFile);
        } else if (strcmp(args[1], "/H") == 0) {
            if (args.length != 4)
                return printUsage(is64);
            String exeFile = args[2];
            String htmlFile = args[3];
            ok = addHTML(exeFile, htmlFile);
        } else if (strcmp(args[1], "/S") == 0) {
            if (args.length != 4)
                return printUsage(is64);
            String exeFile = args[2];
            String splashFile = args[3];
            ok = setSplash(exeFile, splashFile);
        } else if (strcmp(args[1], "/M") == 0) {
            if (args.length != 4)
                return printUsage(is64);
            String exeFile = args[2];
            String manifestFile = args[3];
            ok = setManifest(exeFile, manifestFile);
        } else if (strcmp(args[1], "/R") == 0) {
            if (args.length != 4)
                return printUsage(is64);
            String exeFile = args[2];
            String iniFile = args[3];
            return executeResourceScript(exeFile, iniFile, false);
        } else if (strcmp(args[1], "/W") == 0) {
            if (args.length != 4)
                return printUsage(is64);
            String exeFile = args[2];
            String iniFile = args[3];
            return executeResourceScript(exeFile, iniFile, true);
        } else if (strcmp(args[1], "/C") == 0) {
            if (args.length != 3)
                return printUsage(is64);
            String exeFile = args[2];
            ok = clearResources(exeFile);
        } else if (strcmp(args[1], "/L") == 0) {
            if (args.length != 3)
                return printUsage(is64);
            String exeFile = args[2];
            ok = listResources(exeFile);
        } else if (strcmp(args[1], "/P") == 0) {
            if (args.length != 3)
                return printUsage(is64);
            String exeFile = args[2];
            ok = listINI(exeFile);
        } else if (strcmp(args[1], "/D") == 0) {
            return printScriptHelp();
        } else {
            return printUsage(is64);
        }

        if (ok)
            Log.info("Ok");

        return ok ? 0 : 1;
    }

    private static boolean listINI(String exeFile) {
        return false;
    }

    private static boolean listResources(String exeFile) {
        return false;
    }

    private static boolean clearResources(String exeFile) {
        long hModule = Native.loadLibrary(exeFile);
        if (hModule == 0) {
            Log.error("Could not load exe to clear resources: " + exeFile);
            return false;
        }

        ResourceEntry[] entries = Resources.findResources(hModule);
        Native.freeLibrary(hModule);

        if (entries == null || entries.length == 0)
            return true;

        long hUpdate = Resources.beginUpdateResource(exeFile, false);
        if (hUpdate == 0) {
            Log.error("Could not load exe to clear resources: " + exeFile);
            return false;
        }

        for (int i = 0; i < entries.length; i++) {
            Resources.updateResource(hUpdate, entries[i], 0, 0);
        }

        Resources.endUpdateResource(hUpdate, false);

        return true;
    }

    private static int executeResourceScript(String exeFile, String iniFile, boolean b) {
        return 0;
    }

    private static boolean setManifest(String exeFile, String manifestFile) {
        return false;
    }

    private static boolean setSplash(String exeFile, String splashFile) {
        return false;
    }

    private static boolean addHTML(String exeFile, String htmlFile) {
        return false;
    }

    private static boolean addJar(String exeFile, String jarFile) {
        return false;
    }

    private static boolean setINI(String exeFile, String iniFile) {
        return false;
    }

    private static boolean addIcon(String exeFile, String iconFile) {
        return false;
    }

    private static boolean setIcon(String exeFile, String iconFile) {
        return false;
    }

    private static int strcmp(String s1, String s2) {
        return s1.compareTo(s2);
    }

    private static int printUsage(boolean is64) {
        if (is64)
            printf("WinRun4J 64-bit Resource Editor v1.0 (winrun4j.sf.net)\n\n");
        else
            printf("WinRun4J Resource Editor v1.0 (winrun4j.sf.net)\n\n");
        printf("Edits resources in executables (EXE) and dynamic link-libraries (DLL).\n\n");
        if (is64)
            printf("RCEDIT64 <option> <exe/dll> [resource]\n\n");
        else
            printf("RCEDIT <option> <exe/dll> [resource]\n\n");
        printf("  filename\tSpecifies the filename of the EXE/DLL.\n");
        printf("  resource\tSpecifies the name of the resource to add to the EXE/DLL.\n");
        printf("  /I\t\tSet the icon as the default icon for the executable.\n");
        printf("  /A\t\tAdds an icon to the EXE/DLL.\n");
        printf("  /N\t\tSets the INI file.\n");
        printf("  /J\t\tAdds a JAR file.\n");
        printf("  /E\t\tExtracts a JAR file from the EXE/DLL.\n");
        printf("  /S\t\tSets the splash image.\n");
        printf("  /H\t\tAdds an HTML file the EXE/DLL.\n");
        printf("  /C\t\tClears all resources from the EXE/DLL.\n");
        printf("  /L\t\tLists the resources in the EXE/DLL.\n");
        printf("  /P\t\tOutputs the contents of the INI file in the EXE.\n");
        printf("  /R\t\tLoads a script file listing resource settings.\n");
        printf("  /W\t\tSame as /R but clears all resources first.\n");
        printf("  /D\t\tFurther help on /R command.\n");

        return 1;
    }

    private static int printScriptHelp() {
        printf("Use /R to set a series of resource options on a single executable.\n\n");
        printf("RCEDIT /R <exe/dll> <script file>\n\n");
        printf("ini=<ini file>\n");
        printf("icon.1=<main icon file>\n");
        printf("icon.2=<extra icon file>\n");
        printf("icon.n=<extra icon file>\n");
        printf("jar.1=<jar file>\n");
        printf("html.1=<html file>\n");
        return 1;
    }

    private static void printf(String str) {
        System.out.print(str);
    }
}
