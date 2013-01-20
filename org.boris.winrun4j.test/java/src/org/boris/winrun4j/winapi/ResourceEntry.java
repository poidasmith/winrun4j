/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Peter Smith
 *******************************************************************************/
package org.boris.winrun4j.winapi;

public class ResourceEntry
{
    public final ResourceId type;
    public final ResourceId name;
    public final int language;

    public ResourceEntry(ResourceId type, ResourceId name, int language) {
        this.type = type;
        this.name = name;
        this.language = language;
    }

    public boolean isValid() {
        return type != null && name != null;
    }
}
