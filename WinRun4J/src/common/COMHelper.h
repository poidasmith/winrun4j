/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Peter Smith
 *******************************************************************************/

#ifndef COMHELPER_H
#define COMHELPER_H

#include "Runtime.h"

BSTR ConvertCharToBSTR(char* str);
void FreeBSTR(BSTR bstr);
SAFEARRAY* ConvertCharArrayToSafeArray(TCHAR** arr);
void FreeSafeArray(SAFEARRAY* arr);
void ConvertBSTRToChar(BSTR bstr, char* str, int size);

template<class T>
class COMBase : public T {
public:
	COMBase() : ref(1) {}
	virtual ~COMBase() {}

	virtual HRESULT __stdcall QueryInterface(const IID& iid, void** ppv)
	{
		if(IsEqualIID(iid, IID_IUnknown) || IsEqualIID(iid, __uuidof(T))) {
			*ppv = static_cast<T*>(this) ; 
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
			PostMessage(NULL, WM_QUIT, 0, 0);
			return 0;
		}
		return ref;
	}

private:
	long ref;
};

template <class T>
class ClassFactoryBase : public IClassFactory 
{
public:
	ClassFactoryBase() : ref(1) {}
	virtual ~ClassFactoryBase() {}

	virtual HRESULT __stdcall CreateInstance(IUnknown* pUnknownOuter, const IID& iid, void** ppv) 
	{
		if (pUnknownOuter != NULL)	{
			return CLASS_E_NOAGGREGATION;
		}

		T* instance = new T;
		HRESULT hr = instance->QueryInterface(iid, ppv);
		instance->Release();
		return hr;
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
};

#endif // COMHELPER_H