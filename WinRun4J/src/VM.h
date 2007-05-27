/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Peter Smith
 *******************************************************************************/

#ifndef VM_H
#define VM_H

#include <windows.h>
#include "INI.h"

// VM versions
#define VM_VERSION_MAX ":vm.version.max"
#define VM_VERSION ":vm.version"
#define VM_VERSION_MIN ":vm.version.min"

// VM keys
#define HEAP_SIZE_MAX_PERCENT ":vm.heapsize.max.percent"
#define HEAP_SIZE_MIN_PERCENT ":vm.heapsize.min.percent"


// VM args
#define VM_ARG_HEAPSIZE "-Xmx"


struct VM {
	static char* FindJavaVMLibrary(dictionary *ini);
	static void ExtractSpecificVMArgs(dictionary* ini, TCHAR** args, int& count);

private:
	static bool GetJavaVMLibrary(LPSTR filename, DWORD filesize, LPSTR version);
};

#endif // VM_UTILS_H