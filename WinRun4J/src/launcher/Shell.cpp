/*******************************************************************************
* This program and the accompanying materials
* are made available under the terms of the Common Public License v1.0
* which accompanies this distribution, and is available at 
* http://www.eclipse.org/legal/cpl-v10.html
* 
* Contributors:
*     Peter Smith
*******************************************************************************/

#include "Shell.h"
#include "../common/Log.h"
#include "../java/JNI.h"
#include "../java/VM.h"
#include "DDE.h"
#include <tlhelp32.h>
#include <psapi.h>
#include <shfolder.h>

#define SINGLE_INSTANCE_OPTION ":single.instance"

BOOL CALLBACK EnumWindowsProcSingleInstance(HWND hWnd, LPARAM lParam)
{
	DWORD procId = 0;
	GetWindowThreadProcessId(hWnd, &procId);
	if((DWORD)lParam == procId) {
		WINDOWINFO wi;
		wi.cbSize = sizeof(WINDOWINFO);
		GetWindowInfo(hWnd, &wi);
		if((wi.dwStyle & WS_VISIBLE) != 0) {
			SetForegroundWindow(hWnd);
			Log::Warning("Single Instance Shutdown");
			return FALSE;
		}
	}

	return TRUE;
}

int Shell::CheckSingleInstance(dictionary* ini)
{
	char* singleInstance = iniparser_getstr(ini, SINGLE_INSTANCE_OPTION);
	if(singleInstance == NULL) {
		return 0;
	}

	// Check for single instance mode
	bool processOnly = true;
	bool dde = false;

	if(strcmp(singleInstance, "window") == 0)
		processOnly = false;
	else if (strcmp(singleInstance, "dde") == 0) {
		processOnly = false;
		dde = true;
	} else if(strcmp(singleInstance, "process") != 0) {		
		Log::Warning("Invalid single instance mode: %s", singleInstance);
		return 0;
	}

	char thisModule[MAX_PATH];
	DWORD thisProcessId = GetCurrentProcessId();
	GetModuleFileName(0, thisModule, MAX_PATH);
	HANDLE h = CreateToolhelp32Snapshot(TH32CS_SNAPPROCESS, 0);
	PROCESSENTRY32 e;
	e.dwSize = sizeof(PROCESSENTRY32);
	char otherModule[MAX_PATH];

	if(Process32First(h, &e)) {
		HANDLE hProcess = OpenProcess(PROCESS_QUERY_INFORMATION | PROCESS_VM_READ,	FALSE, e.th32ProcessID);
		GetModuleFileNameEx(hProcess, 0, otherModule, MAX_PATH);
		CloseHandle(hProcess);
		if(thisProcessId != e.th32ProcessID && strcmp(thisModule, otherModule) == 0) {
			if (dde && DDE::NotifySingleInstance(ini)) {
				Log::Warning("Single Instance Shutdown");
				return 1;
			}
			if(processOnly) {
				Log::Warning("Single Instance Shutdown");
				return 1;
			}
			return !EnumWindows(EnumWindowsProcSingleInstance, e.th32ProcessID);
		}
		while(Process32Next(h, &e)) {
			HANDLE hProcess = OpenProcess(PROCESS_QUERY_INFORMATION | PROCESS_VM_READ, FALSE, e.th32ProcessID);
			GetModuleFileNameEx(hProcess, 0, otherModule, MAX_PATH);
			CloseHandle(hProcess);
			if(thisProcessId != e.th32ProcessID && strcmp(thisModule, otherModule) == 0) {
				if (dde && DDE::NotifySingleInstance(ini)) {
					Log::Warning("Single Instance Shutdown");
					return 1;
				}
				if(processOnly) {
					Log::Warning("Single Instance Shutdown");
					return 1;
				}
				return !EnumWindows(EnumWindowsProcSingleInstance, e.th32ProcessID);
			}
		}
	} 

	return 0;
}

jstring Shell::GetLogicalDrives(JNIEnv* env, jobject self)
{
	char buf[MAX_PATH];
	DWORD len = GetLogicalDriveStrings(MAX_PATH, buf);
	if(len > 0 && len <= MAX_PATH) {
		for(int i = 0; i < len-1; i++) {
			if(buf[i] == 0)
				buf[i] = '|';
		}

		return env->NewStringUTF(buf);
	}
	return NULL;
}

jstring Shell::GetFolderPath(JNIEnv* env, jobject self, jint type)
{
	char path[MAX_PATH];
	if(SUCCEEDED(SHGetFolderPath(NULL, type, NULL, 0, path)))
		return env->NewStringUTF(path);
	return NULL;
}

jstring Shell::GetEnvironmentVariable(JNIEnv* env, jobject self, jstring name)
{
	if(!name)
		return NULL;
	jboolean iscopy = false;
	const char* str = env->GetStringUTFChars(name, &iscopy);
	char buf[4096];
	int len = ::GetEnvironmentVariable(str, buf, 4096);
	if(!len || len > 4096)
		return NULL;
	return env->NewStringUTF(buf);
}

jobject Shell::GetEnvironmentStrings(JNIEnv* env, jobject self, jintArray arr)
{
	LPCH envs = ::GetEnvironmentStrings();
	env->SetIntArrayRegion(arr, 0, 1, (const jint*) &envs);
	char* prev = envs;
	while(true) {
		char* c = prev+1;
		if(!*prev && !*c)
			break;
		prev = c;
	}
	return env->NewDirectByteBuffer(envs, (jlong) (prev - envs + 1));
}

void Shell::FreeEnvironmentStrings(JNIEnv* env, jobject self, jint p)
{
	::FreeEnvironmentStrings((LPCH) p);
}

jstring Shell::ExpandEnvironmentString(JNIEnv* env, jobject self, jstring str)
{
	if(!str)
		return NULL;
	jboolean iscopy = false;
	const char* ch = env->GetStringUTFChars(str, &iscopy);
	char buf[4096];
	int size = ::ExpandEnvironmentStrings(ch, buf, 4096);
	if(size > 4096)
		return NULL;
	return env->NewStringUTF(buf);
}

jstring Shell::GetCommandLine(JNIEnv* env, jobject self)
{
	return env->NewStringUTF(::GetCommandLine());
}

jintArray Shell::GetOSVersionNumbers(JNIEnv* env, jobject self)
{
	OSVERSIONINFOEX os;
	os.dwOSVersionInfoSize = sizeof(OSVERSIONINFOEX);
	GetVersionEx((LPOSVERSIONINFO) &os);
	jintArray res = env->NewIntArray(9);
	env->SetIntArrayRegion(res, 0, 1, (const jint*) &os.dwMajorVersion);
	env->SetIntArrayRegion(res, 1, 1, (const jint*) &os.dwMinorVersion);
	env->SetIntArrayRegion(res, 2, 1, (const jint*) &os.dwBuildNumber);
	env->SetIntArrayRegion(res, 3, 1, (const jint*) &os.dwPlatformId);
	env->SetIntArrayRegion(res, 4, 1, (const jint*) &os.wServicePackMajor);
	env->SetIntArrayRegion(res, 5, 1, (const jint*) &os.wServicePackMinor);
	env->SetIntArrayRegion(res, 6, 1, (const jint*) &os.wSuiteMask);
	env->SetIntArrayRegion(res, 7, 1, (const jint*) &os.wProductType);
	env->SetIntArrayRegion(res, 8, 1, (const jint*) &os.wReserved);
	return res;
}

jstring Shell::GetOSVersionCSD(JNIEnv* env, jobject self)
{
	OSVERSIONINFOEX os;
	os.dwOSVersionInfoSize = sizeof(OSVERSIONINFOEX);
	GetVersionEx((LPOSVERSIONINFO) &os);
	return env->NewStringUTF(os.szCSDVersion);
}

jclass g_fsmClass;
jmethodID g_fsmCallbackMethod;

typedef struct _DIROL {
	OVERLAPPED overlapped;
	char* pBuffer;
	HANDLE hDir;
} DIROL;

VOID CALLBACK OnDirectoryChanges(DWORD errCode, DWORD numBytes, LPOVERLAPPED overlapped)
{
	JNIEnv* env = VM::GetJNIEnv(true);
	DIROL* dol = (DIROL*) overlapped;
	jobject buf = env->NewDirectByteBuffer(dol->pBuffer, numBytes);
	env->CallStaticVoidMethod(g_fsmClass, g_fsmCallbackMethod, (jlong) dol->hDir, buf);
}

jlong Shell::RegisterDirectoryChangeListener(JNIEnv* env, jobject self, 
	jstring directory, jboolean subtree, jint notifyFilter, jint bufferSize)
{
	if(!directory)
		return 0;

	jboolean iscopy = false;
	const char* dir = env->GetStringUTFChars(directory, &iscopy);
	HANDLE hDir = CreateFile(dir, FILE_FLAG_BACKUP_SEMANTICS|FILE_FLAG_OVERLAPPED, 0, 0, 0, 0, 0);
	if(!hDir)
		return 0;
	DIROL* dol = (DIROL*) malloc(sizeof(DIROL));
	dol->hDir = hDir;
	dol->pBuffer = (char*) malloc(bufferSize);
	BOOL res = ReadDirectoryChangesW(hDir, dol->pBuffer, bufferSize, subtree, 
		notifyFilter, 0, &dol->overlapped, OnDirectoryChanges);
	if(!res) {
		CloseHandle(hDir);
		return 0;
	}

	return (jlong) dol;
}

void Shell::CloseDirectoryHandle(JNIEnv* env, jobject self, jlong handle)
{
	if(!handle) return;
	DIROL* dol = (DIROL*) handle;
	CloseHandle(dol->hDir);
	free(dol->pBuffer);
	free(dol);
}

bool Shell::RegisterNatives(JNIEnv *env)
{
	Log::Info("Registering natives for Shell class");

	jclass clazz = JNI::FindClass(env, "org/boris/winrun4j/Shell");
	if(clazz == NULL) {
		JNI::ClearException(env);
		Log::Warning("Could not find Shell class");
		return false;
	}
	
	JNINativeMethod nm[9];
	nm[0].name = "getLogicalDriveStrings";
	nm[0].signature = "()Ljava/lang/String;";
	nm[0].fnPtr = (void*) GetLogicalDrives;
	nm[1].name = "getFolderPathString";
	nm[1].signature = "(I)Ljava/lang/String;";
	nm[1].fnPtr = (void*) GetFolderPath;
	nm[2].name = "getEnvironmentVariable";
	nm[2].signature = "(Ljava/lang/String;)Ljava/lang/String;";
	nm[2].fnPtr = (void*) GetEnvironmentVariable;
	nm[3].name = "getEnvironmentStrings";
	nm[3].signature = "([I)Ljava/nio/ByteBuffer;";
	nm[3].fnPtr = (void*) GetEnvironmentStrings;
	nm[4].name = "freeEnvironmentStrings";
	nm[4].signature = "(I)V";
	nm[4].fnPtr = (void*) FreeEnvironmentStrings;
	nm[5].name = "expandEnvironmentString";
	nm[5].signature = "(Ljava/lang/String;)Ljava/lang/String;";
	nm[5].fnPtr = (void*) ExpandEnvironmentString;
	nm[6].name = "getCommandLine";
	nm[6].signature = "()Ljava/lang/String;";
	nm[6].fnPtr = (void*) GetCommandLine;
	nm[7].name = "getOSVersionNumbers";
	nm[7].signature = "()[I";
	nm[7].fnPtr = (void*) GetOSVersionNumbers;
	nm[8].name = "getOSVersionCSD";
	nm[8].signature = "()Ljava/lang/String;";
	nm[8].fnPtr = (void*) GetOSVersionCSD;
	env->RegisterNatives(clazz, nm, 9);

	if(env->ExceptionCheck()) {
		JNI::PrintStackTrace(env);
		return false;
	}

	clazz = JNI::FindClass(env, "org/boris/winrun4j/FileSystemMonitor");
	if(clazz == NULL) {
		JNI::ClearException(env);
		Log::Warning("Could not find FileSystemMonitor class");
		return false;
	}

	g_fsmClass = clazz;
	g_fsmCallbackMethod = env->GetStaticMethodID(g_fsmClass, "callback", "(JLjava/io/ByteBuffer;)V");
	if(g_fsmCallbackMethod == NULL) {
		JNI::ClearException(env);
		Log::Warning("Could not find FileSystemMonitor.callback method");
		return false;
	}

	nm[0].name = "register";
	nm[0].signature = "(Ljava/lang/String;ZII)J";
	nm[0].fnPtr = (void*) RegisterDirectoryChangeListener;
	nm[1].name = "closeHandle";
	nm[1].signature = "(J)V";
	nm[1].fnPtr = (void*) CloseDirectoryHandle;
	env->RegisterNatives(clazz, nm, 2);

	if(env->ExceptionCheck()) {
		JNI::PrintStackTrace(env);
		return false;
	}

	return true;
}