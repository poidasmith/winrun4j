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

import org.boris.winrun4j.PInvoke;
import org.boris.winrun4j.PInvoke.DllImport;
import org.boris.winrun4j.PInvoke.MarshalAs;
import org.boris.winrun4j.PInvoke.Struct;

public class BindingExample2
{
    static {
        PInvoke.bind(BindingExample2.class);
    }

    public static void main(String[] args) throws Exception {
        OSVERSIONINFOEX ver = new OSVERSIONINFOEX();
        if (GetVersionEx(ver)) {
            System.out.println(ver.majorVersion);
            System.out.println(ver.minorVersion);
            System.out.println(ver.csdVersion);
        }
    }

    @DllImport(lib = "kernel32.dll", wideChar = false)
    public static native boolean GetVersionEx(OSVERSIONINFOEX version);

    public static class OSVERSIONINFOEX implements Struct
    {
        public int sizeOf = PInvoke.sizeOf(OSVERSIONINFOEX.class, false);
        public int majorVersion;
        public int minorVersion;
        public int buildNumber;
        public int platformId;
        @MarshalAs(sizeConst = 128)
        public String csdVersion;
        public short servicePackMajor;
        public short servicePackMinor;
        public short suiteMask;
        public byte productType;
        public byte reserved;
    }

}
