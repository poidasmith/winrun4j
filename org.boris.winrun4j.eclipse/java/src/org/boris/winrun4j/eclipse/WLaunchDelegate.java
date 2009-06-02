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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.launching.IVMRunner;
import org.eclipse.jdt.launching.JavaLaunchDelegate;

public class WLaunchDelegate extends JavaLaunchDelegate
{
    public IVMRunner getVMRunner(ILaunchConfiguration configuration, String mode)
            throws CoreException {
        return new WRunner(configuration, verifyVMInstall(configuration), mode);
    }
}
