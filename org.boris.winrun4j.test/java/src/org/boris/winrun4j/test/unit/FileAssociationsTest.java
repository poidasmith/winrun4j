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

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.boris.winrun4j.Launcher;
import org.boris.winrun4j.test.framework.TestHelper;
import org.boris.winrun4j.winapi.DDEML;

public class FileAssociationsTest
{
    public void testBasic() throws Exception {
        Launcher l = TestHelper.launcher().main(FileAssociationsTest.class);
        l.classpath(new File(".")).arg("-test").arg("-hello");
        l.dde(true, DDEML.class);
        l.fileAss(".fte", "File Association Test", "Testing file assocations");
        l.fileAss(".ft2", "File Association Test 2", "Testing file assocations");
        String result = TestHelper.run(l, "--WinRun4J:RegisterFileAssociations");
        assertTrue(result.contains("[info] Registering .fte"));
        assertTrue(result.contains("[info] Registering .ft2"));
        result = TestHelper.run(l, "--WinRun4J:UnregisterFileAssociations");
        assertTrue(result.contains("[info] Unregistering .fte"));
        assertTrue(result.contains("[info] Unregistering .ft2"));
    }

    public static void main(String[] args) throws Exception {
        new FileAssociationsTest().testBasic();
    }
}
