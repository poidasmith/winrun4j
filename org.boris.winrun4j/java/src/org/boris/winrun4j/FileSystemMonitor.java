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

import java.io.File;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;

/**
 * A JNI wrapper on ReadDirectoryChangesW function in win32.
 */
public class FileSystemMonitor
{
    private static HashMap listeners = new HashMap();

    /**
     * Register a new listener for directory changes.
     */
    public static boolean addListener(File directory, boolean subtree, int notifyFilter,
            FileSystemListener listener) {
        if (directory == null || listener == null)
            return false;

        long handle = register(directory.getAbsolutePath(), subtree, notifyFilter, 4096);
        if (handle != 0) {
            ListenerEntry le = new ListenerEntry();
            le.directory = directory;
            le.listener = listener;
            listeners.put(new Long(handle), le);
            return true;
        }

        return false;
    }

    /**
     * Removes a listener and cleans up assocatied resources.
     */
    public static void removeListener(File directory, FileSystemListener listener) {
        Iterator i = listeners.keySet().iterator();
        Long found = null;
        while (i.hasNext()) {
            Long l = (Long) i.next();
            ListenerEntry le = (ListenerEntry) listeners.get(l);
            if (le.directory.equals(directory) && le.listener.equals(listener)) {
                closeHandle(l.longValue());
                found = l;
                break;
            }
        }

        listeners.remove(found);
    }

    /**
     * This method will be called from JNI.
     */
    public static void callback(long handle, ByteBuffer overlapped) {
        ListenerEntry le = (ListenerEntry) listeners.get(new Long(handle));
        if (le != null) {
            while (overlapped.remaining() > 0) {
                int next = overlapped.getInt();
                int action = overlapped.getInt();
                int len = overlapped.getInt();
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < len; i++) {
                    sb.append(overlapped.getChar());
                }
                le.listener.fileChange(action, sb.toString());
                if (next == 0)
                    break;
                overlapped.position(next);
            }
        }
    }

    /**
     * Internal method - used to register a new listener for a directory
     * changes.
     */
    private static native long register(String directory, boolean subtree, int notifyFilter,
            int bufferSize);

    /**
     * Closes the handle to the directory and cleans up memory.
     */
    private static native void closeHandle(long handle);

    private static class ListenerEntry
    {
        public File directory;
        public FileSystemListener listener;
    }
}
