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
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.preference.IPreferenceStore;

public class WClasspathContainerInitializer extends ClasspathContainerInitializer
{
    public static IPath WINRUN4J_CONTAINER_PATH = new Path(
            "org.boris.winrun4j.eclipse.WINRUN4J_CONTAINER");

    private static WClasspathContainer instance;
    private static IClasspathEntry entry;

    public static class WClasspathContainer implements IClasspathContainer
    {
        private IClasspathEntry[] entries;

        public WClasspathContainer(IClasspathEntry[] entries) {
            this.entries = entries;
        }

        public IClasspathEntry[] getClasspathEntries() {
            return entries;
        }

        public String getDescription() {
            return WMessages.classpathContainer_description;
        }

        public int getKind() {
            return IClasspathContainer.K_APPLICATION;
        }

        public IPath getPath() {
            return WINRUN4J_CONTAINER_PATH;
        }
    }

    public static synchronized WClasspathContainer getContainerInstance() {
        buildInstances();
        return instance;
    }

    public static synchronized IClasspathEntry getContainerEntry() {
        buildInstances();
        return entry;
    }

    public void initialize(IPath containerPath, IJavaProject project) throws CoreException {
        if (WINRUN4J_CONTAINER_PATH.equals(containerPath)) {
            WClasspathContainer container = getContainerInstance();
            JavaCore.setClasspathContainer(containerPath, new IJavaProject[] { project },
                    new IClasspathContainer[] { container }, null);
        }
    }

    private static synchronized void buildInstances() {
        if (instance == null) {
            IPath bundleBase = WActivator.getBundleLocation();
            IPath jarLocation = bundleBase.append("/launcher/WinRun4J.jar");
            IPreferenceStore prefs = WActivator.getDefault().getPreferenceStore();
            String libraryDir = prefs.getString(IWPreferenceConstants.LIBRARY_DIR);
            if (libraryDir != null) {
                jarLocation = new Path(libraryDir);
            }
            String libraryFile = prefs.getString(IWPreferenceConstants.LIBRARY_FILE);
            if (libraryFile != null && !"".equals(libraryFile)) {
                jarLocation = new Path(libraryFile);
            }
            IPath srcLocation = bundleBase.append("/launcher/WinRun4J-src.jar");
            String librarySrc = prefs.getString(IWPreferenceConstants.LIBRARY_SRC);
            if (librarySrc != null && !"".equals(librarySrc)) {
                srcLocation = new Path(librarySrc);
            }
            IClasspathEntry library = JavaCore.newLibraryEntry(jarLocation, srcLocation, null,
                    false);
            instance = new WClasspathContainer(new IClasspathEntry[] { library });
            entry = JavaCore.newContainerEntry(WINRUN4J_CONTAINER_PATH);
        }
    }
}
