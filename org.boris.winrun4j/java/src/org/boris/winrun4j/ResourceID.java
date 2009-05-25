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

public class ResourceID
{
    public final String id;
    public final int iid;

    public ResourceID(String id) {
        this.id = id;
        this.iid = -1;
    }

    public ResourceID(int iid) {
        this.id = null;
        this.iid = iid;
    }

    public boolean isIntResource() {
        return id == null;
    }

    // Standard Resource Types
    public static final ResourceID RT_CURSOR = new ResourceID(1);
    public static final ResourceID RT_BITMAP = new ResourceID(2);
    public static final ResourceID RT_ICON = new ResourceID(3);
    public static final ResourceID RT_MENU = new ResourceID(4);
    public static final ResourceID RT_DIALOG = new ResourceID(5);
    public static final ResourceID RT_STRING = new ResourceID(6);
    public static final ResourceID RT_FONTDIR = new ResourceID(7);
    public static final ResourceID RT_FONT = new ResourceID(8);
    public static final ResourceID RT_ACCELERATOR = new ResourceID(9);
    public static final ResourceID RT_RCDATA = new ResourceID(10);
    public static final ResourceID RT_MESSAGETABLE = new ResourceID(11);
    public static final ResourceID RT_GROUP_CURSOR = new ResourceID(12);
    public static final ResourceID RT_GROUP_ICON = new ResourceID(13);
    public static final ResourceID RT_VERSION = new ResourceID(16);
    public static final ResourceID RT_DLGINCLUDE = new ResourceID(17);
    public static final ResourceID RT_PLUGPLAY = new ResourceID(19);
    public static final ResourceID RT_VXD = new ResourceID(20);
    public static final ResourceID RT_ANICURSOR = new ResourceID(21);
    public static final ResourceID RT_ANIICON = new ResourceID(22);
    public static final ResourceID RT_HTML = new ResourceID(23);

    // WinRun4J types
    public static final ResourceID RT_INI_FILE = new ResourceID(687);
    public static final ResourceID RT_JAR_FILE = new ResourceID(688);
    public static final ResourceID RT_SLPASH_FILE = new ResourceID(689);
}
