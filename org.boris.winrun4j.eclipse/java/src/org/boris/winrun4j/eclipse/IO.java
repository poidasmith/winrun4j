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
    public static boolean is64Bit(File exe) throws IOException {
        InputStream is = new FileInputStream(exe);
        int magic = is.read() | is.read() << 8;
        if(magic != 0x5A4D) 
            throw new IOException("Invalid Exe");
        for(int i = 0; i < 58; i++) is.read();
        int address = is.read() | is.read() << 8 | is.read() << 16 | is.read() << 24;
        for(int i = 0; i < address - 60; i++) is.read();
        int machineType = is.read() | is.read() << 8;
        return machineType == 0x8664;
    }

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
        if (manifest != null && manifest.exists() && manifest.isFile()) {
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

    public static String removeExtension(File f) {
        if (f == null)
            return null;
        String n = f.getName();
        int idx = n.lastIndexOf('.');
        if (idx == -1)
            return n;
        return n.substring(0, idx);
    }
    
    public static Process exec(String[] args, boolean wait) throws IOException {
        final Process p = Runtime.getRuntime().exec(args);
        
        Thread stdout = threadedCopy(p.getInputStream(), System.out, false);
        Thread stderr = threadedCopy(p.getErrorStream(), System.out, false);
        if(wait) {
            try {
                stdout.join();
            } catch (InterruptedException e) {
            }
            try {
                stderr.join();
            } catch (InterruptedException e) {
            }
        }

        return p;
    }
    
    public static Thread threadedCopy(final InputStream is, final OutputStream os, final boolean close) {
        Thread t = new Thread(new Runnable() {
            public void run() {
                try {
                    copy(is, os, close);
                } catch (IOException e) {
                }
            }});
        t.setDaemon(true);
        t.start();
        return t;
    }
}
