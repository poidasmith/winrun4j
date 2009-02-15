/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Peter Smith
 *******************************************************************************/
package org.boris.winrun4j.classloader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class GenerateCodeBuffer
{
    public static void main(String[] args) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        InputStream is = EmbeddedClassLoader.class
                .getResourceAsStream("EmbeddedClassLoader.class");
        copy(is, bos, true);
        byte[] b = bos.toByteArray();
        System.out.print("    ");
        for (int i = 0; i < b.length; i++) {
            System.out.print("0x");
            String s = Integer.toHexString(b[i] & 0x000000ff);
            if (s.length() == 1)
                System.out.print("0");
            System.out.print(s);
            if (i < b.length - 1)
                System.out.print(", ");
            if ((i + 1) % 10 == 0) {
                System.out.println();
                System.out.print("    ");
            }
        }
    }

    public static void copy(InputStream r, OutputStream w, boolean close)
            throws IOException {
        byte[] buf = new byte[4096];
        int len = 0;
        while ((len = r.read(buf)) > 0) {
            w.write(buf, 0, len);
        }
        if (close) {
            r.close();
            w.close();
        }
    }
}
