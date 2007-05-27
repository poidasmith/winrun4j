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

// VM Version keys
#define MAX_VER

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
	// Extract memory size
	MEMORYSTATUS ms;
	GlobalMemoryStatus(&ms);
	int overallMax = 1530;
	int availMax = (int)(ms.dwTotalPhys/1024/1024) - 80;

	// Look for max heap size percent
	TCHAR *MaxHeapSizePercentStr = iniparser_getstr(ini, HEAP_SIZE_MAX_PERCENT);
	if(MaxHeapSizePercentStr != NULL) {
		double percent = atof(MaxHeapSizePercentStr);
		if(percent < 0 || percent > 100) {
			Log::Error("Error with heap size percent. Should be between 0 and 100.\n");
		} else {
			Log::Info("Percent is: %f\n", percent);
			Log::Info("Avail Phys: %dm\n", availMax);
			int size = (int)((percent/100) * (double)(availMax));
			if(size > overallMax) {
				size = overallMax;
			}
			TCHAR sizeArg[MAX_PATH];
			sprintf(sizeArg, "-Xmx%um", size);
			args[count++] = strdup(sizeArg);
		}
	}

	// Look for min heap size percent
	TCHAR *MinHeapSizePercentStr = iniparser_getstr(ini, HEAP_SIZE_MIN_PERCENT);
	if(MinHeapSizePercentStr != NULL) {
		double percent = atof(MinHeapSizePercentStr);
		if(percent < 0 || percent > 100) {
			Log::Error("Error with heap size percent. Should be between 0 and 100.\n");
		} else {
			Log::Info("Percent is: %f\n", percent);
			Log::Info("Avail Phys: %dm\n", availMax);
			int size = (int)((percent/100) * (double)(availMax));
			if(size > overallMax) {
				size = overallMax;
			}
			TCHAR sizeArg[MAX_PATH];
			sprintf(sizeArg, "-Xms%um", size);
			args[count++] = strdup(sizeArg);
		}
	}
}