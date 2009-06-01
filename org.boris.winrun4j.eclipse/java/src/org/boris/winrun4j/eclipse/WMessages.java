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

import org.eclipse.osgi.util.NLS;

class WMessages extends NLS
{
    private static final String BUNDLE_NAME = "org.boris.winrun4j.eclipse.WMessages"; //$NON-NLS-1$

    public static String exportWizard_title;
    public static String exportWizardPage_title;
    public static String exportWizardPage_description;
    public static String exportWizardPage_launchConfigLabel;
    public static String preferencePage_description;
    public static String classpathContainer_description;
    public static String classpathContainerPage_title;
    public static String classpathContainerPage_description;

    static {
        NLS.initializeMessages(BUNDLE_NAME, WMessages.class);
    }
}
