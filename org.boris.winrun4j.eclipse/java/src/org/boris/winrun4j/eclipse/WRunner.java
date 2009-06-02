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
            subMonitor.beginTask(WMessages.WRunner_debugLaunch_title, 10);
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
                abort(WMessages.WRunner_exportError, e, IStatus.ERROR);
            } finally {
                monitor.done();
            }
            return;
        }

        try {
            monitor.subTask(WMessages.WRunner_extractLauncher_task);
            launcher = LauncherHelper
                    .createTemporaryLauncher(IWLaunchConfigurationConstants.LAUNCHER_TYPE_32_WIN);
            monitor.worked(1);
        } catch (Exception e) {
            abort(WMessages.WRunner_extractLauncher_error, e, IStatus.ERROR);
        }

        try {
            monitor.subTask(WMessages.WRunner_generateINI_task);
            inf = LauncherHelper.buildTemporaryIniFile(ini);
            monitor.worked(1);
        } catch (Exception e) {
            abort(WMessages.WRunner_generateINI_error, e, IStatus.ERROR);
        }

        String[] cmdLine = new String[] { launcher.getAbsolutePath(), "--WinRun4J:ExecuteINI", //$NON-NLS-1$
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
                abort(WMessages.WRunner_attachDebug_error, e, IStatus.ERROR);
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
                abort(WMessages.WRunner_attachDebug_error, e, IStatus.ERROR);
            }
        }

        monitor.done();
    }

    private ListeningConnector getListeningConnector() {
        List connectors = Bootstrap.virtualMachineManager().listeningConnectors();
        for (int i = 0; i < connectors.size(); i++) {
            ListeningConnector c = (ListeningConnector) connectors.get(i);
            if ("com.sun.jdi.SocketListen".equals(c.name())) { //$NON-NLS-1$
                return c;
            }
        }

        return null;
    }

    protected String renderDebugTarget(String classToRun, int port) {
        String format = WMessages.WRunner_debugTarget;
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
        int launcherType = launchConfig.getAttribute(
                IWLaunchConfigurationConstants.ATTR_LAUNCHER_TYPE,
                IWLaunchConfigurationConstants.LAUNCHER_TYPE_32_WIN);
        boolean wildcardCp = launchConfig.getAttribute(
                IWLaunchConfigurationConstants.ATTR_WILDCARD_CLASSPATH, false);

        // Copy over launcher
        monitor.beginTask(WMessages.WRunner_generateLauncher_task, 1);
        IO.copy(LauncherHelper.getLauncherOriginal(launcherType),
                new FileOutputStream(launcherFile), true);

        // Set icon if specified
        if (launcherIcon != null && launcherIcon.isFile() && launcherIcon.exists()) {
            monitor.beginTask(WMessages.WRunner_settingIcon_task, 1);
            LauncherHelper.runResourceEditor("/I", launcherFile, launcherIcon, false); //$NON-NLS-1$
        }

        // Generate classpath
        HashSet cpNames = new HashSet();
        int cpi = 1;
        while (true) {
            String k = IWINIConstants.CLASSPATH_PREFIX + cpi;
            String v = (String) ini.get(k);
            cpi++;
            if (Lang.isEmpty(v)) //$NON-NLS-1$
                break;

            File f = new File(v);
            if (f.isDirectory()) {
                // Generate unique name from folder
                String nf = f.getName();
                String nft = IO.removeExtension(f);
                if (nf.equals("bin")) { //$NON-NLS-1$
                    nft = f.getParentFile().getName();
                    nf = nft + ".jar"; //$NON-NLS-1$
                }
                int nfi = 2;
                while (cpNames.contains(nf)) {
                    nf = nft + "-" + nfi + ".jar"; //$NON-NLS-1$ //$NON-NLS-2$
                    nfi++;
                }
                cpNames.add(nf);

                // Generate jar file for entry
                File genf = new File(launcherDir, nf);
                File manifest = new File(new File(f.getParentFile(), "META-INF"), "MANIFEST.MF"); //$NON-NLS-1$ //$NON-NLS-2$
                File tf = File.createTempFile("winrun4j", ".jar"); //$NON-NLS-1$ //$NON-NLS-2$
                tf.deleteOnExit();
                IO.jar(f, manifest, tf);

                // Copy to output dir or embed in executable
                if (standard) {
                    IO.copy(new FileInputStream(tf), new FileOutputStream(genf), true);
                    tf.delete();
                    if (wildcardCp)
                        ini.remove(k);
                    else
                        ini.put(k, genf.getName());
                } else {
                    LauncherHelper.runResourceEditor("/J", launcherFile, tf, false); //$NON-NLS-1$
                    ini.remove(k);
                }
            } else {
                String fe = IO.removeExtension(f);
                String nf = fe + ".jar"; //$NON-NLS-1$
                int nfi = 2;
                while (cpNames.contains(nf)) {
                    nf = fe + "-" + nfi + ".jar"; //$NON-NLS-1$ //$NON-NLS-2$
                    nfi++;
                }
                cpNames.add(nf);
                File genf = new File(launcherDir, nf);
                if (standard) {
                    IO.copy(new FileInputStream(f), new FileOutputStream(genf), true);
                    if (wildcardCp)
                        ini.remove(k);
                    else
                        ini.put(k, genf.getName());
                } else {
                    LauncherHelper.runResourceEditor("/J", launcherFile, f, false); //$NON-NLS-1$
                    ini.remove(k);
                }
            }
        }

        // Now remove unwanted ini items
        ini.remove(IWINIConstants.VM_LOCATION);
        ini.remove(IWINIConstants.WORKING_DIRECTORY);
        if ("false".equals(ini.get(IWINIConstants.DDE_ENABLED))) {
            ini.remove(IWINIConstants.DDE_ENABLED);
            ini.remove(IWINIConstants.DDE_SERVER_NAME);
            ini.remove(IWINIConstants.DDE_TOPIC);
            ini.remove(IWINIConstants.DDE_WINDOW_CLASS);
        }
        if ("".equals(ini.get(IWINIConstants.LOG_FILE))) { //$NON-NLS-1$ 
            ini.remove(IWINIConstants.LOG_OVERWRITE);
        }
        if ("".equals(ini.get(IWINIConstants.SPLASH_IMAGE))) { //$NON-NLS-1$
            ini.remove(IWINIConstants.SPLASH_AUTOHIDE);
        }
        if (wildcardCp) {
            ini.put(IWINIConstants.CLASSPATH_PREFIX + 1, "*.jar");
            ini.put(IWINIConstants.CLASSPATH_PREFIX + 2, "*.zip");
        }

        // Create ini file
        File launcherIni = new File(launcherDir, IO.removeExtension(launcherFile) + ".ini"); //$NON-NLS-1$
        LauncherHelper.buildIniFile(launcherIni, ini);

        // Embed ini file if required
        if (exportType != IWLaunchConfigurationConstants.EXPORT_TYPE_STANDARD) {
            LauncherHelper.runResourceEditor("/N", launcherFile, launcherIni, false); //$NON-NLS-1$
            launcherIni.delete();
        }
    }
}
