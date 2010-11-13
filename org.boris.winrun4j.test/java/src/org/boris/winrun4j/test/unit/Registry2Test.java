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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.boris.winrun4j.RegistryKey;
import org.boris.winrun4j.test.framework.TestHelper;
import org.junit.Test;

public class Registry2Test
{
    @Test
    public void test1() throws Exception {
        RegistryKey software = RegistryKey.HKEY_CURRENT_USER.getSubKey("Software");
        String keyName = "Testing-WinRun4J";
        RegistryKey key = software.createSubKey(keyName);

        // Check that key was created
        assertTrue(key.exists());

        // Doubles
        int ivalue = new Random().nextInt();
        key.setDoubleWord("testone", ivalue);
        assertEquals(ivalue, key.getDoubleWord("testone", -1));

        // Strings
        String ts = "This is a test of some type of string we wish to send";
        key.setString("teststr", ts);
        assertEquals(ts, key.getString("teststr"));

        // Multi strings
        String[] tsm = new String[] { "asdF", "234" };
        key.setMultiString("multi", tsm);
        String[] ms = key.getMultiString("multi");
        TestHelper.assertArrayEquals(tsm, ms);

        // Binary
        byte[] b1 = TestHelper.createRandomByteArray();
        key.setBinary("bin1", b1);
        TestHelper.assertArrayEquals(b1, key.getBinary("bin1"));

        // Clean up
        software.deleteSubKey(keyName);

        // Check that delete worked
        assertFalse(key.exists());
    }

    @Test
    public void testKeyPath() throws Exception {
        RegistryKey software = RegistryKey.HKEY_CURRENT_USER.getSubKey("Software");
        String keyName = "Testing-WinRun4J";
        RegistryKey key = software.createSubKey(keyName);
        key.setString(null, "Def");
        key.setString("other", "Def2");
        assertEquals("Def", software.get(keyName + "/"));
        assertEquals("Def2", software.get(keyName + "/other"));
    }

    public static void main(String[] args) throws Exception {
        new Registry2Test().testKeyPath();
    }
}
