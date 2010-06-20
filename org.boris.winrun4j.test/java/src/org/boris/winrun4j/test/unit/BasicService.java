/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Peter Smith
 *******************************************************************************/
package org.boris.winrun4j.test.unit;

import org.boris.commons.lang.Threads;
import org.boris.winrun4j.AbstractService;
import org.boris.winrun4j.ServiceException;
import org.boris.winrun4j.test.framework.Launcher;
import org.boris.winrun4j.winapi.Kernel32;
import org.boris.winrun4j.winapi.Services;

public class BasicService extends AbstractService
{
    public int serviceMain(String[] args) throws ServiceException {
        while (!shutdown) {
            Threads.sleepQuietly(10);
        }
        return 0;
    }

    public int serviceRequest(int control) throws ServiceException {
        if (Services.SERVICE_CONTROL_SHUTDOWN == control)
            Kernel32.debugBreak();
        return super.serviceRequest(control);
    }

    public static Launcher launcher() {
        Launcher l = new Launcher();
        l.service(BasicService.class,
                "Basic Service", "A test service for winrun4j").
                startup("auto").
                depends("Tcpip").
                depends("Bonjour");
        return l;
    }
}
