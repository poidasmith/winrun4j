/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Peter Smith
 *******************************************************************************/

#include "INI.h"
#include "Log.h"
#include "../java/JNI.h"

#define ALLOW_INI_OVERRIDE    ":ini.override"
#define INI_FILE_LOCATION     ":ini.file.location"
#define INI_REGISTRY_LOCATION ":ini.registry.location"

static dictionary* g_ini = NULL;

void INI::GetNumberedKeysFromIni(dictionary* ini, TCHAR* keyName, TCHAR** entries, UINT& index)
{
	UINT i = 0;
	TCHAR entryName[MAX_PATH];
	while(true) {
		sprintf(entryName, "%s.%d", keyName, i+1);
		TCHAR* entry = iniparser_getstr(ini, entryName);
		if(entry != NULL) {
			entries[index++] = _strdup(entry);
		}
		i++;
		if(i > 10 && entry == NULL) {
			break;
		}
	}
	entries[index] = NULL;
}

/* 
 * The ini filename is in the same directory as the executable and 
 * called the same (except with ini at the end). 
 */
dictionary* INI::LoadIniFile(HINSTANCE hInstance)
{
	TCHAR filename[MAX_PATH], inifile[MAX_PATH];
	GetModuleFileName(hInstance, filename, sizeof(filename));
	strcpy(inifile, filename);
	int len = strlen(inifile);
	// It is assumed the executable ends with "exe"
	inifile[len - 1] = 'i';
	inifile[len - 2] = 'n';
	inifile[len - 3] = 'i';

	return LoadIniFile(hInstance, inifile);
}

dictionary* INI::LoadIniFile(HINSTANCE hInstance, LPSTR inifile)
{
	dictionary* ini = NULL;

	// Set PWD environment variable so that it can be used in the INI file
	TCHAR oldpwd[MAX_PATH], newpwd[MAX_PATH];
	GetEnvironmentVariable("PWD", oldpwd, MAX_PATH);
	GetCurrentDirectory(MAX_PATH, newpwd);
	SetEnvironmentVariable("PWD", newpwd);

	// First attempt to load INI from exe
	HRSRC hi = FindResource(hInstance, MAKEINTRESOURCE(1), RT_INI_FILE);
	if(hi) {
		HGLOBAL hg = LoadResource(hInstance, hi);
		PBYTE pb = (PBYTE) LockResource(hg);
		DWORD* pd = (DWORD*) pb;
		if(*pd == INI_RES_MAGIC) {
			ini = iniparser_load((char *) &pb[RES_MAGIC_SIZE], true);	
			if(!ini) {
				Log::Warning("Could not load embedded INI file");
			}
		}
	}

	// Check if we have already loaded an embedded INI file - if so 
	// then we only need to load and merge the INI file (if present)
	if(ini && iniparser_getboolean(ini, ALLOW_INI_OVERRIDE, 1)) {
		dictionary* ini2 = iniparser_load(inifile);
		if(ini2) {
			for(int i = 0; i < ini2->n; i++) {
				char* key = ini2->key[i];
				char* value = ini2->val[i];
				iniparser_setstr(ini, key, value);
			}		
			iniparser_freedict(ini2);
		}
	} else if(!ini) {
		ini = iniparser_load(inifile);
		if(ini == NULL) {
			Log::Error("Could not load INI file: %s", inifile);
			// Reset PWD environment variable
			SetEnvironmentVariable("PWD", oldpwd);
			return NULL;
		}
	}

	// Expand environment variables
	ExpandVariables(ini);

	// Now check if we have an external file to load
	char* iniFileLocation = iniparser_getstr(ini, INI_FILE_LOCATION);
	if(iniFileLocation) {
		Log::Info("Loading INI keys from file location: %s", iniFileLocation);
		dictionary* ini3 = iniparser_load(iniFileLocation);
		if(ini3) {
			for(int i = 0; i < ini3->n; i++) {
				char* key = ini3->key[i];
				char* value = ini3->val[i];
				iniparser_setstr(ini, key, value);
			}		
			iniparser_freedict(ini3);
		} else {
			Log::Warning("Could not load INI keys from file: %s", iniFileLocation);
		}
	}

	// Attempt to parse registry location to include keys if present
	ParseRegistryKeys(ini);

	iniparser_setstr(ini, MODULE_INI, inifile);

	// Add module name to ini
	TCHAR filename[MAX_PATH], filedir[MAX_PATH];
	GetModuleFileName(hInstance, filename, MAX_PATH);
	iniparser_setstr(ini, MODULE_NAME, filename);

	// strip off filename to get module directory
	GetFileDirectory(filename, filedir);
	iniparser_setstr(ini, MODULE_DIR, filedir);

	// stip off filename to get ini directory
	GetFileDirectory(inifile, filedir);
	iniparser_setstr(ini, INI_DIR, filedir);

	// Log init
	Log::Init(hInstance, iniparser_getstr(ini, LOG_FILE), iniparser_getstr(ini, LOG_LEVEL), ini);
	Log::Info("Module Name: %s", filename);
	Log::Info("Module INI: %s", inifile);
	Log::Info("Module Dir: %s", filedir);
	Log::Info("INI Dir: %s", filedir);

	// Store a reference to be used by JNI functions
	g_ini = ini;

	// Reset PWD environment variable
	SetEnvironmentVariable("PWD", oldpwd);

	return ini;
}

char* INI::GetString(dictionary* ini, const TCHAR* section, const TCHAR* key, TCHAR* defValue, bool defFromMainSection)
{
	char tmp[MAX_PATH];
	tmp[0] = 0;
	if(section)
		strcat(tmp, section);
	strcat(tmp, key);
	if(section && defFromMainSection) defValue = iniparser_getstring(ini, key, defValue);
	return iniparser_getstring(ini, tmp, defValue);
}

int INI::GetInteger(dictionary* ini, const TCHAR* section, const TCHAR* key, int defValue, bool defFromMainSection)
{
	char tmp[MAX_PATH];
	tmp[0] = 0;
	if(section)
		strcat(tmp, section);
	strcat(tmp, key);
	if(section && defFromMainSection) defValue = iniparser_getint(ini, key, defValue);
	return iniparser_getint(ini, tmp, defValue);
}

bool INI::GetBoolean(dictionary* ini, const TCHAR* section, const TCHAR* key, bool defValue, bool defFromMainSection)
{
	char tmp[MAX_PATH];
	tmp[0] = 0;
	if(section)
		strcat(tmp, section);
	strcat(tmp, key);
	if(section && defFromMainSection) defValue = iniparser_getboolean(ini, key, defValue);
	return iniparser_getboolean(ini, tmp, defValue);
}

void INI::ParseRegistryKeys(dictionary* ini)
{
	// Now check if we have a registry location to load from
	char* iniRegistryLocation = iniparser_getstr(ini, INI_REGISTRY_LOCATION);
	if(!iniRegistryLocation) {
		return;
	}

	Log::Info("Loading INI keys from registry: %s", iniRegistryLocation);

	// find root key
	int len = strlen(iniRegistryLocation);
	int slash = 0;
	while(slash < len && iniRegistryLocation[slash] != '\\')
		slash++;

	if(slash == len) {
		Log::Warning("Unable to parse registry location (%s) - keys not included", iniRegistryLocation);
		return;
	}

	HKEY hKey = 0;
	char* rootKey = strdup(iniRegistryLocation);
	rootKey[slash] = 0;
	if(strcmp(rootKey, "HKEY_LOCAL_MACHINE") == 0) {
		hKey = HKEY_LOCAL_MACHINE;
	} else if(strcmp(rootKey, "HKEY_CURRENT_USER") == 0) {
		hKey = HKEY_CURRENT_USER;
	} else if(strcmp(rootKey, "HKEY_CLASSES_ROOT") == 0) {
		hKey = HKEY_CLASSES_ROOT;
	} else {
		Log::Warning("Unrecognized registry root key: %s", rootKey);
		free(rootKey);
		return;
	}
	free(rootKey);

	HKEY subKey;
	if(RegOpenKeyEx(hKey, &iniRegistryLocation[slash+1], 0, KEY_READ, &subKey) != ERROR_SUCCESS) {
		Log::Warning("Unable to open registry location (%s)", iniRegistryLocation);
		return;
	}

	DWORD index = 0;
	char name[MAX_PATH + 2];
	char data[4096];
	DWORD type;
	DWORD nameLen = MAX_PATH;
	DWORD dataLen = 4096;
	name[0] = ':';
	while(RegEnumValue(subKey, index, &name[1], &nameLen, NULL, &type, (LPBYTE) data, &dataLen) == ERROR_SUCCESS) {
		bool hasNamespace = StrContains(&name[1], ':');
		char* key = hasNamespace ? &name[1] : name;
		if(type == REG_DWORD) {
			DWORD val = *((LPDWORD)data);
			sprintf(data, "%d", val);
			iniparser_setstr(ini, key, data);
		} else if(type == REG_SZ && dataLen > 1) {
			iniparser_setstr(ini, key, data);
		}
		nameLen = MAX_PATH;
		dataLen = 4096;
		index++;
	}
}


void INI::ExpandVariables(dictionary* ini)
{
	char tmp[4096];
	for(int i = 0; i < ini->size; i++) {
		char* key = ini->key[i];
		char* value = ini->val[i];
		int size = ExpandEnvironmentStrings(value, tmp, 4096);
		if(size == 0) {
			Log::Warning("Could not expand variable: %s", value);
		}
		iniparser_setstr(ini, key, tmp);
	}
}

#ifndef NO_JAVA
extern "C" __declspec(dllexport) dictionary* __cdecl INI_GetDictionary()
{
	return g_ini;
}

extern "C" __declspec(dllexport) const char* __cdecl INI_GetProperty(const char* key)
{
	return iniparser_getstr(g_ini, key);
}
#endif


