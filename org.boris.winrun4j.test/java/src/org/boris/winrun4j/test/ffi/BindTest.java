/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Peter Smith
 *******************************************************************************/
package org.boris.winrun4j.test.ffi;

import org.boris.winrun4j.DllImport;
import org.boris.winrun4j.NativeBinder;

public class BindTest
{
    public static void main(String[] args) throws Exception {
        NativeBinder.bind(BindTest.class);
        System.out.println(GetCurrentProcessId());
    }

    @DllImport("kernel32.dll")
    public static native int GetCurrentProcessId();

    // public static native int GetEnvironmentVariable(String lpName,
    // StringBuilder lpBuffer, int nSize);
}
