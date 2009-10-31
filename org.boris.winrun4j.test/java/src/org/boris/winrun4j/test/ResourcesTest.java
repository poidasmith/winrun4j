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

import org.boris.winrun4j.Native;
import org.boris.winrun4j.winapi.Kernel32;
import org.boris.winrun4j.winapi.ResourceEntry;
import org.boris.winrun4j.winapi.Resources;

public class ResourcesTest
{
    public static void main(String[] args) throws Exception {
        long module = Kernel32.loadLibraryEx("user32", Kernel32.LOAD_LIBRARY_AS_DATAFILE);
        ResourceEntry[] entries = Resources.findResources(module);
        for (ResourceEntry entry : entries) {
            Reflection.println(entry);
            Reflection.println(entry.type);
            Reflection.println(entry.name);
        }
        Native.freeLibrary(module);
    }
}
