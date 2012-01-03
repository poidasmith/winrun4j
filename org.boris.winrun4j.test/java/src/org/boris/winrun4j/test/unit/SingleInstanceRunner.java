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

import org.boris.winrun4j.test.framework.Threads;

public class SingleInstanceRunner
{
    public static void main(String[] args) throws Exception {
        String mode = args[0];
        if (mode.equals("process")) {
            Threads.sleepQuietly(Long.MAX_VALUE);
        } else if (mode.equals("window")) {
        } else if (mode.equals("dde")) {
        } else {
            System.exit(1);
        }
    }
}
