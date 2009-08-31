/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Peter Smith
 *******************************************************************************/
package org.boris.winrun4j.asm;

import java.lang.reflect.Method;

public abstract class Callback
{
    private Object object;
    private Method method;

    public Callback(Class clazz, Object o, Method m) {
        this.object = o;
        this.method = m;
    }

    // Allow for overriding
    private final int icall(long stack, int size) {
        return callback(stack, size);
    }

    public int callback(long stack, int size) {
        return 0;
    }

    public long compileFunction() {
        // generate function and store into memory
        return 0;
    }
}
