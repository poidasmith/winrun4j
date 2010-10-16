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

import java.nio.ByteBuffer;

/**
 * General JNI utilities for windows. Use at own risk.
 */
/**
 * @author Poida
 *
 */
/**
 * @author Poida
 * 
 */
public class Native
{
    public static final boolean IS_64 = "amd64".equals(System.getProperty("os.arch"));

    /**
     * Load a native library (eg. "kernel32")
     */
    public static native long loadLibrary(String filename);

    /**
     * Free the library.
     */
    public static native void freeLibrary(long ptr);

    /**
     * Get a pointer to a function exported by the library.
     */
    public static native long getProcAddress(long ptr, String name);

    /**
     * Allocate a block of memory.
     */
    public static native long malloc(int size);

    /**
     * Free the memory.
     */
    public static native void free(long ptr);

    /**
     * Get an accessor for a given block of memory.
     */
    public static native ByteBuffer fromPointer(long ptr, long size);

    /**
     * Binds a function pointer to a native method.
     */
    public static native boolean bind(Class clazz, String fn, String sig, long ptr);

    /**
     * Creates a global reference to an object.
     */
    public static native long newGlobalRef(Object obj);

    /**
     * Deletes a global reference to an object.
     */
    public static native void deleteGlobalRef(long handle);

    /**
     * Returns the object id.
     */
    public static native long getObjectId(Object o);

    /**
     * Returns the object.
     */
    public static native Object getObject(long handle);

    /**
     * Gets a method id.
     */
    public static native long getMethodId(Class clazz, String name, String sig, boolean isStatic);
}
