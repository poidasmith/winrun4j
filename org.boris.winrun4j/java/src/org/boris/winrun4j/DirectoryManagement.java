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

public class DirectoryManagement
{
    private static long library = Native.loadLibrary("kernel32");
    private static long moveFileProc = Native.getProcAddress(library, "MoveFileExA");

    public static boolean moveFile(String existingName, String newName, int flags) {
        if (existingName == null || newName == null)
            throw new NullPointerException();
        long e = NativeHelper.toNativeString(existingName);
        long n = NativeHelper.toNativeString(newName);
        NativeStack ns = new NativeStack();
        ns.addArg32(e);
        ns.addArg32(n);
        ns.addArg32(flags);
        boolean res = NativeHelper.call(moveFileProc, ns) == 1;
        Native.free(e);
        Native.free(n);
        return res;
    }
}
