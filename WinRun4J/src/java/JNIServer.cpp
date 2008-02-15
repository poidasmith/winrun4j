/*******************************************************************************
* This program and the accompanying materials
* are made available under the terms of the Common Public License v1.0
* which accompanies this distribution, and is available at 
* http://www.eclipse.org/legal/cpl-v10.html
* 
* Contributors:
*     Peter Smith
*******************************************************************************/

#include "../common/Runtime.h"
#include "../common/COMHelper.h"
#include "../../build/JNIServer_i.h"
#include "../../build/JNIServer_i.c"
#include <stdio.h>
#include <jni.h>

extern "C" int __cdecl _purecall()
{
	return 0;
}

typedef jint (JNICALL *JNI_createJavaVM)(JavaVM **pvm, JNIEnv **env, void *args);

#define E_VM_NOT_CREATED 1
#define E_LIBRARY_NOT_FOUND 2
#define E_CREATE_FUNCTION_NOT_FOUND 3;

#define STDMETHOD HRESULT __stdcall

class JNIServer : public COMBase<IJNIServer> {
public:
	JNIServer() : jvm(0), env(0), hModule(0) {}
	STDMETHOD CreateJavaVM(BSTR libPath, SAFEARRAY* vmArgs);
	STDMETHOD DestroyJavaVM(long* pResult);
    STDMETHOD FindClass(BSTR name, long *clazz);
    STDMETHOD NewObjectArray(int len, long clazz, long init, long *arr);
    STDMETHOD SetObjectArrayElement(long array, int index, long val);
    STDMETHOD NewStringUTF(BSTR utf, long *str);
    STDMETHOD GetStaticMethodID(long clazz, BSTR name, BSTR sig, long *methodID);
    STDMETHOD CallStaticVoidMethod(long clazz, long methodID, SAFEARRAY *args);
    STDMETHOD ExceptionOccurred(long *throwable);
    STDMETHOD ExceptionDescribe();
    STDMETHOD ExceptionClear();

private:
	JavaVM* jvm;
	JNIEnv* env;
	HMODULE hModule;
};

int RegisterServer()
{
	return 0;
}

int UnregisterServer()
{
	return 0;
}

ClassFactoryBase<JNIServer> factory;

int __stdcall WinMain(HINSTANCE hInstance, HINSTANCE hPrevInstance, LPSTR lpCmdLine, int nCmdShow) 
{
	if (strcmp (lpCmdLine, "Register") == 0) {
		return RegisterServer();
	}
	else if (strstr (lpCmdLine, "Unregister")) {
		return UnregisterServer();
	}

	CoInitialize(0);	
	DWORD registerId = 0;
	HRESULT hr = CoRegisterClassObject(CLSID_JNIServer, &factory, CLSCTX_SERVER, REGCLS_SINGLEUSE, &registerId);
	if(FAILED(hr))
		return hr;

	MSG msg;
	while (GetMessage(&msg, 0, 0, 0) > 0) {
		TranslateMessage(&msg);
		DispatchMessage(&msg);
	}

	CoRevokeClassObject(registerId);
	CoUninitialize();

	return 0;
}

HRESULT __stdcall JNIServer::CreateJavaVM(BSTR libPath, SAFEARRAY* vmArgs)
{
	char* path = ConvertBSTRToChar(libPath);
	hModule = LoadLibrary(path);
	delete path;

	if(hModule == NULL) {
		return E_LIBRARY_NOT_FOUND; 
	}

	JNI_createJavaVM createJavaVM = (JNI_createJavaVM)GetProcAddress(hModule, "JNI_CreateJavaVM");
	if(createJavaVM == NULL) {
		return E_CREATE_FUNCTION_NOT_FOUND; 
	}

	TCHAR** cArgs = ConvertSafeArrayToCharArray(vmArgs);
	int argCount = 0;
	while(cArgs[argCount]) argCount++;
	JavaVMOption* options = (JavaVMOption*) malloc((argCount) * sizeof(JavaVMOption));
	for(int i = 0; i < argCount; i++){
		options[i].optionString = cArgs[i];
		options[i].extraInfo = 0;
	}
		
	JavaVMInitArgs init_args;
	init_args.version = JNI_VERSION_1_2;
	init_args.options = options;
	init_args.nOptions = argCount;
	init_args.ignoreUnrecognized = JNI_TRUE;
	
	int result = createJavaVM(&jvm, &env, &init_args);

	delete cArgs;
	free(options);

	return result;
}

HRESULT __stdcall JNIServer::DestroyJavaVM(long* pResult)
{
	if(!jvm) return E_VM_NOT_CREATED;

	*pResult = jvm->DestroyJavaVM();
	FreeLibrary(hModule);

	return S_OK;
}

STDMETHOD JNIServer::FindClass(BSTR name, long *clazz)
{
	if(!jvm) return E_VM_NOT_CREATED;

	char* nameStr = ConvertBSTRToChar(name);
	*clazz = (long) env->FindClass(nameStr);
	delete nameStr;

	return S_OK;
}

STDMETHOD JNIServer::NewObjectArray(int len, long clazz, long init, long *arr)
{
	if(!jvm) return E_VM_NOT_CREATED;

	*arr = (long) env->NewObjectArray(len, (jclass) clazz, (jclass) init);

	return S_OK;
}

STDMETHOD JNIServer::SetObjectArrayElement(long array, int index, long val)
{
	if(!jvm) return E_VM_NOT_CREATED;

	env->SetObjectArrayElement((jobjectArray) array, index, (jobject) val);

	return S_OK;
}

STDMETHOD JNIServer::NewStringUTF(BSTR utf, long *str)
{
	if(!jvm) return E_VM_NOT_CREATED;

	char* utfStr = ConvertBSTRToChar(utf);
	*str = (long) env->NewStringUTF(utfStr);
	delete utfStr;

	return S_OK;
}

STDMETHOD JNIServer::GetStaticMethodID(long clazz, BSTR name, BSTR sig, long *methodID)
{
	if(!jvm) return E_VM_NOT_CREATED;

	char* nameStr = ConvertBSTRToChar(name);
	char* sigStr = ConvertBSTRToChar(sig);
	*methodID = (long) env->GetStaticMethodID((jclass) clazz, nameStr, sigStr);
	delete nameStr;
	delete sigStr;

	return S_OK;
}

STDMETHOD JNIServer::CallStaticVoidMethod(long clazz, long methodID, SAFEARRAY *args)
{
	if(!jvm) return E_VM_NOT_CREATED;

	env->CallStaticVoidMethod((jclass) clazz, (jmethodID) methodID, args);	

	return E_NOTIMPL;
}

STDMETHOD JNIServer::ExceptionOccurred(long *throwable)
{
	if(!jvm) return E_VM_NOT_CREATED;

	*throwable = (long) env->ExceptionOccurred();
	return S_OK;
}

STDMETHOD JNIServer::ExceptionDescribe()
{
	if(!jvm) return E_VM_NOT_CREATED;

	env->ExceptionDescribe();
	return S_OK;
}

STDMETHOD JNIServer::ExceptionClear()
{
	if(!jvm) return E_VM_NOT_CREATED;

	env->ExceptionClear();
	return S_OK;
}

