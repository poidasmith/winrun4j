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

import org.boris.winrun4j.winapi.Advapi32;
import org.boris.winrun4j.winapi.Services;
import org.boris.winrun4j.winapi.Services.ENUM_SERVICE_STATUS;
import org.boris.winrun4j.winapi.Services.ENUM_SERVICE_STATUS_PROCESS;

public class ServicesTest
{
    public static void main(String[] args) throws Exception {
        // testEnumDeps();
        testGetName();
    }

    public static void testEnumEx() {
        long handle = Services.OpenSCManager(null, null, Advapi32.SC_MANAGER_ALL_ACCESS);
        ENUM_SERVICE_STATUS_PROCESS[] ss = Services.EnumServiceStatusEx(handle, Services.SERVICE_WIN32, 3, null);
        if (ss != null) {
            for (int i = 0; i < ss.length; i++) {
                System.out.println(Reflection.toString(ss[i], true));
            }
        }
    }

    public static void testEnumDeps() {
        long handle = Services.OpenSCManager(null, null, Advapi32.SC_MANAGER_ALL_ACCESS);
        long service = Services.OpenService(handle, "PlugPlay", Services.SERVICE_ALL_ACCESS);
        ENUM_SERVICE_STATUS[] ss = Services.EnumDependentServices(service, 3);
        if (ss != null) {
            for (int i = 0; i < ss.length; i++) {
                System.out.println(Reflection.toString(ss[i], true));
            }
        }
    }

    public static void testGetName() {
        long handle = Services.OpenSCManager(null, null, Advapi32.SC_MANAGER_ALL_ACCESS);
        System.out.println(Services.GetServiceDisplayName(handle, "RasAuto"));
        System.out.println(Services.GetServiceKeyName(handle, "Remote Access Auto Connection Manager"));
    }
}
