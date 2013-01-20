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

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.junit.Test;

public class JUnitLauncher
{
    public static void main(String[] args) throws Throwable {
        String[] cpElements = System.getProperty("java.class.path").split(";");
        List<String> classes = new ArrayList<String>();
        for(String elem : cpElements) {
            File f = new File(elem);
            if(!f.exists())
                continue;
            if(f.isDirectory())
                findClassesInDir(f, classes);
            else 
                findClassesInZip(f, classes);
        }
        for(String clazz : classes) {
            if(clazz.contains("winrun4j.test"))
                test(clazz);
        }
    }

    private static void findClassesInZip(File f, List<String> classes) throws Throwable {
        ZipInputStream zis = new ZipInputStream(new FileInputStream(f));
        ZipEntry ze = null;
        
        while((ze = zis.getNextEntry()) != null) {
            String s = ze.getName();
            if(s.endsWith(".class")) {
                s = s.substring(0, s.length() - 6);
                s = s.replaceAll("/", ".");
                classes.add(s);
            }
        }
    }

    private static void findClassesInDir(File dir, List<String> classes) throws Throwable {
        File[] files = IO.findFiles(dir, new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".class");
            }
        });
        for(File f : files) {
            String clazz = f.toString().substring(dir.toString().length() + 1);
            clazz = clazz.substring(0, clazz.length() - 6);
            clazz = clazz.replaceAll("\\\\", ".");
            classes.add(clazz);
        }
    }

    public static void test(String className) throws Throwable {
        Class cl = Class.forName(className);
        Method[] ms = cl.getMethods();
        for (Method m : ms) {
            if (Modifier.isStatic(m.getModifiers()))
                continue;
            if (!Modifier.isPublic(m.getModifiers()))
                continue;
            if (m.getName().startsWith("test")) {
                test(cl, m);
            } else {
                Test t = m.getAnnotation(Test.class);
                if (t != null) {
                    test(cl, m);
                }
            }
        }
    }

    private static void test(Class cl, Method m) throws Throwable {
        System.out.println(cl.getName() + "." + m.getName());
        Object o = cl.newInstance();
        try {
            m.invoke(o);
        } catch (InvocationTargetException e) {
            e.getTargetException().printStackTrace();
        }
    }
}
