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

import org.boris.winrun4j.winapi.Console;

public class ConsoleTest
{
    public static void main(String[] args) throws Exception {
        Console.AllocConsole();
        Console.SetConsoleTitle("Testing Console");
        Console.SetConsoleCtrlHandler(new Console.HandlerRoutine() {
            public boolean handlerRoutine(int dwCtrlType) {
                System.out.println(dwCtrlType);
                Console.WriteConsole(0, Integer.toHexString(dwCtrlType));
                return true;
            }
        }, true);
        Thread.sleep(500000);
    }
}
