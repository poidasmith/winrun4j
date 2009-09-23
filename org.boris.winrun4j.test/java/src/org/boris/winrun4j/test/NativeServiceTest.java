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

import java.util.Properties;

import org.boris.winrun4j.INI;

public class NativeServiceTest
{
    public static void main(String[] args) throws Exception {
        if (args != null && args.length == 1) {
            if ("-register".equals(args[0])) {
                register(INI.getProperties());
                return;
            } else if ("-unregister".equals(args[0])) {
                unregister(INI.getProperties());
            } else if ("-console".equals(args[0])) {
                // Run as a simple console app
                new NativeServiceTest().serviceStart(args);
                return;
            }
        }
    }

    public void serviceCtrlHandler(int opCode) {
    }

    public void serviceStart(String[] args) {
    }

    public void initialize(Properties ini) {
    }

    public static void register(Properties ini) {
    }

    public static void unregister(Properties ini) {
    }
}
