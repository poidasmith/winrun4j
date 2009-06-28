package org.boris.winrun4j;

import java.io.File;
import java.util.Properties;

public class Shell
{
    public static native File[] getLogicalDrives();
    
    public static native String getEnvironmentVariable(String key);
    
    public static native Properties getEnvironmentVariables();
    
    public static native OSVersionInfo getVersionInfo();
}
