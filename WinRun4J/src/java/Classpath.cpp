/*******************************************************************************
* This program and the accompanying materials
* are made available under the terms of the Common Public License v1.0
* which accompanies this distribution, and is available at 
* http://www.eclipse.org/legal/cpl-v10.html
* 
* Contributors:
*     Peter Smith
*******************************************************************************/

#include "Classpath.h"
#include "../common/Log.h"
#include "../common/Dictionary.h"

char* MakeClassPathEntry(TCHAR* dirend, TCHAR* path, TCHAR* filename)
{
	TCHAR file[MAX_PATH];
	file[0] = 0;
	if(dirend != NULL) {
		strcat(file, path);
		strcat(file, "\\");
	} 

	strcat(file, filename);
	return strdup(file);
}

void ExpandClassPathEntry(TCHAR** entries, int& index, TCHAR* entry)
{
	WIN32_FIND_DATA FindFileData;
	HANDLE hFind = INVALID_HANDLE_VALUE;
	TCHAR* path = strdup(entry);

	TCHAR* dirend = strrchr(path, '\\');
	if(dirend == NULL) {
		dirend = strrchr(path, '/');
	}
	if(dirend != NULL) {
		path[dirend - path] = 0;
	}
	
	hFind = FindFirstFile(entry, &FindFileData);
	if(hFind != INVALID_HANDLE_VALUE) {
		entries[index++] = MakeClassPathEntry(dirend, path, FindFileData.cFileName);
		while(FindNextFile(hFind, &FindFileData) != 0) {
			entries[index++] = MakeClassPathEntry(dirend, path, FindFileData.cFileName);
		}
	}

	free(path);
}

// Build up the classpath entry from the ini file list
void Classpath::BuildClassPath(dictionary* ini, TCHAR** args, int& count)
{
	// It assumed that the classpath entries are relative to the module directory so we temporarily set
	// the current directory (unless a working directory has been set)
	TCHAR current[MAX_PATH];
	char* workingDirectory = iniparser_getstr(ini, WORKING_DIR);
	if(workingDirectory == NULL) {
		GetCurrentDirectory(MAX_PATH, current);
		SetCurrentDirectory(iniparser_getstr(ini, MODULE_DIR));
	}

	TCHAR* entries[MAX_PATH];
	int i = 0, index = 0;
	TCHAR* entry = NULL;
	TCHAR entryName[MAX_PATH];
	while(true) {
		sprintf(entryName, "%s.%d", CLASS_PATH, i+1);
		entry = iniparser_getstr(ini, entryName);
		if(entry != NULL) {
			ExpandClassPathEntry(entries, index, entry);
		}
		i++;
		if(i > 10 && entry == NULL) {
			break;
		}
	}

	char* classpath = NULL;
	for(int i = 0; i < index; i++) {
		char* temp = (char *) malloc(sizeof(TCHAR)*(strlen(entries[i]) + 1) + (classpath == NULL ? 1 : sizeof(TCHAR)*(strlen(classpath) + 2)));
		temp[0] = 0;
		if(classpath != NULL) {
			lstrcat(temp, classpath);
			lstrcat(temp, ";");
			free(classpath);
		}
		lstrcat(temp, entries[i]);
		classpath = temp;
		free(entries[i]);
	}

	TCHAR *built = strdup(classpath == NULL ? "" : classpath);

	// Add classpath
	Log::Info("Generated Classpath: %s\n", built);
	TCHAR* cpArg = (TCHAR *) malloc(sizeof(TCHAR)*(strlen(classpath) + 1) + sizeof(TCHAR)*(strlen(CLASS_PATH_ARG) + 1));
	lstrcpy(cpArg, CLASS_PATH_ARG);
	lstrcat(cpArg, built);
	args[count++] = cpArg;

	// Now set the working directory back
	if(workingDirectory == NULL) {
		SetCurrentDirectory(current);
	}
}
