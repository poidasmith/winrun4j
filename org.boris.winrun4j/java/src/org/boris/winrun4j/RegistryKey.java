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

import java.util.HashMap;
import java.util.Map;

/**
 * A class that wraps a registry key.
 */
public class RegistryKey
{
    // Root key constants
    public static final RegistryKey HKEY_CLASSES_ROOT = new RegistryKey(0x80000000);
    public static final RegistryKey HKEY_CURRENT_USER = new RegistryKey(0x80000001);
    public static final RegistryKey HKEY_LOCAL_MACHINE = new RegistryKey(0x80000002);
    public static final RegistryKey HKEY_USERS = new RegistryKey(0x80000003);
    public static final RegistryKey HKEY_CURRENT_CONFIG = new RegistryKey(0x80000005);

    // Root key names
    private static Map rootNames = new HashMap();
    static {
        rootNames.put("HKEY_CLASSES_ROOT", HKEY_CLASSES_ROOT);
        rootNames.put("HKEY_CURRENT_USER", HKEY_CURRENT_USER);
        rootNames.put("HKEY_LOCAL_MACHINE", HKEY_LOCAL_MACHINE);
        rootNames.put("HKEY_USERS", HKEY_USERS);
        rootNames.put("HKEY_CURRENT_CONFIG", HKEY_CURRENT_CONFIG);
    }

    public static RegistryKey getRootKey(String name) {
        return (RegistryKey) rootNames.get(name);
    }

    // Value type constants
    public static final int TYPE_NONE = 0x00000001;
    public static final int TYPE_SZ = 0x00000002;
    public static final int TYPE_EXPAND_SZ = 5;
    public static final int TYPE_BINARY = 1;
    public static final int TYPE_DWORD = 2;
    public static final int TYPE_DWORD_LITTLE_ENDIAN = 3;
    public static final int TYPE_DWORD_BIG_ENDIAN = 4;
    public static final int TYPE_LINK = 6;
    public static final int TYPE_MULTI_SZ = 7;
    public static final int TYPE_QWORD = 9;
    public static final int TYPE_QWORD_LITTLE_ENDIAN = 10;

    // The underlying registry handle.
    private RegistryKey parent;
    private boolean isRoot;
    private long handle; // The root handle
    private String[] path;

    /**
     * Creates a new RegistryKey object.
     * 
     * @param key.
     * @param path.
     */
    private RegistryKey(long key) {
        this.isRoot = true;
        this.handle = key;
    }

    /**
     * Creates a new RegistryKey object.
     * 
     * @param parent.
     * @param path.
     */
    public RegistryKey(RegistryKey parent, String path) {
        this.parent = parent;
        this.handle = parent.handle;
        if (parent.isRoot) {
            this.path = new String[] { path };
        } else {
            this.path = new String[parent.path.length + 1];
            System.arraycopy(parent.path, 0, this.path, 0, parent.path.length);
            this.path[parent.path.length] = path;
        }
    }

    /**
     * Creates a new RegistryKey object.
     * 
     * @param parent.
     * @param path.
     */
    public RegistryKey(RegistryKey parent, String[] path) {
        this.parent = parent;
        this.handle = parent.handle;
        if (parent.isRoot) {
            this.path = path;
        } else {
            this.path = new String[parent.path.length + path.length];
            System.arraycopy(parent.path, 0, this.path, 0, parent.path.length);
            System.arraycopy(path, 0, this.path, parent.path.length, path.length);
        }
    }

    /**
     * Indicates if this registry key exists.
     */
    public boolean exists() {
        if (isRoot)
            return true;
        long h = openKeyHandle(handle, path, true);
        closeKeyHandle(h);
        return h != 0;
    }

    /**
     * Opens up the key for this path. Windows doesn't provide a way to open up
     * a path.
     */
    private long openKeyHandle(long handle, String[] path, boolean readOnly) {
        long h = handle;
        if (path == null)
            return h;
        for (int i = 0; i < path.length; i++) {
            long nh = openKeyHandle(h, path[i], readOnly);
            if (h != handle)
                closeKeyHandle(h);
            h = nh;
        }
        return h;
    }

    /**
     * Gets the subkeys for this key.
     * 
     * @return String[].
     */
    public String[] getSubKeyNames() {
        long h = openKeyHandle(handle, path, true);
        String[] res = getSubKeyNames(h);
        closeKeyHandle(h);
        return res;
    }

    /**
     * Gets a subkey of a particular name (or null).
     * 
     * @param name.
     * 
     * @return RegistryKey.
     */
    public RegistryKey getSubKey(String name) {
        return new RegistryKey(this, name);
    }

    /**
     * Creates a sub key.
     * 
     * @param name
     * @return RegistryKey.
     */
    public RegistryKey createSubKey(String name) {
        long h = openKeyHandle(handle, path, false);
        if (h != 0) {
            long n = createSubKey(h, name);
            closeKeyHandle(h);
            if (n != 0) {
                closeKeyHandle(n);
                return new RegistryKey(this, name);
            }
        }

        return null;
    }

    /**
     * Gets the values for this key.
     * 
     * @return String[].
     */
    public String[] getValueNames() {
        long h = openKeyHandle(handle, path, true);
        String[] res = getValueNames(h);
        closeKeyHandle(h);
        return res;
    }

    /**
     * Gets the path of the key.
     * 
     * @return String.
     */
    public String[] getPath() {
        return path;
    }

    /**
     * Gets the parent key.
     * 
     * @return RegistryKey.
     */
    public RegistryKey getParent() {
        return parent;
    }

    /**
     * Deletes the sub key.
     */
    public void deleteSubKey(String subKey) {
        if (!isRoot) {
            long h = openKeyHandle(handle, path, false);
            deleteSubKey(h, subKey);
            closeKeyHandle(h);
        }
    }

    /**
     * Gets the type of a value.
     * 
     * @param name.
     * 
     * @return int.
     */
    public long getType(String name) {
        long h = openKeyHandle(handle, path, true);
        long res = getType(h, name);
        closeKeyHandle(h);
        return res;
    }

    /**
     * Gets a string.
     * 
     * @param name.
     */
    public String getString(String name) {
        long h = openKeyHandle(handle, path, true);
        String res = getString(h, name);
        closeKeyHandle(h);
        return res;
    }

    /**
     * Gets a binary value.
     * 
     * @param name.
     * 
     * @return byte[].
     */
    public byte[] getBinary(String name) {
        long h = openKeyHandle(handle, path, true);
        byte[] res = getBinary(h, name);
        closeKeyHandle(h);
        return res;
    }

    /**
     * Gets a DWORD.
     * 
     * @param name.
     * 
     * @return long.
     */
    public long getDoubleWord(String name) {
        long h = openKeyHandle(handle, path, true);
        long res = getDoubleWord(h, name);
        closeKeyHandle(h);
        return res;
    }

    /**
     * Gets a value.
     * 
     * @param name.
     * 
     * @return String.
     */
    public String getExpandedString(String name) {
        long h = openKeyHandle(handle, path, true);
        String res = getExpandedString(h, name);
        closeKeyHandle(h);
        return res;
    }

    /**
     * Gets a value.
     * 
     * @param name.
     * 
     * @return String[].
     */
    public String[] getMultiString(String name) {
        long h = openKeyHandle(handle, path, true);
        String[] res = getMultiString(h, name);
        closeKeyHandle(h);
        return res;
    }

    /**
     * Sets a value.
     * 
     * @param name.
     * @param value.
     */
    public void setString(String name, String value) {
        long h = openKeyHandle(handle, path, false);
        setString(h, name, value);
        closeKeyHandle(h);
    }

    /**
     * Sets a value.
     * 
     * @param name.
     * @param value.
     */
    public void setBinary(String name, byte[] value) {
        long h = openKeyHandle(handle, path, false);
        setBinary(h, name, value);
        closeKeyHandle(h);
    }

    /**
     * Sets the value.
     * 
     * @param name.
     * @param value.
     */
    public void setDoubleWord(String name, long value) {
        long h = openKeyHandle(handle, path, false);
        setDoubleWord(h, name, value);
        closeKeyHandle(h);
    }

    /**
     * Sets the value.
     * 
     * @param name.
     * @param value.
     */
    public void setMultiString(String name, String[] value) {
        long h = openKeyHandle(handle, path, false);
        setMultiString(h, name, value);
        closeKeyHandle(h);
    }

    /**
     * Deletes the value.
     */
    public void deleteValue(String name) {
        long h = openKeyHandle(handle, path, false);
        deleteValue(h, name);
        closeKeyHandle(h);
    }

    /**
     * Create a key handle.
     * 
     * @param rootKey
     * @param keyPath.
     * 
     * @return long.
     */
    public static native long openKeyHandle(long rootKey, String keyPath, boolean readOnly);

    /**
     * Close a key.
     * 
     * @param handle.
     */
    public static native void closeKeyHandle(long handle);

    /**
     * Get the sub key names.
     * 
     * @param handle.
     * 
     * @return long[].
     */
    public static native String[] getSubKeyNames(long handle);

    /**
     * Gets the values.
     * 
     * @param handle.
     * 
     * @return String[].
     */
    public static native String[] getValueNames(long handle);

    /**
     * Creates a sub key.
     * 
     * @param handle.
     * @param key.
     */
    public static native long createSubKey(long handle, String key);

    /**
     * Deletes the sub key.
     * 
     * @param handle.
     */
    public static native void deleteSubKey(long handle, String key);

    /**
     * Gets the name.
     * 
     * @param parent.
     * @param name.
     * 
     * @return long.
     */
    public static native long getType(long parent, String name);

    /**
     * Delets the key.
     * 
     * @param parent.
     * @param name.
     */
    public static native void deleteValue(long parent, String name);

    /**
     * Gets the value.
     * 
     * @param parent.
     * @param name.
     * 
     * @return String.
     */
    public static native String getString(long parent, String name);

    /**
     * Gets the value.
     * 
     * @param parent.
     * @param name.
     * 
     * @return byte[].
     */
    public static native byte[] getBinary(long parent, String name);

    /**
     * Gets the value.
     * 
     * @param parent.
     * @param name.
     * 
     * @return long.
     */
    public static native long getDoubleWord(long parent, String name);

    /**
     * Gets the value.
     * 
     * @param parent.
     * @param name.
     * 
     * @return String.
     */
    public static native String getExpandedString(long parent, String name);

    /**
     * Gets the value.
     * 
     * @param parent.
     * @param name.
     * 
     * @return String[].
     */
    public static native String[] getMultiString(long parent, String name);

    /**
     * Sets the value.
     * 
     * @param parent.
     * @param name.
     * @param value.
     */
    public static native void setString(long parent, String name, String value);

    /**
     * Sets the value.
     * 
     * @param parent.
     * @param name.
     * @param value.
     */
    public static native void setBinary(long parent, String name, byte[] value);

    /**
     * Sets the value.
     * 
     * @param parent.
     * @param name.
     * @param value.
     */
    public static native void setDoubleWord(long parent, String name, long dword);

    /**
     * Sets the value.
     * 
     * @param parent.
     * @param name.
     * @param value.
     */
    public static native void setMultiString(long parent, String name, String[] value);
}
