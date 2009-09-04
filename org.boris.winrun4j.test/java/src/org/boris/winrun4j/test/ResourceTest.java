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
    static long java = Native.loadLibrary("jvm");
    static long getCreateJavaVMs = Native.getProcAddress(java,
            "JNI_GetCreatedJavaVMs");

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
        long env = getJniEnv();
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

    private static void printHex(long h) {
        System.out.println("0x" + Integer.toHexString((int) h));
    }

    public static long getJavaVm() {
        long vms = Native.malloc(4);
        NativeHelper.call(getCreateJavaVMs, vms, 1, 0);
        ByteBuffer bb = Native.fromPointer(vms, 4).order(
                ByteOrder.LITTLE_ENDIAN);
        int vm = bb.getInt();
        Native.free(vms);
        return vm;
    }

    public static long getJniEnv() {
        return getJniEnv(getJavaVm(), true);
    }

    public static long getJniEnv(long jvm, boolean attachDaemon) {
        long penv = Native.malloc(4);
        ByteBuffer jb = Native.fromPointer(jvm, 4).order(
                ByteOrder.LITTLE_ENDIAN);
        long pAttachProc = jb.getInt() + (attachDaemon ? 28 : 16); // AttachCurrentThread(AsDaemon)
        long attachProc = Native.fromPointer(pAttachProc, 4).order(
                ByteOrder.LITTLE_ENDIAN).getInt();
        NativeHelper.call(attachProc, jvm, penv, 0);
        ByteBuffer bb = Native.fromPointer(penv, 4);
        bb = bb.order(ByteOrder.LITTLE_ENDIAN);
        int env = bb.getInt();
        Native.free(penv);
        return env;
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

        long callback = makeCallback(env, clazz, mid, csim);
        printHex(callback);

        // while (true)
        NativeHelper.call(enumResourceTypes, 0, callback, 0);
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

    public static long makeCallback(long env, long clazz, long mid, long csim) {
        // printHex(env);
        // printHex(clazz);
        // printHex(mid);
        // printHex(csim);
        long ptr = Native.malloc(200);
        ByteBuffer bb = Native.fromPointer(ptr + 2, 198).order(
                ByteOrder.LITTLE_ENDIAN);
        bb.put((byte) 0x55);
        bb.put((byte) 0x8B);
        bb.put((byte) 0xEC);
        // call static void method
        bb.put((byte) 0x55); // push ebp
        bb.put((byte) 0x68); // push mid
        bb.putInt((int) mid);
        bb.put((byte) 0x68); // push clazz
        bb.putInt((int) clazz);
        bb.put((byte) 0x68); // 
        bb.putInt((int) env);
        bb.put((byte) 0xB8); // mov eax, csim
        bb.putInt((int) (csim));
        bb.put((byte) 0xFF); // call eax
        bb.put((byte) 0xD0);
        bb.put((byte) 0x8B);
        bb.put((byte) 0xE5);
        bb.put((byte) 0x5D);
        bb.put((byte) 0xC3);
        return ptr + 2;
    }

    public static int typeCallback(int stack) {
        System.out.println("Hello callback " + stack);
        ByteBuffer bb = Native.fromPointer(stack - 20, 20).order(
                ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < 5; i++) {
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
