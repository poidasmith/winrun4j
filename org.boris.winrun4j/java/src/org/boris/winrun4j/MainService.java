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

    public int serviceMain(String[] args) throws ServiceException {
        // Set context class loader to avoid troubles
        if(Thread.currentThread().getContextClassLoader() == null)
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
        
        try {
            Class c = Class.forName(serviceClass);
            Method m = c.getMethod("main", String[].class);
            m.invoke(null, (Object) args);
        } catch (Exception e) {
            throw new ServiceException(e);
        }
        return 0;
    }

    public int serviceRequest(int control) throws ServiceException {
        switch (control) {
        case SERVICE_CONTROL_STOP:
        case SERVICE_CONTROL_SHUTDOWN:
            System.exit(0);
            break;
        default:
            break;
        }
        return 0;
    }
}
