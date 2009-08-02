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

public interface MoveFileFlags
{
    int MOVEFILE_REPLACE_EXISTING = 0x00000001;
    int MOVEFILE_COPY_ALLOWED = 0x00000002;
    int MOVEFILE_DELAY_UNTIL_REBOOT = 0x00000004;
    int MOVEFILE_WRITE_THROUGH = 0x00000008;
    int MOVEFILE_CREATE_HARDLINK = 0x00000010;
    int MOVEFILE_FAIL_IF_NOT_TRACKABLE = 0x00000020;
}
