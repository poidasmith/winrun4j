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

import org.boris.winrun4j.INI;
import org.boris.winrun4j.Log;

public class INITest
{
    public static void main(String[] args) throws Exception {
        String[] keys = INI.getPropertyKeys();
        for (int i = 0; i < keys.length; i++) {
            Log.info(keys[i] + "=" + INI.getProperty(keys[i]));
        }
    }
}
