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

// The java code for the EmbeddedClassLoader - used to load classes
// from jars embedded inside executabless
static BYTE g_classLoader[] = {
    0xca, 0xfe, 0xba, 0xbe, 0x00, 0x00, 0x00, 0x2e, 0x00, 0x0a, 
    0x07, 0x00, 0x02, 0x01, 0x00, 0x32, 0x6f, 0x72, 0x67, 0x2f, 
    0x62, 0x6f, 0x72, 0x69, 0x73, 0x2f, 0x77, 0x69, 0x6e, 0x72, 
    0x75, 0x6e, 0x34, 0x6a, 0x2f, 0x63, 0x6c, 0x61, 0x73, 0x73, 
    0x6c, 0x6f, 0x61, 0x64, 0x65, 0x72, 0x2f, 0x45, 0x6d, 0x62, 
    0x65, 0x64, 0x64, 0x65, 0x64, 0x43, 0x6c, 0x61, 0x73, 0x73, 
    0x4c, 0x6f, 0x61, 0x64, 0x65, 0x72, 0x07, 0x00, 0x04, 0x01, 
    0x00, 0x15, 0x6a, 0x61, 0x76, 0x61, 0x2f, 0x6c, 0x61, 0x6e, 
    0x67, 0x2f, 0x43, 0x6c, 0x61, 0x73, 0x73, 0x4c, 0x6f, 0x61, 
    0x64, 0x65, 0x72, 0x01, 0x00, 0x06, 0x3c, 0x69, 0x6e, 0x69, 
    0x74, 0x3e, 0x01, 0x00, 0x03, 0x28, 0x29, 0x56, 0x01, 0x00, 
    0x04, 0x43, 0x6f, 0x64, 0x65, 0x0a, 0x00, 0x03, 0x00, 0x09, 
    0x0c, 0x00, 0x05, 0x00, 0x06, 0x00, 0x21, 0x00, 0x01, 0x00, 
    0x03, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x01, 0x00, 
    0x05, 0x00, 0x06, 0x00, 0x01, 0x00, 0x07, 0x00, 0x00, 0x00, 
    0x11, 0x00, 0x01, 0x00, 0x01, 0x00, 0x00, 0x00, 0x05, 0x2a, 
    0xb7, 0x00, 0x08, 0xb1, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
};

bool JNI::RunMainClass( JNIEnv* env, TCHAR* mainClassStr, TCHAR* progArgs[] )
{
	jclass mainClass = env->FindClass(mainClassStr);
	if(mainClass == NULL) {
		Log::Error("Could not find main class");
		return false;
	}
	
	jclass stringClass = env->FindClass("java/lang/String");
	if(stringClass == NULL) {
		Log::Error("Could not find String class");
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

	PrintStackTrace(env);
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
		JNI::PrintStackTrace(env);
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

// Dump stack trace for exception (if present)
jthrowable JNI::PrintStackTrace(JNIEnv* env)
{
	jthrowable thr = env->ExceptionOccurred();
	if(thr) {
		// Print out the stack trace for this exception
		jclass c = env->GetObjectClass(thr);
		jmethodID m = env->GetMethodID(c, "printStackTrace", "()V");
		env->CallVoidMethod(thr, m);
		env->ExceptionClear();
	}
	return thr;
}

// Clear JNI exception
void JNI::ClearException(JNIEnv* env)
{
	if(env && env->ExceptionOccurred()) {
		env->ExceptionClear();
	}
}
