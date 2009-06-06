/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Peter Smith
 *******************************************************************************/
package org.boris.winrun4j.test;

import org.boris.winrun4j.AbstractService;
import org.boris.winrun4j.Log;
import org.boris.winrun4j.ServiceException;

public class TestServiceArgs extends AbstractService
{
    public int main(String[] args) throws ServiceException {
        if (args == null) {
            Log.error("Null Args");
        } else {
            Log.info("args.length=" + args.length);
            for (int i = 0; i < args.length; i++) {
                Log.info("args[" + i + "]=" + args[i]);
            }
        }

        while (!shutdown) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
        }
        return 0;
    }
}
