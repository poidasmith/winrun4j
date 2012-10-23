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

import java.lang.reflect.Method;

public class MainService implements Service
{
    private String serviceClass = INI.getProperty("MainService:class");
    private volatile boolean shutdown = false;

    public int serviceMain(final String[] args) throws ServiceException {
        try {
            Class c = Class.forName(serviceClass);
            final Method m = c.getMethod("main", String[].class);
            // Create a thread to run the service in, and use this thread to monitor it.
            Thread t = new Thread() {
                public void run() {
                    try {
                        m.invoke(null, (Object) args);
                    } catch (Exception e) {
                    }
                    shutdown = true;
                }
            };
            t.start();
            while (!shutdown) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {}
            }
        } catch (Exception e) {
            throw new ServiceException(e);
        }
        return 0;
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
}
