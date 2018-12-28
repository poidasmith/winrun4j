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
#include "JNI.h"
#include "../common/Log.h"
#include "../common/INI.h"
#include "../launcher/Service.h"

// VM Registry keys
#define JRE_REG_PATH             TEXT("Software\\JavaSoft\\Java Runtime Environment")
#define JRE_REG_PATH_NEW         TEXT("Software\\JavaSoft\\JRE")
#define JRE_REG_PATH_WOW6432     TEXT("Software\\Wow6432Node\\JavaSoft\\Java Runtime Environment")
#define IBM_JRE_REG_PATH         TEXT("Software\\IBM\\Java2 Runtime Environment")
#define IBM_JRE_REG_PATH_WOW6432 TEXT("Software\\Wow6432Node\\IBM\\Java2 Runtime Environment")
#define JRE_VERSION_KEY          TEXT("CurrentVersion")
#define JRE_LIB_KEY              TEXT("RuntimeLib")

// VM Version keys
#define MAX_VER

namespace 
{
	HINSTANCE g_hInstance = 0;
	HMODULE g_jniLibrary = 0;
	JavaVM *jvm = 0;
	JNIEnv *env = 0;
};

typedef jint (JNICALL *JNI_createJavaVM)(JavaVM **pvm, JNIEnv **env, void *args);

JavaVM* VM::GetJavaVM()
{
	return jvm;
}

JNIEnv* VM::GetJNIEnv(bool daemon)
{
	if(!jvm) return NULL;
	JNIEnv* env = 0;
	if(daemon) {
		jvm->AttachCurrentThreadAsDaemon((void**) &env, NULL);
	} else {
		jvm->AttachCurrentThread((void**) &env, NULL);
	}
	return env;
}

void VM::DetachCurrentThread()
{
	if(jvm) jvm->DetachCurrentThread();
}

char* VM::FindJavaVMLibrary(dictionary *ini)
{
	//	If "vm.sysfirst" is specified, then default to using the system's 
	//	already installed JVM rather than the one specified in "vm.location".

	int findSystemVmFirst = iniparser_getboolean(ini, VM_SYSFIRST, 0);
	char* vmDefaultLocation = GetJavaVMLibrary(
		iniparser_getstr(ini, VM_VERSION),
		iniparser_getstr(ini, VM_VERSION_MIN),
		iniparser_getstr(ini, VM_VERSION_MAX)
	);

	if (findSystemVmFirst && vmDefaultLocation != NULL)
		return vmDefaultLocation;

	char* vmLocations = iniparser_getstr(ini, VM_LOCATION);
	//Configuration example: vm.location=..\jre\bin\client\jvm.dll|..\..\jre\bin\client\jvm.dll
	//Tested: vm.location=|foo|| |..\jre\bin\client\jvm.dll|G:\jdk1.6.0_26_32b\jre\bin\client\jvm.dll
	Log::Info("Configured vm.location: %s", vmLocations);

	if(vmLocations != NULL)
	{
	
		// If the working.dir is not specified then we assume the vm location is relative to the
		// module dir
		char defWorkingDir[MAX_PATH];
		char* workingDir = iniparser_getstr(ini, WORKING_DIR);
		if(!workingDir) {
			GetCurrentDirectory(MAX_PATH, defWorkingDir);
			SetCurrentDirectory(iniparser_getstr(ini, INI_DIR));
		}

		char *delimiter = "|";
	   	char *vmLocation = strtok(vmLocations, delimiter);
	   
	   	while (vmLocation != NULL)
	   	{
			
			// Check if file is valid (ie. accessible or present)
			DWORD fileAttr = GetFileAttributes(vmLocation);
			if(fileAttr != INVALID_FILE_ATTRIBUTES)
			{
				char vmFull[MAX_PATH];
				GetFullPathName(vmLocation, MAX_PATH, vmFull, NULL);
	
				// Reset working dir if set
				if(!workingDir) {
					SetCurrentDirectory(defWorkingDir);
				}
	
				return strdup(vmFull);
				
			}//end of if(fileAttr != INVALID_FILE_ATTRIBUTES)
			
			Log::Info("vm.location item not found: %s", vmLocation); 		
    		vmLocation = strtok(NULL, delimiter);
			
		}//end of while (vmLocation != NULL)
		
		// Reset working dir if set
		if(!workingDir) {
			SetCurrentDirectory(defWorkingDir);
		}
		
		return NULL;
	}

	return vmDefaultLocation;
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
		for(UINT i = 0; i < numVersions; i++) {
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
	for(UINT i = 0; i < numVersions; i++) {
		bool higher = (min == NULL || minV.Compare(versions[i]) <= 0) &&
			(max == NULL || maxV.Compare(versions[i]) >= 0) &&
			(maxVer == NULL || maxVer->Compare(versions[i]) < 0);

		if(higher) maxVer = &versions[i];
	}

	return maxVer;
}

void VM::FindVersions(Version* versions, DWORD* numVersions)
{	
	HKEY hKey;
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

 	if(RegOpenKeyEx(HKEY_LOCAL_MACHINE, JRE_REG_PATH_NEW, 0, KEY_READ, &hKey) == ERROR_SUCCESS) {
		for(; *numVersions < size; (*numVersions)++) {
			length = MAX_PATH;
			if(RegEnumKeyEx(hKey, *numVersions, version, &length, NULL, NULL, NULL, NULL) != ERROR_SUCCESS)
				break;
			
			versions[*numVersions].Parse(version);
			versions[*numVersions].SetRegPath(JRE_REG_PATH_NEW);
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

void VM::ExtractSpecificVMArgs(dictionary* ini, TCHAR** args, UINT& count)
{
	// Extract memory size
	MEMORYSTATUS ms;
	GlobalMemoryStatus(&ms);
#ifdef X64
	int overallMax = 8000;
#else
	int overallMax = 1530;
#endif
	int availMax = (ms.dwTotalPhys/1024/1024) - 80;

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
			Log::Error("Error with heap size percent. Should be between 0 and 100.");
		} else {
			TCHAR ptmp[MAX_PATH];
			sprintf(ptmp, "%u", (unsigned int) percent);
			Log::Info("Percent is: %s", ptmp);
			Log::Info("Avail Phys: %dm", availMax);
			double size = (percent/100)*((double)availMax);
			if(size > overallMax) {
				size = overallMax;
			}
			TCHAR sizeArg[MAX_PATH];
			sprintf(sizeArg, "-Xmx%um", (UINT) size);
			args[count++] = strdup(sizeArg);
		}
	}

	// Look for min heap size percent
	TCHAR *MinHeapSizePercentStr = iniparser_getstr(ini, HEAP_SIZE_MIN_PERCENT);
	if(MinHeapSizePercentStr != NULL) {
		double percent = atof(MinHeapSizePercentStr);
		if(percent < 0 || percent > 100) {
			Log::Warning("Error with heap size percent. Should be between 0 and 100.");
		} else {
			Log::Info("Percent is: %f", percent);
			Log::Info("Avail Phys: %dm", availMax);
			int size = (int)((percent/100) * (double)(availMax));
			if(size > overallMax) {
				size = overallMax;
			}
			TCHAR sizeArg[MAX_PATH];
			sprintf(sizeArg, "-Xms%um", size);
			args[count++] = strdup(sizeArg);
		}
	}

	// Look for java.library.path.N entries
	TCHAR *libPaths[MAX_PATH];
	UINT libPathsCount = 0;
	INI::GetNumberedKeysFromIni(ini, JAVA_LIBRARY_PATH, libPaths, libPathsCount);
	if(libPathsCount > 0) {
	
		//toPeter: Sorry I am repeating the same code, i really hate it but I cannot afford a refactoring right now, duplicit to #83 row
		// If the working.dir is not specified then we assume the java.library.path location is relative to the module dir
		char defWorkingDir[MAX_PATH];
		char* workingDir = iniparser_getstr(ini, WORKING_DIR);
		if(!workingDir) {
			GetCurrentDirectory(MAX_PATH, defWorkingDir);
			SetCurrentDirectory(iniparser_getstr(ini, INI_DIR));
		}
		
		TCHAR libPathArg[4096];
		libPathArg[0] = 0;
		strcat(libPathArg, "-Djava.library.path=");
		for(int i =0 ; i < libPathsCount; i++) {
		
			//rel2abs
			char fullpath[MAX_PATH];
			GetFullPathName(libPaths[i], MAX_PATH, fullpath, NULL);
			strcat(libPathArg, fullpath);
			strcat(libPathArg, ";");
		}
		args[count++] = strdup(libPathArg);
		
		//toPeter: Sorry I am repeating the same code, i really hate it but I cannot afford a refactoring right now, duplicit to #119 row
		// Reset working dir if set
		if(!workingDir) {
			SetCurrentDirectory(defWorkingDir);
		}
	}
}

void VM::LoadRuntimeLibrary(TCHAR* libPath)
{
	int len = strlen(libPath);
	TCHAR binPath[MAX_PATH];
	strcpy(binPath, libPath);

	// strip off "client\jvm.dll" or "server\jvm.dll"
	int i, sc=0;
	for(i = len - 1; i >=0; i--) {
		if(binPath[i] == '\\') {
			binPath[i] = 0;
			sc++;
			if(sc>1)
				break;
		}
	}

	// Append library path and load - we have a couple of choices here depending on 
	// VM version - we don't treat failure here as an error as some VM versions
	// don't have this runtime installed
	strcat(binPath, "\\msvcr71.dll");
	if(!LoadLibrary(binPath)) {
		binPath[i] = 0;
		strcat(binPath, "\\msvcrt.dll");
		if(!LoadLibrary(binPath)) {
			binPath[i] = 0;
			strcat(binPath, "\\msvcr100.dll");
			if(!LoadLibrary(binPath)) {
				// Now resort to using SetDllDirectory - must use dynamic binding as 
				// this function is not available on all versions of windows
				typedef BOOL (WINAPI *LPFNSetDllDirectory)(LPCTSTR lpPathname);
				HINSTANCE hKernel32 = GetModuleHandle("kernel32");
				LPFNSetDllDirectory lpfnSetDllDirectory = (LPFNSetDllDirectory)GetProcAddress(hKernel32, "SetDllDirectoryA");
				if (lpfnSetDllDirectory != NULL) {
					binPath[i] = 0;
					lpfnSetDllDirectory(binPath);
				}
			}
		}
	}
}

int VM::StartJavaVM(TCHAR* libPath, TCHAR* vmArgs[], HINSTANCE hInstance)
{
	g_hInstance = hInstance;

	// We need to load an MS runtime library before the VM otherwise 
	// bad things happen so we assume the VM is located under a bin path and 
	// inside this bin dir there is the dll
	LoadRuntimeLibrary(libPath);

	// Load the JVM library 
	g_jniLibrary = LoadLibrary(libPath);
	if(g_jniLibrary == NULL) {
		Log::Error("ERROR: Could not load library: %s", libPath);
		return -1;
	}

	// Grab the create VM function address
	JNI_createJavaVM createJavaVM = (JNI_createJavaVM)GetProcAddress(g_jniLibrary, "JNI_CreateJavaVM");
	if(createJavaVM == NULL) {
		Log::Error("ERROR: Could not find JNI_CreateJavaVM function");
		return -1; 
	}

	// Count the vm args
	int numVMArgs = -1;
	while(vmArgs[++numVMArgs] != NULL) {}

	// Add the options for exit and abort hooks
	int numHooks = 2;
	
	JavaVMOption* options = (JavaVMOption*) malloc((numVMArgs + numHooks) * sizeof(JavaVMOption));
	for(int i = 0; i < numVMArgs; i++){
		options[i].optionString = _strdup(vmArgs[i]);
		options[i].extraInfo = 0;
	}

	// Setup hook pointers
	options[numVMArgs].optionString = "abort";
	options[numVMArgs].extraInfo = (void*) &VM::AbortHook;
	options[numVMArgs + 1].optionString = "exit";
	options[numVMArgs + 1].extraInfo = (void*) &VM::ExitHook;
		
	JavaVMInitArgs init_args;
	init_args.version = JNI_VERSION_1_2;
	init_args.options = options;
	init_args.nOptions = numVMArgs + numHooks;
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

	JNIEnv* env = VM::GetJNIEnv(true);
	JNI::PrintStackTrace(env);

	int result = jvm->DestroyJavaVM();
	if(g_jniLibrary) {
		FreeLibrary(g_jniLibrary);
		g_jniLibrary = 0;
	}

	env = 0;
	jvm = 0;

	return result;
}

void VM::AbortHook()
{
	Log::Error("Application aborted.");

	// If we are a service we need to update the service control manager
	Service::Shutdown(255);
}

void VM::ExitHook(int status)
{
	Log::Info("Application exited (%d).", status);

	// If we are a service we need to update the service control manager
	Service::Shutdown(status);
}

