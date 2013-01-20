/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Peter Smith
 *******************************************************************************/
package org.boris.winrun4j.eclipse;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.VMRunnerConfiguration;

public class LauncherHelper
{
    public static InputStream getLauncherOriginal(int launcherType) throws IOException {
        String lc = WActivator.getDefault().getPreferenceStore().getString(
                IWPreferenceConstants.LAUNCHER_LOCATION);
        String path = null;
        switch (launcherType) {
        case IWLaunchConfigurationConstants.LAUNCHER_TYPE_32_CONSOLE:
            path = "/launcher/WinRun4Jc.exe"; //$NON-NLS-1$
            break;
        case IWLaunchConfigurationConstants.LAUNCHER_TYPE_32_WIN:
            path = "/launcher/WinRun4J.exe"; //$NON-NLS-1$
            break;
        case IWLaunchConfigurationConstants.LAUNCHER_TYPE_64_CONSOLE:
            path = "/launcher/WinRun4J64c.exe"; //$NON-NLS-1$
            break;
        case IWLaunchConfigurationConstants.LAUNCHER_TYPE_64_WIN:
            path = "/launcher/WinRun4J64.exe"; //$NON-NLS-1$
            break;
        default:
            throw new IOException(WMessages.LauncherHelper_invalidLauncherType + launcherType);
        }
        InputStream is;
        if (!Lang.isEmpty(lc)
                && launcherType == IWLaunchConfigurationConstants.LAUNCHER_TYPE_32_WIN) {
            is = new FileInputStream(lc);
        } else {
            is = WActivator.getBundleEntry(path).openStream();
        }
        return is;
    }

    public static File buildTemporaryIniFile(Map ini) throws IOException {
        File f = File.createTempFile("winrun4j-", ".ini"); //$NON-NLS-1$ //$NON-NLS-2$
        f.deleteOnExit();
        buildIniFile(f, ini);
        return f;
    }

    public static void buildIniFile(File f, Map ini) throws IOException {
        PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(f))));
        for (Iterator i = ini.keySet().iterator(); i.hasNext();) {
            String k = (String) i.next();
            String v = (String) ini.get(k);
            if (k != null && !Lang.isEmpty(v)) {
                pw.print(k);
                pw.print("="); //$NON-NLS-1$
                pw.println(v);
            }
        }
        pw.flush();
        pw.close();
    }

    public static File createTemporaryLauncher(int launcherType) throws IOException {
        String ll = WActivator.getPreference(IWPreferenceConstants.LAUNCHER_LOCATION);
        if (!Lang.isEmpty(ll)) {
            return new File(ll);
        }
        
        String suffix = "";
        switch(launcherType) {
        case IWLaunchConfigurationConstants.LAUNCHER_TYPE_32_CONSOLE:
            suffix = "c";
            break;
        case IWLaunchConfigurationConstants.LAUNCHER_TYPE_64_CONSOLE:
            suffix = "64c";
            break;
        case IWLaunchConfigurationConstants.LAUNCHER_TYPE_64_WIN:
            suffix = "64";
        }

        File tmpDir = new File(System.getProperty("java.io.tmpdir")); //$NON-NLS-1$
        File launcher = new File(tmpDir, WActivator.getVersionedIdentifier() + "-launcher" + suffix + ".exe"); //$NON-NLS-1$
        launcher.deleteOnExit();
        if (launcher.exists()) {
            return launcher;
        }
        FileOutputStream fos = new FileOutputStream(launcher);
        IO.copy(LauncherHelper.getLauncherOriginal(launcherType), fos, true);
        return launcher;
    }

    public static Map generateIni(VMRunnerConfiguration configuration,
            ILaunchConfiguration launchConfig, String vmLocation, boolean debug, int port)
            throws CoreException {
        Map ini = new TreeMap();
        ini.put(IWINIConstants.MAIN_CLASS, configuration.getClassToLaunch());
        if (vmLocation != null)
            ini.put(IWINIConstants.VM_LOCATION, vmLocation);
        String[] vmargs = configuration.getVMArguments();
        int offset = 1;
        if (debug) {
            ini.put(IWINIConstants.VMARG_PREFIX + 1, "-Xdebug"); //$NON-NLS-1$
            ini.put(IWINIConstants.VMARG_PREFIX + 2, "-Xnoagent"); //$NON-NLS-1$
            ini.put(IWINIConstants.VMARG_PREFIX + 3,
                    "-Xrunjdwp:transport=dt_socket,suspend=y,address=" + port); //$NON-NLS-1$
            offset = 4;
        }
        if (vmargs != null) {
            for (int i = 0; i < vmargs.length; i++) {
                ini.put(IWINIConstants.VMARG_PREFIX + (i + offset), vmargs[i]);
            }
        }
        String[] cp = configuration.getClassPath();
        for (int i = 0; i < cp.length; i++) {
            ini.put(IWINIConstants.CLASSPATH_PREFIX + (i + 1), cp[i]);
        }
        String wd = configuration.getWorkingDirectory();
        if (wd != null)
            ini.put(IWINIConstants.WORKING_DIRECTORY, wd);
        String[] args = configuration.getProgramArguments();
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                ini.put("arg." + (i + 1), args[i]); //$NON-NLS-1$
            }
        }
        String llp = WActivator.getPreference(IWPreferenceConstants.DEFAULT_LOG_LEVEL);
        String ll = launchConfig.getAttribute(IWLaunchConfigurationConstants.PROP_LOG_LEVEL,
                (String) null);
        if (Lang.isEmpty(ll))
            ll = llp;
        ini.put(IWINIConstants.LOG_LEVEL, ll);
        ini.put(IWINIConstants.LOG_FILE, launchConfig.getAttribute(
                IWLaunchConfigurationConstants.PROP_LOG_FILE, (String) null));
        ini.put(IWINIConstants.LOG_OVERWRITE, Boolean.toString(launchConfig.getAttribute(
                IWLaunchConfigurationConstants.PROP_LOG_OVERWRITE, true)));
        ini.put(IWINIConstants.SPLASH_IMAGE, launchConfig.getAttribute(
                IWLaunchConfigurationConstants.PROP_SPLASH_FILE, (String) null));
        ini.put(IWINIConstants.SPLASH_AUTOHIDE, Boolean.toString(launchConfig.getAttribute(
                IWLaunchConfigurationConstants.PROP_SPLASH_AUTOHIDE, true)));
        ini.put(IWINIConstants.DDE_ENABLED, Boolean.toString(launchConfig.getAttribute(
                IWLaunchConfigurationConstants.PROP_DDE_ENABLED, false)));
        ini.put(IWINIConstants.DDE_CLASS, launchConfig.getAttribute(
                IWLaunchConfigurationConstants.PROP_DDE_CLASS, (String) null));
        ini.put(IWINIConstants.DDE_SERVER_NAME, launchConfig.getAttribute(
                IWLaunchConfigurationConstants.PROP_DDE_SERVER_NAME, (String) null));
        ini.put(IWINIConstants.DDE_TOPIC, launchConfig.getAttribute(
                IWLaunchConfigurationConstants.PROP_DDE_TOPIC, (String) null));
        ini.put(IWINIConstants.DDE_WINDOW_CLASS, launchConfig.getAttribute(
                IWLaunchConfigurationConstants.PROP_DDE_WINDOW_NAME, (String) null));
        ini.put(IWINIConstants.PROCESS_PRIORITY, launchConfig.getAttribute(
                IWLaunchConfigurationConstants.PROP_PROCESS_PRIORITY, (String) null));
        ini.put(IWINIConstants.SINGLE_INSTANCE, launchConfig.getAttribute(
                IWLaunchConfigurationConstants.PROP_SINGLE_INSTANCE, (String) null));

        return ini;
    }

    public static String getJVMPath(IVMInstall vmInstall, boolean server) {
        String vmType = server ? "server" : "client";
        File f = new File(vmInstall.getInstallLocation(), "bin" + File.separatorChar + "client" //$NON-NLS-1$ //$NON-NLS-2$
                + File.separatorChar + "jvm.dll"); //$NON-NLS-1$
        if (!f.exists()) {
            f = new File(vmInstall.getInstallLocation(), "jre" + File.separatorChar + "bin" //$NON-NLS-1$ //$NON-NLS-2$
                    + File.separatorChar + vmType + File.separatorChar + "jvm.dll"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return f.getAbsolutePath();
    }

    public static Process runResourceEditor(String option, File exeFile, File resourceFile,
            boolean is64bit) throws IOException {
        String entry = is64bit ? "/launcher/RCEDIT64.exe":"/launcher/RCEDIT.exe";
        URL rcUrl = FileLocator.toFileURL(WActivator.getBundleEntry(entry)); 
        File rcTemp = File.createTempFile("rcedit-", ".exe");
        rcTemp.deleteOnExit();
        IO.copy(rcUrl.openStream(), new FileOutputStream(rcTemp), true);
        Process p = IO.exec(new String[] { rcTemp.getAbsolutePath(), option,
                exeFile.getAbsolutePath(), resourceFile.getAbsolutePath() }, true);
        p.destroy();
        rcTemp.delete();

        return p;
    }
}
