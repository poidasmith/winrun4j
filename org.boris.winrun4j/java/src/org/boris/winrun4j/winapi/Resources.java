/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Peter Smith
 *******************************************************************************/
package org.boris.winrun4j.winapi;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import org.boris.winrun4j.Callback;
import org.boris.winrun4j.NativeHelper;

public class Resources
{
    public static int enumResourceTypes(long handle, Callback callback) {
        return (int) NativeHelper.call(Kernel32.library, "EnumResourceTypesW", handle, callback.getPointer(), 0);
    }

    public static int enumResourceNames(long handle, long type, Callback callback) {
        return (int) NativeHelper.call(Kernel32.library, "EnumResourceNamesW", handle, type, callback.getPointer(), 0);
    }

    public static int enumResourceLanguages(long handle, long type, long name, Callback callback) {
        return (int) NativeHelper.call(Kernel32.library, "EnumResourceLanguagesW", handle, type, name, callback
                .getPointer(), 0);
    }

    public static long findResource(long hModule, long name, long type) {
        return NativeHelper.call(Kernel32.library, "FindResource", hModule, name, type);
    }

    public static long findResourceEx(long hModule, long name, long type, int lang) {
        return NativeHelper.call(Kernel32.library, "FindResourceEx", hModule, name, type, lang);
    }

    public static ResourceEntry[] findResources(long hModule) {
        final ArrayList res = new ArrayList();
        final Callback langs = new ResourceLanguagesCallback() {
            protected int languagesCallback(long hModule, long pType, long pName, int wLanguage, long lpParam) {
                ResourceEntry e = new ResourceEntry(ResourceId.fromPointer(pType, true), ResourceId.fromPointer(pName,
                        true), wLanguage);
                res.add(e);
                return 1;
            }
        };
        final Callback names = new ResourceNamesCallback() {
            protected int namesCallback(long hModule, long pType, long pName, long lpParam) {
                return enumResourceLanguages(hModule, pType, pName, langs);
            }
        };
        final Callback types = new ResourceTypesCallback() {
            protected int typesCallback(long hModule, long pType, long lpParam) {
                return enumResourceNames(hModule, pType, names);
            }
        };
        enumResourceTypes(hModule, types);
        types.dispose();
        names.dispose();
        langs.dispose();
        return (ResourceEntry[]) res.toArray(new ResourceEntry[res.size()]);
    }

    public static long loadResource(long hModule, long hResInfo) {
        return NativeHelper.call(Kernel32.library, "LoadResource", hModule, hResInfo);
    }

    public static long lockResource(long hResData) {
        return NativeHelper.call(Kernel32.library, "LockResource", hResData);
    }

    public static long beginUpdateResource(String filename, boolean bDeleteExistingResources) {
        long lpFilename = NativeHelper.toNativeString(filename, true);
        long res = NativeHelper.call(Kernel32.library, "BeginUpdateResourceW", lpFilename, bDeleteExistingResources ? 1
                : 0);
        NativeHelper.free(lpFilename);
        return res;
    }

    public static boolean updateResource(long hUpdate, long lpType, long lpName, int wLanguage, byte[] data) {
        long lpData = NativeHelper.toNative(data, 0, data.length);
        boolean res = updateResource(hUpdate, lpType, lpName, wLanguage, lpData, data.length);
        NativeHelper.free(lpData);
        return res;
    }

    public static boolean updateResource(long hUpdate, long lpType, long lpName, int wLanguage, long lpData, int cbData) {
        return NativeHelper.call(Kernel32.library, "UpdateResourceW", hUpdate, lpType, lpName, wLanguage, lpData,
                cbData) != 0;
    }

    public static boolean updateResource(long hUpdate, ResourceEntry entry, byte[] data) {
        long lpData = NativeHelper.toNative(data, 0, data.length);
        boolean res = updateResource(hUpdate, entry, lpData, data.length);
        NativeHelper.free(lpData);
        return res;
    }

    public static boolean updateResource(long hUpdate, ResourceEntry entry, long lpData, int cbData) {
        if (entry == null || !entry.isValid())
            return false;
        long lpType = entry.type.toNative();
        long lpName = entry.name.toNative();
        boolean res = NativeHelper.call(Kernel32.library, "UpdateResourceW", hUpdate, lpType, lpName, entry.language,
                lpData, cbData) != 0;
        if (!entry.type.isIntResource())
            NativeHelper.free(lpType);
        if (!entry.name.isIntResource())
            NativeHelper.free(lpName);
        return res;
    }

    public static boolean endUpdateResource(long hUpdate, boolean fDiscard) {
        return NativeHelper.call(Kernel32.library, "EndUpdateResourceW", hUpdate, fDiscard ? 1 : 0) != 0;
    }

    public static long sizeOfResource(long hModule, long hResInfo) {
        return NativeHelper.call(Kernel32.library, "SizeOfResource", hModule, hResInfo);
    }

    public static abstract class ResourceTypesCallback extends Callback
    {
        protected final int callback(int stack) {
            ByteBuffer bb = NativeHelper.getBuffer(stack, 12);
            return typesCallback(bb.getInt(), bb.getInt(), bb.getInt());
        }

        protected abstract int typesCallback(long hModule, long pType, long lpParam);
    }

    public static abstract class ResourceNamesCallback extends Callback
    {
        protected final int callback(int stack) {
            ByteBuffer bb = NativeHelper.getBuffer(stack, 12);
            return namesCallback(bb.getInt(), bb.getInt(), bb.getInt(), bb.getInt());
        }

        protected abstract int namesCallback(long hModule, long pType, long pName, long lpParam);
    }

    public static abstract class ResourceLanguagesCallback extends Callback
    {
        protected final int callback(int stack) {
            ByteBuffer bb = NativeHelper.getBuffer(stack, 12);
            return languagesCallback(bb.getInt(), bb.getInt(), bb.getInt(), bb.getInt(), bb.getInt());
        }

        protected abstract int languagesCallback(long hModule, long pType, long pName, int wLanguage, long lpParam);
    }

    public static class ICONENTRY
    {
        public int width;
        public int height;
        public int colorCount;
        public int reserved;
        public int planes;
        public int bitCount;
        public int wordsInRes;
        public int imageOffset;
    }

    public static class ICONHEADER
    {
        public int reserved;
        public int type;
        public ICONENTRY[] entries;
    }

    public static class ICONIMAGE
    {
        public BITMAPINFOHEADER header;
        public RGBQUAD colors;
        public int[] xors;
        public int[] ands;
    }

    public static class BITMAPINFOHEADER
    {
        public int biSize;
        public int biWidth;
        public int biHeight;
        public int biPlanes;
        public int biBitCount;
        public int biCompression;
        public int biSizeImage;
        public int biXPelsPerMeter;
        public int biYPelsPerMeter;
        public int biClrUsed;
        public int biClrImportant;
    }

    public static class RGBQUAD
    {
        public int rgbBlue;
        public int rgbGreen;
        public int rgbRed;
        public int rgbReserved;
    }

    public static class GRPICONENTRY
    {
        public int width;
        public int height;
        public int colourCount;
        public int reserved;
        public int planes;
        public int bitCount;
        public int bytesInRes;
        public int bytesInRes2;
        public int reserved2;
        public int id;
    }

    public static class GRPICONHEADER
    {
        public int reserved;
        public int type;
        public GRPICONENTRY[] entries;
    }
}
