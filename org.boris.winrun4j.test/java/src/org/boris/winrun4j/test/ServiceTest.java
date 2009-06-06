package org.boris.winrun4j.test;

import org.boris.winrun4j.EventLog;
import org.boris.winrun4j.Service;
import org.boris.winrun4j.ServiceException;

/**
 * A basic service.
 */
public class ServiceTest implements Service
{
    private volatile boolean shutdown = false;

    public int doRequest(int request) throws ServiceException {
        switch (request) {
        case SERVICE_CONTROL_STOP:
        case SERVICE_CONTROL_SHUTDOWN:
            shutdown = true;
            break;
        }
        return 0;
    }

    public int main(String[] args) throws ServiceException {
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
