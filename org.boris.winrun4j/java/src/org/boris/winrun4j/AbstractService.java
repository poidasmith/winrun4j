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
    
    public AbstractService() {
        // Set context class loader to avoid troubles
        if(Thread.currentThread().getContextClassLoader() == null)
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
    }

    public int serviceRequest(int control) throws ServiceException {
        switch (control) {
        case SERVICE_CONTROL_STOP:
        case SERVICE_CONTROL_SHUTDOWN:
            shutdown = true;
            break;
        default:
            break;
        }
        return 0;
    }

    public boolean isShutdown() {
        return shutdown;
    }
}
