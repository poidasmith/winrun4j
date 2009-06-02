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
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

public class WActivator extends AbstractUIPlugin
{
    private static BundleContext context;
    private static String id;
    private static String version;
    private static String versionId;
    private static WActivator instance;

    public void start(BundleContext context) throws Exception {
        super.start(context);
        WActivator.instance = this;
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

    public static WActivator getDefault() {
        return instance;
    }

    public static BundleContext getContext() {
        return context;
    }

    public void stop(BundleContext context) throws Exception {
        super.stop(context);
    }

    public static URL getBundleEntry(String path) {
        return context.getBundle().getEntry(path);
    }

    public static IPath getBundleLocation() {
        Bundle bundle = context.getBundle();
        if (bundle == null)
            return null;

        URL local = null;
        try {
            local = FileLocator.toFileURL(bundle.getEntry("/"));
        } catch (IOException e) {
            return null;
        }
        String fullPath = new File(local.getPath()).getAbsolutePath();
        return Path.fromOSString(fullPath);
    }

    public static Image getLauncherImage() {
        return createImage("/icons/winrun4j.gif");
    }

    private static Map images = new HashMap();
    private static Map descriptors = new HashMap();

    private static Image createImage(String location) {
        Image i = (Image) images.get(location);
        if (i == null) {
            images.put(location, i = ImageDescriptor.createFromURL(getBundleEntry(location))
                    .createImage());
        }
        return i;
    }

    public static ImageDescriptor getImageDescriptor(String location) {
        ImageDescriptor id = (ImageDescriptor) descriptors.get(location);
        if (id == null) {
            descriptors.put(location, id = ImageDescriptor.createFromURL(getBundleEntry(location)));
        }
        return id;
    }

    public static String getPreference(String key) {
        return getDefault().getPreferenceStore().getString(key);
    }
}
