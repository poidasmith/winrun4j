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

namespace 
{
	char* g_serviceId = 0;
	int g_controlsAccepted = 0;
	int g_returnCode = 0;
	SERVICE_STATUS g_serviceStatus;
	SERVICE_STATUS_HANDLE g_serviceStatusHandle;
	jclass g_serviceClass;
	jobject g_serviceInstance;
	jmethodID g_controlMethod;
	jmethodID g_mainMethod;
	HANDLE g_event;
}

#define SERVICE_ID ":service.id"
#define SERVICE_NAME ":service.name"
#define SERVICE_DESCRIPTION ":service.description"
#define SERVICE_CONTROLS ":service.controls"
#define SERVICE_STARTUP ":service.startup"
#define SERVICE_DEPENDENCY ":service.dependency"
#define SERVICE_USER ":service.user"
#define SERVICE_PWD ":service.password"
#define SERVICE_LOAD_ORDER_GROUP ":service.loadordergroup"

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
			Log::Error("Error in SetServiceStatus: %d", GetLastError());
		}

		// Detach this thread so it doesn't block
		VM::DetachCurrentThread();

		return;
	
	case SERVICE_INTERROGATE:
		break;
	}

	if(!SetServiceStatus(g_serviceStatusHandle, &g_serviceStatus)) {
		Log::Error("Error in SetServiceStatus: %d", GetLastError());
	}
}

void WINAPI ServiceStart(DWORD argc, LPTSTR *argv)
{
	g_serviceStatus.dwServiceType = SERVICE_WIN32;
	g_serviceStatus.dwCurrentState = SERVICE_START_PENDING;
	g_serviceStatus.dwControlsAccepted = g_controlsAccepted;
	g_serviceStatus.dwWin32ExitCode = 0;
	g_serviceStatus.dwServiceSpecificExitCode = 0;
	g_serviceStatus.dwWaitHint = 0;

	// Register the service
	g_serviceStatusHandle = RegisterServiceCtrlHandler(g_serviceId, ServiceCtrlHandler);

	if(g_serviceStatusHandle == (SERVICE_STATUS_HANDLE)0) {
		Log::Error("Error registering service control handler: %d", GetLastError());
		return;
	}

	Service::Main(argc, argv);
}

int Service::Initialise(dictionary* ini)
{
	g_serviceId = iniparser_getstr(ini, SERVICE_ID);
	if(g_serviceId == NULL) {
		Log::Error("Service ID not specified");
		return 1;
	}

	// Parse controls accepted
	char* controls = iniparser_getstr(ini, SERVICE_CONTROLS);
	if(controls) {
		int len = strlen(controls);
		int nb = 0;
		for(int i = 0; i < len; i++) {
			if(controls[i] == '|') {
				controls[i] = 0;
				nb++;
			}
		}
		char* p = controls;
		char* e = controls + len;
		for(int i = 0; i <= nb; i++) {
			int plen = strlen(p);
			StrTrim(p, " ");
			if(strcmp("stop", p) == 0) {
				g_controlsAccepted |= SERVICE_ACCEPT_STOP;
			} else if(strcmp("shutdown", p) == 0) {
				g_controlsAccepted |= SERVICE_ACCEPT_SHUTDOWN;
			} else if(strcmp("pause", p) == 0) {
				g_controlsAccepted |= SERVICE_ACCEPT_PAUSE_CONTINUE;
			} else if(strcmp("param", p) == 0) {
				g_controlsAccepted |= SERVICE_ACCEPT_PARAMCHANGE;
			} else if(strcmp("netbind", p) == 0) {
				g_controlsAccepted |= SERVICE_ACCEPT_NETBINDCHANGE;
			} else if(strcmp("hardware", p) == 0) {
				g_controlsAccepted |= SERVICE_ACCEPT_HARDWAREPROFILECHANGE;
			} else if(strcmp("power", p) == 0) {
				g_controlsAccepted |= SERVICE_ACCEPT_POWEREVENT;
			} else if(strcmp("session", p) == 0) {
				g_controlsAccepted |= SERVICE_ACCEPT_SESSIONCHANGE;
			}

			p += plen + 1;
			if(p >= e) break;
		}
	} else {
		g_controlsAccepted = SERVICE_ACCEPT_STOP | SERVICE_ACCEPT_SHUTDOWN;
	}

	// Initialise JNI members
	JNIEnv* env = VM::GetJNIEnv();
	if(env == NULL) {
		Log::Error("JNIEnv is null");
		return 1;
	}

	g_serviceClass = env->FindClass(iniparser_getstr(ini, SERVICE_CLASS));
	if(g_serviceClass == NULL) {
		Log::Error("Could not find service class");
		return 1;
	}

	g_serviceInstance = env->NewObject(g_serviceClass, env->GetMethodID(g_serviceClass, "<init>", "()V"));
	if(g_serviceInstance == NULL) {
		Log::Error("Could not create service class");
		return 1;
	}

	g_controlMethod = env->GetMethodID(g_serviceClass, "serviceRequest", "(I)I");
	if(g_controlMethod == NULL) {
		Log::Error("Could not find control method class");
		return 1;
	}

	g_mainMethod = env->GetMethodID(g_serviceClass, "serviceMain", "([Ljava/lang/String;)I");
	if(g_mainMethod == NULL) {
		Log::Error("Could not find control main class");
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
		Log::Error("Service control dispatcher error: %d", GetLastError());
		return 2;
	}

	return 0;
}

// We expect the commandline to be "--WinRun4J:RegisterService"
int Service::Register(dictionary* ini)
{
	Log::Info("Registering Service...");

	g_serviceId = iniparser_getstr(ini, SERVICE_ID);
	if(g_serviceId == NULL) {
		Log::Error("Service ID not specified");
		return 1;
	}

	// Grab service name
	char* name = iniparser_getstr(ini, SERVICE_NAME);
	if(!name) {
		Log::Error("Service name not specified");
		return 1;
	}

	// Grab service description
	char* description = iniparser_getstr(ini, SERVICE_DESCRIPTION);
	if(!description) {
		Log::Error("Service description not specified");
		return 1;
	}

	// Check for startup mode override
	DWORD startupMode = SERVICE_DEMAND_START;
	char* startup = iniparser_getstr(ini, SERVICE_STARTUP);
	if(startup != NULL) {
		if(strcmp(startup, "auto") == 0) {
			startupMode = SERVICE_AUTO_START;
			Log::Info("Service startup mode: SERVICE_AUTO_START");
		} else if(strcmp(startup, "boot") == 0) {
			startupMode = SERVICE_BOOT_START;
			Log::Info("Service startup mode: SERVICE_BOOT_START");
		} else if(strcmp(startup, "demand") == 0) {
			startupMode = SERVICE_DEMAND_START;
			Log::Info("Service startup mode: SERVICE_DEMAND_START");
		} else if(strcmp(startup, "disabled") == 0) {
			startupMode = SERVICE_DISABLED;
			Log::Info("Service startup mode: SERVICE_DISABLED");
		} else if(strcmp(startup, "system") == 0) {
			startupMode = SERVICE_SYSTEM_START;
			Log::Info("Service startup mode: SERVICE_SYSTEM_START");
		} else {
			Log::Warning("Unrecognized service startup mode: %s", startup);
		}
	}

	// Check for dependencies
	TCHAR* dependencies[MAX_PATH];
	int depCount = 0;
	INI::GetNumberedKeysFromIni(ini, SERVICE_DEPENDENCY, dependencies, depCount);
	
	// Make dependency list
	TCHAR* depList = NULL;
	int depListSize = 0;
	for(int i = 0; i < depCount; i++) {
		depListSize += strlen(dependencies[i]) + 1;
	}
	if(depListSize > 0) {
		depList = (TCHAR*) malloc(depListSize);
		if(depList == 0) {
			Log::Error("Could not create dependency list");
			return 1;
		}

		int depPointer = (int) depList;
		for(int i = 0; i < depCount; i++) {
			strcpy((TCHAR*) depPointer, dependencies[i]);
			depPointer += strlen(dependencies[i]) + 1;
		}
	}

	char* loadOrderGroup = iniparser_getstr(ini, SERVICE_LOAD_ORDER_GROUP);

	// Check for user account
	char* user = iniparser_getstr(ini, SERVICE_USER);
	char* pwd = iniparser_getstr(ini, SERVICE_PWD);

	TCHAR path[MAX_PATH];
	TCHAR quotePath[MAX_PATH];
	quotePath[0] = '\"';
	quotePath[1] = 0;
	GetModuleFileName(NULL, path, MAX_PATH);
	strcat(quotePath, path);
	strcat(quotePath, "\"");
	SC_HANDLE h = OpenSCManager(NULL, NULL, SC_MANAGER_CREATE_SERVICE);
	if(!h) {
		DWORD error = GetLastError();
		Log::Error("Could not access service manager: %d", error);
		return error;
	}
	SC_HANDLE s = CreateService(h, g_serviceId, name, SERVICE_ALL_ACCESS, 
		SERVICE_WIN32_OWN_PROCESS, startupMode,
		SERVICE_ERROR_NORMAL, quotePath, loadOrderGroup, NULL, (LPCTSTR)depList, user, pwd);
	if(!s) {
		DWORD error = GetLastError();
		if(error == ERROR_SERVICE_EXISTS) {
			Log::Warning("Service already exists");
		} else {
			Log::Error("Could not create service: %d", error);
		}
		return error;
	}
	CloseServiceHandle(s);
	CloseServiceHandle(h);

	// Add description 
	strcpy(path, "System\\CurrentControlSet\\Services\\");
	strcat(path, g_serviceId);
	HKEY key;
	RegOpenKey(HKEY_LOCAL_MACHINE, path, &key);
	RegSetValueEx(key, "Description", 0, REG_SZ, (BYTE*) description, strlen(description));

 	return 0;
}

// We expect the commandline to be "--WinRun4J:UnregisterService"
int Service::Unregister(dictionary* ini)
{
	Log::Info("Unregistering Service...");

	const char* serviceId = iniparser_getstr(ini, SERVICE_ID);
	if(serviceId == NULL) {
		Log::Error("Service ID not specified");
		return 1;
	}

	SC_HANDLE h = OpenSCManager(NULL, NULL, SC_MANAGER_CREATE_SERVICE);
	if(!h) {
		DWORD error = GetLastError();
		Log::Error("Could not access service manager: %d", error);
		return error;
	}
	SC_HANDLE s = OpenService(h, serviceId, SC_MANAGER_ALL_ACCESS);
	if(!s) {
		DWORD error = GetLastError();
		Log::Error("Could not open service: %d", error);
		return error;
	}

	return DeleteService(s);
}

int Service::Control(DWORD opCode)
{
	JNIEnv* env = VM::GetJNIEnv();
	return env->CallIntMethod(g_serviceInstance, g_controlMethod, (jint) opCode);
}

DWORD ServiceMainThread(LPVOID lpParam)
{
	JNIEnv* env = VM::GetJNIEnv();

	// Now signal launcher thread
	SetEvent(g_event);

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
	for(UINT i = 0; i < argc - 1; i++) {
		env->SetObjectArrayElement(args, i, env->NewStringUTF(argv[i+1]));
	}
	env->NewGlobalRef(args);

	// Create the event
	g_event = CreateEvent(0, TRUE, FALSE, 0);

	CreateThread(0, 0, (LPTHREAD_START_ROUTINE)ServiceMainThread, (LPDWORD) args, 0, 0);
	g_serviceStatus.dwCurrentState = SERVICE_RUNNING;
	SetServiceStatus(g_serviceStatusHandle, &g_serviceStatus);

	// Need to wait for service thread to attach
	WaitForSingleObject(g_event, INFINITE);

	// Detach this thread so it doesn't block
	VM::DetachCurrentThread();

	return 0;
}