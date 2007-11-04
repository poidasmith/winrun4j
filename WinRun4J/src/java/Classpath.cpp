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

void ExpandClassPathEntry(char* arg, char** result, int* current, int max)
{
	// Check for too many results
	if(*current >= max) {
		return;
	}

	// Convert to full path
	char fullpath[MAX_PATH];
	GetFullPathName(arg, MAX_PATH, fullpath, NULL);
	WIN32_FIND_DATA fd;

	// Check for special case - where we don't have a wildcard
	if(strchr(arg, '*') == NULL) {
		if(FindFirstFile(fullpath, &fd) != INVALID_HANDLE_VALUE) {
			result[*current] = strdup(fullpath);
			(*current)++;
			return;
		}
	}

	int len = strlen(fullpath);
	int prev = 0;
	bool hasStar = false;
	char search[MAX_PATH];
	for(int i = 0; i <= len; i++) {
		if(fullpath[i] == '/' || fullpath[i] == '\\' || fullpath[i] == 0) {
			if(hasStar) {
				// Temp set end of string to be current position
				fullpath[i] = 0;
				HANDLE h = FindFirstFile(fullpath, &fd);
				if(h == INVALID_HANDLE_VALUE) {
					return;
				} else {
					if(strcmp(fd.cFileName, ".") != 0 && strcmp(fd.cFileName, "..") != 0) {
						if(prev != 0) fullpath[prev] = 0;
						strcpy(search, fullpath);
						if(prev != 0) fullpath[prev] = '/';
						strcat(search, "/");
						strcat(search, fd.cFileName);
						if(i < len - 1)	{
							strcat(search, "/");
							strcat(search, &fullpath[i + 1]);
						}
						ExpandClassPathEntry(search, result, current, max);
					}
					while(FindNextFile(h, &fd) != 0) {
						if(strcmp(fd.cFileName, ".") != 0 && strcmp(fd.cFileName, "..") != 0) {
							if(prev != 0) fullpath[prev] = 0;
							strcpy(search, fullpath);
							if(prev != 0) fullpath[prev] = '/';
							strcat(search, "/");
							strcat(search, fd.cFileName);
							if(i < len - 1)	{
								strcat(search, "/");
								strcat(search, &fullpath[i + 1]);
							}
							ExpandClassPathEntry(search, result, current, max);
						}
					}
					return;
				}
			} 
			hasStar = false;
			prev = i;
		} else if(fullpath[i] == '*') {
			hasStar = true;
		}
	}
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
			ExpandClassPathEntry(entry, entries, &index, MAX_PATH);
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
