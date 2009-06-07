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

public class FileVerb
{
    private String verb;
    private String label;
    private String command;
    private String ddeCommand;
    private String ddeApplication;
    private String ddeTopic;

    public FileVerb(String verb) {
        this.verb = verb;
    }

    public String getVerb() {
        return verb;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getDDECommand() {
        return ddeCommand;
    }

    public void setDDECommand(String ddeCommand) {
        this.ddeCommand = ddeCommand;
    }

    public String getDDEApplication() {
        return ddeApplication;
    }

    public void setDDEApplication(String ddeApplication) {
        this.ddeApplication = ddeApplication;
    }

    public String getDDETopic() {
        return ddeTopic;
    }

    public void setDDETopic(String ddeTopic) {
        this.ddeTopic = ddeTopic;
    }
}
