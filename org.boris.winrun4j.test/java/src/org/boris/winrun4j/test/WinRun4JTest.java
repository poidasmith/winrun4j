/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Peter Smith
 *******************************************************************************/
package org.boris.winrun4j.test;

import org.boris.winrun4j.Native;

public class WinRun4JTest
{
    public static boolean isRunningInLauncher() {
        try {
            Native.loadLibrary("kernel32");
            return true;
        } catch(Throwable t) {
            return false;
        }
    }
    
    public static void main(String[] args) throws Exception {
        System.out.println(isRunningInLauncher());
    }
}
