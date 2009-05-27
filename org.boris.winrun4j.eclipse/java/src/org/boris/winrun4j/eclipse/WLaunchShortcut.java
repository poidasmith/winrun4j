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

import org.eclipse.jdt.debug.ui.launchConfigurations.JavaApplicationLaunchShortcut;

public class WLaunchShortcut extends JavaApplicationLaunchShortcut
{
    protected String getTypeSelectionTitle() {
        return "WinRun4J Application";
    }
}
