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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

/**
 * Wraps another process as a service and (optionally) restarts the service on
 * failure (with exponential backoff).
 * 
 * @see http://en.wikipedia.org/wiki/Exponential_backoff
 */
public class RetryService implements Service
{
    private volatile boolean init = false;
    private volatile boolean shutdown = false;

    // Process params
    private String[] cmd;
    private String[] env;
    private File startDir;

    // Expononential backoff params
    private int maxRetries = 16;
    private int backoff = 1;
    private int backoffMultiplierSeconds = 1;
    private Random rand = new Random();

    public int serviceMain(String[] args) throws ServiceException {
        init();
        int result = 0;
        while (true) {
            result = exec();
            if (shutdown) {
                Log.warning("Shutting down due to request from service manager");
                break;
            }
            if (maxRetries <= 0) {
                Log.warning("Shutting down as max retry limit reached");
                break;
            }
            backoff();
        }
        return result;
    }

    private void backoff() {
        maxRetries--;
        int sleepSeconds = rand.nextInt(backoffMultiplierSeconds * (int) Math.pow(2, backoff++));
        Log.warning(String.format("Retry backoff: will sleep for %d seconds and then restart", sleepSeconds));
        try {
            Thread.sleep(sleepSeconds);
        } catch (InterruptedException e) {
        }
    }

    public int serviceRequest(int control) throws ServiceException {
        switch (control) {
        case SERVICE_CONTROL_STOP:
        case SERVICE_CONTROL_SHUTDOWN:
            shutdown = true;
            break;
        default:
            break;
        }
        return 0;
    }

    private synchronized void init() throws ServiceException {
        if (init)
            return;

        this.cmd = INI.getNumberedEntries("RetryService:cmd");

        // Handle child process environment variables
        String[] env = INI.getNumberedEntries("RetryService:env");
        boolean envAppend = Boolean.parseBoolean(
                INI.getProperty("RetryService:env.append", "false"));
        if (envAppend) {
            if (env != null && env.length > 0) {
                List<String> envs = new ArrayList<String>();
                Properties p = Environment.getEnvironmentVariables();
                for (String e : env) {
                    String[] kv = e.split("=");
                    if (kv.length == 2)
                        p.setProperty(kv[0], kv[1]);
                }
                for (Object o : p.keySet()) {
                    envs.add(o + "=" + p.get(o));
                }
                this.env = envs.toArray(new String[envs.size()]);
            }
        } else if (env != null && env.length > 0) {
            this.env = env;
        }

        String startDir = INI.getProperty("RetryService:start.dir");
        if (startDir != null)
            this.startDir = new File(startDir);

        // Exponential backoff options
        String maxR = INI.getProperty("RetryService:max.retries");
        if (maxR != null)
            this.maxRetries = Integer.parseInt(maxR);
        String backoffSecs = INI.getProperty("RetryService:backoff.seconds");
        if (backoffSecs != null)
            this.backoffMultiplierSeconds = Integer.parseInt(backoffSecs);

        this.init = true;
    }

    private int exec() throws ServiceException {
        try {
            Process p = Runtime.getRuntime().exec(cmd, env, startDir);
            return p.waitFor();
        } catch (IOException e) {
            throw new ServiceException(e);
        } catch (InterruptedException e) {
            throw new ServiceException(e);
        }
    }
}
