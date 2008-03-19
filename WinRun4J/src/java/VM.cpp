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
#include "../common/Log.h"
#include "../common/INI.h"

// VM Registry keys
#define JRE_REG_PATH TEXT("Software\\JavaSoft\\Java Runtime Environment")
#define JRE_REG_PATH_WOW6432 TEXT("Software\\Wow6432Node\\JavaSoft\\Java Runtime Environment")
#define IBM_JRE_REG_PATH TEXT("Software\\IBM\\Java2 Runtime Environment")
#define IBM_JRE_REG_PATH_WOW6432 TEXT("Software\\Wow6432Node\\IBM\\Java2 Runtime Environment")
#define JRE_VERSION_KEY TEXT("CurrentVersion")
#define JRE_LIB_KEY TEXT("RuntimeLib")

// VM Version keys
#define MAX_VER

static HINSTANCE g_hInstance = 0;
static HMODULE g_jniLibrary = 0;
static JavaVM *jvm = 0;
static JNIEnv *env = 0;

typedef jint (JNICALL *JNI_createJavaVM)(JavaVM **pvm, JNIEnv **env, void *args);

JavaVM* VM::GetJavaVM()
{
	return jvm;
}

JNIEnv* VM::GetJNIEnv()
{
	if(!jvm) return NULL;
	JNIEnv* env = 0;
	jvm->AttachCurrentThread((void**) &env, NULL);
	return env;
}

void VM::DetachCurrentThread()
{
	if(jvm) jvm->DetachCurrentThread();
}

char* VM::FindJavaVMLibrary(dictionary *ini)
{
	char* vmLocation = iniparser_getstr(ini, VM_LOCATION);
	if(vmLocation != NULL)
	{
		// Check if file is valid
		HMODULE module = LoadLibrary(vmLocation);
		if(module != NULL)
			return vmLocation;
	}

	return GetJavaVMLibrary(iniparser_getstr(ini, VM_VERSION), iniparser_getstr(ini, VM_VERSION_MIN), iniparser_getstr(ini, VM_VERSION_MAX));
}

// Find an appropriate VM library (this needs improving)
char* VM::GetJavaVMLibrary(LPSTR version, LPSTR min, LPSTR max)
{
	TCHAR filename[MAX_PATH];
	HKEY hKey, hVersionKey;

	// Find the available versions
	DWORD numVersions = 255;
	Version versions[255];
	FindVersions(versions, &numVersions);

	// Now get the appropriate version
	Version* v = FindVersion(versions, numVersions, version, min, max);

	// If version is null we could not find anything
	if(!v) return NULL;

	// Now just grab the vm dll from the version
	if(RegOpenKeyEx(HKEY_LOCAL_MACHINE, v->GetRegPath(), 0, KEY_READ, &hKey) != ERROR_SUCCESS)
		return NULL;

	if(RegOpenKeyEx(hKey, v->GetVersionStr(), 0, KEY_READ, &hVersionKey) != ERROR_SUCCESS)
		return NULL;

	DWORD length = MAX_PATH;
	if(RegQueryValueEx(hVersionKey, JRE_LIB_KEY, NULL, NULL, (LPBYTE)&filename, &length) != ERROR_SUCCESS)
		return NULL;

// Add check for registry bug with sun amd64 
#ifdef X64
	HANDLE hFile = CreateFile(filename, GENERIC_READ, FILE_SHARE_READ, 0, OPEN_EXISTING, 0, 0);
	if(hFile == INVALID_HANDLE_VALUE) {
		// In this case we assume the registry says "client" dir but the dll is actually
		// only available under the "server" dir.
		int len = strlen(filename);
		if(len > 14 && strcmp(&filename[len - 14], "client\\jvm.dll") == 0) {
			char replace[] = "server";
			for(int i = 0; i < 6; i++) {
				filename[len - 14 + i] = replace[i];
			}
		}
	} else {
		CloseHandle(hFile);
	}
#endif

	RegCloseKey(hVersionKey);
	RegCloseKey(hKey);

	return strdup(filename);
}

Version* VM::FindVersion(Version* versions, DWORD numVersions, LPSTR version, LPSTR min, LPSTR max)
{
	// If an exact version is specified we need to search for it 
	if(version != NULL)
	{
		Version v;
		v.Parse(version);
		for(int i = 0; i < numVersions; i++) {
			if(v.Compare(versions[i]) == 0) {
				return &versions[i];
			}
		}

		return NULL;
	}

	// Now search for maximum version (that falls between min and max)
	Version minV, maxV;
	if(min != NULL) minV.Parse(min);
	if(max != NULL) maxV.Parse(max);

	Version* maxVer = NULL;
	for(int i = 0; i < numVersions; i++) {
		bool higher = (min == NULL || minV.Compare(versions[i]) <= 0) &&
			(max == NULL || maxV.Compare(versions[i]) >= 0) &&
			(maxVer == NULL || maxVer->Compare(versions[i]) < 0);

		if(higher) maxVer = &versions[i];
	}

	return maxVer;
}

void VM::FindVersions(Version* versions, DWORD* numVersions)
{	
	HKEY hKey, hVersionKey;
	DWORD length;
	TCHAR version[MAX_PATH];
	DWORD size = *numVersions;
	*numVersions = 0;

	if(RegOpenKeyEx(HKEY_LOCAL_MACHINE, JRE_REG_PATH, 0, KEY_READ, &hKey) == ERROR_SUCCESS) {
		for(; *numVersions < size; (*numVersions)++) {
			length = MAX_PATH;
			if(RegEnumKeyEx(hKey, *numVersions, version, &length, NULL, NULL, NULL, NULL) != ERROR_SUCCESS)
				break;
			
			versions[*numVersions].Parse(version);
			versions[*numVersions].SetRegPath(JRE_REG_PATH);
		}
	}

	if(RegOpenKeyEx(HKEY_LOCAL_MACHINE, IBM_JRE_REG_PATH, 0, KEY_READ, &hKey) == ERROR_SUCCESS) {
		DWORD offset = *numVersions;
		for(; *numVersions < size; (*numVersions)++) {
			length = MAX_PATH;
			if(RegEnumKeyEx(hKey, *numVersions - offset, version, &length, NULL, NULL, NULL, NULL) != ERROR_SUCCESS)
				break;
			
			versions[*numVersions].Parse(version);
			versions[*numVersions].SetRegPath(IBM_JRE_REG_PATH);
		}
	}

#ifndef X64
	// Find the 32 bit installs on a 64 bit machine
	if(RegOpenKeyEx(HKEY_LOCAL_MACHINE, JRE_REG_PATH_WOW6432, 0, KEY_READ, &hKey) == ERROR_SUCCESS) {
		for(; *numVersions < size; (*numVersions)++) {
			length = MAX_PATH;
			if(RegEnumKeyEx(hKey, *numVersions, version, &length, NULL, NULL, NULL, NULL) != ERROR_SUCCESS)
				break;
			
			versions[*numVersions].Parse(version);
			versions[*numVersions].SetRegPath(JRE_REG_PATH_WOW6432);
		}
	}

	if(RegOpenKeyEx(HKEY_LOCAL_MACHINE, IBM_JRE_REG_PATH_WOW6432, 0, KEY_READ, &hKey) == ERROR_SUCCESS) {
		DWORD offset = *numVersions;
		for(; *numVersions < size; (*numVersions)++) {
			length = MAX_PATH;
			if(RegEnumKeyEx(hKey, *numVersions - offset, version, &length, NULL, NULL, NULL, NULL) != ERROR_SUCCESS)
				break;
			
			versions[*numVersions].Parse(version);
			versions[*numVersions].SetRegPath(IBM_JRE_REG_PATH_WOW6432);
		}
	}
#endif
}

int Version::Compare(Version& other) 
{
	DWORD v1, v2;
	for(int index = 0; index < 10; index++) {
		v1 = VersionPart[index];
		v2 = other.VersionPart[index];
		if(v1 != v2) {
			return v1 - v2;
		}
	}
	return 0;
}

void Version::Parse(LPSTR version)
{
	strcpy(VersionStr, version);
	int index = 0;
	TCHAR v[MAX_PATH];
	strcpy(v, version);
	char* output = strtok(v, "._");
	while(output != NULL) {
		VersionPart[index++] = atoi(output);
		output = strtok(NULL, "._");
	}

	// Fill out versions to 10 places with zeroes
	for(; index < 10; index++) {
		VersionPart[index] = 0;
	}
	Parsed = true;
}

void VM::ExtractSpecificVMArgs(dictionary* ini, TCHAR** args, int& count)
{
	// Extract memory size
	MEMORYSTATUS ms;
	GlobalMemoryStatus(&ms);
	int overallMax = 1530;
	int availMax = (int)(ms.dwTotalPhys/1024/1024) - 80;

	// Look for preferred VM size
	TCHAR* PreferredHeapSizeStr = iniparser_getstr(ini, HEAP_SIZE_PREFERRED);
	if(PreferredHeapSizeStr != NULL) {
		int sizeMeg = atoi(PreferredHeapSizeStr);
		if(sizeMeg > availMax) {
			sizeMeg = availMax;
		}
		TCHAR sizeArg[MAX_PATH];
		sprintf(sizeArg, "-Xmx%um", sizeMeg);
		args[count++] = strdup(sizeArg);
	}

	// Look for max heap size percent
	TCHAR *MaxHeapSizePercentStr = iniparser_getstr(ini, HEAP_SIZE_MAX_PERCENT);
	if(MaxHeapSizePercentStr != NULL && PreferredHeapSizeStr == NULL) {
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
			Log::Warning("Error with heap size percent. Should be between 0 and 100.\n");
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

int VM::StartJavaVM(TCHAR* libPath, TCHAR* vmArgs[], HINSTANCE hInstance)
{
	g_hInstance = hInstance;

	/* Find binPath where the DLLs required to launch the JVM are stored. It is two directories up from the
	* jvm.dll in the standard Java directory layout. Specifically we require the specific copy of msvcr71.dll
	* that is in that path to be loaded prior to loading the JVM. This follows the recommendations and discussion
	* from http://www.duckware.com/tech/java6msvcr71.html
	*/
	TCHAR binPath[MAX_PATH];
	strcpy(binPath, libPath);
	for(int i = strlen(binPath) - 1; i >= 0; i--) {
		if(binPath[i] == '\\') {
			binPath[i] = 0;
			break;
		}
	}
	for(int i = strlen(binPath) - 1; i >= 0; i--) {
		if(binPath[i] == '\\') {
			binPath[i] = 0;
			break;
		}
	}

	/* Save and set current directory to the binPath and set the DLL search directory */
	int currentDirectoryLength = GetCurrentDirectory(0, NULL);
	TCHAR *saveCurrentDirectory = (TCHAR*)malloc(currentDirectoryLength * sizeof(TCHAR));
	if (GetCurrentDirectory(currentDirectoryLength, saveCurrentDirectory) == 0) {
		Log::Error("ERROR: Could not get current directory\n");
		return -1;
	}
	SetCurrentDirectory(binPath);

	/* Dynamic binding to SetDllDirectory() as it is only available in XP SP1+ */
	typedef BOOL (WINAPI *LPFNSetDllDirectory)(LPCTSTR lpPathname);
	HINSTANCE hKernel32 = GetModuleHandle("kernel32");
	LPFNSetDllDirectory lpfnSetDllDirectory = (LPFNSetDllDirectory)GetProcAddress(hKernel32, "SetDllDirectoryA");
	if (lpfnSetDllDirectory != NULL) {
		lpfnSetDllDirectory(binPath);
	}

	/* Load the JVM library */
	g_jniLibrary = LoadLibrary(libPath);
	if(g_jniLibrary == NULL) {
		Log::Error("ERROR: Could not load library: %s\n", libPath);
		return -1;
	}

	/* Restore current directory and DLL search directory */
	SetCurrentDirectory(saveCurrentDirectory);
	if (lpfnSetDllDirectory != NULL) {
		lpfnSetDllDirectory(NULL);
	}

	JNI_createJavaVM createJavaVM = (JNI_createJavaVM)GetProcAddress(g_jniLibrary, "JNI_CreateJavaVM");
	if(createJavaVM == NULL) {
		Log::Error("ERROR: Could not find JNI_CreateJavaVM function\n");
		return -1; 
	}

	// Count the vm args
	int numVMArgs = -1;
	while(vmArgs[++numVMArgs] != NULL) {}
	
	JavaVMOption* options = (JavaVMOption*) malloc((numVMArgs) * sizeof(JavaVMOption));
	for(int i = 0; i < numVMArgs; i++){
		options[i].optionString = _strdup(vmArgs[i]);
		options[i].extraInfo = 0;
	}
		
	JavaVMInitArgs init_args;
	init_args.version = JNI_VERSION_1_2;
	init_args.options = options;
	init_args.nOptions = numVMArgs;
	init_args.ignoreUnrecognized = JNI_TRUE;
	
	int result = createJavaVM(&jvm, &env, &init_args);

	for(int i = 0; i < numVMArgs; i++){
		free( options[i].optionString );
	}
	free(options);

	return result;
}

int VM::CleanupVM() 
{
	if (jvm == 0 || env == 0) {
		FreeLibrary(g_jniLibrary);
		return 1;
	}

	JNIEnv* env = VM::GetJNIEnv();

	if (env && env->ExceptionOccurred()) {
		env->ExceptionDescribe();
		env->ExceptionClear();
	}

	int result = jvm->DestroyJavaVM();
	if(g_jniLibrary) {
		FreeLibrary(g_jniLibrary);
		g_jniLibrary = 0;
	}

	env = 0;
	jvm = 0;

	return result;
}

