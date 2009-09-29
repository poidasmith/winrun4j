/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Peter Smith
 *******************************************************************************/
package org.boris.winrun4j.winapi;

import java.nio.ByteBuffer;

import org.boris.winrun4j.Callback;
import org.boris.winrun4j.Native;
import org.boris.winrun4j.NativeHelper;

public class User32
{
    public static final long library = Native.loadLibrary("user32");

    public interface WindowProc
    {
        public int windowProc(long hWnd, int uMsg, long wParam, long lParam);
    }

    public static class WindowProcCallback extends Callback
    {
        private WindowProc callback;

        public WindowProcCallback(WindowProc callback) {
            this.callback = callback;
        }

        protected int callback(int stack) {
            ByteBuffer bb = NativeHelper.getBuffer(stack + 8, 16);
            return callback.windowProc(bb.getInt(), bb.getInt(), bb.getInt(), bb.getInt());
        }
    }

}
