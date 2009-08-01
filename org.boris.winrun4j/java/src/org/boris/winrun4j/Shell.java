package org.boris.winrun4j;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Properties;
import java.util.StringTokenizer;

public class Shell
{
    /**
     * Retrieves a list of valid drives on the system.
     */
    public static File[] getLogicalDrives() {
        String s = getLogicalDriveStrings();
        ArrayList l = new ArrayList();
        StringTokenizer st = new StringTokenizer(s, "|");
        while (st.hasMoreTokens()) {
            l.add(new File(st.nextToken()));
        }
        return (File[]) l.toArray(new File[l.size()]);
    }

    /**
     * Retrieves a special folder path (@see FoldPathType).
     */
    public static File getFolderPath(int type) {
        String s = getFolderPathString(type);
        if (s != null && s.length() > 0) {
            return new File(s);
        } else {
            return null;
        }
    }

    /**
     * Retrieves an enviroment variable.
     */
    public static native String getEnvironmentVariable(String key);

    /**
     * Retrieves all the environment variables as a property bag.
     */
    public static Properties getEnvironmentVariables() {
        int[] arr = new int[1];
        ByteBuffer b = getEnvironmentStrings(arr);
        Properties p = new Properties();
        while (b.remaining() > 0) {
            String k = getString(b);
            int idx = k.indexOf('=');
            if (idx != -1) {
                p.put(k.substring(0, idx), k.substring(idx + 1));
            }
        }
        freeEnvironmentStrings(arr[0]);
        return p;
    }

    /**
     * Expands a string and replaces %value% with the environment variable
     * value.
     */
    public static native String expandEnvironmentString(String str);

    /**
     * Gets the full command line arguments for the application.
     */
    public static String[] getCommandLineArgs() {
        String s = getCommandLine();
        boolean inQuote = false;
        ArrayList args = new ArrayList();
        StringBuffer sb = new StringBuffer();
        int len = s.length();
        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            if (c == '"') {
                inQuote = !inQuote;
            }
            if (c == ' ') {
                if (inQuote) {
                    sb.append(c);
                } else {
                    args.add(sb.toString());
                    sb.setLength(0);
                }
            } else {
                sb.append(c);
            }
        }
        if (sb.length() > 0)
            args.add(sb.toString());

        return (String[]) args.toArray(new String[args.size()]);
    }

    public static OSVersionInfo getVersionInfo() {
        int[] v = getOSVersionNumbers();
        String csd = getOSVersionCSD();
        return new OSVersionInfo(v[0], v[1], v[2], v[3], v[4], v[5], v[6], v[7], v[8], csd);
    }

    private static native String getLogicalDriveStrings();

    private static native String getFolderPathString(int type);

    private static native ByteBuffer getEnvironmentStrings(int[] arr);

    private static native void freeEnvironmentStrings(int p);

    private static native String getCommandLine();

    private static native int[] getOSVersionNumbers();

    private static native String getOSVersionCSD();

    private static String getString(ByteBuffer b) {
        StringBuffer sb = new StringBuffer();
        while (true) {
            byte bb = b.get();
            if (bb == 0)
                break;
            sb.append((char) bb);
        }
        return sb.toString();
    }
}
