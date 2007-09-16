package org.boris.winrun4j;

/**
 * A class that wraps a registry key.
 */
public class RegistryKey {
    public static final String HKCR = "HKEY_CLASSES_ROOT";
    public static final String HKCU = "HKEY_CURRENT_USER";
    public static final String HKLM = "HKEY_LOCAL_MACHINE";
    public static final String HKU = "HKEY_USERS";
    public static final String HKCC = "HKEY_CURRENT_CONFIG";
    
    // The underlying registry handle.
    private long handle;

    /**
     * Creates a new RegistryKey object.
     *
     * @param keyPath (eg. "HKEY_LOCAL_MACHINE/Software").
     */
    public RegistryKey(String keyPath) {
        handle = createKeyHandle(keyPath);
    }

    /**
     * Creates a new RegistryKey object.
     *
     * @param handle.
     */
    private RegistryKey(long handle) {
        this.handle = handle;
    }

    /**
     * Indicates if the underlying handle is valid.
     *
     * @return boolean.
     */
    public boolean isValid() {
        return handle != 0;
    }

    /**
     * Gets the subkeys for this key.
     *
     * @return RegistryKey[].
     */
    public RegistryKey[] getSubKeys() {
        long[] handles = getSubKeys(handle);
        RegistryKey[] keys = new RegistryKey[handles.length];

        for (int i = 0; i < keys.length; i++) {
            keys[i] = new RegistryKey(handles[i]);
        }

        return keys;
    }

    /**
     * Gets a subkey of a particular name (or null).
     *
     * @param name.
     *
     * @return RegistryKey.
     */
    public RegistryKey getSubKey(String name) {
        long h = getSubKey(handle, name);

        return (h == 0) ? null : new RegistryKey(h);
    }

    /**
     * Gets the values for this key.
     *
     * @return RegistryValue[].
     */
    public RegistryValue[] getValues() {
        long[] handles = getValues(handle);
        RegistryValue[] values = new RegistryValue[handles.length];

        for (int i = 0; i < values.length; i++) {
            values[i] = new RegistryValue(handle, handles[i]);
        }

        return values;
    }

    /**
     * Gets a value of a particular name.
     *
     * @param name (or null for the default).
     *
     * @return RegistryValue.
     */
    public RegistryValue getValue(String name) {
        long h = getValue(handle, name);

        return (h == 0) ? null : new RegistryValue(handle, h);
    }

    /**
     * Gets the name of the key.
     *
     * @return String.
     */
    public String getName() {
        return getName(handle);
    }

    /**
     * Gets the parent key.
     *
     * @return RegistryKey.
     */
    public RegistryKey getParent() {
        long h = getParent(handle);

        return (h == 0) ? null : new RegistryKey(h);
    }

    /**
     * Creates a sub key.
     *
     * @param name.
     *
     * @return RegistryKey.
     */
    public RegistryKey createSubKey(String name) {
        long h = createSubKey(handle, name);

        return (h == 0) ? null : new RegistryKey(h);
    }

    /**
     * Creates a value.
     *
     * @param name.
     *
     * @return RegistryValue.
     */
    public RegistryValue createValue(String name) {
        long h = createValue(handle, name);

        return (h == 0) ? null : new RegistryValue(handle, h);
    }

    /**
     * Deletes this key.
     */
    public void delete() {
        deleteKey(handle);
        handle = 0;
    }

    /**
     * Create a key handle.
     *
     * @param keyPath.
     *
     * @return long.
     */
    private native long createKeyHandle(String keyPath);

    /**
     * Get the sub key handles.
     *
     * @param handle.
     *
     * @return long[].
     */
    private native long[] getSubKeys(long handle);

    /**
     * Gets a sub key.
     *
     * @param handle.
     * @param name.
     *
     * @return long.
     */
    private native long getSubKey(long handle, String name);

    /**
     * Gets the values.
     *
     * @param handle.
     *
     * @return long[].
     */
    private native long[] getValues(long handle);

    /**
     * Gets a value.
     *
     * @param handle.
     * @param name.
     *
     * @return long.
     */
    private native long getValue(long handle, String name);

    /**
     * Gets the name.
     *
     * @param handle.
     *
     * @return String.
     */
    private native String getName(long handle);

    /**
     * Gets the parent.
     *
     * @param handle.
     *
     * @return long.
     */
    private native long getParent(long handle);

    /**
     * Creates a sub key.
     *
     * @param handle.
     * @param name.
     *
     * @return long.
     */
    private native long createSubKey(long handle, String name);

    /**
     * Creates a value.
     *
     * @param handle.
     * @param name.
     *
     * @return long.
     */
    private native long createValue(long handle, String name);

    /**
     * Deletes the key.
     *
     * @param handle.
     */
    private native void deleteKey(long handle);
}
