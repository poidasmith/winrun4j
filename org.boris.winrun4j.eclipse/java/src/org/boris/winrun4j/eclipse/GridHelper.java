/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Peter Smith
 *******************************************************************************/
package org.boris.winrun4j.eclipse;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Control;

class GridHelper
{
    public static void setHorizontalSpan(Control c, int span) {
        GridData gd = get(c);
        gd.horizontalSpan = span;
        c.setLayoutData(gd);
    }

    private static GridData get(Control c) {
        Object o = c.getLayoutData();
        if (o instanceof GridData) {
            return (GridData) o;
        } else {
            return new GridData();
        }
    }

    public static void grabExcessHorizontalSpace(Control c, boolean b) {
        GridData gd = get(c);
        gd.grabExcessHorizontalSpace = b;
        c.setLayoutData(gd);
    }
}
