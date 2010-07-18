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

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

import org.boris.commons.lang.Threads;
import org.boris.winrun4j.AbstractService;
import org.boris.winrun4j.ServiceException;
import org.boris.winrun4j.test.framework.Launcher;

public class BasicService extends AbstractService
{
    public int serviceMain(String[] args) throws ServiceException {
        testArgs(args);
        while (!shutdown) {
            Threads.sleepQuietly(10);
        }
        return 0;
    }

    private void testArgs(String[] args) throws ServiceException {
        File f = new File(args[0]);
        try {
            PrintStream ps = new PrintStream(new FileOutputStream(f));
            for (String arg : args)
                ps.println(arg);
            ps.flush();
            ps.close();
        } catch (Exception e) {
            throw new ServiceException(e);
        }
    }

    public int serviceRequest(int control) throws ServiceException {
        return super.serviceRequest(control);
    }

    public static Launcher launcher() {
        Launcher l = new Launcher();
        l.service(BasicService.class,
                "Basic Service", "A test service for winrun4j").
                testcp().
                startup("auto").
                depends("Tcpip").
                debug(8787, true, false);
        return l;
    }
}
