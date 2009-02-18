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

public class Test
{
    public static void main(String[] args) throws Exception {
        ClassLoader cl = "asdf".getClass().getClassLoader();
        Class c = "asdf".getClass();
        System.out.println(cl);
    }
}
