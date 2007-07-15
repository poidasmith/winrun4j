package org.boris.winrun4j;

import java.util.Properties;


/**
 * Provides access to the INI file used to startup the app.
 */
public class INI {
    /**
     * Gets a property from the INI file.
     *
     * @param key.
     *
     * @return String.
     */
    public static native String getProperty(String key);

    /**
     * Gets the keys from the INI file.
     *
     * @return String.
     */
    public static native String[] getPropertyKeys();

    /**
     * Get the set of properties as a map.
     *
     * @return Map.
     */
    public static Properties getProperties() {
        Properties props = new Properties();
        String[] keys = getPropertyKeys();

        for (int i = 0; i < keys.length; i++) {
            props.put(keys[i], getProperty(keys[i]));
        }

        return props;
    }
}
