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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

class IO
{
    public static void copy(InputStream r, OutputStream w, boolean close) throws IOException {
        byte[] buf = new byte[4096];
        int len = 0;
        while ((len = r.read(buf)) > 0) {
            w.write(buf, 0, len);
        }
        if (close) {
            r.close();
            w.close();
        }
    }

    public static File[] findFiles(File dir, FilenameFilter filter) {
        Set files = new HashSet();
        findFiles(dir, filter, files);
        return (File[]) files.toArray(new File[0]);
    }

    private static void findFiles(File dir, FilenameFilter filter, Set files) {
        File[] f = dir.listFiles();
        for (int i = 0; i < f.length; i++) {
            File ff = f[i];
            if (ff.isDirectory()) {
                findFiles(ff, filter, files);
            } else {
                if (filter == null || filter.accept(ff.getParentFile(), ff.getName()))
                    files.add(ff);
            }
        }
    }

    public static void jar(File directory, File manifest, File jar) throws IOException {
        JarOutputStream jos = new JarOutputStream(new FileOutputStream(jar));
        if (manifest != null) {
            jos.putNextEntry(new ZipEntry("META-INF/MANIFEST.MF"));
            copy(new FileInputStream(manifest), jos, false);
        }
        File[] classes = findFiles(directory, null);
        int len = directory.toString().length();
        for (int i = 0; i < classes.length; i++) {
            File f = classes[i];
            if (f.isFile()) {
                String n = f.toString().substring(len + 1);
                n = n.replace('\\', '/');
                jos.putNextEntry(new ZipEntry(n));
                copy(new FileInputStream(f), jos, false);
            }
        }
        jos.close();
    }
}
