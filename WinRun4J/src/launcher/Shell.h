/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Peter Smith
 *******************************************************************************/

#ifndef SHELL_H
#define SHELL_H

#include "../common/Runtime.h"
#include "../common/Dictionary.h"
#include <jni.h>

class Shell {
public:
	static bool RegisterNatives(JNIEnv* env);
	static int CheckSingleInstance(dictionary* ini);
	
private:
	static jstring GetLogicalDrives(JNIEnv* env, jobject self);
	static jstring GetFolderPath(JNIEnv* env, jobject self, jint type);
	static jstring GetEnvironmentVariable(JNIEnv* env, jobject self, jstring name);
	static jobject GetEnvironmentStrings(JNIEnv* env, jobject self, jintArray arr);
	static void FreeEnvironmentStrings(JNIEnv* env, jobject self, jint p);
	static jstring ExpandEnvironmentString(JNIEnv* env, jobject self, jstring str);
	static jstring GetCommandLine(JNIEnv* env, jobject self);
	static jintArray GetOSVersionNumbers(JNIEnv* env, jobject self);
	static jstring GetOSVersionCSD(JNIEnv* env, jobject self);
};

#endif // SHELL_H