/*******************************************************************************
* This program and the accompanying materials
* are made available under the terms of the Common Public License v1.0
* which accompanies this distribution, and is available at 
* http://www.eclipse.org/legal/cpl-v10.html
* 
* Contributors:
*     Peter Smith
*******************************************************************************/

#include "JNI.h"
#include "../common/Log.h"

bool JNI::RunMainClass( JNIEnv* env, TCHAR* mainClassStr, TCHAR* progArgs[] )
{
	jclass mainClass = env->FindClass(mainClassStr);
	if(mainClass == NULL) {
		Log::Error("Could not find main class");
		return false;
	}
	
	jclass stringClass = env->FindClass("java/lang/String");
	if(stringClass == NULL) {
		Log::Error("Could not find string class");
		return false;
	}

	// Count the args
	int argc = 0;
	while(progArgs[argc++] != NULL);

	// Create the run args
	jobjectArray args = env->NewObjectArray(argc - 1, stringClass, NULL);
	for(int i = 0; i < argc - 1; i++) {
		env->SetObjectArrayElement(args, i, env->NewStringUTF(progArgs[i]));
	}

	jmethodID mainMethod = env->GetStaticMethodID(mainClass, "main", "([Ljava/lang/String;)V");
	if(mainMethod == NULL) {
		Log::Error("Could not find main method.");
		return false;
	}

	env->CallStaticVoidMethod(mainClass, mainMethod, args);
	ClearException(env);

	return true;
}

char* JNI::CallStringMethod( JNIEnv* env, jclass clazz, jobject obj, char* name )
{
	jmethodID methodID = env->GetMethodID(clazz, name, "()Ljava/lang/String;");
	if(methodID == NULL) {
		Log::SetLastError("Could not find '%s' method", name);
		return NULL;
	}

	jstring str = (jstring) env->CallObjectMethod(obj, methodID);
	if(str == NULL) {
		return NULL;
	}

	if(env->ExceptionCheck()) {
		Log::SetLastError("Error calling %s method: %s", name, GetExceptionMessage(env));
		return NULL;
	}

	jboolean iscopy = false;
	const char* chars = env->GetStringUTFChars(str, &iscopy);
	char* tmp = strdup(chars);
	env->ReleaseStringUTFChars(str, chars);
	return tmp;
}

const bool JNI::CallBooleanMethod( JNIEnv* env, jclass clazz, jobject obj, char* name )
{
	jmethodID methodID = env->GetMethodID(clazz, name, "()Z");
	if(methodID == NULL) {
		Log::SetLastError("Could not find '%s' method", name);
		return NULL;
	}

	return env->CallBooleanMethod(obj, methodID);
}

// Clear JNI exception
void JNI::ClearException(JNIEnv* env)
{
	if(env && env->ExceptionOccurred()) {
		env->ExceptionDescribe();
		env->ExceptionClear();
	}
}

char* JNI::GetExceptionMessage(JNIEnv* env)
{
	jthrowable thr = env->ExceptionOccurred();
	env->ExceptionClear();
	if(thr != NULL) {
		return CallStringMethod(env, env->GetObjectClass(thr), thr, "getMessage");
	}
}
