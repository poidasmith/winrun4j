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

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A manager for DDE.
 */
public class DDE
{
    private static Set listeners = new LinkedHashSet();

    /**
     * To be called by the application when it is ready to receive DDE messages.
     */
    public static native void ready();

    /**
     * Add a listener.
     * 
     * @param listener.
     */
    public static void addListener(DDEExecuteListener listener) {
        DDE.listeners.add(listener);
    }

    /**
     * Execute a command. This will be called from WinRun4J binary.
     * 
     * @param command.
     */
    public static void execute(String command) {
        Iterator i = listeners.iterator();
        while (i.hasNext()) {
            DDEExecuteListener listener = (DDEExecuteListener) i.next();
            listener.execute(command);
        }
    }
}
