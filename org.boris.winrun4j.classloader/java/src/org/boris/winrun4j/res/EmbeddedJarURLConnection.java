/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Peter Smith
 *******************************************************************************/
package org.boris.winrun4j.res;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;

import org.boris.winrun4j.classloader.ByteBufferInputStream;
import org.boris.winrun4j.classloader.EmbeddedClassLoader;

public class EmbeddedJarURLConnection extends URLConnection
{
    protected EmbeddedJarURLConnection(URL url) {
        super(url);
    }

    public void connect() throws IOException {
    }

    public InputStream getInputStream() throws IOException {
        ByteBuffer bb = EmbeddedClassLoader.getJar(null, url.getFile());
        if (bb == null)
            return null;
        else
            return new ByteBufferInputStream(bb);
    }
}
