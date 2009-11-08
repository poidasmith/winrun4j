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
    long[] stack = new long[100];
    int ptr = stack.length - 1;

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
        push(value);
    }

    public void add(long[] args) {
        for (int i = 0; i < args.length; i++)
            add(args[i]);
    }

    private void push(long b) {
        stack[ptr--] = (int) (b);
    }

    public int size() {
        return stack.length - 1 - ptr;
    }

    public long[] toArray() {
        long[] a = new long[stack.length - 1 - ptr];
        System.arraycopy(stack, ptr + 1, a, 0, stack.length - 1 - ptr);
        return a;
    }
}
