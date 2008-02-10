/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Peter Smith
 *******************************************************************************/

#ifndef JNI_CLIENT_H
#define JNI_CLIENT_H

#include "../common/Runtime.h"
#include <jni.h>

__declspec(dllexport) HRESULT WINAPI CreateJavaVM(TCHAR* libPath, TCHAR** vmArgs, JavaVM** jvm);

#endif // JNI_CLIENT_H