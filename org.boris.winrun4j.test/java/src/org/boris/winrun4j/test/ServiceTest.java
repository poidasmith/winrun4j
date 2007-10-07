package org.boris.winrun4j.test;

import org.boris.winrun4j.EventLog;
import org.boris.winrun4j.Service;
import org.boris.winrun4j.ServiceException;


/**
 * A basic service.
 */
public class ServiceTest implements Service, Runnable {
    private int returnCode = 0;
    private boolean shutdown = false;
    
    /* (non-Javadoc)
     * @see org.boris.winrun4j.Service#doRequest(int)
     */
    public int doRequest(int request) throws ServiceException {
        switch(request) {
        case SERVICE_ACCEPT_SHUTDOWN:
            shutdown = true;
            break;
        }
        return 0;
    }

    /* (non-Javadoc)
     * @see org.boris.winrun4j.Service#getControlsAccepted()
     */
    public int getControlsAccepted() {
        return SERVICE_ACCEPT_SHUTDOWN;
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
        Thread t = new Thread(this);
        t.setDaemon(true);
        t.start();
        
        try {
            synchronized(this) {
                wait();
            }
        } catch (Exception e) {
            throw new ServiceException(e);
        }
        
        return returnCode;
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run() {
        while(!shutdown) {
            try {
                Thread.sleep(60000);
            } catch (InterruptedException e) {
            }
            
            EventLog.report("WinRun4J Test Service", EventLog.INFORMATION, "Ping");
        }
    }
}
