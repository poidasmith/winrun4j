
#include "Classpath.h"
#include "Log.h"
#include <string>

using namespace std;

char* MakeClassPathEntry(TCHAR* dirend, TCHAR* path, TCHAR* filename)
{
	TCHAR file[MAX_PATH];
	file[0] = 0;
	if(dirend != NULL) {
		strcat_s(file, sizeof(file), path);
		strcat_s(file, sizeof(file), "\\");
	} 

	strcat_s(file, sizeof(file), filename);
	return _strdup(file);
}

void ExpandClassPathEntry(TCHAR** entries, int& index, TCHAR* entry)
{
	WIN32_FIND_DATA FindFileData;
	HANDLE hFind = INVALID_HANDLE_VALUE;
	TCHAR* path = _strdup(entry);

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
	// the current directory 
	TCHAR current[MAX_PATH];
	GetCurrentDirectory(MAX_PATH, current);
	SetCurrentDirectory(iniparser_getstr(ini, MODULE_DIR));

	TCHAR* entries[MAX_PATH];
	int i = 0, index = 0;
	TCHAR* entry = NULL;
	TCHAR entryName[MAX_PATH];
	while(true) {
		sprintf_s(entryName, sizeof(entryName), "%s.%d", CLASS_PATH, i+1);
		entry = iniparser_getstr(ini, entryName);
		if(entry != NULL) {
			ExpandClassPathEntry(entries, index, entry);
		}
		i++;
		if(i > 10 && entry == NULL) {
			break;
		}
	}

	string classpath = "";
	for(int i = 0; i < index; i++) {
		classpath += entries[i];
		classpath += ";";
		free(entries[i]);
	}

	TCHAR *built = _strdup(classpath.c_str());

	// Add classpath
	Log::Info("Generated Classpath: %s\n", built);
	TCHAR* cpArg = (TCHAR *) malloc(sizeof(TCHAR)*classpath.size() + sizeof(TCHAR)*strlen(CLASS_PATH_ARG) + 1);
	strcpy(cpArg, CLASS_PATH_ARG);
	strcat(cpArg, classpath.c_str());
	args[count++] = cpArg;

	// Now set the working directory back
	SetCurrentDirectory(current);
}
