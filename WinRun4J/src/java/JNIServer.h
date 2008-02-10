/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Peter Smith
 *******************************************************************************/

#ifndef JNI_SERVER_H
#define JNI_SERVER_H

#include "../common/Runtime.h"
#include <ComDef.h>

// The CLSID
// {05FE15B4-AB13-4064-A22A-1A816059B6AC}
static const GUID CJNIServer = { 0x5fe15b4, 0xab13, 0x4064, { 0xa2, 0x2a, 0x1a, 0x81, 0x60, 0x59, 0xb6, 0xac } };

// The IID
// {05FE15B4-AB13-4064-A22A-1A816059B6AD}
struct __declspec(uuid("05FE15B4-AB13-4064-A22A-1A816059B6AD")) __declspec(novtable) IJNIServer : public IUnknown
{
	virtual HRESULT __stdcall Test(long *pVal) = 0;
};

#endif // JNI_SERVER_H