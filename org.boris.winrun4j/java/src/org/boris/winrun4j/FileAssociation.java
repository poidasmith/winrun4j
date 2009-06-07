/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Peter Smith
 *******************************************************************************/
package org.boris.winrun4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileAssociation
{
    private String extension;
    private String name;
    private String contentType;
    private String perceivedType;
    private String description;
    private String icon;
    private Map verbs = new HashMap();
    private List openWithList = new ArrayList();

    public FileAssociation(String extension) {
        this.extension = extension;
    }

    public String getPerceivedType() {
        return perceivedType;
    }

    public void setPerceivedType(String perceivedType) {
        this.perceivedType = perceivedType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getExtension() {
        return extension;
    }

    public void put(FileVerb fv) {
        verbs.put(fv.getVerb(), fv);
    }

    public void addOpenWith(String ow) {
        openWithList.add(ow);
    }
}
