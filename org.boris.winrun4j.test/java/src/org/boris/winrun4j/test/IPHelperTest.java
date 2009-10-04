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

import org.boris.winrun4j.winapi.IPHelper;
import org.boris.winrun4j.winapi.ToolHelper;
import org.boris.winrun4j.winapi.IPHelper.MIB_TCPROW;
import org.boris.winrun4j.winapi.Kernel32.PROCESSENTRY32;

public class IPHelperTest
{
    public static void main(String[] args) throws Exception {
        MIB_TCPROW[] r = IPHelper.GetExtendedTcpTable(true, IPHelper.AF_INET, 5);
        Reflection.printArray(r, true);

        PROCESSENTRY32[] procs = ToolHelper.CreateProcessSnaphost();
        Reflection.printArray(procs, true);
    }
}
