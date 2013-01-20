package org.boris.winrun4j.test;

import org.boris.winrun4j.AbstractService;
import org.boris.winrun4j.EventLog;
import org.boris.winrun4j.ServiceException;

/**
 * A basic service.
 */
public class ServiceTest extends AbstractService
{
    public int serviceMain(String[] args) throws ServiceException {
        int count = 0;
        while (!shutdown) {
            try {
                Thread.sleep(6000);
            } catch (InterruptedException e) {
            }

            if (++count % 10 == 0)
                EventLog.report("WinRun4J Test Service", EventLog.INFORMATION, "Ping");
        }

        return 0;
    }
}
