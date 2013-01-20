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

interface IWLaunchConfigurationConstants
{
    String TYPE = "org.boris.winrun4j.eclipse.launch";

    // This is a new launch type (ie. as opposed to "run" or "debug")
    String LAUNCH_TYPE_EXPORT = "export";

    // These extra properties are stored in the launch configuration file
    String PROP_LOG_LEVEL = "log.level";
    String PROP_SINGLE_INSTANCE = "single.instance";
    String PROP_PROCESS_PRIORITY = "process.priority";
    String PROP_LOG_OVERWRITE = "log.overwrite";
    String PROP_LOG_FILE = "log";
    String PROP_SPLASH_FILE = "splash.image";
    String PROP_SPLASH_AUTOHIDE = "splash.autohide";
    String PROP_DDE_ENABLED = "dde.enabled";
    String PROP_DDE_CLASS = "dde.class";
    String PROP_DDE_SERVER_NAME = "dde.server";
    String PROP_DDE_TOPIC = "dde.topic";
    String PROP_DDE_WINDOW_NAME = "dde.window";

    // These extra attributes are required for the export launch type
    String ATTR_LAUNCHER_FILE = "launcher.file";
    String ATTR_LAUNCHER_ICON = "launcher.icon";
    String ATTR_EXPORT_TYPE = "launcher.export.type";
    String ATTR_LAUNCHER_TYPE = "launcher.exe.type";
    String ATTR_WILDCARD_CLASSPATH = "launcher.wildcard.classpath";

    // How we export the final launcher
    public static final int EXPORT_TYPE_STANDARD = 1;
    public static final int EXPORT_TYPE_EMBED_INI = 2;
    public static final int EXPORT_TYPE_FAT = 3;

    // Which type of launcher to use
    public static final int LAUNCHER_TYPE_32_WIN = 1;
    public static final int LAUNCHER_TYPE_32_CONSOLE = 2;
    public static final int LAUNCHER_TYPE_64_WIN = 3;
    public static final int LAUNCHER_TYPE_64_CONSOLE = 4;
}
