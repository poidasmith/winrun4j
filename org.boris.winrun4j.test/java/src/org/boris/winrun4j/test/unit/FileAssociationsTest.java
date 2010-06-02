/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Peter Smith
 *******************************************************************************/
package org.boris.winrun4j.test.unit;

import java.io.File;

import junit.framework.TestCase;

import org.boris.winrun4j.test.framework.Launcher;
import org.boris.winrun4j.winapi.DDEML;

public class FileAssociationsTest extends TestCase
{
    public void testBasic() throws Exception {
        Launcher l = new Launcher()
                .main(FileAssociationsTest.class)
                .classpath(new File("."))
                .arg("-test")
                .arg("-hello")
                .dde(true, DDEML.class)
                .fileAss(".java", "Java File", "A java file");
        System.out.println(l.toString());
    }
}
