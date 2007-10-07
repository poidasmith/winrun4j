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

SERVICE_STATUS g_serviceStatus;
SERVICE_STATUS_HANDLE g_serviceStatusHandle;
jclass g_serviceClass;
jobject g_serviceInstance;
jmethodID g_controlMethod;
jmethodID g_controlsAcceptMethod;
jmethodID g_getNameMethod;
jmethodID g_mainMethod;

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
		g_serviceStatus.dwWin32ExitCode = Service::Control(opCode);
		g_serviceStatus.dwCurrentState = SERVICE_STOPPED;
		g_serviceStatus.dwCheckPoint = 0;
		g_serviceStatus.dwWaitHint = 0;
		
		if(!SetServiceStatus(g_serviceStatusHandle, &g_serviceStatus)) {
			Log::Error("Error in SetServiceStatus: %d\n", GetLastError());
		}

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
	g_serviceStatusHandle = RegisterServiceCtrlHandler(Service::GetName(), 
		ServiceCtrlHandler);

	if(g_serviceStatusHandle == (SERVICE_STATUS_HANDLE)0)
	{
		Log::Error("Error registering service control handler: %d\n", GetLastError());
		return;
	}

	Service::Main(argc, argv);
}

int Service::Run(HINSTANCE hInstance, dictionary* ini, int argc, char* argv[])
{
	// Initialise JNI members
	JNIEnv* env = VM::GetJNIEnv();
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

	g_controlMethod = env->GetMethodID(g_serviceClass, "control", "(I)V");
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

	g_mainMethod = env->GetMethodID(g_serviceClass, "main", "([Ljava/lang/String;)V");
	if(g_mainMethod == NULL) {
		Log::Error("Could not find control main class\n");
		return 1;
	}

	const char* serviceName = GetName();
	if(serviceName == NULL) {
		Log::Error("Could not find service name\n");
		return 1;
	}
	
	SERVICE_TABLE_ENTRY dispatchTable[] = { 
		{ (LPSTR) serviceName, ServiceStart }, { NULL, NULL } 
	};

	if(!StartServiceCtrlDispatcher(dispatchTable)) {
		Log::Error("Service control dispatcher error: %d\n", GetLastError());
		return 2;
	}
}

void Service::Register(LPSTR lpCmdLine)
{
}

void Service::Unregister(LPSTR lpCmdLine)
{
}

const char* Service::GetName()
{
	JNIEnv* env = VM::GetJNIEnv();
	jstring name = (jstring) env->CallObjectMethod(g_serviceInstance, g_getNameMethod);
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
	return env->CallIntMethod(g_serviceInstance, g_controlsAcceptMethod, (jint) opCode);
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

	return env->CallIntMethod(g_serviceInstance, g_mainMethod, args);
}