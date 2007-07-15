package org.boris.winrun4j;

/**
 * Used to control the splash screen.
 */
public class SplashScreen {
    /**
     * Grabs the splash screen window handle.
     *
     * @return long.
     */
    public static native long getWindowHandle();

    /**
     * Closes the splash.
     */
    public static native void close();
}
