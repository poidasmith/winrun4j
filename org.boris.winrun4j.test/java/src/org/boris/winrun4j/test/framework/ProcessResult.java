/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Peter Smith
 *******************************************************************************/
package org.boris.winrun4j.test.framework;

import java.io.ByteArrayOutputStream;

public class ProcessResult
{
    private ByteArrayOutputStream stdout;
    private ByteArrayOutputStream stderr;
    private ByteArrayOutputStream stdstr;
    private Process process;

    public ProcessResult(Process p) {
        this.process = p;
        this.stdout = new ByteArrayOutputStream();
        this.stderr = new ByteArrayOutputStream();
        this.stdstr = new ByteArrayOutputStream();

        IO.threadedCopy(p.getInputStream(), new TeeOutputStream(stdout, stdstr));
        IO.threadedCopy(p.getErrorStream(), new TeeOutputStream(stderr, stdstr));
    }

    public String getStdOut() {
        return stdout.toString();
    }

    public String getStdErr() {
        return stderr.toString();
    }

    public String getStdStr() {
        return stdstr.toString();
    }

    public ProcessResult waitFor() throws InterruptedException {
        process.waitFor();
        return this;
    }

    public boolean isActive() {
        try {
            process.exitValue();
            return false;
        } catch (IllegalThreadStateException e) {
            return true;
        }
    }

    public int exitValue() {
        return process.exitValue();
    }

    public void destroy() {
        process.destroy();
    }

    public String toString() {
        return getStdStr();
    }

    public void printStreams() {
        System.out.println(getStdStr());
    }
}
