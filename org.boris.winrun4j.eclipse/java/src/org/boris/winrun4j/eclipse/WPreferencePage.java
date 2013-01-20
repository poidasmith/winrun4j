package org.boris.winrun4j.eclipse;

import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
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