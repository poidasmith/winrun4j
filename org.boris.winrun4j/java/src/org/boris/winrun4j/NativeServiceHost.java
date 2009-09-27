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

import java.nio.ByteBuffer;

public class NativeServiceHost
{
    public static void main(String[] args) {
        if (args != null && args.length == 1 && "--WinRun4J:RegisterService".equals(args[0])) {
        } else if (args != null && args.length == 1 && "--WinRun4J:RegisterService".equals(args[0])) {
        } else if (args != null && args.length == 1 && "--WinRun4J:Console".equals(args[0])) {
        } else {
        }
    }

    public interface ServiceMain
    {
        void serviceMain(String[] args);
    }

    public static class ServiceMainCallback extends Callback
    {
        private ServiceMain callback;

        public ServiceMainCallback(ServiceMain callback) {
            this.callback = callback;
        }

        protected int callback(int stack) {
            int argc = NativeHelper.getInt(stack + 8);
            ByteBuffer bb = NativeHelper.getBuffer(stack + 12, argc * 4);
            String[] args = new String[argc];
            for (int i = 0; i < argc; i++) {
                long ptr = NativeHelper.getInt(bb.getInt());
                args[i] = NativeHelper.getString(ptr, 1024, true);
            }
            callback.serviceMain(args);
            return 0;
        }
    }
}
