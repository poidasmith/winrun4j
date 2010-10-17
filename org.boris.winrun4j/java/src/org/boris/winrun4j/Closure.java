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

import java.lang.reflect.Method;

import org.boris.winrun4j.FFI.CIF;
import org.boris.winrun4j.PInvoke.Delegate;

public class Closure
{
    private static boolean is64 = Native.IS_64;
    private static long invokeId = Native.getMethodId(Closure.class, "invoke", "(JJ)V", false);

    // The closure method
    private Object callbackObj;
    private Method callbackMethod;

    // The parameter information
    private int[] argTypes;
    private int returnType;
    private CIF cif;

    // The closure handles
    private long objectId;
    private long methodId;
    private long handle;
    private long callback;

    private Closure() {
    }

    public static Closure build(Class clazz, Object callback) {
        if (callback == null)
            return null;

        Method[] methods = clazz.getMethods();
        if (methods.length > 1) {
            for (Method m : methods) {
                Delegate d = m.getAnnotation(Delegate.class);
                if (d != null) {
                    return build(callback, m);
                }
            }
        } else {
            // This is the typical case where an interface is defined
            // with only the callback method
            return build(callback, methods[0]);
        }

        return null;
    }

    public static Closure build(Object obj, Method m) {
        Closure c = new Closure();
        c.callbackObj = obj;
        c.callbackMethod = m;

        // Determine argument types
        Class[] params = m.getParameterTypes();
        Class returnType = m.getReturnType();
        c.argTypes = new int[params.length];
        for (int i = 0; i < params.length; i++) {
            c.argTypes[i] = NativeBinder.getArgType(params[i]);
        }
        c.returnType = NativeBinder.getArgType(returnType);

        // Create callback pointer and connect to our invoke method
        c.cif = CIF.prepare(FFI.ABI_STDCALL, params.length);
        c.objectId = Native.newGlobalRef(c);
        c.methodId = invokeId;
        c.handle = FFI.prepareClosure(c.cif.get(), c.objectId, c.methodId);
        c.callback = NativeHelper.getPointer(c.handle);

        return c;
    }

    public long getPointer() {
        return callback;
    }

    public void invoke(long resp, long args) {
        Object[] jargs = new Object[argTypes.length];
        if (jargs.length > 0) {
        }

        try {
            Object res = callbackMethod.invoke(callbackObj, jargs);
        } catch (Exception e) {
            // TODO: propogate exception
        }
    }

    public synchronized void destroy() {
        if (cif != null) {
            cif.destroy();
            cif = null;
        }
    }
}
