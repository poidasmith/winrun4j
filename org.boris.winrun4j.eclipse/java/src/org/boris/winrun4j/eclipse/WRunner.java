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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.jdt.launching.AbstractVMRunner;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.VMRunnerConfiguration;

public class WRunner extends AbstractVMRunner
{
    private IVMInstall vmInstall;
    private String mode;

    public WRunner(IVMInstall vmInstall, String mode) {
        this.vmInstall = vmInstall;
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

        Map ini = new HashMap();
        ini.put("main.class", configuration.getClassToLaunch());
        ini.put("vm.location", getJVMPath());
        String[] vmargs = configuration.getVMArguments();
        if (vmargs != null) {
            for (int i = 0; i < vmargs.length; i++) {
                ini.put("vmarg." + (i + 1), vmargs[i]);
            }
        }
        String[] cp = configuration.getClassPath();
        for (int i = 0; i < cp.length; i++) {
            ini.put("classpath." + (i + 1), cp[i]);
        }
        ini.put("working.directory", configuration.getWorkingDirectory());
        String[] args = configuration.getProgramArguments();
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                ini.put("arg." + (i + 1), args[i]);
            }
        }

        monitor.subTask("Generating INI file...");
        try {
            File inf = buildIniFile(ini);
        } catch (IOException e) {
        } finally {
            monitor.done();
        }
    }

    private File buildIniFile(Map ini) throws IOException {
        File f = File.createTempFile("winrun4j", ".ini");
        PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(f))));
        for (Iterator i = ini.keySet().iterator(); i.hasNext();) {
            String k = (String) i.next();
            String v = (String) ini.get(k);
            pw.print(k);
            pw.print("=");
            pw.println(v);
        }
        pw.flush();
        pw.close();
        return f;
    }

    private String getJVMPath() {
        File f = new File(vmInstall.getInstallLocation(), "bin" + File.separatorChar + "client"
                + File.separatorChar + "jvm.dll");
        if (!f.exists()) {
            f = new File(vmInstall.getInstallLocation(), "jre" + File.separatorChar + "bin"
                    + File.separatorChar + "jvm.dll");
        }
        return f.getAbsolutePath();
    }
}
