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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URISyntaxException;
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
    public static InputStream getLauncherOriginal() throws IOException {
        String lc = WActivator.getDefault().getPreferenceStore().getString(
                IWPreferenceConstants.LAUNCHER_LOCATION);
        InputStream is;
        if (lc != null) {
            is = new FileInputStream(lc);
        } else {
            is = WActivator.getBundleEntry("/launcher/WinRun4J.exe").openStream();
        }
        return is;
    }

    public static File buildTemporaryIniFile(Map ini) throws IOException {
        File f = File.createTempFile("winrun4j-", ".ini");
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
            if (k != null && v != null && !"".equals(v)) {
                pw.print(k);
                pw.print("=");
                pw.println(v);
            }
        }
        pw.flush();
        pw.close();
    }

    public static File createTemporaryLauncher() throws IOException {
        File tmpDir = new File(System.getProperty("java.io.tmpdir"));
        File launcher = new File(tmpDir, WActivator.getVersionedIdentifier() + "-launcher.exe");
        launcher.deleteOnExit();
        if (launcher.exists()) {
            return launcher;
        }
        FileOutputStream fos = new FileOutputStream(launcher);
        IO.copy(LauncherHelper.getLauncherOriginal(), fos, true);
        return launcher;
    }

    public static Map generateIni(VMRunnerConfiguration configuration,
            ILaunchConfiguration launchConfig, String vmLocation, boolean debug, int port)
            throws CoreException {
        Map ini = new TreeMap();
        ini.put("main.class", configuration.getClassToLaunch());
        if (vmLocation != null)
            ini.put("vm.location", vmLocation);
        String[] vmargs = configuration.getVMArguments();
        int offset = 1;
        if (debug) {
            ini.put("vmarg.1", "-Xdebug");
            ini.put("vmarg.2", "-Xnoagent");
            ini.put("vmarg.3", "-Xrunjdwp:transport=dt_socket,suspend=y,address=" + port);
            offset = 4;
        }
        if (vmargs != null) {
            for (int i = 0; i < vmargs.length; i++) {
                ini.put("vmarg." + (i + offset), vmargs[i]);
            }
        }
        String[] cp = configuration.getClassPath();
        for (int i = 0; i < cp.length; i++) {
            ini.put("classpath." + (i + 1), cp[i]);
        }
        String wd = configuration.getWorkingDirectory();
        if (wd != null)
            ini.put("working.directory", wd);
        String[] args = configuration.getProgramArguments();
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                ini.put("arg." + (i + 1), args[i]);
            }
        }
        ini.put("log.level", launchConfig.getAttribute(
                IWLaunchConfigurationConstants.PROP_LOG_LEVEL, (String) null));
        ini.put("log", launchConfig.getAttribute(IWLaunchConfigurationConstants.PROP_LOG_FILE,
                (String) null));
        ini.put("log.overwrite", Boolean.toString(launchConfig.getAttribute(
                IWLaunchConfigurationConstants.PROP_LOG_OVERWRITE, true)));
        ini.put("splash.image", launchConfig.getAttribute(
                IWLaunchConfigurationConstants.PROP_SPLASH_FILE, (String) null));
        ini.put("splash.autohide", Boolean.toString(launchConfig.getAttribute(
                IWLaunchConfigurationConstants.PROP_SPLASH_AUTOHIDE, true)));
        ini.put("dde.enabled", Boolean.toString(launchConfig.getAttribute(
                IWLaunchConfigurationConstants.PROP_DDE_ENABLED, false)));
        ini.put("dde.class", launchConfig.getAttribute(
                IWLaunchConfigurationConstants.PROP_DDE_CLASS, (String) null));
        ini.put("dde.server.name", launchConfig.getAttribute(
                IWLaunchConfigurationConstants.PROP_DDE_SERVER_NAME, (String) null));
        ini.put("dde.topic", launchConfig.getAttribute(
                IWLaunchConfigurationConstants.PROP_DDE_TOPIC, (String) null));
        ini.put("dde.window.class", launchConfig.getAttribute(
                IWLaunchConfigurationConstants.PROP_DDE_WINDOW_NAME, (String) null));
        ini.put("process.priority", launchConfig.getAttribute(
                IWLaunchConfigurationConstants.PROP_PROCESS_PRIORITY, (String) null));
        ini.put("single.instance", launchConfig.getAttribute(
                IWLaunchConfigurationConstants.PROP_SINGLE_INSTANCE, (String) null));

        return ini;
    }

    public static String getJVMPath(IVMInstall vmInstall) {
        File f = new File(vmInstall.getInstallLocation(), "bin" + File.separatorChar + "client"
                + File.separatorChar + "jvm.dll");
        if (!f.exists()) {
            f = new File(vmInstall.getInstallLocation(), "jre" + File.separatorChar + "bin"
                    + File.separatorChar + "client" + File.separatorChar + "jvm.dll");
        }
        return f.getAbsolutePath();
    }

    public static Process runResourceEditor(String option, File exeFile, File resourceFile)
            throws IOException {

        File rcEdit = null;
        URL rcUrl = FileLocator.toFileURL(WActivator.getBundleEntry("/launcher/RCEDIT.exe"));
        try {
            rcEdit = new File(rcUrl.toURI());
        } catch (URISyntaxException e) {
            rcEdit = new File(rcUrl.getPath());
        }
        ProcessBuilder pb = new ProcessBuilder(new String[] { rcEdit.getAbsolutePath(), option,
                exeFile.getAbsolutePath(), resourceFile.getAbsolutePath() });

        Process p = pb.start();
        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line = null;
        while ((line = br.readLine()) != null) {
            System.out.println(line);
        }

        return p;
    }
}
