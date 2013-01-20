/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Peter Smith
 *******************************************************************************/
package org.boris.winrun4j.winapi;

import java.util.ArrayList;

import org.boris.winrun4j.winapi.Kernel32.PROCESSENTRY32;

public class ToolHelper
{
    public static PROCESSENTRY32[] createProcessSnaphost() {
        long handle = Kernel32.CreateToolhelp32Snapshot(2, 0);
        ArrayList pes = new ArrayList();
        PROCESSENTRY32 pe = new PROCESSENTRY32();
        if (Kernel32.Process32First(handle, pe)) {
            pes.add(pe);
            PROCESSENTRY32 pen = new PROCESSENTRY32();
            while (Kernel32.Process32Next(handle, pen)) {
                pes.add(pen);
                pen = new PROCESSENTRY32();
            }
        }
        if (pes.size() == 0)
            return null;

        return (PROCESSENTRY32[]) pes.toArray(new PROCESSENTRY32[pes.size()]);
    }
}
