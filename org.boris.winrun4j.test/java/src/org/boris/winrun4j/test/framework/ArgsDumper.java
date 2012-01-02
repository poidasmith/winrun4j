/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Peter Smith
 *******************************************************************************/
package org.boris.winrun4j.test.framework;

public class ArgsDumper
{
    public static void main(String[] args) throws Exception {
        for (int i = 0; i < args.length; i++) {
            System.out.print("'" + args[i] + "' ");
        }
        System.out.println();
    }
}
