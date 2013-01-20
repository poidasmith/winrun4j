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

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.Launch;
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
            WLaunchDelegate lcd = new WLaunchDelegate();
            ILaunchConfigurationWorkingCopy lc = ewp.getLaunchConfig().getWorkingCopy();
            lc.setAttribute(IWLaunchConfigurationConstants.ATTR_LAUNCHER_FILE, ewp
                    .getLauncherFile().getAbsolutePath());
            File icon = ewp.getLauncherIcon();
            if (icon != null)
                lc.setAttribute(IWLaunchConfigurationConstants.ATTR_LAUNCHER_ICON, icon
                        .getAbsolutePath());
            lc.setAttribute(IWLaunchConfigurationConstants.ATTR_EXPORT_TYPE, ewp.getExportType());
            lc.setAttribute(IWLaunchConfigurationConstants.ATTR_LAUNCHER_TYPE, ewp
                    .getLauncherType());
            lc.setAttribute(IWLaunchConfigurationConstants.ATTR_WILDCARD_CLASSPATH, ewp
                    .isWildcardClasspath());
            Launch l = new Launch(lc, IWLaunchConfigurationConstants.LAUNCH_TYPE_EXPORT, null);
            lcd.launch(lc, l.getLaunchMode(), l, new NullProgressMonitor());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void init(IWorkbench workbench, IStructuredSelection selection) {
        setWindowTitle(WMessages.WExportWizard_title);
        setDefaultPageImageDescriptor(WActivator.getImageDescriptor("icons/exportapp_wiz.png"));
        setNeedsProgressMonitor(true);
        setDialogSettings(WActivator.getDefault().getDialogSettings());
    }
}
