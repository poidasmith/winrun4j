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

public interface FileNotifyFilterType
{
    int FILE_NOTIFY_CHANGE_FILE_NAME = 0x1;
    int FILE_NOTIFY_CHANGE_DIR_NAME = 0x2;
    int FILE_NOTIFY_CHANGE_ATTRIBUTES = 0x4;
    int FILE_NOTIFY_CHANGE_SIZE = 0x8;
    int FILE_NOTIFY_CHANGE_LAST_WRITE = 0x10;
    int FILE_NOTIFY_CHANGE_LAST_ACCESS = 0x20;
    int FILE_NOTIFY_CHANGE_CREATION = 0x40;
    int FILE_NOTIFY_CHANGE_SECURITY = 0x100;
    int ALL = 0x17f;
}
