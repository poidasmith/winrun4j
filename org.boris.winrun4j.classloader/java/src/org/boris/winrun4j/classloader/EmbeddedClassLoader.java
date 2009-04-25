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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class EmbeddedClassLoader extends URLClassLoader
{
    private String[] jars;
    private ByteBuffer[] buffers;

    public EmbeddedClassLoader() {
        super(makeUrls(), ClassLoader.getSystemClassLoader());
        jars = listJars(null);
        buffers = new ByteBuffer[jars.length];
        for (int i = 0; i < buffers.length; i++) {
            buffers[i] = getJar(null, jars[i]);
        }
    }

    private static URL[] makeUrls() {
        String p = System.getProperty("java.class.path");
        if (p == null)
            return new URL[0];
        StringTokenizer st = new StringTokenizer(p, ";");
        ArrayList urls = new ArrayList();
        while (st.hasMoreTokens()) {
            try {
                urls.add(new URL(st.nextToken()));
            } catch (MalformedURLException e) {
            }
        }
        return (URL[]) urls.toArray(new URL[0]);
    }

    public URL findResource(String name) {

        return null;
    }

    public InputStream getResourceAsStream(String name) {
        for (int i = 0; i < buffers.length; i++) {
            ByteBuffer bb = buffers[i];
            bb.position(0);
            ZipInputStream zis = new ZipInputStream(new ByteBufferInputStream(
                    bb));
            ZipEntry ze = null;
            try {
                while ((ze = zis.getNextEntry()) != null) {
                    if (name.equals(ze.getName())) {
                        return zis;
                    }
                }
            } catch (IOException e) {
                return null;
            }
        }
        return null;
    }

    protected Class findClass(String name) throws ClassNotFoundException {
        String cname = name.replace('.', '/').concat(".class");
        for (int i = 0; i < buffers.length; i++) {
            ByteBuffer bb = buffers[i];
            bb.position(0);
            ZipInputStream zis = new ZipInputStream(new ByteBufferInputStream(
                    bb));
            ZipEntry ze = null;
            try {
                while ((ze = zis.getNextEntry()) != null) {
                    String s = ze.getName();
                    if (cname.equals(s)) {
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        byte[] buf = new byte[4096];
                        int len = 0;
                        while ((len = zis.read(buf)) > 0) {
                            bos.write(buf, 0, len);
                        }
                        byte[] cb = bos.toByteArray();
                        return defineClass(name, cb, 0, cb.length);
                    }
                }
            } catch (IOException e) {
                throw new ClassNotFoundException(name, e);
            }
        }

        throw new ClassNotFoundException(name);
    }

    public static native ByteBuffer getJar(String library, String jarName);

    public static native String[] listJars(String library);
}
