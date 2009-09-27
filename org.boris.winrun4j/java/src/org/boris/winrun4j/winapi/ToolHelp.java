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

import org.boris.winrun4j.NativeHelper;


public class ToolHelp
{
    public static final long library = Kernel32.library;

    public static long CreateToolHelp32Snapshot(int flags, long th32ProcessID) {
        return NativeHelper.call(library, "CreateToolHelp32Snapshot", flags, th32ProcessID);
    }

    public static HEAPENTRY32 Heap32First(long th32ProcessID, long th32HeapID) {
        return null;
    }

    public static class HEAPENTRY32
    {
        public int size;
        public long handle;
        public long address;
        public int blockSize;
        public int flags;
        public int lockCount;
        public int resvd;
        public long th32ProcessID;
        public long th32HeapID;
    }

    public static class HEAPLIST32
    {
        public int size;
        public long th32ProcessID;
        public long th32HeapID;
        public int flags;
    }

    public static class MODULEENTRY32
    {
        public int dize;
        public long th32ModuleID;
        public long th32ProcessID;
        public int glblcntUsage;
        public int proccntUsage;
        public long modBaseAddr;
        public int modBaseSize;
        public long module;
        public String szModule;
        public String szExePath;
    }

    public static class PROCESSENTRY32
    {
        public int size;
        public int cntUsage;
        public long th32ProcessID;
        public long th32DefaultHeapID;
        public long th32ModuleID;
        public int cndThreads;
        public long th32ParentProcessID;
        public long pcPriClassBase;
        public int flags;
        public String exExePath;
    }

    public static class THREADENTRY32
    {
    }
}
