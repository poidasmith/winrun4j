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

static dictionary* g_ini = NULL;

void INI::GetNumberedKeysFromIni(dictionary* ini, TCHAR* keyName, TCHAR** entries, int& index)
{
	int i = 0;
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

/* The ini filename is in the same directory as the executable and called the same (except with ini at the end). */
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
	if(ini) {
		dictionary* ini2 = iniparser_load(inifile);
		if(ini2) {
			for(int i = 0; i < ini2->size; i++) {
				char* key = ini2->key[i];
				char* value = ini2->val[i];
				iniparser_setstr(ini, key, value);
			}		
		}
	} else {
		ini = iniparser_load(inifile);
		if(ini == NULL) {
			Log::Error("Could not load INI file: %s", inifile);
			return NULL;
		}
	}

	// Expand environment variables
	ExpandVariables(ini);

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

	return ini;
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

extern "C" __declspec(dllexport) dictionary* __cdecl INI_GetDictionary()
{
	return g_ini;
}

extern "C" __declspec(dllexport) const char* __cdecl INI_GetProperty(const char* key)
{
	return iniparser_getstr(g_ini, key);
}


