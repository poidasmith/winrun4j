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

public class NativeStack
{
    byte[] stack = new byte[1024];
    int ptr = 1023;

    public NativeStack() {
    }

    public NativeStack(long[] args) {
        add(args);
    }

    public void add64(long handle) {
        add(handle >> 32);
        add(handle & 0x0ffffffff);
    }

    public void add(float value) {
        add(Float.floatToIntBits(value));
    }

    public void add(double value) {
        add64(Double.doubleToLongBits(value));
    }

    public void add(long value) {
        push(value >> 24);
        push(value >> 16);
        push(value >> 8);
        push(value);
    }

    public void add(long[] args) {
        for (int i = 0; i < args.length; i++)
            add(args[i]);
    }

    public void add(byte[] stack) {
        for (int i = 0; i < stack.length; i++) {
            this.stack[ptr--] = stack[i];
        }
    }

    public byte[] toBytes() {
        byte[] a = new byte[1023 - ptr];
        System.arraycopy(stack, ptr + 1, a, 0, a.length);
        return a;
    }

    private void push(long b) {
        stack[ptr--] = (byte) (b & 0xff);
    }
}
