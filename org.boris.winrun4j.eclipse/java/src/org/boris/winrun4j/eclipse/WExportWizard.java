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

import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;

public class WExportWizard extends Wizard implements IExportWizard
{
    private IWorkbench workbench;
    private IStructuredSelection selection;
    private WExportWizardPage exportWizardPage;

    public void addPages() {
        super.addPages();
        exportWizardPage = new WExportWizardPage(selection);
        addPage(exportWizardPage);
    }

    public boolean performFinish() {
        return false;
    }

    public void init(IWorkbench workbench, IStructuredSelection selection) {
        this.workbench = workbench;
        this.selection = selection;
        setWindowTitle("WinRun4J Fat Executable Export");
        setDefaultPageImageDescriptor(JavaPluginImages.DESC_WIZBAN_FAT_JAR_PACKAGER);
        setNeedsProgressMonitor(true);
    }
}
