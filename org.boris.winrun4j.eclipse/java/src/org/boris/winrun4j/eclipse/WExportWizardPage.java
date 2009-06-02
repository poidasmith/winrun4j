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

import java.io.File;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class WExportWizardPage extends WizardPage
{
    private static final String PAGE_NAME = "WinRun4JExportWizardPage"; //$NON-NLS-1$
    private static final String SETTING_LAUNCH_CONFIG = "WinRun4J.export.launchConfig"; //$NON-NLS-1$
    private static final String SETTING_LAUNCHER_NAME = "WinRun4J.export.launcherName"; //$NON-NLS-1$
    private static final String SETTING_OUTPUTDIR = "WinRun4J.export.outputDir"; //$NON-NLS-1$
    private static final String SETTING_OUTPUTDIR_LIST = "WinRun4J.export.outputDirList"; //$NON-NLS-1$
    private static final String SETTING_ICON_LIST = "WinRun4J.export.iconList"; //$NON-NLS-1$
    private static final String SETTING_ICON = "WinRun4J.export.icon"; //$NON-NLS-1$
    private static final String SETTING_EXPORT_TYPE = "WinRun4J.export.exportType"; //$NON-NLS-1$
    private static final String SETTING_LAUNCHER_TYPE = "WinRun4J.export.launcherType"; //$NON-NLS-1$
    private static final String SETTING_WILDCARD_CLASSPATH = "WinRun4J.export.wildcardClasspath";
    private Map launchConfigs = new TreeMap();

    // UI elements
    private Combo launchConfigurationCombo;
    private Text launcherNameText;
    private Combo outputDirectoryCombo;
    private Combo launcherIconCombo;
    private Combo exportTypeCombo;
    private Button wildcardClasspathCheck;
    private Combo launcherTypeCombo;

    // Model data
    private ILaunchConfiguration launchConfig;
    private File launcherFile;
    private File launcherIcon;

    protected WExportWizardPage() {
        super(PAGE_NAME);
        setTitle(WMessages.WExportWizardPage_title);
        setDescription(WMessages.WExportWizardPage_description);
    }

    public ILaunchConfiguration getLaunchConfig() {
        return launchConfig;
    }

    public File getLauncherFile() {
        return launcherFile;
    }

    public File getLauncherIcon() {
        return launcherIcon;
    }

    public int getExportType() {
        return exportTypeCombo.getSelectionIndex() + 1;
    }

    public int getLauncherType() {
        return launcherTypeCombo.getSelectionIndex() + 1;
    }

    public boolean isWildcardClasspath() {
        return wildcardClasspathCheck.getSelection();
    }

    public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        GridLayout layout = new GridLayout(2, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);

        // Launch config
        Label lcl = new Label(composite, SWT.NULL);
        lcl.setText(WMessages.WExportWizardPage_launchConfig);
        GridHelper.setHorizontalSpan(lcl, 2);
        launchConfigurationCombo = new Combo(composite, SWT.NULL);
        launchConfigurationCombo.setItems(findLaunchConfigurations());
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        gd.grabExcessHorizontalSpace = true;
        launchConfigurationCombo.setLayoutData(gd);
        launchConfigurationCombo.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                updateSelectedLaunchConfiguration();
                setPageComplete(isPageComplete());
            }
        });

        // Launcher name
        Label lnl = new Label(composite, SWT.NULL);
        lnl.setText(WMessages.WExportWizardPage_launcherName);
        GridHelper.setHorizontalSpan(lcl, 2);
        launcherNameText = new Text(composite, SWT.BORDER);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        gd.grabExcessHorizontalSpace = true;
        launcherNameText.setLayoutData(gd);
        launcherNameText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                updateLauncherFile();
                setPageComplete(isPageComplete());
            }
        });

        // Output dir
        Label odl = new Label(composite, SWT.NULL);
        odl.setText(WMessages.WExportWizardPage_outputDir);
        GridHelper.setHorizontalSpan(odl, 2);
        outputDirectoryCombo = new Combo(composite, SWT.NULL);
        outputDirectoryCombo.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                updateLauncherFile();
                setPageComplete(isPageComplete());
            }
        });
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.grabExcessHorizontalSpace = true;
        outputDirectoryCombo.setLayoutData(gd);
        Button odcb = new Button(composite, SWT.PUSH);
        odcb.setText(WMessages.WExportWizardPage_outputDirBrowse);
        odcb.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                browseForOutputDirectory();
            }
        });

        // Icon
        Label il = new Label(composite, SWT.NULL);
        il.setText(WMessages.WExportWizardPage_launcherIcon);
        GridHelper.setHorizontalSpan(il, 2);
        launcherIconCombo = new Combo(composite, SWT.NULL);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.grabExcessHorizontalSpace = true;
        launcherIconCombo.setLayoutData(gd);
        launcherIconCombo.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                updateLauncherIconFile();
                setPageComplete(isPageComplete());
            }
        });
        Button ib = new Button(composite, SWT.PUSH);
        ib.setText(WMessages.WExportWizardPage_launcherIconBrowse);
        ib.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                browseForLauncherIcon();
            }
        });

        // Export type
        Label etl = new Label(composite, SWT.NULL);
        etl.setText(WMessages.WExportWizardPage_exportType);
        GridHelper.setHorizontalSpan(etl, 2);
        exportTypeCombo = new Combo(composite, SWT.READ_ONLY);
        exportTypeCombo.setItems(new String[] { WMessages.WExportWizardPage_exportTypeStandard,
                WMessages.WExportWizardPage_exportTypeEmbedIni,
                WMessages.WExportWizardPage_exportTypeFat });
        exportTypeCombo.select(0);
        GridHelper.setHorizontalSpan(exportTypeCombo, 2);

        // Wildcard classpath
        wildcardClasspathCheck = new Button(composite, SWT.CHECK);
        wildcardClasspathCheck.setText("Wildcard Classpath");
        GridHelper.setHorizontalSpan(wildcardClasspathCheck, 2);

        // Launcher type
        Label ltl = new Label(composite, SWT.NULL);
        ltl.setText(WMessages.WExportWizardPage_launcherType);
        GridHelper.setHorizontalSpan(ltl, 2);
        launcherTypeCombo = new Combo(composite, SWT.READ_ONLY);
        launcherTypeCombo.setItems(new String[] { WMessages.WExportWizardPage_launcherType32W,
                WMessages.WExportWizardPage_launcherType32C,
                WMessages.WExportWizardPage_launcherType64W,
                WMessages.WExportWizardPage_launcherType64C });
        launcherTypeCombo.select(0);
        GridHelper.setHorizontalSpan(launcherTypeCombo, 2);

        Dialog.applyDialogFont(composite);
        setControl(composite);

        // Use IDialogSettings to remember selections/dir/file history
        loadSettings();
    }

    protected void updateLauncherIconFile() {
        String ln = launcherIconCombo.getText();
        if (ln == null || "".equals(ln)) { //$NON-NLS-1$
            launcherIcon = null;
            return;
        }

        launcherIcon = new File(ln);
    }

    protected void updateLauncherFile() {
        String ln = launcherNameText.getText();
        String od = outputDirectoryCombo.getText();
        if (ln == null || "".equals(ln) || od == null || "".equals(od)) { //$NON-NLS-1$ //$NON-NLS-2$
            this.launcherFile = null;
            return;
        }
        File odf = new File(od);
        this.launcherFile = new File(odf, ln);
    }

    protected void updateSelectedLaunchConfiguration() {
        this.launchConfig = (ILaunchConfiguration) launchConfigs.get(UIHelper
                .getSelection(launchConfigurationCombo));
        if (launchConfig == null) {
            this.launcherNameText.setText(""); //$NON-NLS-1$
            this.launchConfig = null;
            return;
        }
        String ln = launchConfig.getName();
        int idx = ln.indexOf('(');
        if (idx != -1) {
            ln = ln.substring(0, idx - 1);
        }
        ln += ".exe"; //$NON-NLS-1$
        this.launcherNameText.setText(ln);
    }

    protected void browseForLauncherIcon() {
        FileDialog fd = new FileDialog(getShell());
        fd.setFilterExtensions(new String[] { "*.ico" }); //$NON-NLS-1$
        fd.setText(WMessages.WExportWizardPage_30);
        String fn = fd.open();
        if (fn == null)
            fn = ""; //$NON-NLS-1$
        this.launcherIconCombo.setText(fn);
    }

    protected void browseForOutputDirectory() {
        DirectoryDialog dd = new DirectoryDialog(getShell());
        dd.setText(WMessages.WExportWizardPage_outputDirDialog);
        dd.setMessage(WMessages.WExportWizardPage_selectDir);
        String ds = dd.open();
        if (ds == null)
            ds = ""; //$NON-NLS-1$
        this.outputDirectoryCombo.setText(ds);
    }

    private String[] findLaunchConfigurations() {
        try {
            launchConfigs.clear();
            ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
            ILaunchConfigurationType type = manager
                    .getLaunchConfigurationType(IWLaunchConfigurationConstants.TYPE);
            ILaunchConfiguration[] launchConfigs = manager.getLaunchConfigurations(type);

            for (int i = 0; i < launchConfigs.length; i++) {
                ILaunchConfiguration launchConfig = launchConfigs[i];
                if (!launchConfig.getAttribute(IDebugUIConstants.ATTR_PRIVATE, false)) {
                    String projectName = launchConfig.getAttribute(
                            IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, ""); //$NON-NLS-1$                    
                    this.launchConfigs.put(
                            launchConfig.getName() + " - " + projectName, launchConfig); //$NON-NLS-1$
                }
            }
        } catch (CoreException e) {
            e.printStackTrace();
            // FIXME
        }

        return (String[]) launchConfigs.keySet().toArray(new String[0]);
    }

    public boolean isPageComplete() {
        setMessage(null);
        if (launchConfigurationCombo.getText().equals("")) { //$NON-NLS-1$
            setMessage(WMessages.WExportWizardPage_selectConfig, WARNING);
            return false;
        }
        if (launcherNameText.getText().equals("")) { //$NON-NLS-1$
            setMessage(WMessages.WExportWizardPage_specifyLauncher, WARNING);
            return false;
        }
        if (outputDirectoryCombo.getText().equals("")) { //$NON-NLS-1$
            setMessage(WMessages.WExportWizardPage_specifyOutputDir, WARNING);
            return false;
        }
        return this.launchConfig != null && this.launcherFile != null;
    }

    public void saveSettings() {
        IDialogSettings id = getDialogSettings();
        if (id == null)
            return;
        id.put(SETTING_LAUNCH_CONFIG, launchConfigurationCombo.getText());
        id.put(SETTING_LAUNCHER_NAME, launcherNameText.getText());
        id.put(SETTING_OUTPUTDIR, outputDirectoryCombo.getText());
        updateSettingList(id, SETTING_OUTPUTDIR_LIST, outputDirectoryCombo.getText());
        id.put(SETTING_ICON, launcherIconCombo.getText());
        updateSettingList(id, SETTING_ICON_LIST, launcherIconCombo.getText());
        id.put(SETTING_EXPORT_TYPE, getExportType());
        id.put(SETTING_LAUNCHER_TYPE, getLauncherType());
        id.put(SETTING_WILDCARD_CLASSPATH, wildcardClasspathCheck.getSelection());
    }

    public void updateSettingList(IDialogSettings id, String setting, String value) {
        String[] a = id.getArray(setting);
        if (a == null)
            a = new String[0];
        TreeSet hs = new TreeSet(Arrays.asList(a));
        hs.add(value);
        id.put(setting, (String[]) hs.toArray(new String[hs.size()]));
    }

    public void loadSettings() {
        IDialogSettings id = getDialogSettings();
        if (id == null)
            return;
        String slc = id.get(SETTING_LAUNCH_CONFIG);
        if (slc != null)
            UIHelper.select(launchConfigurationCombo, slc);
        String sln = id.get(SETTING_LAUNCHER_NAME);
        if (sln != null)
            launcherNameText.setText(sln);
        String[] sol = id.getArray(SETTING_OUTPUTDIR_LIST);
        if (sol != null)
            outputDirectoryCombo.setItems(sol);
        UIHelper.select(outputDirectoryCombo, id.get(SETTING_OUTPUTDIR));
        String[] sil = id.getArray(SETTING_ICON_LIST);
        if (sil != null)
            launcherIconCombo.setItems(sil);
        UIHelper.select(launcherIconCombo, id.get(SETTING_ICON));
        int val = 0;
        try {
            val = id.getInt(SETTING_EXPORT_TYPE);
        } catch (NumberFormatException e) {
        }
        if (val != 0)
            val--;
        exportTypeCombo.select(val);
        val = 0;
        try {
            val = id.getInt(SETTING_LAUNCHER_TYPE);
        } catch (NumberFormatException e) {
        }
        if (val != 0)
            val--;
        launcherTypeCombo.select(val);
        wildcardClasspathCheck.setSelection(id.getBoolean(SETTING_WILDCARD_CLASSPATH));
    }

}
