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

import org.boris.winrun4j.FileNotifyFilterType;
import org.boris.winrun4j.FileSystemListener;
import org.boris.winrun4j.FileSystemMonitor;

public class FileSystemMonitorTest
{
    public static void main(String[] args) throws Exception {
        File tempDir = new File(System.getProperty("java.io.tmpdir"));
        System.out.println(tempDir);
        DebugFileSystemListener listener = new DebugFileSystemListener();
        boolean res = FileSystemMonitor.addListener(tempDir, true,
                FileNotifyFilterType.FILE_NOTIFY_CHANGE_CREATION, listener);
        if (!res) {
            System.err.println("Could not register listener");
        }

        int i = 30;
        while (i-- > 0) {
            Thread.sleep(1000);
            File f = File.createTempFile("asdf", ".df");
            System.out.printf("Created: %s\n", f.getName());
            f.deleteOnExit();
        }

        FileSystemMonitor.removeListener(tempDir, listener);
    }

    public static class DebugFileSystemListener implements FileSystemListener
    {
        public void fileChange(int eventType, String filename) {
            System.out.printf("[%d]: %s\n", eventType, filename);
        }
    }
}
