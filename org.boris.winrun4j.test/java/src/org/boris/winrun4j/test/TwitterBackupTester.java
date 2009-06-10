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

import org.boris.commons.io.IO;

public class TwitterBackupTester
{
    public static void main(String[] args) throws Exception {
        IO.deleteDirectoryTree(new File("F:/TEMP/TwitterBackup.14634581"), false);
        TwitterBackup.main(new String[] { "-user", "14634581", "-outdir",
                "F:/TEMP/TwitterBackup.14634581", "-limit", "month" });
    }
}
