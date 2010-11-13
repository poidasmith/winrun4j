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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

import org.boris.winrun4j.PInvoke.UIntPtr;
import org.boris.winrun4j.Registry.QUERY_INFO;

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
    public static final int TYPE_NONE = 1;
    public static final int TYPE_SZ = 2;
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
        Registry.closeKey(h);
        return h != 0;
    }

    /**
     * Gets a value from a key path.
     * 
     * @param The path.
     * @return String.
     */
    public String get(String path) {
        if (path == null)
            return getString(null);

        boolean defaultValue = path.endsWith("/");
        String[] p = path.split("/");
        if (p.length == 0) {
            return getString(path);
        }

        RegistryKey k = this;
        int len = defaultValue ? p.length : p.length - 1;
        for (int i = 0; i < len; i++) {
            k = k.getSubKey(p[i]);
        }

        if (k.exists())
            return defaultValue ? k.getString(null) : k.getString(p[p.length - 1]);

        return null;
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
                Registry.closeKey(h);
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
        QUERY_INFO qi = Registry.queryInfoKey(h);
        if (qi == null)
            return null;
        String[] keys = new String[qi.subKeyCount];
        for (int i = 0; i < keys.length; i++) {
            StringBuilder name = new StringBuilder();
            UIntPtr cbName = new UIntPtr(qi.maxSubkeyLen);
            int res = Registry.enumKeyEx(h, i, name, cbName, 0, 0, 0, null);
            if (res != 0)
                continue;
            keys[i] = name.toString();
        }

        Registry.closeKey(h);
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
            UIntPtr phkResult = new UIntPtr();
            int res = Registry.createKey(h, name, phkResult);
            Registry.closeKey(h);
            if (res != 0)
                return null;
            if (phkResult.value != 0) {
                Registry.closeKey(phkResult.value);
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
        QUERY_INFO info = Registry.queryInfoKey(handle);
        if (info == null)
            return null;
        String[] res = new String[info.valueCount];
        for (int i = 0; i < res.length; i++) {
            StringBuilder sb = new StringBuilder();
            UIntPtr valueLen = new UIntPtr(255);
            if (Registry.enumValue(handle, i, sb, valueLen, 0, null, null, null) != 0)
                res[i] = sb.toString();
        }
        Registry.closeKey(h);
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
            Registry.deleteKey(h, subKey);
            Registry.closeKey(h);
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
        UIntPtr type = new UIntPtr();
        int res = Registry.queryValueEx(h, name, 0, type, (StringBuilder) null, null);
        Registry.closeKey(h);
        if (res == 0)
            return type.value;
        return -1;
    }

    /**
     * Gets a string.
     * 
     * @param name.
     */
    public String getString(String name) {
        long h = openKeyHandle(handle, path, true);
        byte[] data = Registry.queryValueEx(h, name, 512);
        Registry.closeKey(h);
        if (data != null)
            return NativeHelper.getString(data, true);
        return null;
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
        byte[] res = Registry.queryValueEx(h, name, 4096);
        Registry.closeKey(h);
        return res;
    }

    /**
     * Gets a DWORD.
     * 
     * @param name.
     * 
     * @return int.
     */
    public int getDoubleWord(String name, int defaultValue) {
        long h = openKeyHandle(handle, path, true);
        byte[] b = Registry.queryValueEx(h, name, 4);
        int res = defaultValue;
        if (b != null && b.length == 4) {
            res = ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getInt();
        }
        Registry.closeKey(h);
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
        byte[] b = Registry.queryValueEx(h, name, 4096);
        String[] res = null;
        if (b != null)
            res = NativeHelper.getMultiString(ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN), true);
        Registry.closeKey(h);
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
        byte[] b = NativeHelper.toBytes(value, true);
        Registry.setValueEx(h, name, 0, Registry.REG_SZ, b, b.length);
        Registry.closeKey(h);
    }

    /**
     * Sets a value.
     * 
     * @param name.
     * @param value.
     */
    public void setExpandedString(String name, String value) {
        long h = openKeyHandle(handle, path, false);
        byte[] b = NativeHelper.toBytes(value, true);
        Registry.setValueEx(h, name, 0, Registry.REG_EXPAND_SZ, b, b.length);
        Registry.closeKey(h);
    }

    /**
     * Sets a value.
     * 
     * @param name.
     * @param value.
     */
    public void setBinary(String name, byte[] value) {
        long h = openKeyHandle(handle, path, false);
        Registry.setValueEx(h, name, 0, 3, value, value.length);
        Registry.closeKey(h);
    }

    /**
     * Sets the value.
     * 
     * @param name.
     * @param value.
     */
    public void setDoubleWord(String name, int value) {
        long h = openKeyHandle(handle, path, false);
        byte[] b = new byte[4];
        ByteBuffer bb = ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN);
        bb.putInt(value);
        Registry.setValueEx(h, name, 0, Registry.REG_DWORD, b, b.length);
        Registry.closeKey(h);
    }

    /**
     * Sets a multi-string value.
     * 
     * @param name.
     * @param value.
     */
    public void setMultiString(String name, String[] value) {
        long h = openKeyHandle(handle, path, false);
        byte[] b = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            for (int i = 0; i < value.length; i++) {
                bos.write(NativeHelper.toBytes(value[i], true));
            }
            bos.write(new byte[] { 0, 0 });
            b = bos.toByteArray();
        } catch (IOException e) {
            // Should not happen as it is a byte array
        }
        Registry.setValueEx(h, name, 0, Registry.REG_MULTI_SZ, b, b.length);
        Registry.closeKey(h);
    }

    /**
     * Deletes the value.
     */
    public void deleteValue(String name) {
        long h = openKeyHandle(handle, path, false);
        Registry.deleteKey(h, name);
        Registry.closeKey(h);
    }

    /**
     * Open a registry key in read or write mode.
     */
    private long openKeyHandle(long rootKey, String keyPath, boolean readOnly) {
        UIntPtr key = new UIntPtr();
        Registry.openKeyEx(rootKey, keyPath, 0, readOnly ? 0x20019 : 0xF003F, key);
        return key.value;
    }
}
