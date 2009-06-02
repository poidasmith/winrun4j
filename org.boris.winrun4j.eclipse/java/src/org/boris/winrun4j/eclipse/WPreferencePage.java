package org.boris.winrun4j.eclipse;

import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class WPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage
{
    public WPreferencePage() {
        super(GRID);
        setPreferenceStore(WActivator.getDefault().getPreferenceStore());
        setDescription(WMessages.WPreferencePage_description);
    }

    public void createFieldEditors() {
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

        // Default log level
        ComboFieldEditor cfe = new ComboFieldEditor(IWPreferenceConstants.DEFAULT_LOG_LEVEL,
                "&Default Log Level:", new String[][] { { "", "" }, { "info", "info" },
                        { "warn", "warn" }, { "error", "error" }, { "none", "none" } },
                getFieldEditorParent());
        addField(cfe);
    }

    public void init(IWorkbench workbench) {
    }
}