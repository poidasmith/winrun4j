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


public class Resources
{
    static long kernel32 = Native.loadLibrary("kernel32");
    static long enumResourceTypes = Native.getProcAddress(kernel32, "EnumResourceTypesW");
    static long enumResourceNames = Native.getProcAddress(kernel32, "EnumResourceNamesW");
    static long enumResourceLanguages = Native.getProcAddress(kernel32, "EnumResourceLanguagesW");

    public static void main(String[] args) throws Exception {
        final Callback langs = new Callback() {
            protected int callback(int stack) {
                System.out.println("Lang callback");
                ByteBuffer bb = Native.fromPointer(stack + 8, 20).order(ByteOrder.LITTLE_ENDIAN);
                long module = bb.getInt();
                long type = bb.getInt();
                long name = bb.getInt();
                int lang = bb.getInt();
                long param = bb.getInt();
                printHex(module);
                printHex(type);
                printHex(name);
                printHex(lang);
                printHex(param);
                return 1;
            }
        };
        final Callback names = new Callback() {
            protected int callback(int stack) {
                System.out.println("Name callback");
                ByteBuffer bb = Native.fromPointer(stack + 8, 16).order(ByteOrder.LITTLE_ENDIAN);
                long module = bb.getInt();
                long type = bb.getInt();
                long name = bb.getInt();
                long param = bb.getInt();
                printHex(module);
                printHex(type);
                printHex(name);
                printHex(param);
                return (int) NativeHelper.call(enumResourceLanguages, 0, type, name, langs
                        .getPointer(), 0);
            }
        };
        final Callback types = new Callback() {
            protected int callback(int stack) {
                System.out.println("Type callback");
                ByteBuffer bb = Native.fromPointer(stack + 8, 12).order(ByteOrder.LITTLE_ENDIAN);
                long module = bb.getInt();
                long type = bb.getInt();
                long param = bb.getInt();
                printHex(module);
                printHex(type);
                printHex(param);
                return (int) NativeHelper.call(enumResourceNames, 0, type, names.getPointer(), 0);
            }
        };

        NativeHelper.call(enumResourceTypes, 0, types.getPointer(), 0);
        types.cleanup();
        names.cleanup();
        langs.cleanup();
    }

    public static void printHex(long h) {
        System.out.println("0x" + Integer.toHexString((int) h));
    }

    public static int EnumResourceTypes(long handle, Callback callback) {
        return (int) NativeHelper.call(enumResourceTypes, handle, callback.getPointer(), 0);
    }

    public static int EnumResourceNames(long handle, long type, Callback callback) {
        return (int) NativeHelper.call(enumResourceNames, handle, type, callback.getPointer(), 0);
    }

    public static int EnumResourceLanguages(long handle, long type, long name, Callback callback) {
        return (int) NativeHelper.call(enumResourceLanguages, handle, type, name, callback
                .getPointer(), 0);
    }
}
