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

import org.boris.winrun4j.Native;
import org.boris.winrun4j.NativeHelper;
import org.boris.winrun4j.winapi.Kernel32.PROCESSENTRY32;

public class ToolHelper
{
    public static PROCESSENTRY32[] createProcessSnaphost() {
        long handle = Kernel32.createToolhelp32Snapshot(2, 0);
        long lppe = Native.malloc(Kernel32.PROCESSENTRY32.SIZE);
        NativeHelper.setInt(lppe, Kernel32.PROCESSENTRY32.SIZE);
        ArrayList pes = new ArrayList();
        PROCESSENTRY32 pe = new PROCESSENTRY32();
        if (Kernel32.process32First(handle, lppe)) {
            Kernel32.decode(lppe, pe);
            pes.add(pe);
            while (Kernel32.process32Next(handle, lppe)) {
                pe = new PROCESSENTRY32();
                Kernel32.decode(lppe, pe);
                pes.add(pe);
            }
        }
        NativeHelper.free(lppe);
        if (pes.size() == 0)
            return null;

        return (PROCESSENTRY32[]) pes.toArray(new PROCESSENTRY32[pes.size()]);
    }
}
