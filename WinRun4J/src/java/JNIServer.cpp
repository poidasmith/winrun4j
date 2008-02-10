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
#include "../../build/JNIServer_i.h"
#include "../../build/JNIServer_i.c"
#include <stdio.h>

extern "C" int __cdecl _purecall()
{
	return 0;
}

class JNIServer : public IJNIServer {
public:
	JNIServer() : ref(1) {}
	virtual ~JNIServer() {}

	virtual HRESULT __stdcall QueryInterface(const IID& iid, void** ppv)
	{
		if(IsEqualIID(iid, IID_IUnknown) || IsEqualIID(iid, IID_IJNIServer)) {
			*ppv = static_cast<IJNIServer*>(this) ; 
		}
		else {
			*ppv = NULL ;
			return E_NOINTERFACE ;
		}
		reinterpret_cast<IUnknown*>(*ppv)->AddRef() ;
		return S_OK ;
	}

	virtual ULONG __stdcall AddRef()
	{
		return InterlockedIncrement(&ref);
	}

	virtual ULONG __stdcall Release()
	{
		if (InterlockedDecrement(&ref) == 0) {
			delete this;
			return 0;
		}
		return ref;
	}

	virtual HRESULT __stdcall Test(long *pVal)
	{
		*pVal = 1000;
		return S_OK;
	}

private:
	long ref;
};

class JNIServerFactory : public IClassFactory 
{
public:
	JNIServerFactory() : ref(1), instance(new JNIServer) {}
	virtual ~JNIServerFactory() 
	{
		instance->Release();
	}

	virtual HRESULT __stdcall CreateInstance(IUnknown* pUnknownOuter, const IID& iid, void** ppv) 
	{
		if (pUnknownOuter != NULL)	{
			return CLASS_E_NOAGGREGATION;
		}

		return instance->QueryInterface(iid, ppv);
	}

	virtual HRESULT __stdcall LockServer(BOOL bLock) 
	{
		return S_OK;
	}

	virtual HRESULT __stdcall QueryInterface(const IID& iid, void** ppv)
	{
		if(IsEqualIID(iid, IID_IUnknown) || IsEqualIID(iid, IID_IClassFactory)) {
			*ppv = static_cast<IClassFactory*>(this) ; 
		}
		else {
			*ppv = NULL ;
			return E_NOINTERFACE ;
		}
		reinterpret_cast<IUnknown*>(*ppv)->AddRef() ;
		return S_OK ;
	}

	virtual ULONG __stdcall AddRef()
	{
		return InterlockedIncrement(&ref);
	}

	virtual ULONG __stdcall Release()
	{
		if (InterlockedDecrement(&ref) == 0) {
			delete this;
			return 0;
		}
		return ref;
	}

private:
	long ref;
	JNIServer* instance;
};

int RegisterServer()
{
	return 0;
}

int UnregisterServer()
{
	return 0;
}

JNIServerFactory factory;

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

