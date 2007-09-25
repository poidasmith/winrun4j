package org.boris.winrun4j;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;

import org.eclipse.equinox.launcher.Main;

public class DDEMain extends Main {
    /* (non-Javadoc)
     * @see org.eclipse.equinox.launcher.Main#basicRun(java.lang.String[])
     */
    protected void basicRun(String[] args) throws Exception {
        System.getProperties().put("eclipse.startTime", Long.toString(System.currentTimeMillis())); //$NON-NLS-1$
        Reflection.setField(this, "commands", args);
        String[] passThruArgs = processCommandLine(args);
        if (!debug)
            debug = System.getProperty("osgi.debug") != null;
        Reflection.invoke(this, "setupVMProperties");
        Reflection.invoke(this, "processConfiguration");
        Reflection.invoke(this, "getInstallLocation");
        URL[] bootPath = getBootPath(bootLocation);
        Reflection.invoke(this, "setupJNI", bootPath);
        if (Boolean.FALSE.equals(Reflection.invoke(this, "checkVersion", System.getProperty("java.version"), System.getProperty("osgi.requiredJavaVersion")))) //$NON-NLS-1$
            return;
        setSecurityPolicy(bootPath);
        Reflection.invoke(this, "handleSplash", bootPath);
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
        Class scl = Reflection.forName("org.eclipse.equinox.launcher.Main$StartupClassLoader");
        Constructor c = scl.getDeclaredConstructor(new Class[] { DDEMain.class.getSuperclass(), URL[].class, ClassLoader.class });
        c.setAccessible(true);
        EclipseDDE.loader = (ClassLoader) c.newInstance(new Object[] { this, bootPath, parent });
        Class clazz = EclipseDDE.loader.loadClass("org.eclipse.core.runtime.adaptor.EclipseStarter");
        Method method = clazz.getDeclaredMethod("run", new Class[] {String[].class, Runnable.class}); //$NON-NLS-1$
        try {
            method.invoke(clazz, new Object[] {passThruArgs, Reflection.getField(this, "splashHandler")});
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