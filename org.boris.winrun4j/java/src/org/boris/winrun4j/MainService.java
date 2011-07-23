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

public class MainService extends AbstractService
{
    public int serviceMain(String[] args) throws ServiceException {
        try {
            Class c = Class.forName(INI.getProperty("MainService.class"));
            Method m = c.getMethod("main", String[].class);
            m.invoke(null, (Object) args);
        } catch (Exception e) {
            throw new ServiceException(e);
        }
        return 0;
    }
}
