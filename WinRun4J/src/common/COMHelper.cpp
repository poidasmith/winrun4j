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
