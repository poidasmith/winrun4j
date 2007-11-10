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

// Dictionary key
#define VM_LOCATION_KEY "VM:Location"

// VM Registry keys
#define JRE_REG_PATH TEXT("Software\\JavaSoft\\Java Runtime Environment")
#define JRE_VERSION_KEY TEXT("CurrentVersion")
#define JRE_LIB_KEY TEXT("RuntimeLib")

// VM Version keys
#define MAX_VER

static HINSTANCE g_hInstance = 0;
static HMODULE g_jniLibrary = 0;
static JavaVM *jvm = 0;
static JNIEnv *env = 0;

typedef jint (JNICALL *JNI_createJavaVM)(JavaVM **pvm, JNIEnv **env, void *args);
typedef jint (JNICALL *JNI_getCreatedJavaVMs)(JavaVM **vmBuf, jsize bufLen, jsize *nVMs);

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
	Version versions[20];
	DWORD numVersions = 20;
	FindVersions(versions, &numVersions);

	// Now get the appropriate version
	Version* v = FindVersion(versions, numVersions, version, min, max);

	// If version is null we could not find anything
	if(!v) return NULL;

	// Now just grab the vm dll from the version
	if(RegOpenKeyEx(HKEY_LOCAL_MACHINE, JRE_REG_PATH, 0, KEY_READ, &hKey) != ERROR_SUCCESS)
		return NULL;

	if(RegOpenKeyEx(hKey, v->GetVersionStr(), 0, KEY_READ, &hVersionKey) != ERROR_SUCCESS)
		return NULL;

	DWORD length = MAX_PATH;
	if(RegQueryValueEx(hVersionKey, JRE_LIB_KEY, NULL, NULL, (LPBYTE)&filename, &length) != ERROR_SUCCESS)
		return NULL;

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
			(maxVer == NULL || maxVer->Compare(versions[i]) <= 0);

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

	if(RegOpenKeyEx(HKEY_LOCAL_MACHINE, JRE_REG_PATH, 0, KEY_READ, &hKey) != ERROR_SUCCESS)
		return;

	for(; *numVersions < size; (*numVersions)++) {
		length = MAX_PATH;
		if(RegEnumKeyEx(hKey, *numVersions, version, &length, NULL, NULL, NULL, NULL) != ERROR_SUCCESS)
			break;
		
		versions[*numVersions].Parse(version);
	}
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

void VM::InjectVMID()
{
	JNIEnv* env = GetJNIEnv();
	if(env == NULL) return;

	jclass cls = env->FindClass("java/lang/System");
	if(cls == NULL) {
		Log::SetLastError("Could not find java.lang.System class");
		return;
	}

	jmethodID meth = env->GetStaticMethodID(cls, "setProperty", "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;");
	if(meth == NULL) {
		return;
	}

	char moduleName[MAX_PATH];
	GetModuleFileName(g_hInstance, moduleName, MAX_PATH);

	env->CallStaticObjectMethod(cls, meth, env->NewStringUTF("XLL4J.ModuleName"), env->NewStringUTF(moduleName));
	if(env->ExceptionCheck()) {
		env->ExceptionClear();
	}
}

bool VM::IsMatchingVMID()
{
	JNIEnv* env = GetJNIEnv();
	if(env == NULL) return false;

	jclass cls = env->FindClass("java/lang/System");
	if(cls == NULL) {
		Log::SetLastError("Could not find java.lang.System class");
		return false;
	}

	jmethodID meth = env->GetStaticMethodID(cls, "getProperty", "(Ljava/lang/String;)Ljava/lang/String;");
	if(meth == NULL) {
		return false;
	}

	char moduleName[MAX_PATH];
	GetModuleFileName(g_hInstance, moduleName, MAX_PATH);

	jstring str = (jstring) env->CallStaticObjectMethod(cls, meth, env->NewStringUTF("XLL4J.ModuleName"));

	if(str == NULL)
		return false;

	jboolean iscopy = false;
	const char* s = env->GetStringUTFChars(str, &iscopy);

	int res = strcmp(moduleName, s);
	env->ReleaseStringUTFChars(str, s);

	return res == 0;
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

int VM::StartJavaVM(TCHAR* libPath, TCHAR* vmArgs[], HINSTANCE hInstance, bool attemptAttach)
{
	g_hInstance = hInstance;
	g_jniLibrary = LoadLibrary(libPath);
	if(g_jniLibrary == NULL) {
		Log::Error("ERROR: Could not load library: %s\n", libPath);
		return -1; 
	}

	JNI_createJavaVM createJavaVM = (JNI_createJavaVM)GetProcAddress(g_jniLibrary, "JNI_CreateJavaVM");
	if(createJavaVM == NULL) {
		Log::Error("ERROR: Could not find JNI_CreateJavaVM function\n");
		return -1; 
	}

	JNI_getCreatedJavaVMs getCreatedJavaVMs = (JNI_getCreatedJavaVMs)GetProcAddress(g_jniLibrary, "JNI_GetCreatedJavaVMs");
	if(getCreatedJavaVMs == NULL) {
		Log::Error("ERROR: Could not find JNI_GetCreatedJavaVMs function\n");
		return -1; 
	}

	// We inject a cookie into the VM - this is used to check if the created
	// VM is a match for the one we are attempting to create
	if(attemptAttach) {
		jsize num = 0;
		int res = getCreatedJavaVMs(&jvm, 1, &num);
		if(res == 0 && jvm != 0 && IsMatchingVMID())
			return 0;
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

	// Inject VM cookie
	if(attemptAttach && result == 0)
		InjectVMID();

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

	return result;
}

