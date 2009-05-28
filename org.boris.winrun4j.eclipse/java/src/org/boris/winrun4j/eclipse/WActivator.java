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
    private static BundleContext context;
    private static String id;
    private static String version;
    private static String versionId;

    public void start(BundleContext context) throws Exception {
        WActivator.context = context;
        id = context.getBundle().getSymbolicName();
        version = (String) context.getBundle().getHeaders().get("Bundle-Version");
        versionId = id + "-" + version;
    }

    public static String getIdentifier() {
        return id;
    }

    public static String getVersionedIdentifier() {
        return versionId;
    }

    public static BundleContext getContext() {
        return context;
    }

    public void stop(BundleContext context) throws Exception {
    }
}
