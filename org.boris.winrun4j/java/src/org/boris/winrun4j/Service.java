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

/**
 * A windows service.
 */
public interface Service
{
    // Controls
    int SERVICE_CONTROL_STOP = 0x00000001;
    int SERVICE_CONTROL_PAUSE = 0x00000002;
    int SERVICE_CONTROL_CONTINUE = 0x00000003;
    int SERVICE_CONTROL_INTERROGATE = 0x00000004;
    int SERVICE_CONTROL_SHUTDOWN = 0x00000005;
    int SERVICE_CONTROL_PARAMCHANGE = 0x00000006;
    int SERVICE_CONTROL_NETBINDADD = 0x00000007;
    int SERVICE_CONTROL_NETBINDREMOVE = 0x00000008;
    int SERVICE_CONTROL_NETBINDENABLE = 0x00000009;
    int SERVICE_CONTROL_NETBINDDISABLE = 0x0000000A;
    int SERVICE_CONTROL_DEVICEEVENT = 0x0000000B;
    int SERVICE_CONTROL_HARDWAREPROFILECHANGE = 0x0000000C;
    int SERVICE_CONTROL_POWEREVENT = 0x0000000D;
    int SERVICE_CONTROL_SESSIONCHANGE = 0x0000000E;

    /**
     * Perform a service request.
     * 
     * @param control.
     * 
     * @return int.
     * 
     * @throws ServiceException If an error occurs.
     */
    public int doRequest(int control) throws ServiceException;

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
