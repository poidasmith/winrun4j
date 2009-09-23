/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Peter Smith
 *******************************************************************************/
package org.boris.winrun4j;

import java.util.Properties;

/**
 * Provides access to the INI file used to startup the app.
 */
public class INI
{
    // Known INI keys
    public static final String MAIN_CLASS = ":main.class";
    public static final String SERVICE_CLASS = ":service.class";
    public static final String MODULE_NAME = "winrun4j:modulename";
    public static final String MODULE_INI = "winrun4j:moduleini";
    public static final String MODULE_DIR = "winrun4j:moduledir";
    public static final String INI_DIR = "winrun4j:inidir";
    public static final String WORKING_DIR = ":working.directory";
    public static final String SINGLE_INSTANCE = ":single.instance";
    public static final String DDE_SERVER_NAME = ":dd.server.name";
    public static final String DDE_TOPIC = ":dde.topic";
    public static final String SERVICE_ID = ":service.id";
    public static final String SERVICE_NAME = ":service.name";
    public static final String SERVICE_DESCRIPTION = ":service.description";
    public static final String SERVICE_CONTROLS = ":service.controls";
    public static final String SERVICE_STARTUP = ":service.startup";
    public static final String SERVICE_DEPENDENCY = ":service.dependency";
    public static final String SERVICE_USER = ":service.user";
    public static final String SERVICE_PWD = ":service.password";
    public static final String SERVICE_LOAD_ORDER_GROUP = ":service.loadordergroup";

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
