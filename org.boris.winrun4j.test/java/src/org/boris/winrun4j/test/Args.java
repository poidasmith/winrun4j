/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Peter Smith
 *******************************************************************************/
package org.boris.winrun4j.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Simple command line parser - assumes we have a series of "-command value" or
 * (optionally) "/command value" entries. Eg if we have
 * 
 * <pre>
 * mayapp.exe -file test.java -source network -dir C:/TEMP -debug
 * </pre>
 * 
 * This will map to:
 * 
 * <pre>
 * file=test.java
 * source=network
 * dir=C:/TEMP
 * debug=true
 * </pre>
 * 
 * Multiple entries are turned into Lists:
 * 
 * <pre>
 * myapp.exe -id 1 -id 2 -id fred
 * </pre>
 * 
 * will map to:
 * 
 * <pre>
 * id=[1,2,fred]
 * </pre>
 */
public class Args
{
    public static Map valueOf(String[] args, boolean upper) {
        Map m = new HashMap();
        if (args != null) {
            Iterator i = Arrays.asList(args).iterator();
            String last = null;
            String next = null;
            while (next != null || i.hasNext()) {
                String n = next != null ? next : (String) i.next();
                if (n.startsWith("-") || n.startsWith("/")) {
                    last = n;
                    String k = n.substring(1);
                    if (upper)
                        k = k.toUpperCase();
                    Object v = Boolean.TRUE;
                    if (i.hasNext()) {
                        String s = (String) i.next();
                        if (s.startsWith("-") || s.startsWith("/")) {
                            next = s;
                            last = s;
                        } else {
                            v = s;
                        }
                    }
                    Object e = m.get(k);
                    if (e == null) {
                        m.put(k, v);
                    } else if (e instanceof List) {
                        ((List) e).add(v);
                    } else {
                        ArrayList a = new ArrayList();
                        a.add(e);
                        a.add(v);
                        m.put(k, a);
                    }
                } else if (last != null) {
                    Object e = m.get(last);
                    if (e instanceof List) {
                        ((List) e).add(n);
                    }
                } else {
                    Object e = m.get(null);
                    if (e instanceof List) {
                        ((List) e).add(n);
                    } else if (e != null) {
                        ArrayList a = new ArrayList();
                        a.add(e);
                        a.add(n);
                        m.put(null, a);
                    } else {
                        m.put(null, n);
                    }
                }
            }
        }

        return m;
    }
}
