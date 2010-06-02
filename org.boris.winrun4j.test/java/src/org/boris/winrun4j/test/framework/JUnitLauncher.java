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
import java.io.FilenameFilter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.boris.commons.io.IO;
import org.junit.Test;

public class JUnitLauncher
{
    public static void main(String[] args) throws Throwable {
        File[] classes = IO.findFiles(new File("bin"), new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".class");
            }
        });
        for (File f : classes) {
            String clazz = f.toString().substring(4);
            clazz = clazz.substring(0, clazz.length() - 6).replace('\\', '.');
            test(clazz);
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
            throw e.getTargetException();
        }
    }
}
