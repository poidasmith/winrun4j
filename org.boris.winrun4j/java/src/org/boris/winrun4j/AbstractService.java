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

public abstract class AbstractService implements Service
{
    protected volatile boolean shutdown = false;
    private String name;
    private String description;

    public AbstractService(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public int doRequest(int control) throws ServiceException {
        switch (control) {
        case SERVICE_CONTROL_STOP:
        case SERVICE_CONTROL_SHUTDOWN:
            shutdown = true;
            break;
        }
        return 0;
    }

    public int getControlsAccepted() {
        return SERVICE_ACCEPT_STOP | SERVICE_ACCEPT_SHUTDOWN;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public boolean isShutdown() {
        return shutdown;
    }
}
