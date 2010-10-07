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
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import org.boris.winrun4j.FFI.CIF;

public class NativeBinder implements FFI.Closure
{
    private static Map<Method, NativeBinder> methods = new HashMap();

    private long function;
    private CIF callbackCif;
    private CIF functionCif;
    private int[] argTypes;
    private int[] argFlags;
    private int returnType;
    private int returnFlags;
    private long fargs;
    private long objectId;
    private long methodId;
    private long handle;
    private long callback;

    public static void bind(Class clazz) {
        bind(clazz, null);
    }

    public static void bind(Class clazz, String library) {
        Method[] cms = clazz.getMethods();
        for (Method m : cms) {
            int mod = m.getModifiers();
            if (!Modifier.isStatic(mod))
                continue;
            if (!Modifier.isNative(mod))
                continue;

            NativeBinder nb = methods.get(m);
            if (nb != null)
                nb.destroy();

            register(clazz, m, library);
        }
    }

    private static void register(Class clazz, Method m, String library) {
        DllImport di = m.getAnnotation(DllImport.class);
        if (di == null)
            return;
        String lib = di.value();
        if (lib == null || lib.length() == 0)
            lib = library;
        long lp = Native.loadLibrary(lib);
        if (lp == 0)
            return;
        String fn = di.function();
        if (fn == null || fn.length() == 0)
            fn = m.getName();
        long fun = Native.getProcAddress(lp, fn);
        if (fun == 0) {
            fn += di.wideChar() ? "W" : "A";
            fun = Native.getProcAddress(lp, fn);
        }
        if (fun == 0)
            return;
        Class[] params = m.getParameterTypes();
        Class returnType = m.getReturnType();
        NativeBinder nb = new NativeBinder();
        nb.function = fun;
        nb.callbackCif = CIF.prepare(FFI.ABI_STDCALL, params.length + 2);
        nb.functionCif = CIF.prepare(FFI.ABI_STDCALL, params.length);
        nb.argTypes = new int[params.length];
        nb.argFlags = new int[params.length];
        for (int i = 0; i < params.length; i++)
            nb.argTypes[i] = getArgType(params[i]);
        nb.returnType = getArgType(returnType);
        nb.returnFlags = 0;
        nb.objectId = Native.newGlobalRef(nb);
        nb.methodId = Native.getMethodId(NativeBinder.class, "invoke", "(JJ)V", false);
        nb.handle = FFI.prepareClosure(nb.callbackCif.get(), nb.objectId, nb.methodId);
        nb.callback = NativeHelper.getInt(nb.handle);
        if (!Native.bind(clazz, m.getName(), generateSig(m), nb.callback))
            nb.destroy();
        else
            methods.put(m, nb);
    }

    private static String generateSig(Method m) {
        return "()I";
    }

    private static int getArgType(Class clazz) {
        if (int.class.equals(clazz)) {
            return 1;
        } else if (boolean.class.equals(clazz)) {
            return 2;
        }
        return 0;
    }

    public void invoke(long resp, long args) {
        FFI.call(functionCif.get(), function, resp, fargs);
    }

    public synchronized void destroy() {
        if (callbackCif != null) {
            callbackCif.destroy();
            callbackCif = null;
        }

        if (functionCif != null) {
            functionCif.destroy();
            functionCif = null;
        }
    }
}
