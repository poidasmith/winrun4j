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
import java.util.Map;
import java.util.TreeMap;

public class Launcher
{
    private Map<String, Map<String, String>> bundle = new TreeMap();
    private int classpathIndex;
    private int vmargIndex;
    private int argIndex;
    private int fileAssIndex;

    public Process launch() throws Exception {
        return null;
    }

    public Launcher main(Class clazz) {
        return main(clazz.getName());
    }

    public Launcher main(String main) {
        set(null, "main.class", main);
        return this;
    }

    public Launcher classpath(File f) {
        return classpath(f.getAbsolutePath());
    }

    public Launcher classpath(String entry) {
        classpathIndex++;
        set(null, "classpath." + classpathIndex, entry);
        return this;
    }

    public Launcher workingDir(File dir) {
        return workingDir(dir.getAbsolutePath());
    }

    public Launcher workingDir(String dir) {
        set(null, "working.directory", dir);
        return this;
    }

    public Launcher arg(String value) {
        set(null, "arg." + (++argIndex), value);
        return this;
    }

    public Launcher vmarg(String value) {
        set(null, "vmarg." + (++vmargIndex), value);
        return this;
    }

    public Launcher dde(boolean enabled, Class clazz) {
        set(null, "dde.enabled", enabled);
        set(null, "dde.class", clazz.getName());
        return this;
    }

    public Launcher fileAss(String ext, String name, String desc) {
        int index = fileAssIndex++;
        set("FileAssociations", "file." + index + ".extension", ext);
        set("FileAssociations", "file." + index + ".name", name);
        set("FileAssociations", "file." + index + ".description", desc);
        return this;
    }

    private void set(String section, String name, Object value) {
        Map<String, String> p = bundle.get(section);
        if (p == null)
            bundle.put(section, p = new TreeMap());
        p.put(name, String.valueOf(value));
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        toString(null, bundle.get(null), sb);
        for (String key : bundle.keySet()) {
            if (key != null)
                toString(key, bundle.get(key), sb);
        }

        return sb.toString();
    }

    private void toString(String sectionName, Map<String, String> section, StringBuilder sb) {
        if (sectionName != null) {
            sb.append("[");
            sb.append(sectionName);
            sb.append("]\n");
        }

        for (String key : section.keySet()) {
            sb.append(key);
            sb.append("=");
            sb.append(section.get(key));
            sb.append("\n");
        }

        sb.append("\n");
    }
}
