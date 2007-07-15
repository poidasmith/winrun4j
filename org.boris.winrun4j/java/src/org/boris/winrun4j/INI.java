package org.boris.winrun4j;

import java.util.HashMap;
import java.util.Map;


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
    public static Map<String, String> getProperties() {
        Map<String, String> props = new HashMap();
        String[] keys = getPropertyKeys();

        for (String key : keys) {
            props.put(key, getProperty(key));
        }

        return props;
    }
}
