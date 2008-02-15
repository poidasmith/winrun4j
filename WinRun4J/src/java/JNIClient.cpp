/*******************************************************************************
* This program and the accompanying materials
* are made available under the terms of the Common Public License v1.0
* which accompanies this distribution, and is available at 
* http://www.eclipse.org/legal/cpl-v10.html
* 
* Contributors:
*     Peter Smith
*******************************************************************************/

#include "JNIClient.h"
#include "../common/COMHelper.h"
#include "../../build/JNIServer_i.h"
#include "../../build/JNIServer_i.c"
#include <jni.h>

struct COMJavaVM {
    jint DestroyJavaVM();
    jint AttachCurrentThread(void **penv, void *args);
    jint DetachCurrentThread();
    jint GetEnv(void **penv, jint version);
    jint AttachCurrentThreadAsDaemon(void **penv, void *args);
	IJNIServer* iJVM;
};

struct COMJNIEnv {
	IJNIServer* iJVM;
};

COMJavaVM g_jvm;
COMJNIEnv g_env;

__declspec(dllexport) HRESULT WINAPI CreateJavaVM(TCHAR* libPath, TCHAR** vmArgs, JavaVM** jvm)
{
	CoInitialize(NULL);
	IJNIServer* iJVM = NULL;
	HRESULT hr = CoCreateInstance(CLSID_JNIServer, NULL, CLSCTX_LOCAL_SERVER, IID_IJNIServer, (void **) &iJVM);
	if(FAILED(hr)) {
		return hr;
	}

	BSTR bLibPath = ConvertCharToBSTR(libPath);
	SAFEARRAY* sArgs = ConvertCharArrayToSafeArray(vmArgs);
	hr = iJVM->CreateJavaVM(bLibPath, sArgs);
	FreeBSTR(bLibPath);
	FreeSafeArray(sArgs);
	if(FAILED(hr)) {
		return hr;
	}

	g_jvm.iJVM = iJVM;
	*jvm = (JavaVM *)&g_jvm;

	return S_OK;
}

#ifdef JNICLIENT_STANDALONE
BOOL WINAPI DllMain(HINSTANCE hinstDLL, DWORD fdwReason, LPVOID lpvReserved)
{
	return 1;
}
#endif

jint COMJavaVM::DestroyJavaVM()
{
	long result;
	iJVM->DestroyJavaVM(&result);
	return result;
}

jint COMJavaVM::AttachCurrentThread(void **penv, void *args)
{
	return 0;
}

jint COMJavaVM::DetachCurrentThread()
{
	return 0;
}

jint COMJavaVM::GetEnv(void **penv, jint version)
{
	*penv = &g_env;
	return 0;
}

jint COMJavaVM::AttachCurrentThreadAsDaemon(void **penv, void *args)
{
	return 0;
}

