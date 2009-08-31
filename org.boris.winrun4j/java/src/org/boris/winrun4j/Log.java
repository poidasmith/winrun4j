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

/**
 * Log to the launcher log.
 */
public class Log
{
    /**
     * Info log.
     * 
     * @param msg.
     */
    public static void info(String msg) {
        log(0, msg);
    }

    /**
     * Warning log.
     * 
     * @param msg.
     */
    public static void warning(String msg) {
        log(1, msg);
    }

    /**
     * Error log.
     * 
     * @param msg.
     */
    public static void error(String msg) {
        log(2, msg);
    }

    /**
     * Internal log.
     * 
     * @param level.
     * @param msg.
     */
    private static native void log(int level, String msg);
}
