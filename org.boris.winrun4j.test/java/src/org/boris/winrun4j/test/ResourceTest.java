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
import org.boris.winrun4j.Native;
import org.boris.winrun4j.NativeHelper;

public class ResourceTest
{
    static long kernel32 = Native.loadLibrary("kernel32");
    static long enumResourceTypes = Native.getProcAddress(kernel32,
            "EnumResourceTypesW");
    static long enumResourceNames = Native.getProcAddress(kernel32,
            "EnumResourceNamesW");
    static long enumResourceLanguages = Native.getProcAddress(kernel32,
            "EnumResourceLanguagesW");
    static long debugBreak = Native.getProcAddress(kernel32, "DebugBreak");

    public static void main(String[] args) throws Exception {
        // long jvm = getJavaVm();
        // printHex(jvm);
        // long env = getJniEnv(jvm, true);
        // printHex(env);
        // long mid = Native.getMethodId(ResourceTest.class, "getJavaVm", "()J",
        // true);
        // printHex(mid);
        // long mid2 = Native.getMethodId(ResourceTest.class, "getJniEnv",
        // "(JZ)J", true);
        // printHex(mid2);
        long env = Callback.getJniEnv();
        // while (true)
        testGetResourceTypes(env);

        // test direct buffer memeory
        // long ptr = Native.malloc(4);
        // while (true) {
        // Native.fromPointer(ptr, 4).order(ByteOrder.LITTLE_ENDIAN);
        // long c = Native.newGlobalRef(ResourceTest.class);
        // Native.deleteGlobalRef(c);
        // Native.getMethodId(ResourceTest.class, "typeCallback", "(I)I", true);
        // }
    }

    public static void printHex(long h) {
        System.out.println("0x" + Integer.toHexString((int) h));
    }

    public static void testGetResourceTypes(long env) {
        long clazz = Native.newGlobalRef(ResourceTest.class);
        long mid = Native.getMethodId(ResourceTest.class, "typeCallback",
                "(I)I", true);
        long pf = Native.fromPointer(env, 4).order(ByteOrder.LITTLE_ENDIAN)
                .getInt();
        long csim = Native.fromPointer(pf + (516), 4).order(
                ByteOrder.LITTLE_ENDIAN).getInt();
        // for (int i = 0; i > -1; i++)
        // NativeHelper.call(csim, env, clazz, mid, 100);
        // System.out.println(res);
        // Native.deleteGlobalRef(clazz);
        // System.gc();

        long callback = Callback.makeAttachCallback(Callback.getJavaVm(),
                clazz, mid, csim);
        printHex(callback);

        // while (true)
        NativeHelper.call(enumResourceTypes, 0, callback, 1);
        // System.out.println(res);
        // System.out.println(Kernel32.getLastError());

        // System.gc();
        // push ebp - 55
        // mov ebp, esp - 8B EC
        // push ebp - 55
        // push mid - 68 b0 b1 b2 b3
        // push clazz - 68 b0 b1 b2 b3
        // push env - 68 b0 b1 b2 b3
        // call csim - 9A b0 b1 b2 b3
        // mov esp, ebp - 8B E5
        // pop ebp - 5D
        // ret - C2 04 00
    }

    public static int typeCallback(int stack) {
        System.out.println("Hello callback " + Integer.toHexString(stack));
        ByteBuffer bb = Native.fromPointer(stack + 8, 12).order(
                ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < 3; i++) {
            printHex(bb.getInt());
        }
        return 50;
    }

    // need to get
    // jni env
    // class handle
    // method handle
    // call JNI_CallStaticIntMethod(env, class, method, stack pointer)
    // return result

    // Callback asm
    // ============
    // push esp
    // push env [ptr]
    // call [ptr]
    // ret size

    // pop eax = 58
    // pop ecx = 59

    // Get JNI Env asm
    // ===============
    // sub esp, 8 (83 EC 08)
    // 
    // return 8 (C2 08 08)
}
