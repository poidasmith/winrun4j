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

public class RSSItem
{
    public String title;
    public String description;
    public String pubDate;
    public String guid;
    public String link;
    public String hash;

    public static final String NL = "\n";

    public String toString() {
        return title + NL + description + NL + pubDate + NL + guid + NL + link;
    }
}
