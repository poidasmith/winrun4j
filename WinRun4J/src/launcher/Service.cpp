/*******************************************************************************
* This program and the accompanying materials
* are made available under the terms of the Common Public License v1.0
* which accompanies this distribution, and is available at 
* http://www.eclipse.org/legal/cpl-v10.html
* 
* Contributors:
*     Peter Smith
*******************************************************************************/

#include "Service.h"
#include "../common/INI.h"
#include "../common/Log.h"
#include "../java/JNI.h"
#include "../java/VM.h"
#include "../WinRun4J.h"

static char* g_serviceId = 0;
static int g_returnCode = 0;
SERVICE_STATUS g_serviceStatus;
SERVICE_STATUS_HANDLE g_serviceStatusHandle;
jclass g_serviceClass;
jobject g_serviceInstance;
jmethodID g_controlMethod;
jmethodID g_controlsAcceptMethod;
jmethodID g_getNameMethod;
jmethodID g_getDescriptionMethod;
jmethodID g_mainMethod;

#define SERVICE_ID ":service.id"

void WINAPI ServiceCtrlHandler(DWORD opCode)
{
	int result;

	switch(opCode)
	{
	case SERVICE_CONTROL_PAUSE:
		Service::Control(opCode);
		g_serviceStatus.dwCurrentState = SERVICE_PAUSED;
		break;

	case SERVICE_CONTROL_CONTINUE:
		Service::Control(opCode);
		g_serviceStatus.dwCurrentState = SERVICE_RUNNING;
		break;

	case SERVICE_CONTROL_SHUTDOWN:
	case SERVICE_CONTROL_STOP:
		Service::Control(opCode);
		g_serviceStatus.dwWin32ExitCode = 0;
		g_serviceStatus.dwCurrentState = SERVICE_STOP_PENDING;
		g_serviceStatus.dwCheckPoint = 0;
		g_serviceStatus.dwWaitHint = 0;
		
		if(!SetServiceStatus(g_serviceStatusHandle, &g_serviceStatus)) {
			Log::Error("Error in SetServiceStatus: %d\n", GetLastError());
		}

		// Detach this thread so it doesn't block
		VM::DetachCurrentThread();

		return;
	
	case SERVICE_INTERROGATE:
		break;
	}

	if(!SetServiceStatus(g_serviceStatusHandle, &g_serviceStatus)) {
		Log::Error("Error in SetServiceStatus: %d\n", GetLastError());
	}
}

void WINAPI ServiceStart(DWORD argc, LPTSTR *argv)
{
	g_serviceStatus.dwServiceType = SERVICE_WIN32;
	g_serviceStatus.dwCurrentState = SERVICE_START_PENDING;
	g_serviceStatus.dwControlsAccepted = Service::GetControlsAccepted();
	g_serviceStatus.dwWin32ExitCode = 0;
	g_serviceStatus.dwServiceSpecificExitCode = 0;
	g_serviceStatus.dwWaitHint = 0;

	// Register the service
	g_serviceStatusHandle = RegisterServiceCtrlHandler(g_serviceId, ServiceCtrlHandler);

	if(g_serviceStatusHandle == (SERVICE_STATUS_HANDLE)0)
	{
		Log::Error("Error registering service control handler: %d\n", GetLastError());
		return;
	}

	Service::Main(argc, argv);
}

int Service::Initialise(dictionary* ini)
{
	g_serviceId = iniparser_getstr(ini, SERVICE_ID);
	if(g_serviceId == NULL) {
		Log::Error("Service ID not specified\n");
		return 1;
	}

	// Initialise JNI members
	JNIEnv* env = VM::GetJNIEnv();
	if(env == NULL) {
		Log::Error("JNIEnv is null\n");
		return 1;
	}

	g_serviceClass = env->FindClass(iniparser_getstr(ini, SERVICE_CLASS));
	if(g_serviceClass == NULL) {
		Log::Error("Could not find service class\n");
		return 1;
	}

	g_serviceInstance = env->NewObject(g_serviceClass, env->GetMethodID(g_serviceClass, "<init>", "()V"));
	if(g_serviceInstance == NULL) {
		Log::Error("Could not create service class\n");
		return 1;
	}

	g_controlMethod = env->GetMethodID(g_serviceClass, "doRequest", "(I)I");
	if(g_controlMethod == NULL) {
		Log::Error("Could not find control method class\n");
		return 1;
	}

	g_controlsAcceptMethod = env->GetMethodID(g_serviceClass, "getControlsAccepted", "()I");
	if(g_controlsAcceptMethod == NULL) {
		Log::Error("Could not find control getControlsAccepted class\n");
		return 1;
	}

	g_getNameMethod = env->GetMethodID(g_serviceClass, "getName", "()Ljava/lang/String;");
	if(g_getNameMethod == NULL) {
		Log::Error("Could not find control getName class\n");
		return 1;
	}

	g_getDescriptionMethod = env->GetMethodID(g_serviceClass, "getDescription", "()Ljava/lang/String;");
	if(g_getNameMethod == NULL) {
		Log::Error("Could not find control getDescription class\n");
		return 1;
	}

	g_mainMethod = env->GetMethodID(g_serviceClass, "main", "([Ljava/lang/String;)I");
	if(g_mainMethod == NULL) {
		Log::Error("Could not find control main class\n");
		return 1;
	}

	return 0;
}

int Service::Run(HINSTANCE hInstance, dictionary* ini, int argc, char* argv[])
{
	int result = Initialise(ini);
	if(result != 0) {
		return result;
	}
	
	SERVICE_TABLE_ENTRY dispatchTable[] = { 
		{ (LPSTR) g_serviceId, ServiceStart }, { NULL, NULL } 
	};

	if(!StartServiceCtrlDispatcher(dispatchTable)) {
		Log::Error("Service control dispatcher error: %d\n", GetLastError());
		return 2;
	}

	return 0;
}

// We expect the commandline to be "--WinRun4J:RegisterService"
int Service::Register(dictionary* ini)
{
	int result = WinRun4J::StartVM(StripArg0(GetCommandLine()), ini);
	if(result) {
		return result;
	}

	result = Initialise(ini);
	if(result != 0) {
		return result;
	}

	TCHAR path[MAX_PATH];
	TCHAR quotePath[MAX_PATH];
	quotePath[0] = '\"';
	quotePath[1] = 0;
	GetModuleFileName(NULL, path, MAX_PATH);
	strcat(quotePath, path);
	strcat(quotePath, "\"");
	SC_HANDLE h = OpenSCManager(NULL, NULL, SC_MANAGER_CREATE_SERVICE);
	SC_HANDLE s = CreateService(h, g_serviceId, GetName(), SERVICE_ALL_ACCESS, 
		SERVICE_WIN32_OWN_PROCESS, SERVICE_DEMAND_START,
		SERVICE_ERROR_NORMAL, quotePath, NULL, NULL, NULL, NULL, NULL);
	CloseServiceHandle(s);
	CloseServiceHandle(h);

	// Add description 
	strcpy(path, "System\\CurrentControlSet\\Services\\");
	strcat(path, g_serviceId);
	HKEY key;
	RegOpenKey(HKEY_LOCAL_MACHINE, path, &key);
	const char* desc = GetDescription();
	RegSetValueEx(key, "Description", 0, REG_SZ, (BYTE*) desc, strlen(desc));

	return 0;
}

// We expect the commandline to be "--WinRun4J:UnregisterService"
int Service::Unregister(dictionary* ini)
{
	const char* serviceId = iniparser_getstr(ini, SERVICE_ID);
	if(serviceId == NULL) {
		Log::Error("Service ID not specified\n");
		return 1;
	}

	SC_HANDLE h = OpenSCManager(NULL, NULL, SC_MANAGER_CREATE_SERVICE);
	SC_HANDLE s = OpenService(h, serviceId, SC_MANAGER_ALL_ACCESS);
	return DeleteService(s);
}

const char* Service::GetName()
{
	JNIEnv* env = VM::GetJNIEnv();
	jstring name = (jstring) env->CallObjectMethod(g_serviceInstance, g_getNameMethod);
	jboolean iscopy = false;
	return env->GetStringUTFChars(name, &iscopy);
}

const char* Service::GetDescription()
{
	JNIEnv* env = VM::GetJNIEnv();
	jstring name = (jstring) env->CallObjectMethod(g_serviceInstance, g_getDescriptionMethod);
	jboolean iscopy = false;
	return env->GetStringUTFChars(name, &iscopy);
}

int Service::GetControlsAccepted()
{
	JNIEnv* env = VM::GetJNIEnv();
	return env->CallIntMethod(g_serviceInstance, g_controlsAcceptMethod);
}

int Service::Control(DWORD opCode)
{
	JNIEnv* env = VM::GetJNIEnv();
	return env->CallIntMethod(g_serviceInstance, g_controlMethod, (jint) opCode);
}

DWORD ServiceMainThread(LPVOID lpParam)
{
	JNIEnv* env = VM::GetJNIEnv();
	g_returnCode = env->CallIntMethod(g_serviceInstance, g_mainMethod, (jobjectArray) lpParam);

	VM::CleanupVM();
	g_serviceStatus.dwCurrentState = SERVICE_STOPPED;
	SetServiceStatus(g_serviceStatusHandle, &g_serviceStatus);

	return g_returnCode;
}

int Service::Main(DWORD argc, LPSTR* argv)
{
	JNIEnv* env = VM::GetJNIEnv();

	// Create the run args
	jclass stringClass = env->FindClass("java/lang/String");
	jobjectArray args = env->NewObjectArray(argc - 1, stringClass, NULL);
	for(int i = 0; i < argc - 1; i++) {
		env->SetObjectArrayElement(args, i, env->NewStringUTF(argv[i]));
	}

	CreateThread(0, 0, (LPTHREAD_START_ROUTINE)ServiceMainThread, 0, 0, (LPDWORD) args);
	g_serviceStatus.dwCurrentState = SERVICE_RUNNING;
	SetServiceStatus(g_serviceStatusHandle, &g_serviceStatus);

	// Detach this thread so it doesn't block
	VM::DetachCurrentThread();

	return 0;
}