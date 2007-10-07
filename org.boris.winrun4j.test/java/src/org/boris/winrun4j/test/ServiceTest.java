package org.boris.winrun4j.test;

import org.boris.winrun4j.Service;
import org.boris.winrun4j.ServiceException;


/**
 * A basic service.
 */
public class ServiceTest implements Service {
    /* (non-Javadoc)
     * @see org.boris.winrun4j.Service#doRequest(int)
     */
    public int doRequest(int request) throws ServiceException {
        return 0;
    }

    /* (non-Javadoc)
     * @see org.boris.winrun4j.Service#getControlsAccepted()
     */
    public int getControlsAccepted() {
        return SERVICE_ACCEPT_PAUSE_CONTINUE | SERVICE_ACCEPT_STOP | SERVICE_ACCEPT_SHUTDOWN;
    }

    /* (non-Javadoc)
     * @see org.boris.winrun4j.Service#getName()
     */
    public String getName() {
        return "WinRun4J Test Service";
    }

    /* (non-Javadoc)
     * @see org.boris.winrun4j.Service#main(java.lang.String[])
     */
    public int main(String[] args) throws ServiceException {
        return 0;
    }
}
