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
    private static long methodId = Native.getMethodId(Callback.class, "callback", "(J)J", false);
    private static long nativeCallback = Native.getProcAddress(0, "Native_Callback");
    private static final boolean is64 = Native.is64();

    private long thisRef;
    private long callbackPtr;

    public Callback() {
        if (is64)
            build64();
        else
            build();
    }

    protected abstract long callback(long stack);

    public long getPointer() {
        return callbackPtr;
    }

    public void dispose() {
        if (thisRef != 0) {
            Native.deleteGlobalRef(thisRef);
            Native.free(callbackPtr);
            thisRef = 0;
            callbackPtr = 0;
        }
    }

    private void build() {
        thisRef = Native.newGlobalRef(this);
        callbackPtr = Native.malloc(27);
        ByteBuffer bb = NativeHelper.getBuffer(callbackPtr, 27);
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

    private void build64() {
        thisRef = Native.newGlobalRef(this);
        callbackPtr = Native.malloc(27);
        ByteBuffer bb = NativeHelper.getBuffer(callbackPtr, 27);
        bb.put((byte) 0x90); // nop
        bb.put((byte) 0x90); // nop
        bb.put((byte) 0x49); // mov r8,
    }

    public static final Callback NOP = new Callback() {
        public long callback(long stack) {
            return 0;
        }
    };
}
