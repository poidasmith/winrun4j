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

import java.io.File;

import org.boris.winrun4j.Native;
import org.boris.winrun4j.NativeHelper;
import org.boris.winrun4j.PInvoke;
import org.boris.winrun4j.PInvoke.DllImport;

public class Shell32
{
    static {
        PInvoke.bind(Shell32.class, "shell32");
    }

    public static final long library = Native.loadLibrary("shell32");

    public static final int Desktop = 0;
    public static final int Programs = 2;
    public static final int MyDocuments = 0x5;
    public static final int Favorites = 6;
    public static final int Startup = 7;
    public static final int Recent = 8;
    public static final int SendTo = 9;
    public static final int StartMenu = 0xb;
    public static final int MyMusic = 0xd;
    public static final int Desktop2 = 0x10;
    public static final int NetHood = 0x13;
    public static final int Fonts = 0x14;
    public static final int Templates = 0x15;
    public static final int AllStartMenu = 0x16;
    public static final int AllPrograms = 0x17;
    public static final int AllStartup = 0x18;
    public static final int AllDesktop = 0x19;
    public static final int ApplicationData = 0x1a;
    public static final int PrintHood = 0x1b;
    public static final int LocalAppData = 0x1c;
    public static final int AllFavorites = 0x1f;
    public static final int InternetCache = 0x20;
    public static final int Cookies = 0x21;
    public static final int History = 0x22;
    public static final int CommonAppData = 0x23;
    public static final int Windows = 0x24;
    public static final int System = 0x25;
    public static final int ProgramFiles = 0x26;
    public static final int MyPictures = 0x27;
    public static final int Home = 0x28;
    public static final int ProgramFilesCommon = 0x2b;
    public static final int CommonDocuments = 0x2e;
    public static final int AdminTools = 0x2f;
    public static final int AllAdminTools = 0x30;
    public static final int Resources = 0x38;

    public static final int MAX_PATH = 260;
    public static final int MAX_PATHW = 520;

    @DllImport
    public static native long ShellExecute(long hwnd, String operation, String file, String parameters,
            String directory, int nShowCmd);

    public static File getFolderPath(int type) {
        long buf = Native.malloc(MAX_PATHW);
        NativeHelper.call(library, "SHGetFolderPathW", 0, type, 0, 0, buf);
        String res = NativeHelper.getString(buf, MAX_PATHW, true);
        Native.free(buf);
        if (res == null || res.length() == 0)
            return null;
        return new File(res);
    }
}
