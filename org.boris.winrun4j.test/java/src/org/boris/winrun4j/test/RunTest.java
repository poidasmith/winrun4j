package org.boris.winrun4j.test;

import org.boris.winrun4j.NativeHelper;

public class RunTest
{
    public static void main(String[] args) throws Exception {
        System.out.println("Hello world");
        long ptr1 = NativeHelper.toNativeString("Test Logging...", false);
        long ptr2 = NativeHelper.toNativeString("[info]", false);
        NativeHelper.call(0, "Log_LogIt", 0, ptr2, ptr1);
        NativeHelper.free(ptr1, ptr2);
    }
}
