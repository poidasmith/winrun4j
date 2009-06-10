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
import java.io.FileReader;
import java.io.StringWriter;
import java.security.MessageDigest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.boris.commons.io.IO;
import org.boris.commons.xml.XML;
import org.boris.commons.xml.XMLObjectSerializer;
import org.boris.variant.codec.SourceCodec;
import org.w3c.dom.Document;

public class FeedLoader
{
    private static final SimpleDateFormat pubDateFormat1 = new SimpleDateFormat(
            "dd MMM yyyy HH:mm:ss Z");
    private static final SimpleDateFormat pubDateFormat2 = new SimpleDateFormat(
            "E, dd MMM yyyy HH:mm:ss Z");

    private static final boolean USE_CACHE = false;

    public static void main(String[] args) throws Exception {
        String url = "http://twitter.com/statuses/user_timeline/14634581.rss?page=4";
        Document doc = XML.parse(IO.openUrl(url));
        RSS rss = (RSS) XMLObjectSerializer.decode(doc, RSS.class);
        for (int i = 0; i < rss.channel.item.length; i++) {
            byte[] b = MessageDigest.getInstance("SHA1").digest(
                    rss.channel.item[i].toString().getBytes("UTF-8"));
            StringWriter sw = new StringWriter();
            SourceCodec.encodeBinary(b, sw);
            String filename = sw.toString();
            System.out.println(filename);
            String contents = XML.toString(XMLObjectSerializer.encode(rss.channel.item[i], "item"));
            System.out.println(contents);
        }
    }

    public static RSSItem[] loadItems(String url) throws Exception {
        RSS rss = null;
        if (USE_CACHE) {
            File f = URLCache.getCachedUrl(url);
            if (!f.exists()) {
                URLCache.cache(url);
            }

            rss = (RSS) XMLObjectSerializer.decode(XML.parse(new FileReader(f)), RSS.class);
        } else {
            rss = (RSS) XMLObjectSerializer.decode(XML.parse(IO.openUrl(url)), RSS.class);
        }

        return rss.channel.item;
    }

    public static Date parsePubDate(String pubDate) {
        try {
            return pubDateFormat1.parse(pubDate);
        } catch (ParseException e) {
        }
        try {
            return pubDateFormat2.parse(pubDate);
        } catch (ParseException e) {
        }
        return null;
    }

    public static String hash(String hash) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA1");
        byte[] b = md.digest(hash.toString().getBytes("UTF-8"));
        StringWriter sw = new StringWriter();
        SourceCodec.encodeBinary(b, sw);
        return sw.toString();
    }
}
