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

public interface FileNotifyActionType
{
    int FILE_ACTION_ADDED = 1;
    int FILE_ACTION_REMOVED = 2;
    int FILE_ACTION_MODIFIED = 3;
    int FILE_ACTION_RENAMED_OLD_NAME = 4;
    int FILE_ACTION_RENAMED_NEW_NAME = 5;
}
