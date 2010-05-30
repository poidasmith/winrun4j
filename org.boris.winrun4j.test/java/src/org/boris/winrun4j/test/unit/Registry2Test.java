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

import java.util.Random;

import junit.framework.TestCase;

import org.boris.winrun4j.RegistryKey;
import org.boris.winrun4j.test.framework.TestHelper;
import org.boris.winrun4j.winapi.Environment;
import org.junit.Test;

public class Registry2Test extends TestCase
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
        assertEquals(ivalue, key.getDoubleWord("testone"));

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

        // Expand string
        String es1 = "OK, %TEMP%";
        String expected = "OK, " + Environment.getEnvironmentVariable("TEMP");

        // Clean up
        software.deleteSubKey(keyName);

        // Check that delete worked
        assertFalse(key.exists());
    }
}
