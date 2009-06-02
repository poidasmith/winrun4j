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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jdi.Bootstrap;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.launching.AbstractVMRunner;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.SocketUtil;
import org.eclipse.jdt.launching.VMRunnerConfiguration;

import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.ListeningConnector;

class WRunner extends AbstractVMRunner
{
    private IVMInstall vmInstall;
    private boolean debug;
    private ILaunchConfiguration launchConfig;
    private String mode;

    public WRunner(ILaunchConfiguration configuration, IVMInstall vmInstall, String mode) {
        this.launchConfig = configuration;
        this.vmInstall = vmInstall;
        this.debug = ILaunchManager.DEBUG_MODE.equals(mode);
        this.mode = mode;
    }

    protected String getPluginIdentifier() {
        return WActivator.getIdentifier();
    }

    public void run(VMRunnerConfiguration configuration, ILaunch launch, IProgressMonitor monitor)
            throws CoreException {
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }

        IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1);
        try {
            subMonitor.beginTask("WinRun4J Debug Launch", 10);
            doRun(configuration, launch, subMonitor);
        } finally {
            subMonitor.done();
        }
    }

    public void doRun(VMRunnerConfiguration configuration, ILaunch launch, IProgressMonitor monitor)
            throws CoreException {

        int port = -1;
        if (debug) {
            port = SocketUtil.findFreePort();
        }
        String jvm = LauncherHelper.getJVMPath(vmInstall);
        Map ini = LauncherHelper.generateIni(configuration, launchConfig, jvm, debug, port);
        File launcher = null;
        File inf = null;

        // If we are just exporting then fire off here
        if (IWLaunchConfigurationConstants.LAUNCH_TYPE_EXPORT.equals(mode)) {
            try {
                doExport(ini, monitor);
            } catch (Exception e) {
                abort("Could not export application", e, IStatus.ERROR);
            } finally {
                monitor.done();
            }
            return;
        }

        try {
            monitor.subTask("Extracting launcher executable");
            launcher = LauncherHelper.createTemporaryLauncher();
            monitor.worked(1);
        } catch (Exception e) {
            abort("Could not extract launcher", e, IStatus.ERROR);
        }

        try {
            monitor.subTask("Generating INI file...");
            inf = LauncherHelper.buildTemporaryIniFile(ini);
            monitor.worked(1);
        } catch (Exception e) {
            abort("Could not generate INI file for launch", e, IStatus.ERROR);
        }

        String[] cmdLine = new String[] { launcher.getAbsolutePath(), "--WinRun4J:ExecuteINI",
                inf.getAbsolutePath() };

        if (monitor.isCanceled())
            return;

        // Debug listener setup
        ListeningConnector lc = getListeningConnector();
        Map m = lc.defaultArguments();
        if (debug) {
            Connector.IntegerArgument pa = (Connector.IntegerArgument) m.get("port"); //$NON-NLS-1$
            pa.setValue(port);
            try {
                lc.startListening(m);
            } catch (Throwable e) {
                monitor.done();
                abort("Error attaching debugger...", e, IStatus.ERROR);
            }
        }

        monitor.worked(1);

        String wd = configuration.getWorkingDirectory();
        File wdf = wd == null ? null : new File(wd);
        Process p = exec(cmdLine, wdf, configuration.getEnvironment());
        if (p == null) {
            return;
        }

        monitor.worked(1);

        // check for cancellation
        if (monitor.isCanceled()) {
            p.destroy();
            return;
        }

        IProcess process = newProcess(launch, p, renderProcessLabel(cmdLine),
                getDefaultProcessMap());
        process.setAttribute(IProcess.ATTR_CMDLINE, renderCommandLine(cmdLine));

        // Check for debugger
        if (debug) {
            try {
                VirtualMachine vm = lc.accept(m);
                JDIDebugModel.newDebugTarget(launch, vm, renderDebugTarget(configuration
                        .getClassToLaunch(), port), process, true, false, configuration
                        .isResumeOnStartup());
            } catch (Exception e) {
                monitor.done();
                abort("Error attaching debugger...", e, IStatus.ERROR);
            }
        }

        monitor.done();
    }

    private ListeningConnector getListeningConnector() {
        List connectors = Bootstrap.virtualMachineManager().listeningConnectors();
        for (int i = 0; i < connectors.size(); i++) {
            ListeningConnector c = (ListeningConnector) connectors.get(i);
            if ("com.sun.jdi.SocketListen".equals(c.name())) {
                return c;
            }
        }

        return null;
    }

    protected String renderDebugTarget(String classToRun, int port) {
        String format = "{0} at localhost:{1}";
        return MessageFormat.format(format, new String[] { classToRun, String.valueOf(port) });
    }

    private String renderCommandLine(String[] cmdLine) {
        return getCmdLineAsString(cmdLine);
    }

    private String renderProcessLabel(String[] cmdLine) {
        return cmdLine[0];
    }

    private void doExport(Map ini, IProgressMonitor monitor) throws CoreException, IOException {
        File launcherFile = new File(launchConfig.getAttribute(
                IWLaunchConfigurationConstants.ATTR_LAUNCHER_FILE, (String) null));
        File launcherDir = launcherFile.getParentFile();
        String icon = launchConfig.getAttribute(IWLaunchConfigurationConstants.ATTR_LAUNCHER_ICON,
                (String) null);
        File launcherIcon = null;
        if (icon != null)
            launcherIcon = new File(icon);
        int exportType = launchConfig.getAttribute(IWLaunchConfigurationConstants.ATTR_EXPORT_TYPE,
                IWLaunchConfigurationConstants.EXPORT_TYPE_STANDARD);
        boolean standard = IWLaunchConfigurationConstants.EXPORT_TYPE_FAT != exportType;

        // Copy over launcher
        monitor.beginTask("Generating launcher file", 1);
        IO.copy(LauncherHelper.getLauncherOriginal(), new FileOutputStream(launcherFile), true);

        // Set icon if specified
        if (launcherIcon != null && launcherIcon.isFile() && launcherIcon.exists()) {
            monitor.beginTask("Setting launcher icon", 1);
            LauncherHelper.runResourceEditor("/I", launcherFile, launcherIcon);
        }

        // Generate classpath
        HashSet cpNames = new HashSet();
        int cpi = 1;
        while (true) {
            String k = "classpath." + cpi;
            String v = (String) ini.get(k);
            cpi++;
            if (v == null || "".equals(v))
                break;

            File f = new File(v);
            if (f.isDirectory()) {
                // Generate unique name from folder
                String nf = f.getName();
                String nft = IO.removeExtension(f);
                if (nf.equals("bin")) {
                    nft = f.getParentFile().getName();
                    nf = nft + ".jar";
                }
                int nfi = 2;
                while (cpNames.contains(nf)) {
                    nf = nft + "-" + nfi + ".jar";
                    nfi++;
                }
                cpNames.add(nf);

                // Generate jar file for entry
                File genf = new File(launcherDir, nf);
                File manifest = new File(new File(f.getParentFile(), "META-INF"), "MANIFEST.MF");
                File tf = File.createTempFile("winrun4j", ".jar");
                tf.deleteOnExit();
                IO.jar(f, manifest, tf);

                // Copy to output dir or embed in executable
                if (standard) {
                    IO.copy(new FileInputStream(tf), new FileOutputStream(genf), true);
                    tf.delete();
                    ini.put(k, genf.getName());
                } else {
                    LauncherHelper.runResourceEditor("/J", launcherFile, tf);
                    ini.remove(k);
                }
            } else {
                String fe = IO.removeExtension(f);
                String nf = fe + ".jar";
                int nfi = 2;
                while (cpNames.contains(nf)) {
                    nf = fe + "-" + nfi + ".jar";
                    nfi++;
                }
                cpNames.add(nf);
                File genf = new File(launcherDir, nf);
                if (standard) {
                    IO.copy(new FileInputStream(f), new FileOutputStream(genf), true);
                    ini.put(k, genf.getName());
                } else {
                    LauncherHelper.runResourceEditor("/J", launcherFile, f);
                    ini.remove(k);
                }
            }
        }

        // Now remove unwanted ini items
        ini.remove("vm.location");
        ini.remove("working.directory");
        if ("false".equals(ini.get("dde.enabled"))) {
            ini.remove("dde.enabled");
            ini.remove("dde.server.name");
            ini.remove("dde.topic");
            ini.remove("dde.window.class");
        }
        if ("".equals(ini.get("log"))) {
            ini.remove("log.overwrite");
        }
        if ("".equals(ini.get("splash.image"))) {
            ini.remove("splash.autohide");
        }

        // Create ini file
        File launcherIni = new File(launcherDir, IO.removeExtension(launcherFile) + ".ini");
        LauncherHelper.buildIniFile(launcherIni, ini);

        // Embed ini file if required
        if (exportType != IWLaunchConfigurationConstants.EXPORT_TYPE_STANDARD) {
            LauncherHelper.runResourceEditor("/N", launcherFile, launcherIni);
            launcherIni.delete();
        }
    }
}
