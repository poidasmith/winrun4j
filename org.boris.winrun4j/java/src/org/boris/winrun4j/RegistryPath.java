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

import java.util.StringTokenizer;

public class RegistryPath
{
    public static void setString(String path, String value) throws RegistryException {
        Path p = parse(path);
        if (p != null) {
            p.key.setString(p.property, value);
        }
    }

    public static String getString(String path) throws RegistryException {
        Path p = parse(path);
        if (p == null)
            return null;
        String res = p.key.getString(p.property);
        return res;
    }

    private static Path parse(String path) throws RegistryException {
        StringTokenizer st = new StringTokenizer(path, "/\\");
        String base = st.nextToken();
        RegistryKey root = RegistryKey.getRootKey(base);
        if (root == null) {
            throw new RegistryException("Unknown root key: " + base);
        }

        RegistryKey k = root;
        String property = null;
        while (st.hasMoreTokens()) {
            String t = st.nextToken();
            if (!st.hasMoreTokens() && t.startsWith("@")) {
                property = t.substring(1);
            } else {
                k = k.getSubKey(t);
            }
        }

        return new Path(property, k);
    }

    private static class Path
    {
        public Path(String property, RegistryKey key) {
            this.property = property;
            this.key = key;
        }

        public String property;
        public RegistryKey key;
    }
}
