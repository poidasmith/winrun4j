/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Peter Smith
 *******************************************************************************/
package org.boris.winrun4j.classloader;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class EmbeddedClassLoader extends ClassLoader
{
    URL[] urls;

    public EmbeddedClassLoader(ClassLoader parent) {
        super(parent);
        String cl = System.getProperty("java.class.path");
        StringTokenizer st = new StringTokenizer(cl, ";");
        ArrayList urls = new ArrayList();
        while (st.hasMoreTokens())
            try {
                urls.add(new URL(st.nextToken()));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        this.urls = (URL[]) urls.toArray(new URL[0]);
    }

    protected Class findClass(String name) throws ClassNotFoundException {
        return null;
    }

    public static native String[] listJars(String library);

    public static native byte[] getJar(String library, String jarName);
}
