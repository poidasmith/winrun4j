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

public class Advapi32
{
    public static final long library = Native.loadLibrary("advapi32");

    public static final int SC_MANAGER_ALL_ACCESS = 0xF003F;

    public static class GUID
    {
        public int data1;
        public short data2;
        public short data3;
        public int data4;
        public int data5;
    }
}
