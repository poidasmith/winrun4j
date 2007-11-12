package org.boris.winrun4j.test;

import org.boris.winrun4j.EventLog;
import org.boris.winrun4j.Service;
import org.boris.winrun4j.ServiceException;


/**
 * A basic service.
 */
public class ServiceTest implements Service {
    private int returnCode = 0;
    private boolean shutdown = false;
    
    /* (non-Javadoc)
     * @see org.boris.winrun4j.Service#doRequest(int)
     */
    public int doRequest(int request) throws ServiceException {
        switch(request) {
        case SERVICE_CONTROL_STOP:
        case SERVICE_CONTROL_SHUTDOWN:
            shutdown = true;
            break;
        }
        return 0;
    }

    /* (non-Javadoc)
     * @see org.boris.winrun4j.Service#getControlsAccepted()
     */
    public int getControlsAccepted() {
        return SERVICE_ACCEPT_STOP | SERVICE_ACCEPT_SHUTDOWN;
    }

    /* (non-Javadoc)
     * @see org.boris.winrun4j.Service#getName()
     */
    public String getName() {
        return "WinRun4J Test Service";
    }

    /* (non-Javadoc)
     * @see org.boris.winrun4j.Service#getDescription()
     */
    public String getDescription() {
        return "An example service using WinRun4J.";
    }

    /* (non-Javadoc)
     * @see org.boris.winrun4j.Service#main(java.lang.String[])
     */
    public int main(String[] args) throws ServiceException {
        int count = 0;
        while(!shutdown) {
            try {
                Thread.sleep(6000);
            } catch (InterruptedException e) {
            }
            
            if(++count % 10 == 0) 
                EventLog.report("WinRun4J Test Service", EventLog.INFORMATION, "Ping");
        }
        
        return returnCode;
    }
}
