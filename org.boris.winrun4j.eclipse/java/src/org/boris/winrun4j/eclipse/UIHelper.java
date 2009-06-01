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

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

class UIHelper
{
    public static void select(Combo combo, String data) {
        int c = combo.getItemCount();
        for (int i = 0; i < c; i++) {
            if (combo.getItem(i).equals(data)) {
                combo.select(i);
                break;
            }
        }
    }

    public static String getSelection(Combo combo) {
        int i = combo.getSelectionIndex();
        if (i != -1) {
            return combo.getItem(i);
        }
        return null;
    }

    public static Composite createComposite(Composite parent, Font font, int columns, int hspan,
            int fill) {
        Composite g = new Composite(parent, SWT.NONE);
        g.setLayout(new GridLayout(columns, false));
        g.setFont(font);
        GridData gd = new GridData(fill);
        gd.horizontalSpan = hspan;
        g.setLayoutData(gd);
        return g;
    }
}
