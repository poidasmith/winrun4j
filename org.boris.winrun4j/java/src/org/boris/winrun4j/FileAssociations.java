/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Peter Smith
 *******************************************************************************/
package org.boris.winrun4j;

public class FileAssociations
{
    /**
     * Register this module as the handler for the given file extension.
     */
    public static void register(String extension, String name, String description)
            throws RegistryException {
        RegistryPath.setString("HKEY_CLASSES_ROOT/" + extension + "/@", name);
        RegistryPath.setString("HKEY_CLASSES_ROOT/" + name + "/@", description);
        RegistryPath.setString("HKEY_CLASSES_ROOT/" + name + "/DefaultIcon/@", INI
                .getProperty(INI.MODULE_DIR));
        RegistryPath.setString("HKEY_CLASSES_ROOT/" + name + "/shell/Open/command/@", INI
                .getProperty(INI.MODULE_DIR)
                + " \"%1\"");
        RegistryPath.setString("HKEY_CLASSES_ROOT/" + name + "/shell/Open/ddeexec/@", "%1");
        RegistryPath
                .setString("HKEY_CLASSES_ROOT/" + name + "/shell/Open/ddeexec/application", "%1");
    }

    /**
     * Is this module registered as the handler for the given file extension?
     */
    public static boolean isModuleRegistered(String extension) {
        return false;
    }
}
