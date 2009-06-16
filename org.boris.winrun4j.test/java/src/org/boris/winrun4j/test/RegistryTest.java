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

import org.boris.winrun4j.FileAssociation;
import org.boris.winrun4j.FileAssociations;
import org.boris.winrun4j.RegistryKey;

public class RegistryTest
{
    public static void main(String[] args) throws Exception {
        testTypes();
        testFileAss();
        testCreate();
        testFileAssSave();
    }

    public static void testTypes() throws Exception {
        RegistryKey k = RegistryKey.HKEY_CLASSES_ROOT.getSubKey("WMZFile");
        /*System.out.println(VariantObjectSerializer.encode(k.getBinary("EditFlags")));
        System.out.println(VariantObjectSerializer.encode(k
                .getDoubleWord("PreferExecuteOnMismatch")));*/
    }

    public static void testFileAss() throws Exception {
        /*System.out.println(VariantObjectSerializer.encode(FileAssociations.load(".acw")));*/
    }

    public static void testCreate() throws Exception {
        RegistryKey sf = RegistryKey.HKEY_LOCAL_MACHINE.getSubKey("SOFTWARE");
        if (!sf.exists()) {
            System.err.println("software key doesn't exist");
            return;
        }

        System.out.println(sf.getSubKey("WinRun4J").exists());
        RegistryKey s = sf.createSubKey("WinRun4J");
        System.out.println(s.exists());
        s.setString("Something", "Else");
        System.out.println(s.getString("Something"));
        s.setString(null, "Test");
        System.out.println(s.getString(null));
        s.deleteValue("Something");
        System.out.println(s.getString("Something"));
        s.deleteValue(null);
        System.out.println(s.getString(null));
        sf.deleteSubKey("WinRun4J");
        System.out.println(s.exists());
    }

    public static void testFileAssSave() throws Exception {
        FileAssociation fa = new FileAssociation(".winrun4j");
        fa.setName("WinRun4JLaunch");
        fa.setDescription("WinRun4J Launcher Configuration");
        fa.setPerceivedType("text");
        fa.addOpenWith("WinRun4J.exe");
        fa.setIcon("F:\\Development\\tools\\WinRun4J.exe");
        fa.addFileVerb("open", "F:\\Development\\tools\\WinRun4J.exe --WinRun4J:ExecuteINI %1");
        FileAssociations.delete(fa);
        FileAssociations.save(fa);
    }
}
