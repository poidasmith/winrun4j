package org.boris.winrun4j;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class Reflection {
    
    public static void setField(Object o, String fieldName, Object value) {
        try {
            Field f = getClassField(o, fieldName);
            f.setAccessible(true);
            f.set(o, value);
        } catch (Exception e) {
        }
    }
    
    public static Object getField(Object o, String fieldName) {
        try {
            Field f = getClassField(o, fieldName);
            f.setAccessible(true);
            return f.get(o);
        } catch (Exception e) {
        }
        return null;
    }
        
    public static Object invokeStatic(Class c, String methodName, Object[] args) {
        try {
            Class[] types = null;
            if(args != null) {
                types = new Class[args.length];
                for(int i = 0; i < types.length; i++) {
                    types[i] = args[i].getClass();
                }
            }
            Method m = c.getDeclaredMethod(methodName, types);
            return m.invoke(null, args);
        } catch (Exception e) {
            return null;
        }
    }
    
    public static Object invoke(Object o, String methodName, Class[] types, Object[] args) {
        try {
            Method m = o.getClass().getDeclaredMethod(methodName, types);
            m.setAccessible(true);
            return m.invoke(o, args);
        } catch (Exception e) {
            return null;
        }
    }
    
    public static Object invoke(Object o, String methodName) {
        return invoke(o, methodName, null);
    }
    
    public static Object invoke(Object o, String methodName, Object arg1) {
        return invoke(o, methodName, new Object[] { arg1 });
    }

    public static Object invoke(Object o, String methodName, Object arg1, Object arg2) {
        return invoke(o, methodName, new Object[] { arg1, arg2 });
    }

    public static Object invoke(Object o, String methodName, Object[] args) {
        try {
            Class[] types = null;
            if(args != null) {
                types = new Class[args.length];
                for(int i = 0; i < types.length; i++) {
                    types[i] = args[i].getClass();
                }
            }
            Method m = o.getClass().getDeclaredMethod(methodName, types);
            m.setAccessible(true);
            
            return m.invoke(o, args);
        } catch (Exception e) {
            return null;
        }
    }
    
    public static Class forName(String className) {
        try {
            return Class.forName(className);
        } catch (Throwable t) {
            return null;
        }
    }
    
    public static Class forName(ClassLoader cl, String className) {
        try {
            return cl.loadClass(className);
        } catch(Throwable t) {
            return null;
        }
    }
    
    private static Field getClassField(Object o, String fieldName) {
        Class cl = o.getClass();
        while(cl != null) {
            try {
                return cl.getDeclaredField(fieldName);
            } catch (Exception e) {
                cl = cl.getSuperclass();
            }
        }
        return null;
    }

}
