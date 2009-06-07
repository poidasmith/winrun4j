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

import org.boris.variant.util.VariantObjectSerializer;
import org.boris.winrun4j.FileAssociations;
import org.boris.winrun4j.RegistryKey;
import org.boris.winrun4j.RegistryPath;

public class RegistryTest
{
    public static void main(String[] args) throws Exception {
        // testTypes();
        // testFileAss();
        testCreate();
    }

    public static void testTypes() throws Exception {
        RegistryKey k = RegistryKey.HKEY_CLASSES_ROOT.getSubKey("WMZFile");
        System.out.println(VariantObjectSerializer.encode(k.getBinary("EditFlags")));
        System.out.println(VariantObjectSerializer.encode(k
                .getDoubleWord("PreferExecuteOnMismatch")));
    }

    public static void testFileAss() throws Exception {
        System.out.println(VariantObjectSerializer.encode(FileAssociations.load(".acw")));
    }

    public static void testSimpleOutput() throws Exception {
        System.out.println(RegistryPath
                .getString("HKEY_LOCAL_MACHINE/SOFTWARE/Ericsson/Erlang/5.6.2/@"));
        System.out.println(RegistryPath
                .getString("HKEY_LOCAL_MACHINE\\SOFTWARE\\Apple Inc.\\Bonjour\\@FileVersion"));
        System.out.println(RegistryPath
                .getString("HKEY_LOCAL_MACHINE\\SOFTWARE\\Apple Inc.\\Bonjour\\@Version"));
    }

    public static void testCreate() throws Exception {
        RegistryKey sf = RegistryKey.HKEY_LOCAL_MACHINE.getSubKey("SOFTWARE");
        if (!sf.exists()) {
            System.err.println("software key doesn't exist");
            return;
        }

        RegistryKey s = sf.createSubKey("WinRun4J");
        s.setString("Something", "Else");
        s.setString(null, "Test");
    }
}
