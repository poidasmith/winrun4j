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
    static long enumResourceTypes = Native.getProcAddress(kernel32, "EnumResourceTypes");
    static long enumResourceNames = Native.getProcAddress(kernel32, "EnumResourceNames");
    static long enumResourceLanguages = Native.getProcAddress(kernel32, "EnumResourceLanguages");
    static long java = Native.loadLibrary("jvm");
    static long getCreateJavaVMs = Native.getProcAddress(java, "JNI_GetCreatedJavaVMs");

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
        testGetResourceTypes();
    }

    private static void printHex(long h) {
        System.out.println("0x" + Integer.toHexString((int) h));
    }

    public static long getJavaVm() {
        long vms = Native.malloc(4);
        NativeHelper.call(getCreateJavaVMs, vms, 1, 0);
        ByteBuffer bb = Native.fromPointer(vms, 4).order(ByteOrder.LITTLE_ENDIAN);
        int vm = bb.getInt();
        Native.free(vms);
        return vm;
    }

    public static long getJniEnv() {
        return getJniEnv(getJavaVm(), true);
    }

    public static long getJniEnv(long jvm, boolean attachDaemon) {
        long penv = Native.malloc(4);
        ByteBuffer jb = Native.fromPointer(jvm, 4).order(ByteOrder.LITTLE_ENDIAN);
        long pAttachProc = jb.getInt() + (attachDaemon ? 28 : 16); // AttachCurrentThread(AsDaemon)
        long attachProc = Native.fromPointer(pAttachProc, 4).order(ByteOrder.LITTLE_ENDIAN)
                .getInt();
        NativeHelper.call(attachProc, jvm, penv, 0);
        ByteBuffer bb = Native.fromPointer(penv, 4);
        bb = bb.order(ByteOrder.LITTLE_ENDIAN);
        int env = bb.getInt();
        Native.free(penv);
        return env;
    }

    public static void testGetResourceTypes() {
        long env = getJniEnv();
        long clazz = Native.newGlobalRef(ResourceTest.class);
        long mid = Native.getMethodId(ResourceTest.class, "typeCallback", "(J)I", true);
        long pf = Native.fromPointer(env, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
        long csim = Native.fromPointer(pf + (130 * 4), 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
        long res = NativeHelper.call(csim, env, clazz, mid, 100);
        // push esp
        // push mid
        // push clazz
        // push env
        // call csim
        // sub esp, 16
        // ret 16
    }

    public static int typeCallback(long stack) {
        System.out.println("Hello callback " + stack);
        return 0;
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
