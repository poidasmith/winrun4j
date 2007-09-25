package org.boris.winrun4j;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.eclipse.equinox.launcher.Main;

/**
 * A hack to allow eclipse to load files from explorer.
 */
public class EclipseDDE {
    private static Main main = null;
    static ClassLoader loader = null;
    
    /**
     * Launch a file. This is called from WinRun4J binary (due to dde.class setting in INI file).
     *
     * @param cmdLine.
     */
    public static void execute(String cmdLine) {
        try {
            Class ecs = Reflection.forName(loader, "org.eclipse.core.runtime.adaptor.EclipseStarter");
            Field cf = ecs.getDeclaredField("context");
            cf.setAccessible(true);
            Object bundleContext = cf.get(null);
            Object[] bundles = (Object[]) Reflection.invoke(bundleContext, "getBundles");
            for(int i = 0; i < bundles.length; i++) {
                Object bundle = bundles[i];
                Object bundleData = Reflection.getField(bundle, "bundledata");
                if(bundleData != null) {
                    Object symbolicName = Reflection.getField(bundleData, "symbolicName");
                    if("org.eclipse.ui.ide".equals(symbolicName)) {
                        Object proxy = Reflection.getField(bundle, "proxy");
                        Object loader = Reflection.getField(proxy, "loader");
                        ClassLoader cl = (ClassLoader) Reflection.getField(loader, "classloader");
                        Class ide = Reflection.forName(cl, "org.eclipse.ui.ide.IDE");
                        Class pui = Reflection.forName(cl, "org.eclipse.ui.PlatformUI");
                        Class efs = Reflection.forName(cl, "org.eclipse.core.filesystem.EFS");
                        Object lfs = Reflection.invokeStatic(efs, "getLocalFileSystem", null);
                        final Object fileStore = Reflection.invoke(lfs, "fromLocalFile", new File(cmdLine));
                        Object workbench = Reflection.invokeStatic(pui, "getWorkbench", null);
                        Object window = Reflection.getField(workbench, "activeWorkbenchWindow");
                        final Object page = Reflection.invoke(window, "getActivePage");
                        Class iwb = Reflection.forName(cl, "org.eclipse.ui.IWorkbenchPage");
                        Class ifs = Reflection.forName(cl, "org.eclipse.core.filesystem.IFileStore");
                        final Method m = ide.getDeclaredMethod("openEditorOnFileStore", new Class[] {iwb, ifs});
                        Runnable r = new Runnable() {
                            public void run() {
                                try {
                                    m.invoke(null, new Object[] { page, fileStore });
                                } catch (Exception e) {
                                }
                            }};
                        Class dsc = Reflection.forName(cl, "org.eclipse.swt.widgets.Display");
                        Object display = Reflection.invokeStatic(dsc, "getDefault", null);
                        Reflection.invoke(display, "asyncExec", new Class[] { Runnable.class }, new Object[] {r});

                        break;
                    }
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
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
}
