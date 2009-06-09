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

import java.io.StringWriter;
import java.security.MessageDigest;

import org.boris.commons.io.IO;
import org.boris.commons.xml.XML;
import org.boris.commons.xml.XMLObjectSerializer;
import org.boris.variant.codec.SourceCodec;
import org.w3c.dom.Document;

public class RSSDump
{
    public static void main(String[] args) throws Exception {
        String url = "http://twitter.com/statuses/user_timeline/14634581.rss?page=4";
        Document doc = XML.parse(IO.openUrl(url));
        RSS rss = (RSS) XMLObjectSerializer.decode(doc, RSS.class);
        for (int i = 0; i < rss.item.length; i++) {
            byte[] b = MessageDigest.getInstance("SHA1").digest(
                    rss.item[i].toString().getBytes("UTF-8"));
            StringWriter sw = new StringWriter();
            SourceCodec.encodeBinary(b, sw);
            String filename = sw.toString();
            System.out.println(filename);
            String contents = XML.toString(XMLObjectSerializer.encode(rss.item[i], "item"));
            System.out.println(contents);
        }
    }

    public static RSSItem[] loadItems(String url) throws Exception {
        RSS rss = (RSS) XMLObjectSerializer.decode(IO.openUrl(url), RSS.class);

        return rss.item;
    }

    public static void hashItems(RSSItem[] items) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA1");
        for (int i = 0; i < items.length; i++) {
            byte[] b = md.digest(items[i].toString().getBytes("UTF-8"));
            StringWriter sw = new StringWriter();
            SourceCodec.encodeBinary(b, sw);
            items[i].hash = sw.toString();
        }
    }
}
