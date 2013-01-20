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
 * An exception thrown by a service.
  */
public class ServiceException extends Exception {
    /**
     * Creates a new ServiceException object.
     */
    public ServiceException() {
    }

    /**
     * Creates a new ServiceException object.
     *
     * @param message.
     * @param cause.
     */
    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new ServiceException object.
     *
     * @param message.
     */
    public ServiceException(String message) {
        super(message);
    }

    /**
     * Creates a new ServiceException object.
     *
     * @param cause.
     */
    public ServiceException(Throwable cause) {
        super(cause);
    }
}
