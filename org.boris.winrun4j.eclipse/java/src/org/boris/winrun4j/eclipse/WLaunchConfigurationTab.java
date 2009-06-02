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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class WLaunchConfigurationTab extends AbstractLaunchConfigurationTab implements
        SelectionListener, ModifyListener
{
    // Logging UI elements
    private Combo logLevelCombo;
    private Text logFileText;
    private Button logFileOverwriteCheck;

    // Splash UI elements
    private Text splashImageText;
    private Button splashAutoHideCheck;

    // DDE UI elements
    private Button ddeEnabledCheck;
    private Text ddeClassText;
    private Text ddeServerNameText;
    private Text ddeTopicNameText;
    private Text ddeWindowNameText;

    // Misc UI elements
    private Combo singleInstanceCombo;
    private Combo processPriorityCombo;

    public Image getImage() {
        return WActivator.getLauncherImage();
    }

    public void createControl(Composite parent) {
        Composite comp = UIHelper.createComposite(parent, parent.getFont(), 1, 1,
                GridData.FILL_BOTH);
        ((GridLayout) comp.getLayout()).verticalSpacing = 0;
        createLogEditor(comp);
        createSplashEditor(comp);
        createDDEEditor(comp);
        createMiscEditor(comp);
        setControl(comp);
    }

    private void createMiscEditor(Composite parent) {
        Font font = parent.getFont();
        Group group = new Group(parent, SWT.NONE);
        group.setText(WMessages.WLaunchConfigurationTab_misc);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        group.setLayoutData(gd);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        group.setLayout(layout);
        group.setFont(font);

        // Single instance
        Label t = new Label(group, SWT.NULL);
        t.setFont(font);
        t.setText(WMessages.WLaunchConfigurationTab_singleInstance);
        this.singleInstanceCombo = new Combo(group, SWT.DROP_DOWN | SWT.READ_ONLY);
        this.singleInstanceCombo.setItems(new String[] { "", WMessages.WLaunchConfigurationTab_process, WMessages.WLaunchConfigurationTab_singleInstance_window, WMessages.WLaunchConfigurationTab_singleInstance_dde }); //$NON-NLS-1$
        this.singleInstanceCombo.setFont(font);
        this.singleInstanceCombo.addSelectionListener(this);

        // Process priority
        t = new Label(group, SWT.NULL);
        t.setFont(font);
        t.setText(WMessages.WLaunchConfigurationTab_processPr);
        this.processPriorityCombo = new Combo(group, SWT.DROP_DOWN | SWT.READ_ONLY);
        this.processPriorityCombo.setItems(new String[] { "", WMessages.WLaunchConfigurationTab_processPr_idle, WMessages.WLaunchConfigurationTab_processPr_below, WMessages.WLaunchConfigurationTab_processPr_normal, //$NON-NLS-1$
                WMessages.WLaunchConfigurationTab_processPr_above, WMessages.WLaunchConfigurationTab_processPr_high, WMessages.WLaunchConfigurationTab_processPr_realtime });
        this.processPriorityCombo.setFont(font);
        this.processPriorityCombo.addSelectionListener(this);
        this.processPriorityCombo.setVisibleItemCount(7);
    }

    private void createDDEEditor(Composite parent) {
        Font font = parent.getFont();
        Group group = new Group(parent, SWT.NONE);
        group.setText(WMessages.WLaunchConfigurationTab_dde);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        group.setLayoutData(gd);
        GridLayout layout = new GridLayout();
        layout.numColumns = 7;
        group.setLayout(layout);
        group.setFont(font);

        // DDE enabled
        Label t = new Label(group, SWT.NULL);
        t.setFont(font);
        t.setText(WMessages.WLaunchConfigurationTab_dde_enabled);
        this.ddeEnabledCheck = new Button(group, SWT.CHECK);
        this.ddeEnabledCheck.addSelectionListener(this);
        gd = new GridData();
        gd.horizontalSpan = 6;
        this.ddeEnabledCheck.setLayoutData(gd);

        // DDE class
        t = new Label(group, SWT.NULL);
        t.setFont(font);
        t.setText(WMessages.WLaunchConfigurationTab_dde_class);
        this.ddeClassText = new Text(group, SWT.SEARCH);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 6;
        this.ddeClassText.setLayoutData(gd);
        this.ddeClassText.addModifyListener(this);

        // DDE server name
        t = new Label(group, SWT.NULL);
        t.setFont(font);
        t.setText(WMessages.WLaunchConfigurationTab_dde_server_name);
        this.ddeServerNameText = new Text(group, SWT.SEARCH);
        this.ddeServerNameText.addModifyListener(this);
        gd = new GridData();
        gd.widthHint = 70;
        this.ddeServerNameText.setLayoutData(gd);

        // DDE topic name
        t = new Label(group, SWT.NULL);
        t.setFont(font);
        t.setText(WMessages.WLaunchConfigurationTab_dde_topic);
        this.ddeTopicNameText = new Text(group, SWT.SEARCH);
        this.ddeTopicNameText.addModifyListener(this);
        gd = new GridData();
        gd.widthHint = 70;
        this.ddeTopicNameText.setLayoutData(gd);

        // DDE window name
        t = new Label(group, SWT.NULL);
        t.setFont(font);
        t.setText(WMessages.WLaunchConfigurationTab_dde_window);
        this.ddeWindowNameText = new Text(group, SWT.SEARCH);
        this.ddeWindowNameText.addModifyListener(this);
        gd = new GridData();
        gd.widthHint = 70;
        this.ddeWindowNameText.setLayoutData(gd);
    }

    private void createSplashEditor(Composite parent) {
        Font font = parent.getFont();
        Group group = new Group(parent, SWT.NONE);
        group.setText(WMessages.WLaunchConfigurationTab_splashScreen);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        group.setLayoutData(gd);
        GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        group.setLayout(layout);
        group.setFont(font);

        // Splash File
        Label t = new Label(group, SWT.NULL);
        t.setFont(font);
        t.setText(WMessages.WLaunchConfigurationTab_splashFile);
        this.splashImageText = new Text(group, SWT.SEARCH);
        this.splashImageText.addModifyListener(this);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        this.splashImageText.setLayoutData(gd);
        Button b = new Button(group, SWT.NULL);
        b.setFont(font);
        b.setText(WMessages.WLaunchConfigurationTab_splashFileBrowse);
        b.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                browseForSplashImage();
            }
        });

        // Splash auto-hide
        t = new Label(group, SWT.NULL);
        t.setFont(font);
        t.setText(WMessages.WLaunchConfigurationTab_splashAutoHide);
        this.splashAutoHideCheck = new Button(group, SWT.CHECK);
        this.splashAutoHideCheck.addSelectionListener(this);
        gd = new GridData();
        gd.horizontalSpan = 2;
        this.splashAutoHideCheck.setLayoutData(gd);
    }

    private void createLogEditor(Composite parent) {
        Font font = parent.getFont();
        Group group = new Group(parent, SWT.NONE);
        group.setText(WMessages.WLaunchConfigurationTab_logging);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        group.setLayoutData(gd);
        GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        group.setLayout(layout);
        group.setFont(font);

        // Log level
        Label t = new Label(group, SWT.NULL);
        t.setFont(font);
        t.setText(WMessages.WLaunchConfigurationTab_logLevel);
        this.logLevelCombo = new Combo(group, SWT.DROP_DOWN | SWT.READ_ONLY);
        this.logLevelCombo.setItems(new String[] { "", WMessages.WLaunchConfigurationTab_logLevel_info, WMessages.WLaunchConfigurationTab_logLevel_warn, WMessages.WLaunchConfigurationTab_logLevel_error, WMessages.WLaunchConfigurationTab_logLevel_none }); //$NON-NLS-1$
        this.logLevelCombo.setFont(font);
        this.logLevelCombo.addSelectionListener(this);
        gd = new GridData();
        gd.horizontalSpan = 2;
        this.logLevelCombo.setLayoutData(gd);

        // Log file
        t = new Label(group, SWT.NULL);
        t.setFont(font);
        t.setText(WMessages.WLaunchConfigurationTab_logFile);
        this.logFileText = new Text(group, SWT.SEARCH);
        this.logFileText.setMessage(WMessages.WLaunchConfigurationTab_logFile_message);
        this.logFileText.addModifyListener(this);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        this.logFileText.setLayoutData(gd);

        // Log file overwrite
        t = new Label(group, SWT.NULL);
        t.setFont(font);
        t.setText(WMessages.WLaunchConfigurationTab_logFile_overwrite);
        this.logFileOverwriteCheck = new Button(group, SWT.CHECK);
        this.logFileOverwriteCheck.addSelectionListener(this);
        gd = new GridData();
        gd.horizontalSpan = 2;
        this.logFileOverwriteCheck.setLayoutData(gd);
        this.logFileOverwriteCheck.addSelectionListener(this);
    }

    public String getName() {
        return "WinRun4J"; //$NON-NLS-1$
    }

    public void initializeFrom(ILaunchConfiguration configuration) {
        try {
            UIHelper.select(logLevelCombo, configuration.getAttribute(
                    IWLaunchConfigurationConstants.PROP_LOG_LEVEL, "")); //$NON-NLS-1$
            logFileText.setText(configuration.getAttribute(
                    IWLaunchConfigurationConstants.PROP_LOG_FILE, "")); //$NON-NLS-1$
            logFileOverwriteCheck.setSelection(configuration.getAttribute(
                    IWLaunchConfigurationConstants.PROP_LOG_OVERWRITE, true));
            splashImageText.setText(configuration.getAttribute(
                    IWLaunchConfigurationConstants.PROP_SPLASH_FILE, "")); //$NON-NLS-1$
            splashAutoHideCheck.setSelection(configuration.getAttribute(
                    IWLaunchConfigurationConstants.PROP_SPLASH_AUTOHIDE, true));
            ddeEnabledCheck.setSelection(configuration.getAttribute(
                    IWLaunchConfigurationConstants.PROP_DDE_ENABLED, false));
            ddeClassText.setText(configuration.getAttribute(
                    IWLaunchConfigurationConstants.PROP_DDE_CLASS, "")); //$NON-NLS-1$
            ddeServerNameText.setText(configuration.getAttribute(
                    IWLaunchConfigurationConstants.PROP_DDE_SERVER_NAME, "")); //$NON-NLS-1$
            ddeTopicNameText.setText(configuration.getAttribute(
                    IWLaunchConfigurationConstants.PROP_DDE_TOPIC, "")); //$NON-NLS-1$
            ddeWindowNameText.setText(configuration.getAttribute(
                    IWLaunchConfigurationConstants.PROP_DDE_WINDOW_NAME, "")); //$NON-NLS-1$
            UIHelper.select(singleInstanceCombo, configuration.getAttribute(
                    IWLaunchConfigurationConstants.PROP_SINGLE_INSTANCE, "")); //$NON-NLS-1$
            UIHelper.select(processPriorityCombo, configuration.getAttribute(
                    IWLaunchConfigurationConstants.PROP_PROCESS_PRIORITY, "")); //$NON-NLS-1$

            updateEnablements();
        } catch (CoreException e) {
            e.printStackTrace();
        }
    }

    public void performApply(ILaunchConfigurationWorkingCopy configuration) {
        updateConfig(configuration, IWLaunchConfigurationConstants.PROP_LOG_LEVEL, UIHelper
                .getSelection(logLevelCombo));
        updateConfig(configuration, IWLaunchConfigurationConstants.PROP_LOG_FILE, logFileText
                .getText());
        updateConfig(configuration, IWLaunchConfigurationConstants.PROP_LOG_OVERWRITE,
                logFileOverwriteCheck.getSelection());
        updateConfig(configuration, IWLaunchConfigurationConstants.PROP_SPLASH_FILE,
                splashImageText.getText());
        updateConfig(configuration, IWLaunchConfigurationConstants.PROP_SPLASH_AUTOHIDE,
                splashAutoHideCheck.getSelection());
        updateConfig(configuration, IWLaunchConfigurationConstants.PROP_DDE_ENABLED,
                ddeEnabledCheck.getSelection());
        updateConfig(configuration, IWLaunchConfigurationConstants.PROP_DDE_CLASS, ddeClassText
                .getText());
        updateConfig(configuration, IWLaunchConfigurationConstants.PROP_DDE_SERVER_NAME,
                ddeServerNameText.getText());
        updateConfig(configuration, IWLaunchConfigurationConstants.PROP_DDE_TOPIC, ddeTopicNameText
                .getText());
        updateConfig(configuration, IWLaunchConfigurationConstants.PROP_DDE_WINDOW_NAME,
                ddeWindowNameText.getText());
        updateConfig(configuration, IWLaunchConfigurationConstants.PROP_SINGLE_INSTANCE, UIHelper
                .getSelection(singleInstanceCombo));
        updateConfig(configuration, IWLaunchConfigurationConstants.PROP_PROCESS_PRIORITY, UIHelper
                .getSelection(processPriorityCombo));
    }

    private void updateConfig(ILaunchConfigurationWorkingCopy configuration, String property,
            boolean value) {
        configuration.setAttribute(property, value);
    }

    private void updateConfig(ILaunchConfigurationWorkingCopy configuration, String property,
            String value) {
        if (value != null) {
            configuration.setAttribute(property, value);
        } else {
            configuration.removeAttribute(property);
        }
    }

    public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
        configuration.setAttribute(IWLaunchConfigurationConstants.PROP_SPLASH_AUTOHIDE, true);
        configuration.setAttribute(IWLaunchConfigurationConstants.PROP_LOG_OVERWRITE, true);
    }

    protected void update() {
        updateEnablements();
        setDirty(true);
        updateLaunchConfigurationDialog();
    }

    public void widgetDefaultSelected(SelectionEvent e) {
    }

    public void widgetSelected(SelectionEvent e) {
        update();
    }

    private void updateEnablements() {
        logFileOverwriteCheck.setEnabled(!logFileText.getText().equals("")); //$NON-NLS-1$
        splashAutoHideCheck.setEnabled(!splashImageText.getText().equals("")); //$NON-NLS-1$
        boolean dde = ddeEnabledCheck.getSelection();
        ddeClassText.setEnabled(dde);
        ddeServerNameText.setEnabled(dde);
        ddeTopicNameText.setEnabled(dde);
        ddeWindowNameText.setEnabled(dde);
    }

    public void modifyText(ModifyEvent e) {
        update();
    }

    protected void browseForSplashImage() {
        FileDialog fd = new FileDialog(getShell());
        fd.setFilterExtensions(new String[] { WMessages.WLaunchConfigurationTab_splash_filterList });
        fd.setText(WMessages.WLaunchConfigurationTab_splashSelect_message);
        String f = fd.open();
        if (f != null)
            splashImageText.setText(f);
    }
}
