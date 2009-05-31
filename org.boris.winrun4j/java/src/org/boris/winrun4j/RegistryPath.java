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

import java.util.StringTokenizer;

public class RegistryPath
{
    public static void setValue(String path, String value) throws RegistryException {
        // path="HKEY_CLASS_ROOT/.java/@", value="Hello World"

        StringTokenizer st = new StringTokenizer(path, "/");

    }
}
