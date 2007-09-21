package org.boris.winrun4j;

/**
 * A listener for an execute call.
 */
public interface FileAssociationListener {
    /**
     * Execute based on the given command line.
     *
     * @param cmdLine.
     */
    void execute(String cmdLine);
}
