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
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;

import org.boris.commons.io.IO;
import org.boris.commons.xml.XML;
import org.boris.commons.xml.XMLObjectSerializer;

/**
 * Saves your twitter statuses to a directory.
 * 
 * <pre>
 *  -user 1243234234 -outdir F:/temp/twitter -limit [day|week|month|year|#hours]
 * </pre>
 */
public class TwitterBackup
{
    private static String user = "14634581";
    private static File outdir = new File("F:/TEMP/TwitterBackup1." + user);
    private static String limit;

    public static void main(String[] args) throws Exception {
        System.out.println("Twitter Backup v0.1.0 @winrun4j\n");
        if (!parseArgs(args)) {
            showUsage();
            System.exit(1);
        }

        if (!outdir.exists() && !outdir.mkdirs()) {
            System.err.println("Could not create backup dir: " + outdir);
            return;
        }

        Date cutoff = calculateCutoff();
        if (cutoff != null) {
            System.out.println("Limit: " + cutoff);
        }

        int page = 1;
        int count = 0;
        while (true) {
            String url = getUrl(user, page);
            System.out.println("Loading Page " + page + ": " + url);
            RSSItem[] items = null;
            try {
                items = RSSLoader.loadItems(url);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (items == null || items.length == 0) {
                if (count == 0) {
                    System.out.println("No new statuses found.");
                } else {
                    System.out.println("Loaded " + count + " statuses.");
                }
                break;
            }
            boolean found = false;
            for (int i = 0; i < items.length; i++) {
                // String h = RSSDump.hash(items[i].toString());
                String h = getGUID(items[i].guid);
                boolean pastCutoff = false;
                if (cutoff != null) {
                    Date pubDate = RSSLoader.parsePubDate(items[i].pubDate);
                    if (pubDate != null && pubDate.compareTo(cutoff) < 0) {
                        pastCutoff = true;
                        System.out.println("Reached time/date limit.");
                    }
                }
                File bf = new File(outdir, h + ".xml");
                if (pastCutoff || bf.exists()) {
                    if (count == 0) {
                        System.out.println("No new statuses found.");
                    } else {
                        System.out.println("Loaded " + count + " statuses.");
                    }
                    found = true;
                    break;
                } else {
                    String contents = XML.toString(XMLObjectSerializer.encode(items[i], "item"));
                    IO.copy(new StringReader(contents), new OutputStreamWriter(
                            new FileOutputStream(bf), "UTF-8"), true);
                    count++;
                }
            }
            if (found)
                break;
            page++;
        }
    }

    private static Date calculateCutoff() {
        if (limit != null) {
            GregorianCalendar gc = new GregorianCalendar();
            gc.setTime(new Date());

            if ("day".equals(limit)) {
                gc.set(Calendar.DAY_OF_MONTH, gc.get(Calendar.DAY_OF_MONTH) - 1);
            } else if ("week".equals(limit)) {
                gc.set(Calendar.WEEK_OF_MONTH, gc.get(Calendar.WEEK_OF_MONTH) - 1);
            } else if ("month".equals(limit)) {
                gc.set(Calendar.MONTH, gc.get(Calendar.MONTH) - 1);
            } else if ("year".equals(limit)) {
                gc.set(Calendar.YEAR, gc.get(Calendar.YEAR) - 1);
            } else {
                try {
                    int hours = Integer.parseInt(limit);
                    gc.set(Calendar.HOUR_OF_DAY, gc.get(Calendar.HOUR_OF_DAY) - hours);
                } catch (Exception e) {
                    System.err.println("Invalid limit specified: " + limit);
                    System.exit(1);
                }
            }

            return gc.getTime();
        }
        return null;
    }

    private static void showUsage() {
        System.out.println("A backup utility for Twitter statuses.");
        System.out.println();
        System.out
                .println("TwitterBackup -user <user> -outdir <backup_dir> [-limit #hours|day|week|month|year]");
        System.out.println();
    }

    private static boolean parseArgs(String[] args) {
        Map m = Args.valueOf(args, true);
        user = getString(m, "USER");
        String of = getString(m, "OUTDIR");
        if (of != null)
            outdir = new File(of);
        limit = getString(m, "LIMIT");
        if (limit != null)
            limit = limit.toLowerCase();
        return user != null && outdir != null;
    }

    private static String getString(Map m, Object k) {
        return (String) get(m, k, String.class);
    }

    private static Object get(Map m, Object k, Class c) {
        Object v = m.get(k);
        if (v == null)
            return null;
        if (c.isAssignableFrom(v.getClass()))
            return v;
        else
            return null;
    }

    private static String getGUID(String url) {
        return url.substring(url.lastIndexOf('/'));
    }

    public static String getUrl(String user, int page) {
        return "http://twitter.com/statuses/user_timeline/" + user + ".rss?page=" + page;
    }

}
