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

import org.boris.winrun4j.RegistryKey;

public class Registry2Test
{

    public static void main(String[] args) throws Exception {
        RegistryKey key = RegistryKey.HKEY_CURRENT_USER.getSubKey("Software").createSubKey("WinRun4J");
        key.setDoubleWord("testone", 2);
        key.setString("teststr", "This is a test of some type of string we wish to send");
        key.setMultiString("multi", new String[] { "asdF", "234" });
        String[] ms = key.getMultiString("multi");
        for (int i = 0; i < ms.length; i++) {
            System.out.println(ms[i]);
        }
    }

    public static void main2(String[] args) throws Exception {
        System.out.println("\n\nRegistry Test\n=============\n");
        RegistryKey key = new RegistryKey(RegistryKey.HKEY_CURRENT_USER, "Control Panel\\Appearance\\Schemes");
        String[] names = key.getValueNames();
        for (int i = 0; i < names.length; i++) {
            System.out.println(names[i]);
        }

        key = new RegistryKey(RegistryKey.HKEY_LOCAL_MACHINE, "SOFTWARE");
        String[] keys = key.getSubKeyNames();
        for (int i = 0; i < keys.length; i++) {
            System.out.println(keys[i]);
        }
    }
}
