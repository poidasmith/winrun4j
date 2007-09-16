package org.boris.winrun4j;

/**
 * Encapsulates a registry value.
 */
public class RegistryValue {
    public static final int BINARY = 1;
    public static final int DWORD = 2;
    public static final int DWORD_LITTLE_ENDIAN = 3;
    public static final int DWORD_BIG_ENDIAN = 4;
    public static final int EXPAND_SZ = 5;
    public static final int LINK = 6;
    public static final int MULTI_SZ = 7;
    public static final int NONE = 8;
    public static final int QWORD = 9;
    public static final int QWORD_LITTLE_ENDIAN = 10;
    public static final int SZ = 11;
    
    // The key handle and the value handle.
    private long parent;
    private long handle;

    /**
     * Creates a new RegistryValue object.
     *
     * @param parent.
     * @param handle.
     */
    RegistryValue(long parent, long handle) {
        this.parent = parent;
        this.handle = handle;
    }

    /**
     * Indicates if the value field is valid.
     *
     * @return boolean.
     */
    public boolean isValid() {
        return (parent != 0) && (handle != 0);
    }

    /**
     * Gets the name of the field.
     *
     * @return String.
     */
    public String getName() {
        return getName(parent, handle);
    }

    /**
     * Gets the type of the value.
     *
     * @return int.
     */
    public int getType() {
        return getType(parent, handle);
    }

    /**
     * Gets the binary value.
     *
     * @return byte[].
     */
    public byte[] getBinary() {
        return getBinary(parent, handle);
    }

    /**
     * Gets the DWORD.
     *
     * @return long.
     */
    public long getDoubleWord() {
        return getDoubleWord(parent, handle);
    }

    /**
     * Gets the value.
     *
     * @return long.
     */
    public long getDoubleWordLittleEndian() {
        return getDoubleWorldLittleEndian(parent, handle);
    }

    /**
     * Gets the value.
     *
     * @return long.
     */
    public long getDoubleWordBigEndian() {
        return getDoubleWorldBigndian(parent, handle);
    }

    /**
     * Gets the value.
     *
     * @return String.
     */
    public String getExpandedString() {
        return getExpandedString(parent, handle);
    }

    /**
     * Gets the value.
     *
     * @return String[].
     */
    public String[] getMultiString() {
        return getMultiString(parent, handle);
    }

    /**
     * Sets the value.
     *
     * @param value.
     */
    public void setString(String value) {
        setString(parent, handle, value);
    }

    /**
     * Sets the value.
     *
     * @param value.
     */
    public void setBinary(byte[] value) {
        setBinary(parent, handle, value);
    }

    /**
     * Sets the value.
     *
     * @param value.
     */
    public void setDoubleWord(long value) {
        setDoubleWord(parent, handle, value);
    }

    /**
     * Sets the value.
     *
     * @param value.
     */
    public void setDoubleWordLittleEndian(long value) {
        setDoubleWordLittleEndian(parent, handle, value);
    }

    /**
     * Sets the value.
     *
     * @param value.
     */
    public void setDoubleWordBigEndian(long value) {
        setDoubleWordBigEndian(parent, handle, value);
    }

    /**
     * Sets the value.
     *
     * @param value.
     */
    public void setMultiString(String[] value) {
        setMultiString(parent, handle, value);
    }

    /**
     * Sets the value.
     *
     * @param value.
     */
    public String getString() {
        return toString();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return toString(parent, handle);
    }

    /**
     * Deletes the value.
     */
    public void delete() {
        delete(parent, handle);
    }

    /**
     * Gets the name.
     *
     * @param parent.
     * @param handle.
     *
     * @return String.
     */
    private native String getName(long parent, long handle);

    /**
     * Gets the name.
     *
     * @param parent.
     * @param handle.
     *
     * @return long.
     */
    private native int getType(long parent, long handle);

    /**
     * Delets the key.
     *
     * @param parent.
     * @param handle.
     */
    private native void delete(long parent, long handle);

    /**
     * Gets the value.
     *
     * @param parent.
     * @param handle.
     *
     * @return String.
     */
    private native String toString(long parent, long handle);

    /**
     * Gets the value.
     *
     * @param parent.
     * @param handle.
     *
     * @return byte[].
     */
    private native byte[] getBinary(long parent, long handle);

    /**
     * Gets the value.
     *
     * @param parent.
     * @param handle.
     *
     * @return long.
     */
    private native long getDoubleWord(long parent, long handle);

    /**
     * Gets the value.
     *
     * @param parent.
     * @param handle.
     *
     * @return long.
     */
    private native long getDoubleWorldLittleEndian(long parent, long handle);

    /**
     * Gets the value.
     *
     * @param parent.
     * @param handle.
     *
     * @return long.
     */
    private native long getDoubleWorldBigndian(long parent, long handle);

    /**
     * Gets the value.
     *
     * @param parent.
     * @param handle.
     *
     * @return String.
     */
    private native String getExpandedString(long parent, long handle);

    /**
     * Gets the value.
     *
     * @param parent.
     * @param handle.
     *
     * @return String[].
     */
    private native String[] getMultiString(long parent, long handle);

    /**
     * Sets the value.
     *
     * @param parent.
     * @param handle.
     * @param value.
     */
    private native void setString(long parent, long handle, String value);

    /**
     * Sets the value.
     *
     * @param parent.
     * @param handle.
     * @param value.
     */
    private native void setBinary(long parent, long handle, byte[] value);

    /**
     * Sets the value.
     *
     * @param parent.
     * @param handle.
     * @param value.
     */
    private native void setDoubleWord(long parent, long handle, long dword);

    /**
     * Sets the value.
     *
     * @param parent.
     * @param handle.
     * @param value.
     */
    private native void setDoubleWordLittleEndian(long parent, long handle,
        long dword);

    /**
     * Sets the value.
     *
     * @param parent.
     * @param handle.
     * @param value.
     */
    private native void setDoubleWordBigEndian(long parent, long handle,
        long dword);

    /**
     * Sets the value.
     *
     * @param parent.
     * @param handle.
     * @param value.
     */
    private native void setMultiString(long parent, long handle, String[] value);
}
