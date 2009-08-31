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

import org.boris.winrun4j.DDE;
import org.boris.winrun4j.DDEListener;

public class DDESingleInstanceTest
{
    public static void main(String[] args) throws Exception {
        DDE.addListener(new DDEListener() {
            public void execute(String command) {
                System.out.println(command);
            }
        });
        DDE.ready();
        System.out.println("Hello world!");
    }
}
