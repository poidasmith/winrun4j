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

import java.nio.ByteBuffer;

/**
 * Not yet implemented.
 */
public class Resources
{
    public static native ByteBuffer find(String module, ResourceId name, ResourceId type,
            int language);

    public static native void enumerate(String module, ResourceEnumCallback callback);
}
