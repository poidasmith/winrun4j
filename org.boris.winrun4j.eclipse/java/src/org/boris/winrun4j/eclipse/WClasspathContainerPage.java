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

import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.ui.wizards.IClasspathContainerPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

public class WClasspathContainerPage extends WizardPage implements IClasspathContainerPage
{
    public WClasspathContainerPage() {
        super("WinRun4JContainerPage");
        setTitle("WinRun4J Launcher Library");
        setDescription("Add the WinRun4J launcher library to this project");
        setImageDescriptor(JavaPluginImages.DESC_WIZBAN_ADD_LIBRARY);
    }

    public boolean finish() {
        return true;
    }

    public boolean canFlipToNextPage() {
        return true;
    }

    public boolean isPageComplete() {
        return true;
    }

    public IClasspathEntry getSelection() {
        return WClasspathContainerInitializer.getContainerEntry();
    }

    public void setSelection(IClasspathEntry containerEntry) {
    }

    public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);
    }
}
