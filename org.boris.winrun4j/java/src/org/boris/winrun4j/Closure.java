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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;

import org.boris.winrun4j.FFI.CIF;
import org.boris.winrun4j.PInvoke.Delegate;
import org.boris.winrun4j.PInvoke.IntPtr;
import org.boris.winrun4j.PInvoke.NativeStruct;
import org.boris.winrun4j.PInvoke.UIntPtr;

public class Closure
{
    private static boolean is64 = Native.IS_64;
    private static long invokeId = Native.getMethodId(Closure.class, "invoke", "(JJ)V", false);

    // The closure method
    private Object callbackObj;
    private Method callbackMethod;
    private boolean wideChar;

    // The parameter information
    private Class[] params;
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

    public static Closure build(Class clazz, Object callback, boolean wideChar) {
        if (callback == null)
            return null;

        Method[] methods = clazz.getMethods();
        if (methods.length > 1) {
            for (Method m : methods) {
                Delegate d = m.getAnnotation(Delegate.class);
                if (d != null) {
                    return build(callback, m, wideChar);
                }
            }
        } else {
            // This is the typical case where an interface is defined
            // with only the callback method
            return build(callback, methods[0], wideChar);
        }

        return null;
    }

    public static Closure build(Object obj, Method m, boolean wideChar) {
        Closure c = new Closure();
        c.callbackObj = obj;
        c.callbackMethod = m;
        c.wideChar = wideChar;

        // Determine argument types
        c.params = m.getParameterTypes();
        Class returnType = m.getReturnType();
        c.argTypes = new int[c.params.length];
        for (int i = 0; i < c.params.length; i++) {
            c.argTypes[i] = NativeBinder.getArgType(c.params[i], m.getName());
        }
        c.returnType = NativeBinder.getArgType(returnType, m.getName());

        // Create callback pointer and connect to our invoke method
        c.cif = CIF.prepare(is64 ? FFI.ABI_WIN64 : FFI.ABI_STDCALL, c.params.length);
        c.objectId = Native.newGlobalRef(c);
        c.methodId = invokeId;
        c.handle = FFI.prepareClosure(c.cif.get(), c.objectId, c.methodId);
        c.callback = NativeHelper.getPointer(c.handle);

        return c;
    }

    public long getPointer() {
        return callback;
    }

    public void invoke(long resp, long args) throws Throwable {
        Object[] jargs = new Object[argTypes.length];
        if (jargs.length > 0) {
            ByteBuffer argp = NativeHelper.getBuffer(args, NativeHelper.PTR_SIZE * argTypes.length);
            for (int i = 0; i < jargs.length; i++) {
                long pValue = is64 ? argp.getLong() : argp.getInt();
                long aValue = 0;
                if (pValue != 0)
                    aValue = NativeHelper.getPointer(pValue);
                switch (argTypes[i]) {
                case NativeBinder.ARG_BOOL:
                    jargs[i] = aValue != 0;
                    break;
                case NativeBinder.ARG_INT:
                    jargs[i] = (int) aValue;
                    break;
                case NativeBinder.ARG_LONG:
                    jargs[i] = aValue;
                    break;
                case NativeBinder.ARG_UINT_PTR:
                    Object ptr = null;
                    if (aValue != 0) {
                        ptr = new UIntPtr(NativeHelper.getInt(aValue));
                    }
                    jargs[i] = ptr;
                    break;
                case NativeBinder.ARG_INT_PTR:
                    ptr = null;
                    if (aValue != 0) {
                        ptr = new IntPtr(NativeHelper.getInt(aValue));
                    }
                    jargs[i] = ptr;
                    break;
                case NativeBinder.ARG_STRUCT_PTR:
                    if (aValue != 0) {
                        Object so = params[i].newInstance();
                        NativeStruct ns = NativeBinder.getStruct(params[i], wideChar);
                        ns.fromNative(aValue, so);
                        jargs[i] = so;
                    }
                    break;
                case NativeBinder.ARG_STRING:
                    if (aValue != 0) {
                        jargs[i] = NativeHelper.getString(aValue, 4096, wideChar);
                    }
                    break;
                }
            }
        }

        Object res = null;
        try {
            res = callbackMethod.invoke(callbackObj, jargs);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }

        long resv = 0;

        if (res != null) {
            switch (returnType) {
            case NativeBinder.ARG_BOOL:
                resv = ((Boolean) res) ? 1 : 0;
                break;
            case NativeBinder.ARG_INT:
                resv = ((Integer) res);
                break;
            case NativeBinder.ARG_LONG:
                resv = ((Long) res);
                break;
            }
        }

        NativeHelper.setPointer(resp, resv);

        if (jargs.length > 0) {
            ByteBuffer argp = NativeHelper.getBuffer(args, NativeHelper.PTR_SIZE * argTypes.length);
            for (int i = 0; i < jargs.length; i++) {
                long pValue = is64 ? argp.getLong() : argp.getInt();
                long aValue = 0;
                if (pValue != 0)
                    aValue = NativeHelper.getPointer(pValue);
                Object o = jargs[i];
                switch (argTypes[i]) {
                case NativeBinder.ARG_INT_PTR:
                case NativeBinder.ARG_UINT_PTR:
                    if (aValue != 0 && o != null) {
                        int value = (int) ((IntPtr) o).value;
                        if (aValue != 0) {
                            NativeHelper.setInt(aValue, value);
                        }
                    }
                    break;
                case NativeBinder.ARG_STRUCT_PTR:
                    if (aValue != 0 && o != null) {
                        NativeStruct ns = NativeBinder.getStruct(params[i], wideChar);
                        ns.toNative(aValue, o);
                    }
                    break;
                }
            }
        }
    }

    public synchronized void destroy() {
        if (cif != null) {
            cif.destroy();
            cif = null;
        }

        if (objectId != 0) {
            Native.deleteGlobalRef(objectId);
            objectId = 0;
        }
    }
}
