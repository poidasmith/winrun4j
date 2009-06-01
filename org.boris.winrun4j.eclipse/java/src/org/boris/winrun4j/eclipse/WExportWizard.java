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
import java.io.FileOutputStream;
import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;

public class WExportWizard extends Wizard implements IExportWizard
{
    private WExportWizardPage ewp;

    public void addPages() {
        super.addPages();
        ewp = new WExportWizardPage();
        ewp.setWizard(this);
        addPage(ewp);
    }

    public boolean performFinish() {
        try {
            ewp.saveSettings();
            exportLauncher(ewp.getLaunchConfig(), ewp.getLauncherFile(), ewp.getLauncherIcon(), ewp
                    .isStandardLauncher());
            return true;
        } catch (CoreException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void init(IWorkbench workbench, IStructuredSelection selection) {
        setWindowTitle(WMessages.exportWizard_title);
        setDefaultPageImageDescriptor(WActivator.getImageDescriptor("icons/exportapp_wiz.png"));
        setNeedsProgressMonitor(true);
        setDialogSettings(WActivator.getDefault().getDialogSettings());
    }

    public static void exportLauncher(ILaunchConfiguration launchConfig, File launcherFile,
            File iconFile, boolean standard) throws CoreException, IOException {
        // Create directory
        File launcherDir = launcherFile.getParentFile();
        if (!launcherDir.exists()) {
            if (!launcherDir.mkdirs())
                throw new CoreException(new Status(IStatus.ERROR, WActivator.getIdentifier(),
                        "Could not create launcher output directory"));
        }

        // Copy over launcher
        IO.copy(LauncherHelper.getLauncherOriginal(), new FileOutputStream(launcherFile), true);

        // Run RCEDIT to set icon
        if (iconFile != null) {
            LauncherHelper.runResourceEditor("/I", launcherFile, iconFile);
        }

        // Copy over generated jars
        IRuntimeClasspathEntry[] entries = JavaRuntime
                .computeUnresolvedRuntimeClasspath(launchConfig);
        entries = JavaRuntime.resolveRuntimeClasspath(entries, launchConfig);

        // Generate jars and copy over
        // Copy over ini file
        // - need naming scheme for directory entries on classpath
    }
}
