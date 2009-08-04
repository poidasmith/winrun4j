/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Peter Smith
 *******************************************************************************/

#ifndef NATIVE_H
#define NATIVE_H

#include "../common/Runtime.h"
#include <jni.h>

class Native {
public:
	static bool RegisterNatives(JNIEnv* env);

private:
};

#endif // EVENTLOG_H