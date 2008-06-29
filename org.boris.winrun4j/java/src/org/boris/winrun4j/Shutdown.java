package org.boris.winrun4j;

public class Shutdown {
    /**
     * Tells WinRun4J to restart itself when it shuts down.
     * 
     * @param restart.
     */
    public static native void setRestartOnShutdown(boolean restart);

    /**
     * Force the process to exit (with an exit code).
     * 
     * @param code.
     */
    public static native void exit(int code);
}
