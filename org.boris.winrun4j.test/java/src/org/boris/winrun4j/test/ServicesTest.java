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

import org.boris.winrun4j.Advapi32;
import org.boris.winrun4j.Services;
import org.boris.winrun4j.Services.ENUM_SERVICE_STATUS;

public class ServicesTest
{
    public static void main(String[] args) throws Exception {
        long handle = Services.OpenSCManager(null, null, Advapi32.SC_MANAGER_ALL_ACCESS);
        ENUM_SERVICE_STATUS[] ss = Services.EnumServiceStatus(handle, Services.SERVICE_WIN32, 3);
        if (ss != null) {
            for (int i = 0; i < ss.length; i++) {
                System.out.println(Reflection.toString(ss[i]));
            }
        }
    }

}
