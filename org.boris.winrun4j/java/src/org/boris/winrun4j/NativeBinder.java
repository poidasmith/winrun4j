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
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.boris.winrun4j.FFI.CIF;
import org.boris.winrun4j.PInvoke.ByteArrayBuilder;
import org.boris.winrun4j.PInvoke.Callback;
import org.boris.winrun4j.PInvoke.DllImport;
import org.boris.winrun4j.PInvoke.IntPtr;
import org.boris.winrun4j.PInvoke.MarshalAs;
import org.boris.winrun4j.PInvoke.NativeStruct;
import org.boris.winrun4j.PInvoke.Struct;
import org.boris.winrun4j.PInvoke.UIntPtr;

public class NativeBinder
{
    private static Map<Method, NativeBinder> methods = new HashMap();
    private static Map<Class, NativeStruct> ansiStructs = new ConcurrentHashMap();
    private static Map<Class, NativeStruct> wideStructs = new ConcurrentHashMap();

    private static boolean is64 = Native.IS_64;
    private static long invokeId = Native.getMethodId(NativeBinder.class, "invoke", "(JJ)V", false);

    private long function;
    private CIF callbackCif;
    private CIF functionCif;

    // The function sigature details
    private int[] argTypes;
    private Class[] params;
    private int returnType;
    private int returnSize;
    private boolean wideChar;

    // Used to contain the args passed to the native function
    private int argSize;
    private long avalue;
    private long pvalue;

    // The java native method closure
    private long objectId;
    private long methodId;
    private long handle;
    private long callback;

    public static void bind(Class clazz) {
        bind(clazz, null);
    }

    public static void bind(Class clazz, String library) {
        Method[] cms = clazz.getDeclaredMethods();
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

        // Determine the library/dll name
        String lib = di.value();
        if (lib == null || lib.length() == 0)
            lib = di.lib();
        if (lib == null || lib.length() == 0)
            lib = library;
        long lp = 0;
        if (!di.internal())
            lp = Native.loadLibrary(lib);
        if (lp == 0 && !di.internal())
            return;

        // Determine the procedure name and get the pointer
        String fn = di.entryPoint();
        if (fn == null || fn.length() == 0)
            fn = m.getName();
        long fun = Native.getProcAddress(lp, fn);
        if (fun == 0) {
            fn += di.wideChar() ? "W" : "A";
            fun = Native.getProcAddress(lp, fn);
        }
        if (fun == 0)
            // TODO: might want to throw here
            return;

        NativeBinder nb = new NativeBinder();
        Class[] params = nb.params = m.getParameterTypes();
        Class returnType = m.getReturnType();
        nb.function = fun;

        // The closure CIF has two extra params for standard JNI env/self args
        int[] types = new int[params.length + 2];
        types[0] = types[1] = FFI.FFI_TYPE_POINTER;
        for (int i = 0; i < params.length; i++) {
            int t = FFI.FFI_TYPE_POINTER;
            if (!is64 && long.class.equals(params[i]))
                t = FFI.FFI_TYPE_SINT64;
            types[i + 2] = t;
        }
        nb.callbackCif = CIF.prepare(is64 ? FFI.ABI_WIN64 : FFI.ABI_STDCALL, types);
        nb.functionCif = CIF.prepare(is64 ? FFI.ABI_WIN64 : FFI.ABI_STDCALL, params.length);

        // Determine the argument types - for quicker conversion
        nb.argTypes = new int[params.length];
        for (int i = 0; i < params.length; i++) {
            nb.argTypes[i] = getArgType(params[i], m.getName());
        }
        nb.returnType = getArgType(returnType, m.getName());
        if (nb.returnType == ARG_STRING) {
            MarshalAs ma = m.getAnnotation(MarshalAs.class);
            if (ma == null) {
                throw new RuntimeException("Return type of string must have MarshalAs size: " + m.getName());
            }
            nb.returnSize = ma.sizeConst();
        }
        nb.wideChar = di.wideChar();

        // Space for the args array used to call the native function
        if (params.length > 0) {
            nb.argSize = params.length * NativeHelper.PTR_SIZE;
            nb.avalue = Native.malloc(nb.argSize);
            nb.pvalue = Native.malloc(nb.argSize);
            ByteBuffer pb = NativeHelper.getBuffer(nb.pvalue, nb.argSize);
            for (int i = 0; i < nb.argTypes.length; i++) {
                if (is64) {
                    pb.putLong(nb.avalue + (i * NativeHelper.PTR_SIZE));
                } else {
                    pb.putInt((int) (nb.avalue + (i * NativeHelper.PTR_SIZE)));
                }
            }
        }

        // Setup the closure that is called through the native method
        nb.objectId = Native.newGlobalRef(nb);
        nb.methodId = invokeId;
        nb.handle = FFI.prepareClosure(nb.callbackCif.get(), nb.objectId, nb.methodId);

        // The actual closure pointer is stored as first item in the closure
        // structure
        nb.callback = NativeHelper.getPointer(nb.handle);

        // Bind the closure to the native method and register
        if (!Native.bind(clazz, m.getName(), generateSig(m), nb.callback))
            nb.destroy();
        else
            methods.put(m, nb);
    }

    public static String generateSig(Method m) {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        Class[] c = m.getParameterTypes();
        for (Class cl : c) {
            sb.append(generateSig(cl));
        }
        sb.append(")");
        sb.append(generateSig(m.getReturnType()));
        return sb.toString();
    }

    public static String generateSig(Class c) {
        if (boolean.class.equals(c)) {
            return "Z";
        } else if (byte.class.equals(c)) {
            return "B";
        } else if (char.class.equals(c)) {
            return "C";
        } else if (short.class.equals(c)) {
            return "S";
        } else if (int.class.equals(c)) {
            return "I";
        } else if (long.class.equals(c)) {
            return "J";
        } else if (float.class.equals(c)) {
            return "F";
        } else if (double.class.equals(c)) {
            return "D";
        } else if (void.class.equals(c)) {
            return "V";
        } else if (c.isArray()) {
            return "[" + generateSig(c.getComponentType());
        } else {
            return "L" + c.getName().replace('.', '/') + ";";
        }
    }

    static final int ARG_INT = 1;
    static final int ARG_BOOL = 2;
    static final int ARG_STRING_BUILDER = 3;
    static final int ARG_UINT_PTR = 4;
    static final int ARG_INT_PTR = 5;
    static final int ARG_STRING = 6;
    static final int ARG_CALLBACK = 7;
    static final int ARG_STRUCT_PTR = 8;
    static final int ARG_LONG = 9;
    static final int ARG_BYTE_ARRAY_BUILDER = 10;
    static final int ARG_SHORT = 11;
    static final int ARG_VOID = 12;
    static final int ARG_BYTE = 13;
    static final int ARG_RAW_CLOSURE = 14;
    static final int ARG_STRING_PTR = 15; // only used in structs
    static final int ARG_BYTE_ARRAY = 16;

    static int getArgType(Class clazz, String source) {
        if (int.class.equals(clazz)) {
            return ARG_INT;
        } else if (boolean.class.equals(clazz)) {
            return ARG_BOOL;
        } else if (byte.class.equals(clazz)) {
            return ARG_BYTE;
        } else if (short.class.equals(clazz)) {
            return ARG_SHORT;
        } else if (long.class.equals(clazz)) {
            return ARG_LONG;
        } else if (void.class.equals(clazz)) {
            return ARG_VOID;
        } else if (IntPtr.class.equals(clazz)) {
            return ARG_INT_PTR;
        } else if (UIntPtr.class.equals(clazz)) {
            return ARG_UINT_PTR;
        } else if (StringBuilder.class.equals(clazz)) {
            return ARG_STRING_BUILDER;
        } else if (String.class.equals(clazz)) {
            return ARG_STRING;
        } else if (Struct.class.isAssignableFrom(clazz)) {
            return ARG_STRUCT_PTR;
        } else if (Callback.class.isAssignableFrom(clazz)) {
            return ARG_CALLBACK;
        } else if (Closure.class.isAssignableFrom(clazz)) {
            return ARG_RAW_CLOSURE;
        } else if (ByteArrayBuilder.class.isAssignableFrom(clazz)) {
            return ARG_BYTE_ARRAY_BUILDER;
        } else if (byte[].class.isAssignableFrom(clazz)) {
            return ARG_BYTE_ARRAY;
        } else {
            throw new RuntimeException("Unrecognized native argument type: " + clazz + " from " + source);
        }
    }

    public void invoke(long resp, long args) throws Exception {
        // The args are coming from the java native method, need to convert
        // into native args
        Object[] jargs = new Object[argSize];

        if (argTypes.length > 0) {
            long offset = 2 * NativeHelper.PTR_SIZE; // skip env,self
            ByteBuffer ib = NativeHelper.getBuffer(args + offset, argSize);
            ByteBuffer pb = NativeHelper.getBuffer(pvalue, argSize);
            ByteBuffer vb = NativeHelper.getBuffer(avalue, argSize);
            for (int i = 0; i < argTypes.length; i++) {
                long argValue = 0;
                long pointer = avalue + (i * NativeHelper.PTR_SIZE);
                long inp = is64 ? ib.getLong() : ib.getInt();
                long inv = inp == 0 ? 0 : NativeHelper.getPointer(inp);

                switch (argTypes[i]) {
                case ARG_BOOL:
                case ARG_SHORT:
                case ARG_INT:
                    argValue = inv;
                    jargs[i] = new Integer((int) inv);
                    break;
                case ARG_UINT_PTR:
                case ARG_INT_PTR:
                    if (inv != 0) {
                        jargs[i] = Native.getObject(inv);
                        int value = (int) ((IntPtr) jargs[i]).value;
                        argValue = Native.malloc(4);
                        NativeHelper.setInt(argValue, value);

                        // Need to check if previous arg is string builder
                        if (i > 0 && value > 0) {
                            long sptr = 0;
                            if (argTypes[i - 1] == ARG_STRING_BUILDER) {
                                int ssize = value;
                                if (wideChar)
                                    ssize *= 2;
                                sptr = Native.malloc(ssize);
                            } else if (argTypes[i - 1] == ARG_BYTE_ARRAY_BUILDER) {
                                sptr = Native.malloc(value);
                            }
                            if (sptr != 0)
                                NativeHelper.setInt(avalue + (i - 1) * NativeHelper.PTR_SIZE, (int) sptr);
                        }
                    }
                    break;
                case ARG_STRING_BUILDER:
                case ARG_BYTE_ARRAY_BUILDER:
                    if (inv != 0) {
                        jargs[i] = Native.getObject(inv);
                    }
                    break;
                case ARG_STRING:
                    if (inv != 0) {
                        jargs[i] = Native.getObject(inv);
                        argValue = NativeHelper.toNativeString(jargs[i], wideChar);
                    }
                    break;
                case ARG_CALLBACK:
                    if (inv != 0) {
                        Object o = Native.getObject(inv);
                        Closure c = Closure.build(params[i], o, wideChar);
                        if (c == null)
                            throw new RuntimeException("Could not create callback for parameter " + (i + 1));
                        argValue = c.getPointer();
                        jargs[i] = c;
                    }
                    break;
                case ARG_RAW_CLOSURE:
                    if (inv != 0) {
                        jargs[i] = Native.getObject(inv);
                        argValue = ((Closure) jargs[i]).getPointer();
                    }
                    break;
                case ARG_STRUCT_PTR:
                    if (inv != 0) {
                        Object o = Native.getObject(inv);
                        NativeStruct ns = null;
                        if (wideChar)
                            ns = wideStructs.get(params[i]);
                        else
                            ns = ansiStructs.get(params[i]);
                        if (ns == null) {
                            ns = NativeStruct.fromClass(params[i], wideChar);
                            if (wideChar)
                                wideStructs.put(params[i], ns);
                            else
                                ansiStructs.put(params[i], ns);
                        }
                        argValue = ns.toNative(o);
                        jargs[i] = o;
                    }
                    break;
                case ARG_BYTE_ARRAY:
                    if (inv != 0) {
                        byte[] b = (byte[]) Native.getObject(inv);
                        jargs[i] = b;
                        argValue = NativeHelper.toNative(b, 0, b.length);
                    }
                }

                if (is64) {
                    vb.putLong(argValue);
                    pb.putLong(pointer);
                } else {
                    vb.putInt((int) (argValue & 0xffffffff));
                    pb.putInt((int) (pointer & 0xffffffff));
                }
            }
        }

        // Call the native function - TODO: handle all return types
        FFI.call(functionCif.get(), function, resp, pvalue);

        if (resp != 0 && returnType == ARG_STRING) {
            long ptr = NativeHelper.getPointer(resp);
            if (ptr != 0) {
                String s = NativeHelper.getString(ptr, returnSize, wideChar);
                long sptr = Native.getObjectId(s);
                NativeHelper.setPointer(resp, sptr);
            }
        }

        // Convert any out params and free up memory
        if (argTypes.length > 0) {
            ByteBuffer vb = NativeHelper.getBuffer(avalue, argSize);
            long prevPointer = 0;

            for (int i = 0; i < argTypes.length; i++) {
                long argValue = is64 ? vb.getLong() : vb.getInt();

                switch (argTypes[i]) {
                case ARG_INT:
                case ARG_BOOL:
                case ARG_SHORT:
                    break;
                case ARG_STRING:
                    NativeHelper.free(argValue);
                    break;
                case ARG_UINT_PTR:
                case ARG_INT_PTR:
                    if (argValue != 0) {
                        int value = NativeHelper.getInt(argValue);
                        ((IntPtr) jargs[i]).value = value;

                        if (i > 0) {
                            if (argTypes[i - 1] == ARG_STRING_BUILDER) {
                                if (value > 0 && jargs[i - 1] != null) {
                                    int ssize = value;
                                    if (wideChar)
                                        ssize *= 2;
                                    String s = NativeHelper.getString(prevPointer, ssize, wideChar);
                                    ((StringBuilder) jargs[i - 1]).setLength(0);
                                    ((StringBuilder) jargs[i - 1]).append(s);
                                }
                                NativeHelper.free(prevPointer);
                            } else if (argTypes[i - 1] == ARG_BYTE_ARRAY_BUILDER) {
                                if (value > 0 && jargs[i - 1] != null) {
                                    ByteBuffer bb = NativeHelper.getBuffer(prevPointer, value);
                                    byte[] buffer = new byte[value];
                                    bb.get(buffer);
                                    ((ByteArrayBuilder) jargs[i - 1]).set(buffer);
                                }
                                NativeHelper.free(prevPointer);
                            }
                        }

                        NativeHelper.free(argValue);
                    }
                    break;
                case ARG_STRING_BUILDER:
                    break;
                case ARG_CALLBACK:
                    if (jargs[i] != null)
                        ((Closure) jargs[i]).destroy();
                    break;
                case ARG_BYTE_ARRAY:
                    if (jargs[i] != null) {
                        NativeHelper.free(argValue);
                    }
                    break;
                case ARG_RAW_CLOSURE:
                    break;
                case ARG_STRUCT_PTR:
                    if (jargs[i] != null) {
                        NativeStruct ns = null;
                        if (wideChar)
                            ns = wideStructs.get(params[i]);
                        else
                            ns = ansiStructs.get(params[i]);
                        ns.fromNative(argValue, jargs[i]);
                    }
                    NativeHelper.free(argValue);
                    break;
                }

                prevPointer = argValue;
            }
        }
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

        if (handle != 0) {
            FFI.freeClosure(handle);
            handle = 0;
            callback = 0;
        }

        if (pvalue != 0) {
            Native.free(pvalue);
            pvalue = 0;
        }

        if (avalue != 0) {
            Native.free(avalue);
            avalue = 0;
        }

        if (objectId != 0) {
            Native.deleteGlobalRef(objectId);
            objectId = 0;
        }
    }
}
