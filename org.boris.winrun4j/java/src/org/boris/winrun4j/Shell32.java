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

import java.io.File;

public class Shell32
{
    public static final long library = Native.loadLibrary("shell32");
    public static final long procGetFolderPath = Native.getProcAddress(library, "SHGetFolderPathW");

    public static File getFolderPath(int type) {
        long buf = Native.malloc(Native.MAX_PATH << 1);
        NativeHelper.call(procGetFolderPath, 0, type, 0, 0, buf);
        String res = NativeHelper.getString(buf, Native.MAX_PATH, true);
        Native.free(buf);
        if (res == null || res.length() == 0)
            return null;
        return new File(res);
    }
}
