package org.boris.winrun4j;

/**
 * A class that wraps a registry key.
 */
public class RegistryKey {
    // Root key constants
    public static final long HKEY_CLASSES_ROOT = 0x80000000;
    public static final long HKEY_CURRENT_USER = 0x80000001;
    public static final long HKEY_LOCAL_MACHINE = 0x80000002;
    public static final long HKEY_USERS = 0x80000003;
    public static final long HKEY_CURRENT_CONFIG = 0x80000005;

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
    private long parent;
    private long handle;
    private String name;

    /**
     * Creates a new RegistryKey object.
     *
     * @param key.
     * @param path.
     */
    public RegistryKey(long key, String path) {
        this.parent = key;
        this.name = path;

        // Handle root key case
        if (path == null) {
            this.handle = key;
        }
    }

    /**
     * Creates a new RegistryKey object.
     *
     * @param parent.
     * @param path.
     */
    public RegistryKey(RegistryKey parent, String path) {
        this.parent = parent.handle;
        this.name = path;
    }

    /**
     * Opens the key.
     *
     * @return boolean.
     */
    public boolean open() {
        handle = openKeyHandle(parent, name);

        return handle != 0;
    }

    /**
     * Closes the key.
     */
    public void close() {
        closeKeyHandle(handle);
        handle = 0;
    }

    /**
     * Indicates if the key is open.
     *
     * @return boolean.
     */
    public boolean isOpen() {
        return handle != 0;
    }

    /**
     * Gets the subkeys for this key.
     *
     * @return String[].
     */
    public String[] getSubKeyNames() {
        return getSubKeyNames(handle);
    }

    /**
     * Gets a subkey of a particular name (or null).
     *
     * @param name.
     *
     * @return RegistryKey.
     */
    public RegistryKey getSubKey(String name) {
        return new RegistryKey(handle, name);
    }

    /**
     * Gets the values for this key.
     *
     * @return String[].
     */
    public String[] getValueNames() {
        return getValueNames(handle);
    }

    /**
     * Gets the name of the key.
     *
     * @return String.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the parent key.
     *
     * @return RegistryKey.
     */
    public RegistryKey getParent() {
        return null;
    }

    /**
     * Deletes this key.
     */
    public void delete() {
        deleteKey(handle);
        handle = 0;
    }

    /**
     * Gets the type of a value.
     *
     * @param name.
     *
     * @return int.
     */
    public int getType(String name) {
        return getType(handle, name);
    }

    /**
     * Gets a string.
     *
     * @param name.
     */
    public String getString(String name) {
        return getString(handle, name);
    }

    /**
     * Gets a binary value.
     *
     * @param name.
     *
     * @return byte[].
     */
    public byte[] getBinary(String name) {
        return getBinary(handle, name);
    }

    /**
     * Gets a DWORD.
     *
     * @param name.
     *
     * @return long.
     */
    public long getDoubleWord(String name) {
        return getDoubleWord(handle, name);
    }

    /**
     * Gets a value.
     *
     * @param name.
     *
     * @return long.
     */
    public long getDoubleWordLittleEndian(String name) {
        return getDoubleWordLittleEndian(handle, name);
    }

    /**
     * Gets a value.
     *
     * @param name.
     *
     * @return long.
     */
    public long getDoubleWordBigEndian(String name) {
        return getDoubleWordBigEndian(handle, name);
    }

    /**
     * Gets a value.
     *
     * @param name.
     *
     * @return String.
     */
    public String getExpandedString(String name) {
        return getExpandedString(handle, name);
    }

    /**
     * Gets a value.
     *
     * @param name.
     *
     * @return String[].
     */
    public String[] getMultiString(String name) {
        return getMultiString(handle, name);
    }

    /**
     * Sets a value.
     *
     * @param name.
     * @param value.
     */
    public void setString(String name, String value) {
        setString(handle, name, value);
    }

    /**
     * Sets a value.
     *
     * @param name.
     * @param value.
     */
    public void setBinary(String name, byte[] value) {
        setBinary(handle, name, value);
    }

    /**
     * Sets the value.
     *
     * @param name.
     * @param value.
     */
    public void setDoubleWord(String name, long value) {
        setDoubleWord(handle, name, value);
    }

    /**
     * Sets the value.
     *
     * @param name.
     * @param value.
     */
    public void setDoubleWordLittleEndian(String name, long value) {
        setDoubleWordLittleEndian(handle, name, value);
    }

    /**
     * Sets the value.
     *
     * @param name.
     * @param value.
     */
    public void setDoubleWordBigEndian(String name, long value) {
        setDoubleWordBigEndian(handle, name, value);
    }

    /**
     * Sets the value.
     *
     * @param name.
     * @param value.
     */
    public void setMultiString(String name, String[] value) {
        setMultiString(handle, name, value);
    }

    /**
     * Deletes the value.
     */
    public void deleteValue(String name) {
        deleteValue(handle, name);
    }

    /**
     * Create a key handle.
     *
     * @param rootKey
     * @param keyPath.
     *
     * @return long.
     */
    private native long openKeyHandle(long rootKey, String keyPath);

    /**
     * Close a key.
     *
     * @param handle.
     */
    private native void closeKeyHandle(long handle);

    /**
     * Get the sub key names.
     *
     * @param handle.
     *
     * @return long[].
     */
    private native String[] getSubKeyNames(long handle);

    /**
     * Gets the values.
     *
     * @param handle.
     *
     * @return String[].
     */
    private native String[] getValueNames(long handle);

    /**
     * Deletes the key.
     *
     * @param handle.
     */
    private native void deleteKey(long handle);

    /**
     * Gets the name.
     *
     * @param parent.
     * @param name.
     *
     * @return long.
     */
    private native int getType(long parent, String name);

    /**
     * Delets the key.
     *
     * @param parent.
     * @param name.
     */
    private native void deleteValue(long parent, String name);

    /**
     * Gets the value.
     *
     * @param parent.
     * @param name.
     *
     * @return String.
     */
    private native String getString(long parent, String name);

    /**
     * Gets the value.
     *
     * @param parent.
     * @param name.
     *
     * @return byte[].
     */
    private native byte[] getBinary(long parent, String name);

    /**
     * Gets the value.
     *
     * @param parent.
     * @param name.
     *
     * @return long.
     */
    private native long getDoubleWord(long parent, String name);

    /**
     * Gets the value.
     *
     * @param parent.
     * @param name.
     *
     * @return long.
     */
    private native long getDoubleWordLittleEndian(long parent, String name);

    /**
     * Gets the value.
     *
     * @param parent.
     * @param name.
     *
     * @return long.
     */
    private native long getDoubleWordBigEndian(long parent, String name);

    /**
     * Gets the value.
     *
     * @param parent.
     * @param name.
     *
     * @return String.
     */
    private native String getExpandedString(long parent, String name);

    /**
     * Gets the value.
     *
     * @param parent.
     * @param name.
     *
     * @return String[].
     */
    private native String[] getMultiString(long parent, String name);

    /**
     * Sets the value.
     *
     * @param parent.
     * @param name.
     * @param value.
     */
    private native void setString(long parent, String name, String value);

    /**
     * Sets the value.
     *
     * @param parent.
     * @param name.
     * @param value.
     */
    private native void setBinary(long parent, String name, byte[] value);

    /**
     * Sets the value.
     *
     * @param parent.
     * @param name.
     * @param value.
     */
    private native void setDoubleWord(long parent, String name, long dword);

    /**
     * Sets the value.
     *
     * @param parent.
     * @param name.
     * @param value.
     */
    private native void setDoubleWordLittleEndian(long parent, String name, long dword);

    /**
     * Sets the value.
     *
     * @param parent.
     * @param name.
     * @param value.
     */
    private native void setDoubleWordBigEndian(long parent, String name, long dword);

    /**
     * Sets the value.
     *
     * @param parent.
     * @param name.
     * @param value.
     */
    private native void setMultiString(long parent, String name, String[] value);
}
