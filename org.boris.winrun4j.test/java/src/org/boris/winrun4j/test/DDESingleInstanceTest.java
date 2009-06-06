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

import org.boris.winrun4j.ActivationListener;
import org.boris.winrun4j.DDE;
import org.boris.winrun4j.Log;

public class DDESingleInstanceTest
{
    public static void main(String[] args) throws Exception {
        DDE.addActivationListener(new ActivationListener() {
            public void activate() {
                Log.info("Activate");
            }
        });
        ready();
        System.out.println("Hello world!");
    }

    public static native void ready();

    public static void execute(String command) {
        System.out.println("Execute: " + command);
    }

    public static void activate() {
        System.out.println("Activating...");
    }
}
