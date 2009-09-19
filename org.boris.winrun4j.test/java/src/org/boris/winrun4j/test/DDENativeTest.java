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

import org.boris.winrun4j.DDEML;
import org.boris.winrun4j.DDEML.DdeCallback;
import org.boris.winrun4j.DDEML.DdeCallbackImpl;

public class DDENativeTest implements DdeCallback
{
    public static void main(String[] args) throws Exception {
        DdeCallbackImpl cb = new DdeCallbackImpl(new DDENativeTest());
        DDEML.DdeInitialize(cb, DDEML.APPCLASS_MONITOR | DDEML.MF_CALLBACKS);
    }

    public long callback(int type, int fmt, long conv, long hsz1, long hsz2, long data, int data1, int data2) {
        return 0;
    }
}
