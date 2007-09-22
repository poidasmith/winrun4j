package org.boris.winrun4j;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;


/**
 * A manager for DDE.
 */
public class DDE {
    private static Set fileAssociationListeners = new LinkedHashSet();

    /**
     * Add a file association listener.
     *
     * @param listener.
     */
    public static void addFileAssocationListener(
        FileAssociationListener listener) {
        DDE.fileAssociationListeners.add(listener);
    }

    /**
     * Execute a command. This will be called from WinRun4J binary.
     *
     * @param command.
     */
    public static void execute(String command) {
        Iterator i = fileAssociationListeners.iterator();
        while(i.hasNext()) {
            FileAssociationListener listener = (FileAssociationListener) i.next();
            listener.execute(command);
        }
    }
}
