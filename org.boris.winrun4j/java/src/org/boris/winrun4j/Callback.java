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
import java.nio.ByteOrder;

public abstract class Callback
{
    static long java = Native.loadLibrary("jvm");
    static long getCreateJavaVMs = Native.getProcAddress(java,
            "JNI_GetCreatedJavaVMs");
    private static long methodId = Native.getMethodId(Callback.class,
            "callback", "(I)I", false);

    private long thisRef;
    private long callbackPtr;

    public Callback() {
        this(false);
    }

    public Callback(boolean attach) {
        thisRef = Native.newGlobalRef(this);
        long jvm = getJavaVm();
        long env = getJniEnv(jvm, false);
        long pf = Native.fromPointer(env, 4).order(ByteOrder.LITTLE_ENDIAN)
                .getInt();
        long csim = Native.fromPointer(pf + (196), 4).order(
                ByteOrder.LITTLE_ENDIAN).getInt();
        callbackPtr = attach ? makeAttachCallback(jvm, thisRef, methodId, csim)
                : makeCallback(env, thisRef, methodId, csim);
    }

    protected abstract int callback(int stack);

    public long getPointer() {
        return callbackPtr;
    }

    public void cleanup() {
        if (thisRef != 0) {
            Native.deleteGlobalRef(thisRef);
            Native.free(callbackPtr);
            thisRef = 0;
            callbackPtr = 0;
        }
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
        long jvmp = NativeHelper.getInt(jvm);
        long pAttachProc = jvmp + (attachDaemon ? 28 : 16); // AttachCurrentThread(AsDaemon)
        long attachProc = NativeHelper.getInt(pAttachProc);
        NativeHelper.call(attachProc, jvm, penv, 0);
        long env = NativeHelper.getInt(penv);
        Native.free(penv);
        return env;
    }

    public static long makeAttachCallback(long jvm, long clazzOrObj,
            long methodId, long fnPtr) {
        int jvmAttach = NativeHelper.getInt(jvm) + 28;
        long pEnv = Native.malloc(4);
        long pFn = Native.malloc(51);
        ByteBuffer bb = NativeHelper.getBuffer(pFn, 51);
        bb.put((byte) 0x90); // nop
        bb.put((byte) 0x90); // nop
        bb.put((byte) 0x55); // push ebp
        bb.put((byte) 0x8B); // mov ebp, esp
        bb.put((byte) 0xEC);
        bb.put((byte) 0x68); // push pEnv
        bb.putInt((int) pEnv);
        bb.put((byte) 0x68); // push null
        bb.putInt(0);
        bb.put((byte) 0xB8); // mov eax, jvmAttach
        bb.putInt(jvmAttach);
        bb.put((byte) 0xFF); // call eax
        bb.put((byte) 0xD0);
        bb.put((byte) 0x58); // pop eax
        bb.put((byte) 0x58); // pop eax
        bb.put((byte) 0x55); // push ebp
        bb.put((byte) 0x68); // push mid
        bb.putInt((int) methodId);
        bb.put((byte) 0x68); // push clazz
        bb.putInt((int) clazzOrObj);
        bb.put((byte) 0x68); // push env
        bb.putInt((int) pEnv);
        bb.put((byte) 0xB8); // mov eax, fnPtr
        bb.putInt((int) (fnPtr));
        bb.put((byte) 0xFF); // call eax
        bb.put((byte) 0xD0);
        bb.put((byte) 0x8B); // mov esp, ebp
        bb.put((byte) 0xE5);
        bb.put((byte) 0x5D); // ret
        bb.put((byte) 0xC3);
        return pFn;
    }

    public static long makeCallback(long env, long clazzOrObj, long methodId,
            long fnPtr) {
        long ptr = Native.malloc(32);
        ByteBuffer bb = NativeHelper.getBuffer(ptr, 32);
        bb.put((byte) 0x90); // nop
        bb.put((byte) 0x90); // nop
        bb.put((byte) 0x55); // push ebp
        bb.put((byte) 0x8B); // mov ebp, esp
        bb.put((byte) 0xEC);
        bb.put((byte) 0x55); // push ebp
        bb.put((byte) 0x68); // push mid
        bb.putInt((int) methodId);
        bb.put((byte) 0x68); // push clazz
        bb.putInt((int) clazzOrObj);
        bb.put((byte) 0x68); // push env
        bb.putInt((int) env);
        bb.put((byte) 0xB8); // mov eax, fnPtr
        bb.putInt((int) (fnPtr));
        bb.put((byte) 0xFF); // call eax
        bb.put((byte) 0xD0);
        bb.put((byte) 0x8B); // mov esp, ebp
        bb.put((byte) 0xE5);
        bb.put((byte) 0x5D); // ret
        bb.put((byte) 0xC3);
        return ptr;
    }
}
