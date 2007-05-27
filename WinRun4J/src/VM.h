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

// VM keys

// VM args

struct VM {
	static char* FindJavaVMLibrary(dictionary *ini);
	static void ExtractSpecificVMArgs(dictionary* ini, TCHAR** args, int& count);

private:
	static bool GetJavaVMLibrary(LPSTR filename, DWORD filesize, LPSTR version);
};

#endif // VM_UTILS_H