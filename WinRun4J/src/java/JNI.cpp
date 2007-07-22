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
		Log::SetLastError("Could not find main class");
		return false;
	}
	
	jclass stringClass = env->FindClass("java/lang/String");
	if(stringClass == NULL) {
		Log::SetLastError("Could not find string class");
		return false;
	}

	// Count the args
	int argc = 0;
	while(progArgs[argc++] != NULL);

	// Create the run args
	jobjectArray args = env->NewObjectArray(argc, stringClass, NULL);
	for(int i = 0; i < argc; i++) {
		env->SetObjectArrayElement(args, i, env->NewStringUTF(progArgs[i]));
	}

	jmethodID mainMethod = env->GetMethodID(mainClass, "main", "([Ljava/lang/String;)V");
	if(mainMethod == NULL) {
		Log::SetLastError("Could not find main method.");
		return false;
	}

	env->CallStaticVoidMethod(mainClass, mainMethod, args);
	ClearJavaException(env);
}

const char* JNI::CallJavaStringMethod( JNIEnv* env, jclass clazz, jobject obj, char* name )
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

	jboolean iscopy = false;
	return env->GetStringUTFChars(str, &iscopy);
}

// Clear JNI exception
void JNI::ClearJavaException(JNIEnv* env)
{
	if(env && env->ExceptionOccurred()) {
		env->ExceptionDescribe();
		env->ExceptionClear();
	}
}
