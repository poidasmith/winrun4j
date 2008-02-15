/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Peter Smith
 *******************************************************************************/

#include "COMHelper.h"

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

SAFEARRAY* ConvertCharArrayToSafeArray(TCHAR** arr)
{
	int argCount = 0;
	while(arr[argCount]) argCount++;
	SAFEARRAY *pSA;
	SAFEARRAYBOUND dim[1];
	dim[0].lLbound = 0;
	dim[0].cElements = argCount;
	pSA = SafeArrayCreate(VT_BSTR, 1, dim);
	if(pSA != 0) {
		for(LONG i = 0; i < argCount; i++) {
			BSTR bstr = ConvertCharToBSTR(arr[i]);
			SafeArrayPutElement(pSA, &i, bstr);
		}
	} else {
		return 0;
	}

	return pSA;
}

TCHAR** ConvertSafeArrayToCharArray(SAFEARRAY* arr)
{
	LONG lb, ub, size;
	HRESULT hr1 = SafeArrayGetLBound(arr, 1, &lb);
	HRESULT hr2 = SafeArrayGetUBound(arr, 1, &ub);
	size = ub - lb + 1;
	TCHAR** carr = new TCHAR*[size + 1];
	for(LONG i = lb; i <= ub; i++) {
		BSTR bstr;
		HRESULT hr = SafeArrayGetElement(arr, &i, &bstr);
		carr[i] = ConvertBSTRToChar(bstr);
	}

	carr[size] = 0;
	return carr;
}

void FreeSafeArray(SAFEARRAY* arr)
{
	SafeArrayDestroy(arr);
}

char* ConvertBSTRToChar(BSTR bstr)
{
	UINT len = SysStringLen(bstr);
	char* str = new char[len];
	WideCharToMultiByte (CP_ACP, 0, bstr, -1, str, len, NULL, NULL);
	return str;
}
