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

import java.util.Arrays;

public class NativeStack
{
    byte[] stack = new byte[1024];
    int ptr = 1023;

    public void addArg32(long handle) {
        addArg32((int) handle);
    }

    public void addArg32(int value) {
        push(value >> 24);
        push(value >> 16);
        push(value >> 8);
        push(value);
    }

    public byte[] toBytes() {
        return Arrays.copyOfRange(stack, ptr + 1, 1024);
    }

    private void push(int b) {
        stack[ptr] = (byte) (b & 0xff);
        ptr--;
    }
}
