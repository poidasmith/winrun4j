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

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class WActivator implements BundleActivator
{
    private static String id;

    public void start(BundleContext context) throws Exception {
        id = context.getBundle().getSymbolicName();
    }

    public static String getIdentifier() {
        return id;
    }

    public void stop(BundleContext context) throws Exception {
    }
}
