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

import java.util.ArrayList;

import org.boris.winrun4j.PInvoke;
import org.boris.winrun4j.PInvoke.Callback;
import org.boris.winrun4j.PInvoke.DllImport;
import org.boris.winrun4j.PInvoke.Struct;

public class Resources
{
    static {
        PInvoke.bind(Resources.class, "kernel32.dll");
    }

    @DllImport
    public static native int EnumResourceTypes(long handle, ResourceTypesCallback callback);

    @DllImport
    public static native int EnumResourceNames(long handle, long type, ResourceNamesCallback callback);

    @DllImport
    public static native int EnumResourceLanguages(long handle, long type, long name, ResourceLanguagesCallback callback);

    @DllImport
    public static native long FindResource(long hModule, long name, long type);

    @DllImport
    public static native long FindResourceEx(long hModule, long name, long type, int lang);

    public static ResourceEntry[] findResources(long hModule) {
        final ArrayList res = new ArrayList();
        final ResourceLanguagesCallback langs = new ResourceLanguagesCallback() {
            public int languagesCallback(long hModule, long pType, long pName, int wLanguage, long lpParam) {
                ResourceEntry e = new ResourceEntry(ResourceId.fromPointer(pType, true), ResourceId.fromPointer(pName,
                        true), wLanguage);
                res.add(e);
                return 1;
            }
        };
        final ResourceNamesCallback names = new ResourceNamesCallback() {
            public int namesCallback(long hModule, long pType, long pName, long lpParam) {
                return EnumResourceLanguages(hModule, pType, pName, langs);
            }
        };
        final ResourceTypesCallback types = new ResourceTypesCallback() {
            public int typesCallback(long hModule, long pType, long lpParam) {
                return EnumResourceNames(hModule, pType, names);
            }
        };
        EnumResourceTypes(hModule, types);
        return (ResourceEntry[]) res.toArray(new ResourceEntry[res.size()]);
    }

    @DllImport
    public static native long LoadResource(long hModule, long hResInfo);

    @DllImport
    public static native long LockResource(long hResData);

    @DllImport
    public static native long BeginUpdateResource(String filename, boolean bDeleteExistingResources);

    @DllImport
    public static native boolean UpdateResource(long hUpdate, String lpType, String lpName, short wLanguage,
            byte[] data, int cbData);

    @DllImport
    public static native boolean UpdateResource(long hUpdate, String lpType, short lpName, short wLanguage,
            byte[] lpData,
            int cbData);

    @DllImport
    public static native boolean updateResource(long hUpdate, ResourceEntry entry, byte[] data, int cbData);

    @DllImport
    public static native boolean EndUpdateResource(long hUpdate, boolean fDiscard);

    @DllImport
    public static native long SizeOfResource(long hModule, long hResInfo);

    public interface ResourceTypesCallback extends Callback
    {
        int typesCallback(long hModule, long pType, long lpParam);
    }

    public interface ResourceNamesCallback extends Callback
    {
        int namesCallback(long hModule, long pType, long pName, long lpParam);
    }

    public interface ResourceLanguagesCallback extends Callback
    {
        int languagesCallback(long hModule, long pType, long pName, int wLanguage, long lpParam);
    }

    public static class ICONENTRY implements Struct
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

    public static class ICONHEADER implements Struct
    {
        public int reserved;
        public int type;
        public ICONENTRY[] entries;
    }

    public static class ICONIMAGE implements Struct
    {
        public BITMAPINFOHEADER header;
        public RGBQUAD colors;
        public int[] xors;
        public int[] ands;
    }

    public static class BITMAPINFOHEADER implements Struct
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

    public static class RGBQUAD implements Struct
    {
        public int rgbBlue;
        public int rgbGreen;
        public int rgbRed;
        public int rgbReserved;
    }

    public static class GRPICONENTRY implements Struct
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

    public static class GRPICONHEADER implements Struct
    {
        public int reserved;
        public int type;
        public GRPICONENTRY[] entries;
    }
}
