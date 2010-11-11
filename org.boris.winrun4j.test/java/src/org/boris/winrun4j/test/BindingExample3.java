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

import org.boris.winrun4j.PInvoke;
import org.boris.winrun4j.PInvoke.Callback;
import org.boris.winrun4j.PInvoke.DllImport;
import org.boris.winrun4j.PInvoke.Struct;

public class BindingExample3
{
    static {
        PInvoke.bind(BindingExample3.class, "user32");
    }

    public static void main(String[] args) throws Exception {
        SomeStruct ss = new SomeStruct();

        WindowEnumProc callback = new WindowEnumProc() {
            public boolean windowEnum(long hWnd, SomeStruct ss) throws Exception {
                System.out.println(hWnd);
                System.out.println(ss);
                if (ss.value > 100)
                    ss.thing = "ok there";
                ss.value++;
                return true;
            }
        };

        EnumWindows(callback, ss);

        System.out.println(ss.value);
        System.out.println(ss.thing);
    }

    @DllImport
    public static native boolean EnumWindows(WindowEnumProc enumFunc, SomeStruct ss);

    public interface WindowEnumProc extends Callback
    {
        boolean windowEnum(long hWnd, SomeStruct ss) throws Exception;
    }

    public static class SomeStruct implements Struct
    {
        public int value;
        public String thing;
    }
}
