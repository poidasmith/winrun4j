/*******************************************************************************
* This program and the accompanying materials
* are made available under the terms of the Common Public License v1.0
* which accompanies this distribution, and is available at 
* http://www.eclipse.org/legal/cpl-v10.html
* 
* Contributors:
*     Peter Smith
*******************************************************************************/

#include "Native.h"
#include "../common/Log.h"
#include "../java/JNI.h"

bool Native::RegisterNatives(JNIEnv *env)
{
	Log::Info("Registering natives for Native class");

	return true;
}

