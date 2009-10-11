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

public abstract class Callback
{
    private static long methodId = Native.getMethodId(Callback.class,
            "callback", "(I)I", false);
    static long nativeCallback = Native.getProcAddress(0, "Native_Callback");

    private long thisRef;
    private long callbackPtr;

    public Callback() {
        makeNativeCallback();
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

    private void makeNativeCallback() {
        thisRef = Native.newGlobalRef(this);
        callbackPtr = Native.malloc(32);
        ByteBuffer bb = NativeHelper.getBuffer(callbackPtr, 32);
        bb.put((byte) 0x90); // nop
        bb.put((byte) 0x90); // nop
        bb.put((byte) 0x55); // push ebp
        bb.put((byte) 0x8B); // mov ebp, esp
        bb.put((byte) 0xEC);
        bb.put((byte) 0x55); // push ebp
        bb.put((byte) 0x68); // push mid
        bb.putInt((int) methodId);
        bb.put((byte) 0x68); // push clazz
        bb.putInt((int) thisRef);
        bb.put((byte) 0xB8); // mov eax, fnPtr
        bb.putInt((int) (nativeCallback));
        bb.put((byte) 0xFF); // call eax
        bb.put((byte) 0xD0);
        bb.put((byte) 0x8B); // mov esp, ebp
        bb.put((byte) 0xE5);
        bb.put((byte) 0x5D); // ret
        bb.put((byte) 0xC3);
    }
}
