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
#include "../../build/JNIServer_i.h"
#include "../../build/JNIServer_i.c"
#include <jni.h>

struct COMJavaVM {
    jint DestroyJavaVM() {
        return 0;
    }
    jint AttachCurrentThread(void **penv, void *args) {
        return 0;
    }
    jint DetachCurrentThread() {
        return 0;
    }

    jint GetEnv(void **penv, jint version) {
        return 0;
    }
    jint AttachCurrentThreadAsDaemon(void **penv, void *args) {
        return 0;
    }

	IJNIServer* iJVM;
	long pJVM;	
};

BSTR ConvertCharToBSTR(char* str)
{
	int a = lstrlenA(str);
	BSTR bstr = SysAllocStringLen(NULL, a);
	MultiByteToWideChar(CP_ACP, 0, str, a, bstr, a);
	return bstr;
}

void FreeBSTR(BSTR bstr)
{
	SysFreeString(bstr);
}

__declspec(dllexport) HRESULT WINAPI CreateJavaVM(TCHAR* libPath, TCHAR** vmArgs, JavaVM** jvm)
{
	CoInitialize(NULL);
	IJNIServer* iJVM = NULL;
	HRESULT hr = CoCreateInstance(CLSID_JNIServer, NULL, CLSCTX_LOCAL_SERVER, IID_IJNIServer, (void **) &iJVM);
	if(FAILED(hr)) {
		printf("Failed to create instance: %x\n", hr);
		return hr;
	}

	long pJVM = NULL;
	hr = iJVM->CreateJavaVM(ConvertCharToBSTR(libPath), NULL, &pJVM);
	if(FAILED(hr)) {
		printf("Failed to call test function: %x\n", hr);
		return hr;
	}

	printf("CreateJavaVM call succeeded\n");

	// Create wrapper struct over COM pointer
	//COMJavaVM* cJVM = new COMJavaVM;
	//cJVM->iJVM = iJVM;
	//cJVM->pJVM = pJVM;
	//*jvm = (JavaVM *)cJVM;

	iJVM->Release();

	return 0;
}