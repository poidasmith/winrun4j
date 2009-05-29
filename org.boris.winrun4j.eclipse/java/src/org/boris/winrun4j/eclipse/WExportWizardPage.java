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

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.dialogs.WizardExportResourcesPage;

public class WExportWizardPage extends WizardExportResourcesPage
{
    private static final String PAGE_NAME = "WinRun4JExportWizardPage";

    protected WExportWizardPage(IStructuredSelection selection) {
        super(PAGE_NAME, selection);
    }

    protected void createDestinationGroup(Composite parent) {
    }

    public void handleEvent(Event event) {
    }

    public boolean isPageComplete() {
        return true;
    }
}
