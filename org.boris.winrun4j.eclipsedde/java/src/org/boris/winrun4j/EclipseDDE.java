package org.boris.winrun4j;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;

import org.eclipse.equinox.launcher.Main;

/**
 * A hack to allow eclipse to load files from explorer.
 */
public class EclipseDDE {
    private static Main main = null;
    private static ClassLoader loader = null;
    
    /**
     * Launch a file. This is called from WinRun4J binary (due to dde.class setting in INI file).
     *
     * @param cmdLine.
     */
    public static void execute(String cmdLine) {
        System.out.println(cmdLine);
        try {
            Class ecs = forName(loader, "org.eclipse.core.runtime.adaptor.EclipseStarter");
            Field cf = ecs.getDeclaredField("context");
            cf.setAccessible(true);
            Object bundleContext = cf.get(null);
            Object[] bundles = (Object[]) invoke(bundleContext, "getBundles");
            for(int i = 0; i < bundles.length; i++) {
                Object bundle = bundles[i];
                Object bundleData = getSuperField(bundle, "bundledata");
                if(bundleData != null) {
                    Object symbolicName = getField(bundleData, "symbolicName");
                    if("org.eclipse.ui.ide".equals(symbolicName)) {
                        Object proxy = getField(bundle, "proxy");
                        Object loader = getField(proxy, "loader");
                        ClassLoader cl = (ClassLoader) getField(loader, "classloader");
                        Class ide = forName(cl, "org.eclipse.ui.ide.IDE");
                        Class pui = forName(cl, "org.eclipse.ui.PlatformUI");
                        Class efs = forName(cl, "org.eclipse.core.filesystem.EFS");
                        Object lfs = invokeStatic(efs, "getLocalFileSystem", null);
                        final Object fileStore = invoke(lfs, "fromLocalFile", new File(cmdLine));
                        Object workbench = invokeStatic(pui, "getWorkbench", null);
                        Object window = getField(workbench, "activeWorkbenchWindow");
                        final Object page = invoke(window, "getActivePage");
                        Class iwb = forName(cl, "org.eclipse.ui.IWorkbenchPage");
                        Class ifs = forName(cl, "org.eclipse.core.filesystem.IFileStore");
                        final Method m = ide.getDeclaredMethod("openEditorOnFileStore", new Class[] {iwb, ifs});
                        Runnable r = new Runnable() {
                            public void run() {
                                try {
                                    m.invoke(null, new Object[] { page, fileStore });
                                } catch (Exception e) {
                                }
                            }};
                        Class dsc = forName(cl, "org.eclipse.swt.widgets.Display");
                        Object display = invokeStatic(dsc, "getDefault", null);
                        invoke(display, "asyncExec", new Class[] { Runnable.class }, new Object[] {r});

                        break;
                    }
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
    
    public static class DDEMain extends Main {
        protected void basicRun(String[] args) throws Exception {
            System.getProperties().put("eclipse.startTime", Long.toString(System.currentTimeMillis())); //$NON-NLS-1$
            setSuperField(this, "commands", args);
            String[] passThruArgs = processCommandLine(args);
            if (!debug)
                debug = System.getProperty("osgi.debug") != null;
            invoke(this, "setupVMProperties");
            invoke(this, "processConfiguration");
            invoke(this, "getInstallLocation");
            URL[] bootPath = getBootPath(bootLocation);
            invoke(this, "setupJNI", bootPath);
            if (Boolean.FALSE.equals(invoke(this, "checkVersion", System.getProperty("java.version"), System.getProperty("osgi.requiredJavaVersion")))) //$NON-NLS-1$
                return;
            setSecurityPolicy(bootPath);
            invoke(this, "handleSplash", bootPath);
            beforeFwkInvocation();
            invokeFramework(passThruArgs, bootPath);
        }
        
        private void invokeFramework(String[] passThruArgs, URL[] bootPath) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, Error, Exception, InvocationTargetException {
            String type = System.getProperty("osgi.frameworkParentClassloader", System.getProperty("osgi.parentClassloader", "boot"));
            ClassLoader parent = null;
            if ("app".equalsIgnoreCase(type))
                parent = ClassLoader.getSystemClassLoader();
            else if ("ext".equalsIgnoreCase(type)) {
                ClassLoader appCL = ClassLoader.getSystemClassLoader();
                if (appCL != null)
                    parent = appCL.getParent();
            } else if ("current".equalsIgnoreCase(type))
                    parent = this.getClass().getClassLoader();
            Class scl = forName("org.eclipse.equinox.launcher.Main$StartupClassLoader");
            Constructor c = scl.getDeclaredConstructor(new Class[] { getClass().getSuperclass(), URL[].class, ClassLoader.class });
            c.setAccessible(true);
            loader = (ClassLoader) c.newInstance(new Object[] { this, bootPath, parent });
            Class clazz = loader.loadClass("org.eclipse.core.runtime.adaptor.EclipseStarter");
            Method method = clazz.getDeclaredMethod("run", new Class[] {String[].class, Runnable.class}); //$NON-NLS-1$
            try {
                method.invoke(clazz, new Object[] {passThruArgs, getSuperField(this, "splashHandler")});
            } catch (InvocationTargetException e) {
                if (e.getTargetException() instanceof Error)
                    throw (Error) e.getTargetException();
                else if (e.getTargetException() instanceof Exception)
                    throw (Exception) e.getTargetException();
                else
                    //could be a subclass of Throwable!
                    throw e;
            }
        }
    }
    
    public static void setSuperField(Object o, String fieldName, Object value) {
        try {
            Field f = o.getClass().getSuperclass().getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(o, value);
        } catch (Exception e) {
        }
    }
    
    public static Object getSuperField(Object o, String fieldName) {
        try {
            Field f = o.getClass().getSuperclass().getDeclaredField(fieldName);
            f.setAccessible(true);
            return f.get(o);
        } catch (Exception e) {
        }
        return null;
    }

    public static Object getField(Object o, String fieldName) {
        try {
            Field f = o.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            return f.get(o);
        } catch (Exception e) {
        }
        return null;
    }
    
    public static void main(String[] args) {
        int result = 0;
        try {
            main = new DDEMain();
            result = main.run(args);
        } catch (Throwable t) {
        } finally {
            if (!Boolean.getBoolean("osgi.noShutdown"))
                System.exit(result);
        }
    }    
    private static URL[] getBootPath(Object main) {

        // setup the path to the framework
        String bootLocation = null;
        String urlString = System.getProperty("osgi.framework", null);
        if (urlString != null) {
            URL url = (URL) invoke(main, "buildURL", urlString, new Boolean(true));
            bootLocation = (String) invoke(main, "resolve", urlString);
        }
        
        return (URL[]) invoke(main, "getBootPath", bootLocation);
    }
    
    private static Object invokeStatic(Class c, String methodName, Object[] args) {
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
    
    private static Object invoke(Object o, String methodName, Class[] types, Object[] args) {
        try {
            Method m = o.getClass().getDeclaredMethod(methodName, types);
            m.setAccessible(true);
            return m.invoke(o, args);
        } catch (Exception e) {
            return null;
        }
    }
    
    private static Object invoke(Object o, String methodName) {
        return invoke(o, methodName, null);
    }
    
    private static Object invoke(Object o, String methodName, Object arg1) {
        return invoke(o, methodName, new Object[] { arg1 });
    }

    private static Object invoke(Object o, String methodName, Object arg1, Object arg2) {
        return invoke(o, methodName, new Object[] { arg1, arg2 });
    }

    private static Object invoke(Object o, String methodName, Object[] args) {
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
    
    private static Class forName(String className) {
        try {
            return Class.forName(className);
        } catch (Throwable t) {
            return null;
        }
    }
    
    private static Class forName(ClassLoader cl, String className) {
        try {
            return cl.loadClass(className);
        } catch(Throwable t) {
            return null;
        }
    }
}
