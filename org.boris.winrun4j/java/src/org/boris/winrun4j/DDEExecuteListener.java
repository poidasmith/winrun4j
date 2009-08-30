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
 * A listener for an execute call.
 */
public interface DDEExecuteListener
{
    /**
     * Execute based on the given command (line).
     * 
     * @param commnd.
     */
    void execute(String command);
}
