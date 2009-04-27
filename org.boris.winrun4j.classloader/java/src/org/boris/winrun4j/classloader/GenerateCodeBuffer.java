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

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

public class GenerateCodeBuffer
{
    public static void main(String[] args) throws Exception {
        PrintStream out = new PrintStream(new BufferedOutputStream(new FileOutputStream("C:\\eclipse\\workspace\\WinRun4J\\src\\java\\EmbeddedClasses.cpp")));
        out.println(HEADER);
        out.println();
        outputClass("EmbeddedClassLoader.class", "g_classLoaderCode", out);
        outputClass("ByteBufferInputStream.class", "g_byteBufferISCode", out);
        out.close();
    }
    
    public static void outputClass(String resource, String varName, PrintStream out) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        InputStream is = EmbeddedClassLoader.class
                .getResourceAsStream(resource);
        copy(is, bos, true);
        byte[] b = bos.toByteArray();
        out.println("static BYTE " + varName + "[] = {"); 
        out.print("    ");
        for (int i = 0; i < b.length; i++) {
            out.print("0x");
            String s = Integer.toHexString(b[i] & 0x000000ff);
            if (s.length() == 1)
                out.print("0");
            out.print(s);
            if (i < b.length - 1)
                out.print(", ");
            if ((i + 1) % 10 == 0) {
                out.println();
                out.print("    ");
            }
        }
        out.println();
        out.println("};");
        out.println();
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
    
    public static final String HEADER = "/*******************************************************************************\n" + 
    		"* This program and the accompanying materials\n" + 
    		"* are made available under the terms of the Common Public License v1.0\n" + 
    		"* which accompanies this distribution, and is available at \n" + 
    		"* http://www.eclipse.org/legal/cpl-v10.html\n" + 
    		"* \n" + 
    		"* Contributors:\n" + 
    		"*     Peter Smith\n" + 
    		"*******************************************************************************/";
}
