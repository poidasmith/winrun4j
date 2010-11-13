/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Peter Smith
 *******************************************************************************/
package org.boris.winrun4j.test.framework;

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.boris.winrun4j.INI;
import org.boris.winrun4j.winapi.Environment;

public class PrintEnvironment
{
    public static void print() {
        StringBuilder sb = new StringBuilder();

        sb.append("\n\nINI Properties\n=============\n\n");
        Map<String, String> inip = INI.getProperties();
        for (String k : inip.keySet()) {
            sb.append(k);
            sb.append("=");
            sb.append(inip.get((String) k));
            sb.append("\n");
        }

        sb.append("System Properties\n=============\n\n");
        Properties p = System.getProperties();
        for (Iterator i = p.keySet().iterator(); i.hasNext();) {
            String k = (String) i.next();
            sb.append(k);
            sb.append("=");
            sb.append(p.getProperty((String) k));
            sb.append("\n");
        }

        sb.append("\n\nEnvironment Variables\n=============\n\n");
        p = Environment.getEnvironmentVariables();
        for (Iterator i = p.keySet().iterator(); i.hasNext();) {
            String k = (String) i.next();
            sb.append(k);
            sb.append("=");
            sb.append(p.getProperty((String) k));
            sb.append("\n");
        }

        System.out.println(sb);
    }
}
