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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class EmbeddedJarURLConnection extends URLConnection
{
    protected EmbeddedJarURLConnection(URL url) {
        super(url);
    }

    public void connect() throws IOException {
    }

    public InputStream getInputStream() throws IOException {
        return null;
    }
}
