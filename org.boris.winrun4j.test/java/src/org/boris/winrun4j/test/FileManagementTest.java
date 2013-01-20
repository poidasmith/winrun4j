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

import org.boris.winrun4j.winapi.FileManagement;
import org.boris.winrun4j.winapi.FileManagement.WIN32_FIND_DATA;

public class FileManagementTest
{
    public static void main(String[] args) throws Exception {
        WIN32_FIND_DATA fd = new WIN32_FIND_DATA();
        long handle = FileManagement.FindFirstFile("f:\\*.*", fd);
        System.out.println(handle);
        Reflection.println(fd);
        while (FileManagement.FindNextFile(handle, fd)) {
            Reflection.println(fd);
        }
        FileManagement.FindClose(handle);
    }
}
