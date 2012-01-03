/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Peter Smith
 *******************************************************************************/
package org.boris.winrun4j.test.framework;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

public class IO
{
    public static String toString(Reader r) throws IOException {
        StringWriter sw = new StringWriter();
        copy(r, sw, true);
        return sw.toString();
    }

    public static String toString(InputStream is) throws IOException {
        return toString(new InputStreamReader(is));
    }

    public static String toString(File f) throws IOException {
        return toString(new FileReader(f));
    }

    public static void copy(File source, File target) throws IOException {
        copy(new FileInputStream(source), new FileOutputStream(target), true);
    }

    public static void copy(Reader r, Writer w, boolean close) throws IOException {
        char[] buf = new char[4096];
        int len = 0;
        while ((len = r.read(buf)) > 0) {
            w.write(buf, 0, len);
        }
        if (close) {
            r.close();
            w.close();
        }
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

    public static String getNameSansExtension(File f) {
        if (f == null)
            return null;
        String n = f.getName();
        int idx = n.lastIndexOf('.');
        if (idx == -1)
            return n;
        return n.substring(0, idx);
    }

    public static String getExtension(File f) {
        if (f == null)
            return null;
        String n = f.getName();
        int idx = n.lastIndexOf('.');
        if (idx == -1) {
            return null;
        }
        return n.substring(idx + 1);
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

    public static byte[] toBytes(File file) throws IOException {
        byte[] b = new byte[(int) file.length()];
        FileInputStream fis = new FileInputStream(file);
        fis.read(b);
        fis.close();
        return b;
    }

    public static BufferedReader openUrl(String url) throws IOException {
        URL u = new URL(url);
        return new BufferedReader(new InputStreamReader(u.openStream()));
    }

    public static String toString(URL url) throws Exception {
        StringWriter sw = new StringWriter();
        IO.copy(IO.openUrl(url.toString()), sw, true);
        return sw.toString();
    }

    public static String[] readLines(Reader r) throws IOException {
        List l = new ArrayList();
        BufferedReader br = new BufferedReader(r);
        String line = null;
        while ((line = br.readLine()) != null) {
            l.add(line);
        }
        return (String[]) l.toArray(new String[0]);
    }

    public static String[] readLines(InputStream is) throws IOException {
        return readLines(new InputStreamReader(is));
    }

    public static String[] readLines(Class clazz, String resource) throws IOException {
        return readLines(new InputStreamReader(clazz.getResourceAsStream(resource)));
    }

    public static String[] toArray(StringTokenizer st) {
        List l = new ArrayList();
        while (st.hasMoreTokens()) {
            l.add(st.nextToken());
        }
        return (String[]) l.toArray(new String[0]);
    }

    public static void findFiles(File dir, FilenameFilter filter, FindFilesCallback callback) {
        File[] f = dir.listFiles();
        for (File fs : f) {
            if (fs.isDirectory()) {
                findFiles(fs, filter, callback);
            } else if (filter == null || filter.accept(dir, fs.getName())) {
                callback.fileFound(fs);
            }
        }
    }

    public static File[] findFiles(File dir, FilenameFilter filter) {
        Set<File> files = new HashSet();
        findFiles(dir, filter, files);
        return files.toArray(new File[0]);
    }

    private static void findFiles(File dir, FilenameFilter filter, Set<File> files) {
        File[] f = dir.listFiles();
        for (File ff : f) {
            if (ff.isDirectory()) {
                findFiles(ff, filter, files);
            } else {
                if (filter == null || filter.accept(ff.getParentFile(), ff.getName()))
                    files.add(ff);
            }
        }
    }

    public static void deleteDirectoryTree(File dir, boolean practice) {
        if (dir == null)
            return;
        File[] f = dir.listFiles();
        if (f == null)
            return;
        for (int i = 0; i < f.length; i++) {
            if (f[i].isDirectory()) {
                deleteDirectoryTree(f[i], practice);
            } else {
                if (practice) {
                    System.out.println(f[i]);
                } else {
                    f[i].delete();
                }
            }
        }

        if (practice) {
            System.out.println(dir);
        } else {
            dir.delete();
        }
    }

    public static Thread threadedCopy(final InputStream input, final OutputStream output) {
        Runnable r = new Runnable() {
            public void run() {
                try {
                    IO.copy(input, output, true);
                } catch (IOException e) {
                }
            }
        };
        Thread t = new Thread(r, "Threaded IO Copy");
        t.setDaemon(true);
        t.start();
        return t;
    }
}
