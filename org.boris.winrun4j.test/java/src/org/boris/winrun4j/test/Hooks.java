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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.boris.winrun4j.Callback;
import org.boris.winrun4j.Kernel32;
import org.boris.winrun4j.Native;
import org.boris.winrun4j.NativeHelper;

public class Hooks
{
    public static final long procCallMsgFilter = Native.getProcAddress(Kernel32.library, "CallMsgFilter");
    public static final long procCallNextHookEx = Native.getProcAddress(Kernel32.library, "CallNextHookEx");
    public static final long procSetWindowsHookEx = Native.getProcAddress(Kernel32.library, "SetWindowsHookEx");
    public static final long procUnhookWindowsHookEx = Native.getProcAddress(Kernel32.library, "UnhookWindowsHookEx");

    public static boolean CallMsgFilter(MSG msg, int code) {
        long ptr = 0;
        if (msg != null) {
            ptr = Native.malloc(18);
            ByteBuffer bb = Native.fromPointer(ptr, 18).order(ByteOrder.LITTLE_ENDIAN);
            bb.putInt(msg.hwnd);
            bb.putInt(msg.message);
            bb.putInt(msg.wParam);
            bb.putInt(msg.lParam);
            bb.putInt(msg.time);
            if (msg.pt != null) {
                bb.putShort((short) msg.pt.x);
                bb.putShort((short) msg.pt.y);
            } else {
                bb.putInt(0);
            }
        }
        boolean res = NativeHelper.call(procCallMsgFilter, ptr, code) != 0;
        if (ptr != 0) {
            Native.free(ptr);
        }
        return res;
    }

    public static long CallNextHookEx(long hook, int code, long wParam, long lParam) {
        return NativeHelper.call(procCallNextHookEx, hook, code, wParam, lParam);
    }

    public static long SetWindwsHookEx(int idHook, Callback lpfn, long hMod, int threadId) {
        return NativeHelper.call(procSetWindowsHookEx, idHook, lpfn.getPointer(), hMod, threadId);
    }

    public static boolean UnhookWindowsHookEx(long hook) {
        return NativeHelper.call(procUnhookWindowsHookEx, hook) != 0;
    }

    public class MouseProcCallback extends Callback
    {
        private MouseProc proc;

        public MouseProcCallback(MouseProc proc) {
            this.proc = proc;
        }

        protected int callback(int stack) {
            ByteBuffer bb = NativeHelper.getBuffer(stack + 8, 12);
            return 0;
        }
    }

    public interface MouseProc
    {
        int cbMouseProc(int code, int id, MOUSEHOOKSTRUCT struc);
    }

    public static class MOUSEHOOKSTRUCT
    {
        public POINT p;
        public int hwnd;
        public int hitTestCode;
        public int extraInfo;
    }

    public static class POINT
    {
        public int x;
        public int y;
    }

    public static class MSG
    {
        public int hwnd;
        public int message;
        public int wParam;
        public int lParam;
        public int time;
        public POINT pt;
    }

    public static void main(String[] args) throws Exception {
    }
}
