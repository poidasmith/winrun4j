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
import java.io.FileWriter;
import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.Map;

import org.boris.commons.io.IO;
import org.boris.commons.io.ProcessResult;
import org.boris.winrun4j.Log;

public class Launcher
{
    public static final File LAUNCHER = new File(
            "F:\\eclipse\\workspace\\WinRun4J\\build\\WinRun4J-Debug\\WinRun4J.exe");

    private Map<String, Map<String, String>> bundle = new LinkedHashMap();
    private int classpathIndex;
    private int vmargIndex;
    private int argIndex;
    private int fileAssIndex;

    public ProcessResult launch(String... args) throws Exception {
        File launcher = File.createTempFile("winrun4j.launcher.", ".exe");
        launcher.deleteOnExit();
        IO.copy(LAUNCHER, launcher);
        File ini = new File(launcher.getParent(), IO.getNameSansExtension(launcher) + ".ini");
        ini.deleteOnExit();
        IO.copy(new StringReader(toString()), new FileWriter(ini), true);
        String[] cmd = new String[args == null ? 1 : args.length + 1];
        cmd[0] = launcher.getAbsolutePath();
        if (args != null) {
            System.arraycopy(args, 0, cmd, 1, args.length);
        }
        return new ProcessResult(Runtime.getRuntime().exec(cmd));
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

    public Launcher vmVersion(String min, String max, String exact) {
        set(null, "vm.version.min", min);
        set(null, "vm.version.max", max);
        set(null, "vm.version", exact);
        return this;
    }

    public Launcher vmLocation(File location) {
        return vmLocation(location.getAbsolutePath());
    }

    public Launcher vmLocation(String location) {
        set(null, "vm.location", location);
        return this;
    }

    public Launcher log(Log.Level level) {
        set(null, "log.level", level.getText());
        return this;
    }

    public Launcher log(String file, Log.Level level, boolean overwrite, boolean andConsole) {
        set(null, "log", file);
        set(null, "log.level", level.getText());
        set(null, "log.overwrite", Boolean.toString(overwrite));
        set(null, "log.file.and.console", Boolean.toString(andConsole));
        return this;
    }

    public Launcher logRoll(double rollSize, String prefix, String suffix) {
        set(null, "log.roll.size", Double.toString(rollSize));
        set(null, "log.roll.prefix", prefix);
        set(null, "log.roll.suffix", suffix);
        return this;
    }

    public Launcher splash(String image, boolean autohide) {
        set(null, "splash.image", image);
        set(null, "splash.autohide", autohide);
        return this;
    }

    public Launcher dde(boolean enabled, Class clazz) {
        set(null, "dde.enabled", enabled);
        set(null, "dde.class", clazz.getName());
        return this;
    }

    public Launcher ddeServer(String server, String topic, String windowClass) {
        set(null, "dde.server.name", server);
        set(null, "dde.topic", topic);
        set(null, "dde.window.class", windowClass);
        return this;
    }

    public Launcher fileAss(String ext, String name, String desc) {
        int index = ++fileAssIndex;
        set("FileAssociations", "file." + index + ".extension", ext);
        set("FileAssociations", "file." + index + ".name", name);
        set("FileAssociations", "file." + index + ".description", desc);
        return this;
    }

    private void set(String section, String name, Object value) {
        if (value == null)
            return;
        Map<String, String> p = bundle.get(section);
        if (p == null)
            bundle.put(section, p = new LinkedHashMap());
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

    public Launcher testcp() {
        return classpath("F:\\eclipse\\workspace\\org.boris.winrun4j.test\\bin").
                classpath("F:\\eclipse\\workspace\\org.boris.winrun4j\\bin").
                classpath("F:\\eclipse\\platform3.5\\plugins\\org.junit*\\*.jar");
    }
}
