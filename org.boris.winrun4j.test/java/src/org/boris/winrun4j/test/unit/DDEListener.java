/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Peter Smith
 *******************************************************************************/
package org.boris.winrun4j.test.unit;

import org.boris.winrun4j.ActivationListener;
import org.boris.winrun4j.DDE;
import org.boris.winrun4j.FileAssociationListener;
import org.boris.winrun4j.test.framework.TestHelper;

public class DDEListener
{
    public static void main(String[] args) throws Exception {
        DDE.addActivationListener(new ActivationListener() {
            public void activate(String cmdLine) {
                System.out.println("activate: " + cmdLine);
                if (cmdLine != null && cmdLine.endsWith(".quit.fte")) {
                    TestHelper.sleep(1000);
                    System.exit(0);
                }
            }
        });
        DDE.addFileAssocationListener(new FileAssociationListener() {
            public void execute(String cmdLine) {
                System.out.println("execute: " + cmdLine);
                if (cmdLine != null && cmdLine.endsWith(".quit.fte")) {
                    TestHelper.sleep(1000);
                    System.exit(0);
                }
            }
        });

        DDE.ready();
        Thread.sleep(100000);
    }
}
