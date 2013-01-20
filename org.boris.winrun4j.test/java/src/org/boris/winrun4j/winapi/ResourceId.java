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

import org.boris.winrun4j.NativeHelper;

public class ResourceId
{
    public final String id;
    public final int iid;

    public ResourceId(String id) {
        this.id = id;
        this.iid = -1;
    }

    public ResourceId(int iid) {
        this.id = null;
        this.iid = iid;
    }

    public boolean isIntResource() {
        return id == null;
    }

    public long toNative() {
        return isIntResource() ? iid : NativeHelper.toNativeString(id, true);
    }

    public static ResourceId fromPointer(long pRes, boolean wideChar) {
        if (pRes >> 16 == 0)
            return new ResourceId((int) pRes);
        else
            return new ResourceId(NativeHelper.getString(pRes, 1024, wideChar));
    }

    // Standard Resource Types
    public static final ResourceId RT_CURSOR = new ResourceId(1);
    public static final ResourceId RT_BITMAP = new ResourceId(2);
    public static final ResourceId RT_ICON = new ResourceId(3);
    public static final ResourceId RT_MENU = new ResourceId(4);
    public static final ResourceId RT_DIALOG = new ResourceId(5);
    public static final ResourceId RT_STRING = new ResourceId(6);
    public static final ResourceId RT_FONTDIR = new ResourceId(7);
    public static final ResourceId RT_FONT = new ResourceId(8);
    public static final ResourceId RT_ACCELERATOR = new ResourceId(9);
    public static final ResourceId RT_RCDATA = new ResourceId(10);
    public static final ResourceId RT_MESSAGETABLE = new ResourceId(11);
    public static final ResourceId RT_GROUP_CURSOR = new ResourceId(12);
    public static final ResourceId RT_GROUP_ICON = new ResourceId(13);
    public static final ResourceId RT_VERSION = new ResourceId(16);
    public static final ResourceId RT_DLGINCLUDE = new ResourceId(17);
    public static final ResourceId RT_PLUGPLAY = new ResourceId(19);
    public static final ResourceId RT_VXD = new ResourceId(20);
    public static final ResourceId RT_ANICURSOR = new ResourceId(21);
    public static final ResourceId RT_ANIICON = new ResourceId(22);
    public static final ResourceId RT_HTML = new ResourceId(23);

    // WinRun4J types
    public static final ResourceId RT_INI_FILE = new ResourceId(687);
    public static final ResourceId RT_JAR_FILE = new ResourceId(688);
    public static final ResourceId RT_SLPASH_FILE = new ResourceId(689);
}
