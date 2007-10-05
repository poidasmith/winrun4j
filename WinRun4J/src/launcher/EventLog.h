/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Peter Smith
 *******************************************************************************/

#ifndef EVENTLOG_H
#define EVENTLOG_H

#include "../common/Runtime.h"
#include <jni.h>

class EventLog {
public:
	static bool RegisterNatives(JNIEnv* env);

private:
	static bool Report(JNIEnv* env, jobject self, jstring source, jint type, jstring msg);
};

#endif // EVENTLOG_H