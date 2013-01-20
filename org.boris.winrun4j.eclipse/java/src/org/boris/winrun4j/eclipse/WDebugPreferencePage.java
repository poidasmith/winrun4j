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

import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class WDebugPreferencePage extends FieldEditorPreferencePage implements
        IWorkbenchPreferencePage
{
    public WDebugPreferencePage() {
        super(GRID);
        setPreferenceStore(WActivator.getDefault().getPreferenceStore());
        setTitle("WinRun4J Debug Settings");
        setDescription("Advanced debug settings for launcher");
    }

    protected void createFieldEditors() {
        // Selection for an alternative launcher exe
        FileFieldEditor ffe = new FileFieldEditor(IWPreferenceConstants.LAUNCHER_LOCATION,
                "&Launcher Location:", getFieldEditorParent());
        ffe.setFileExtensions(new String[] { "*.exe" });
        addField(ffe);

        // Directory for alternative winrun4j library
        DirectoryFieldEditor dfe = new DirectoryFieldEditor(IWPreferenceConstants.LIBRARY_DIR,
                "&Library Directory:", getFieldEditorParent());
        addField(dfe);

        // File/Jar location for winrun4j library
        FileFieldEditor lf = new FileFieldEditor(IWPreferenceConstants.LIBRARY_FILE,
                "&Library File:", getFieldEditorParent());
        lf.setFileExtensions(new String[] { "*.jar;*.zip" });
        addField(lf);

        // Location to winrun4j library source
        addField(new StringFieldEditor(IWPreferenceConstants.LIBRARY_SRC, "&Source Location:",
                getFieldEditorParent()));
    }

    public void init(IWorkbench workbench) {
    }
}
