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
public class SplashScreen
{
    /**
     * Grabs the splash screen window handle.
     * 
     * @return long.
     */
    public static long getWindowHandle() {
        return NativeHelper.call(0, "SplashScreen_GetWindowHandle");
    }

    /**
     * Closes the splash.
     */
    public static void close() {
        NativeHelper.call(0, "SplashScreen_Close");
    }

    /**
     * Writes splash screen text.
     */
    public static void setText(String text, int x, int y) {
        long ptr = NativeHelper.toNativeString(text, false);
        NativeHelper.call(0, "SplashScreen_SetText", ptr, x, y);
        NativeHelper.free(ptr);
    }

    /**
     * Sets the splash screen text font.
     */
    public static void setTextFont(String text, int height) {
        long ptr = NativeHelper.toNativeString(text, false);
        NativeHelper.call(0, "SplashScreen_SetTextFont", ptr, height);
        NativeHelper.free(ptr);
    }

    /**
     * Sets the splash screen text colour.
     */
    public static void setTextColor(int r, int g, int b) {
        NativeHelper.call(0, "SplashScreen_SetTextColor", r, g, b);
    }

    /**
     * Sets the splash screen background text colour.
     */
    public static void setTextBgColor(int r, int g, int b) {
        NativeHelper.call(0, "SplashScreen_SetBbColor", r, g, b);
    }
}
