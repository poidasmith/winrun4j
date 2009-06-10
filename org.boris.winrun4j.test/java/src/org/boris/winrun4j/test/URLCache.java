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

import java.io.File;
import java.io.FileWriter;

import org.boris.commons.io.IO;

public class URLCache
{
    private static File cacheDir = new File("F:/TEMP/urlcache");
    private static String user = "14634581";

    public static void main(String[] args) throws Exception {
        int page = 1;
        cacheDir.mkdirs();
        while (true) {
            String url = TwitterBackup.getUrl(user, page);
            System.out.println("Caching Page " + page + ": " + url);
            cache(url);
            page++;
        }
    }

    public static File getCachedUrl(String url) throws Exception {
        return new File(cacheDir, RSSLoader.hash(url));
    }

    public static void cache(String url) throws Exception {
        File cacheFile = new File(cacheDir, RSSLoader.hash(url));
        IO.copy(IO.openUrl(url), new FileWriter(cacheFile), true);
    }
}
