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

__declspec(dllexport) HRESULT WINAPI CreateJavaVM(TCHAR* libPath, TCHAR** vmArgs, JavaVM** jvm)
{
	CoInitialize(NULL);
	IJNIServer* ijvm = NULL;
	HRESULT hr = CoCreateInstance(CLSID_JNIServer, NULL, CLSCTX_LOCAL_SERVER, IID_IJNIServer, (void **) &ijvm);
	if(FAILED(hr)) {
		printf("Failed to create instance: %x\n", hr);
		return hr;
	}

	long val = 0;
	hr = ijvm->Test(&val);
	if(FAILED(hr)) {
		printf("Failed to call test function: %x\n", hr);
		return hr;
	}

	printf("Server Returned: %d\n", val);

	ijvm->Release();
	CoUninitialize();

	return 0;
}