/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Peter Smith
 *******************************************************************************/

#include "VM.h"
#include "Log.h"
#include "INI.h"

// Dictionary key
#define VM_LOCATION_KEY "VM:Location"

// VM Registry keys
#define JRE_REG_PATH TEXT("Software\\JavaSoft\\Java Runtime Environment")
#define JRE_VERSION_KEY TEXT("CurrentVersion")
#define JRE_LIB_KEY TEXT("RuntimeLib")

char* VM::FindJavaVMLibrary(dictionary *ini)
{
	TCHAR filename[MAX_PATH];
	bool found = GetJavaVMLibrary(filename, sizeof(filename), NULL);
	if(found) {
		return _strdup(filename);
	} else {
		return NULL;
	} 
}

// Find an appropriate VM library (this needs improving)
bool VM::GetJavaVMLibrary(LPSTR filename, DWORD filesize, LPSTR version)
{
	HKEY hKey, hVersionKey;
	if(RegOpenKeyEx(HKEY_LOCAL_MACHINE, JRE_REG_PATH, 0, KEY_READ, &hKey) != ERROR_SUCCESS)
		return false;

	DWORD length = MAX_PATH;
	TCHAR keyName[MAX_PATH];
	if(version == NULL)
	{
		// Find current VM version
		if(RegQueryValueEx(hKey, JRE_VERSION_KEY, NULL, NULL, (LPBYTE)&keyName, &length) != ERROR_SUCCESS)
			return false;
		version = keyName;
	}

	if(RegOpenKeyEx(hKey, version, 0, KEY_READ, &hVersionKey) != ERROR_SUCCESS)
		return false;

	length = MAX_PATH;
	if(RegQueryValueEx(hVersionKey, JRE_LIB_KEY, NULL, NULL, (LPBYTE)&keyName, &length) != ERROR_SUCCESS)
		return false;

	strcpy_s(filename, filesize, keyName);

	RegCloseKey(hVersionKey);
	RegCloseKey(hKey);

	return true;
}

void VM::ExtractSpecificVMArgs(dictionary* ini, TCHAR** args, int& count)
{
	/* FIXME
	// Look for heap size percent
	TCHAR *heapSizePercentStr = iniparser_getstr(ini, HEAP_SIZE_PERCENT);
	if(heapSizePercentStr != NULL) {
		double percent = atof(heapSizePercentStr);
		if(percent < 0 || percent > 100) {
			Log::Info("Error with heap size percent. Should be between 0 and 100.\n");
		}

		Log::Info("Percent is: %f\n", percent);
		MEMORYSTATUS ms;
		GlobalMemoryStatus(&ms);
		Log::Info("Avail Phys: %d\n", ms.dwTotalPhys);
		Log::Info("Avail Virt: %d\n", ms.dwAvailVirtual);
		double size = ((percent/100) * (double)ms.dwTotalPhys);
		TCHAR sizeArg[MAX_PATH];
		sprintf(sizeArg, "-Xmx%u", size);
		//args[count++] = strdup(sizeArg);
	}
	*/
}