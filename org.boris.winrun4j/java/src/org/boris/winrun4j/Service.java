package org.boris.winrun4j;

/**
 * A windows service.
 */
public interface Service {
    int SERVICE_ACCEPT_STOP = 0x00000001;
    int SERVICE_ACCEPT_PAUSE_CONTINUE = 0x00000002;
    int SERVICE_ACCEPT_SHUTDOWN = 0x00000004;
    int SERVICE_ACCEPT_PARAMCHANGE = 0x00000008;
    int SERVICE_ACCEPT_NETBINDCHANGE = 0x00000010;
    int SERVICE_ACCEPT_HARDWAREPROFILECHANGE = 0x00000020;
    int SERVICE_ACCEPT_POWEREVENT = 0x00000040;
    int SERVICE_ACCEPT_SESSIONCHANGE = 0x00000080;

    /**
     * Get the service name.
     *
     * @return String.
     */
    public String getName();

    /**
     * Get the functions accepted.
     *
     * @return int.
     */
    public int getControlsAccepted();

    /**
     * Perform a service request.
     *
     * @param request.
     *
     * @return int.
     *
     * @throws ServiceException  If an error occurs.
     */
    public int doRequest(int request) throws ServiceException;

    /**
     * Run the service.
     *
     * @param args.
     *
     * @return int.
     *
     * @throws ServiceException If an error occurs.
     */
    public int main(String[] args) throws ServiceException;
}
