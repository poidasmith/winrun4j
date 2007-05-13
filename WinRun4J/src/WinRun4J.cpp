/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Peter Smith
 *******************************************************************************/

#include <windows.h>
#include <stdio.h>
#include <string.h>
#include "IniParser.h"
#include "JNIUtils.h"
#include "IconUtils.h"
#include "LogUtils.h"
#include <jni.h>
#include <string>

// Internal keys
#define MODULE_NAME "WinRun4J:ModuleName"
#define MODULE_INI "WinRun4J:ModuleIni"
#define MODULE_BASE "WinRun4J:ModuleBaseName"
#define MODULE_DIR "WinRun4J:ModuleDir"
#define GEN_CLASSPATH "WinRun4J:GeneratedClasspath"

// Ini keys
#define WORKING_DIR ":working.directory"
#define MAIN_CLASS ":main.class"
#define LOG_FILE ":log"
#define CLASS_PATH ":classpath"
#define VM_ARG ":vmarg"
#define PROG_ARG ":arg"
#define HEAP_SIZE_MAX_MIN ":vm.heapsize.min"
#define HEAP_SIZE_MAX_MAX ":vm.heapsize.max"

// VM args
#define CLASS_PATH_ARG "-Djava.class.path="

// VM Registry keys
#define JRE_REG_PATH TEXT("Software\\JavaSoft\\Java Runtime Environment")
#define JRE_VERSION_KEY TEXT("CurrentVersion")
#define JRE_LIB_KEY TEXT("RuntimeLib")

using namespace std;

void SetWorkingDirectory(dictionary* ini)
{
	char* dir = iniparser_getstr(ini, WORKING_DIR);
	if(dir != NULL) {
		// First set the current directory to the module directory
		SetCurrentDirectory(iniparser_getstr(ini, MODULE_DIR));

		// Now set working directory to specified (this allows for a relative working directory)
		SetCurrentDirectory(dir);
	} else {
		Log("Working directory not set\n");
	}
}

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
void BuildClassPath(dictionary* ini)
{
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
	iniparser_setstr(ini, GEN_CLASSPATH, built);
	free(built);
}

void GetNumberedKeysFromIni(dictionary* ini, TCHAR* keyName, TCHAR** entries, int& index)
{
	int i = 0;
	TCHAR entryName[MAX_PATH];
	while(true) {
		sprintf_s(entryName, sizeof(entryName), "%s.%d", keyName, i+1);
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

// Find an appropriate VM library (this needs improving)
bool GetJavaVMLibrary(LPSTR filename, DWORD filesize, LPSTR version)
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


/* The ini filename is in the same directory as the executable and called the same (except with ini at the end). */
dictionary* LoadIniFile()
{
	TCHAR filename[MAX_PATH], inifile[MAX_PATH], filedir[MAX_PATH];
	GetModuleFileName(NULL, filename, sizeof(filename));
	strcpy_s(inifile, sizeof(inifile), filename);
	strcpy_s(filedir, sizeof(filedir), filename);
	int len = strlen(inifile);
	// It is assumed the executable ends with "exe"
	inifile[len - 1] = 'i';
	inifile[len - 2] = 'n';
	inifile[len - 3] = 'i';
	dictionary* ini = iniparser_load(inifile);
	iniparser_setstr(ini, MODULE_NAME, filename);
	iniparser_setstr(ini, MODULE_INI, inifile);
	Log("Module Name: %s\n", filename);
	Log("Module INI: %s\n", inifile);

	// strip off filename to get module directory
	for(int i = len - 1; i >= 0; i--) {
		if(filedir[i] == '\\') {
			filedir[i] = 0;
			break;
		}
	}
	iniparser_setstr(ini, MODULE_DIR, filedir);
	Log("Module Dir: %s\n", filedir);

	// Fix main class - ie. replace x.y.z with x/y/z for use in jni
	char* mainClass = iniparser_getstr(ini, MAIN_CLASS);
	if(mainClass != NULL) {
		len = strlen(mainClass);
		for(int i = 0; i < len; i++) {
			if(mainClass[i] == '.') {
				mainClass[i] = '/';
			}
		}
	}
	if(mainClass == NULL) {
		Log("ERROR: no main class specified\n");
	} else {
		Log("Main Class: %s\n", mainClass);
	}

	return ini;
}

bool StrTrimInChars(LPSTR trimChars, char c)
{
	for(unsigned int i = 0; i < strlen(trimChars); i++) {
		if(c == trimChars[i]) {
			return true;
		}
	}
	return false;
}

void StrTrim(LPSTR str, LPSTR trimChars)
{
	unsigned int start = 0;
	unsigned int end = strlen(str) - 1;
	for(int i = 0; i < end; i++) {
		char c = str[i];
		if(!StrTrimInChars(trimChars, c)) {
			start = i;
			break;
		}
	}
	for(int i = end; i >= 0; i--) {
		char c = str[i];
		if(!StrTrimInChars(trimChars, c)) {
			end = i;
			break;
		}
	}
	if(start != 0 || end != strlen(str) - 1) {
		int k = 0;
		for(int i = start; i <= end; i++, k++) {
			str[k] = str[i];
		}
		str[k] = 0;
	}
}

void ParseCommandLine(LPSTR lpCmdLine, TCHAR** args, int& count)
{
	StrTrim(lpCmdLine, " ");
	int len = strlen(lpCmdLine);
	if(len == 0) {
		return;
	}

	int start = 0;
	bool quote = false;
	TCHAR arg[4096];
	for(int i = 0; i < len; i++) {
		char c = lpCmdLine[i];
		if(c == '\"') {
			quote = !quote;
		} else if(!quote && c == ' ') {
			int k = 0;
			for(int j = start; j < i; j++, k++) {
				arg[k] = lpCmdLine[j];
			}
			arg[k] = 0;
			args[count] = strdup(arg);
			StrTrim(args[count], " ");
			StrTrim(args[count], "\"");
			count++;
			start = i;
		}
	}

	// Add the last one
	int k = 0;
	for(int j = start; j < len; j++, k++) {
		arg[k] = lpCmdLine[j];
	}
	arg[k] = 0;
	args[count] = strdup(arg);
	StrTrim(args[count], " ");
	StrTrim(args[count], "\"");
	count++;
}

void ExtractSpecificVMArgs(dictionary* ini, TCHAR** args, int& count)
{
	// Add classpath
	TCHAR* classpath = iniparser_getstr(ini, GEN_CLASSPATH);
	Log("Generated Classpath: %s\n", classpath);

	// Make arg
	TCHAR* cpArg = (TCHAR *) malloc(sizeof(TCHAR)*strlen(classpath) + sizeof(TCHAR)*strlen(CLASS_PATH_ARG) + 1);
	strcpy(cpArg, CLASS_PATH_ARG);
	strcat(cpArg, classpath);

	// Add classpath
	args[count++] = cpArg;
}

int __stdcall WinMain(HINSTANCE hInstance, HINSTANCE hPrevInstance, LPSTR lpCmdLine, int nCmdShow) {
	// Initialise the logger using std streams
	LogInit(hInstance, NULL);

	// Check for seticon util request
	if(strcmp(lpCmdLine, "--seticon") == 0) {
		SetExeIcon();
		LogClose();
		return 0;
	}

	dictionary* ini = LoadIniFile();
	if(ini == NULL) {
		MessageBox(NULL, "Failed to find or load ini file.", "Startup Error", 0);
		LogClose();
		return 1;
	}

	// Now initialise the logger using std streams + specified log dir
	LogInit(hInstance, iniparser_getstr(ini, LOG_FILE));

	// Attempt to find an appropriate java VM
	TCHAR filename[MAX_PATH];
	bool success = GetJavaVMLibrary(filename, sizeof(filename), NULL);
	if(!success) {
		MessageBox(NULL, "Failed to find Java VM.", "Startup Error", 0);
		LogClose();
		return 1;
	}
	Log("VM: %s\n", filename);

	// Set the current working directory if specified
	SetWorkingDirectory(ini);

	// Build up the classpath
	BuildClassPath(ini);

	// Collect the VM args from the INI file
	TCHAR *vmargs[MAX_PATH];
	int vmargsCount = 0;
	GetNumberedKeysFromIni(ini, VM_ARG, vmargs, vmargsCount);

	// Extract the specific VM args
	ExtractSpecificVMArgs(ini, vmargs, vmargsCount);

	// Log the VM args
	for(int i = 0; i < vmargsCount; i++) {
		Log("vmarg.%d=%s\n", i, vmargs[i]);
	}

	// Collect the program arguments from the INI file
	TCHAR *progargs[MAX_PATH];
	int progargsCount = 0;
	GetNumberedKeysFromIni(ini, PROG_ARG, progargs, progargsCount);

	// Add the args from commandline
	ParseCommandLine(lpCmdLine, progargs, progargsCount);

	// Log the commandline args
	for(int i = 0; i < progargsCount; i++) {
		Log("arg.%d=%s\n", i, progargs[i]);
	}

	// Make sure there is a NULL at the end of the args
	vmargs[vmargsCount] = NULL;
	progargs[progargsCount] = NULL;

	// Fire up the VM
	startJavaVM(filename, vmargs, iniparser_getstr(ini, MAIN_CLASS), progargs);
	
	// Free vm args
	for(int i = 0; i < vmargsCount; i++) {
		free(vmargs[i]);
	}

	// Free program args
	for(int i = 0; i < progargsCount; i++) {
		free(progargs[i]);
	}

	// Free ini file
	iniparser_freedict(ini);

	// Close VM (This will block until all non-daemon java threads finish).
	int result = cleanupVM();

	// Close the log
	LogClose();

	return result;
}
