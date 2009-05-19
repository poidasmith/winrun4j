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

    /**
     * Writes splash screen text.
     */
    public static native void setText(String text, int x, int y);

    /**
     * Sets the splash screen text font.
     */
    public static native void setTextFont(String text, int height);

    /**
     * Sets the splash screen text colour.
     */
    public static native void setTextColor(int r, int g, int b);

    /**
     * Sets the splash screen background text colour.
     */
    public static native void setTextBgColor(int r, int g, int b);
}
