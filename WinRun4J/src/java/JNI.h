/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Peter Smith
 *******************************************************************************/

#ifndef JNI_UTILS_H
#define JNI_UTILS_H

#include "../common/Runtime.h"
#include <stdio.h>
#include <string.h>
#include <jni.h>

class JNI 
{
public:
	static void Init(JNIEnv* env);
	static void ClearException(JNIEnv* env);
	static jthrowable PrintStackTrace(JNIEnv* env);
	static bool RunMainClass( JNIEnv* env, TCHAR* mainClass, TCHAR* progArgs[] );
	static char* CallStringMethod(JNIEnv* env, jclass clazz, jobject obj, char* name);
	static const bool CallBooleanMethod(JNIEnv* env, jclass clazz, jobject obj, char* name);
	static jclass FindClass(JNIEnv* env, TCHAR* mainClassStr);

private:
	static jstring NewString(JNIEnv *env, TCHAR * str);
	static jobjectArray CreateRunArgs(JNIEnv *env, TCHAR* args[]);
	static void LoadEmbeddedClassloader(JNIEnv* env);
	static jobjectArray ListJars(JNIEnv* env, jobject self, jstring library);
	static jobject GetJar(JNIEnv* env, jobject self, jstring library, jstring jarName);
	static jclass DefineClass(JNIEnv* env, const char* filename, const char* name, jobject loader);
	static bool SetClassLoaderJars(JNIEnv* env, jobject classloader);
};
#endif // JNI_UTILS_H
