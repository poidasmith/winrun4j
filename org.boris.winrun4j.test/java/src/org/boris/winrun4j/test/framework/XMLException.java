/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Peter Smith
 *******************************************************************************/
package org.boris.winrun4j.test.framework;

public class XMLException extends Exception
{
    public XMLException() {
    }

    public XMLException(String message) {
        super(message);
    }

    public XMLException(Throwable cause) {
        super(cause);
    }

    public XMLException(String message, Throwable cause) {
        super(message, cause);
    }
}
