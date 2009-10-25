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

import org.boris.winrun4j.Native;
import org.boris.winrun4j.NativeHelper;

public class Gdi32
{
    public static final long library = Native.loadLibrary("gdi32");

    public static long getStockObject(int fnObject) {
        return NativeHelper.call(library, "GetStockObject", fnObject);
    }
}
