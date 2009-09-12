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

import org.boris.winrun4j.Kernel32;
import org.boris.winrun4j.Native;
import org.boris.winrun4j.NativeHelper;

public class ToolHelp
{
    public static final long procCreateToolHelp32Snapshot = Native.getProcAddress(Kernel32.library,
            "CreateToolHelp32Snapshot");

    public static long CreateToolHelp32Snapshot(int flags, long th32ProcessID) {
        return NativeHelper.call(procCreateToolHelp32Snapshot, flags, th32ProcessID);
    }
}
