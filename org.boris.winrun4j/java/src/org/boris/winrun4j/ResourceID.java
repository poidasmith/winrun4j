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

public class ResourceID
{
    public final String id;
    public final int iid;

    public ResourceID(String id) {
        this.id = id;
        this.iid = -1;
    }

    public ResourceID(int iid) {
        this.id = null;
        this.iid = iid;
    }
}
