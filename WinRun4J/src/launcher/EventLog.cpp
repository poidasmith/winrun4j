/*******************************************************************************
* This program and the accompanying materials
* are made available under the terms of the Common Public License v1.0
* which accompanies this distribution, and is available at 
* http://www.eclipse.org/legal/cpl-v10.html
* 
* Contributors:
*     Peter Smith
*******************************************************************************/

#include "EventLog.h"
#include "../common/Log.h"
#include "../java/JNI.h"

bool EventLog::RegisterNatives(JNIEnv *env)
{
	Log::Info("Registering natives for EventLog class");
	jclass clazz = JNI::FindClass(env, "org/boris/winrun4j/EventLog");
	if(clazz == NULL) {
		Log::Warning("Could not find EventLog class");
		if(env->ExceptionCheck())
			env->ExceptionClear();
		return false;
	}

	JNINativeMethod methods[1];
	methods[0].fnPtr = (void*) Report;
	methods[0].name = "report";
	methods[0].signature = "(Ljava/lang/String;ILjava/lang/String;)Z";
	env->RegisterNatives(clazz, methods, 1);
	if(env->ExceptionCheck()) {
		JNI::PrintStackTrace(env);
		env->ExceptionClear();
	}

	return true;
}

bool EventLog::Report(JNIEnv* env, jobject self, jstring source, jint type, jstring msg)
{
	if(source == NULL || msg == NULL)
		return false;

	jboolean iscopy = false;
	const char* src = source ? env->GetStringUTFChars(source, &iscopy) : 0;
	const char* m = msg ? env->GetStringUTFChars(msg, &iscopy) : 0;

	HANDLE h = RegisterEventSource(0, src);
	if(h == NULL) {
		return false;
	}

	return ReportEvent(h, type, 0, 0, 0, 0, strlen(m), 0, (void *) m);
}
