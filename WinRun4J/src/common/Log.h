/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Peter Smith
 *******************************************************************************/

#ifndef LOG_H
#define LOG_H

#include "Runtime.h"
#ifndef NO_JAVA
#include <jni.h>
#endif

enum LoggingLevel { info = 0, warning = 1, error = 2, none = 3 };

struct Log {
	static void Init(HINSTANCE hInstance, const char* logfile, const char* loglevel);
	static void SetLevel(LoggingLevel level);
	static LoggingLevel GetLevel();
	static void Info(const char* format, ...);
	static void Warning(const char* format, ...);
	static void Error(const char* format, ...);
	static void Close();
	static void SetLastError(const char* format, ...);
	static const char* GetLastError();

#ifndef NO_JAVA
	// Used for JNI
	static bool RegisterNatives(JNIEnv* env);
#endif

private:
	static void LogIt(LoggingLevel loggingLevel, const char* marker, const char* format, va_list args);
	static void RedirectIOToConsole();

#ifndef NO_JAVA
	static void JNICALL LogJ(JNIEnv* env, jobject self, jint jlevel, jstring str);
	static void JNICALL SetLastErrorJ(JNIEnv* env, jobject self, jstring str);
	static jstring JNICALL GetLastErrorJ(JNIEnv* env, jobject self);
#endif
};

#endif // LOG_H