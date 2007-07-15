/* 
	Code borrowed from eclipse-launcher and tweaked (heavily).... (this code is licensed as EPL).
*/

/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * 	   Andrew Niefer
 *******************************************************************************/

#include "JNI.h"
#include "../common/Log.h"

jstring JNI::NewJavaString( JNIEnv *env, TCHAR * str )
{
	jstring newString = NULL;
	int length = strlen(str);
	
#ifdef UNICODE
	newString = env->NewString(str, length);
#else
	jbyteArray bytes = env->NewByteArray(length);
	if(bytes != NULL) {
		env->SetByteArrayRegion(bytes, 0, length, (const jbyte *) str);
		if (!env->ExceptionOccurred()) {
			jclass stringClass = env->FindClass("java/lang/String");
			if(stringClass != NULL) {
				jmethodID ctor = env->GetMethodID(stringClass, "<init>",  "([B)V");
				if(ctor != NULL) {
					newString = (jstring) env->NewObject(stringClass, ctor, bytes);
				}
			}
		}
		env->DeleteLocalRef(bytes);
	}
#endif
	if(newString == NULL) {
		env->ExceptionDescribe();
		env->ExceptionClear();
	}
	return newString;
}

jobjectArray JNI::CreateRunArgs( JNIEnv *env, TCHAR * args[] ) 
{
	int index = 0, length = -1;
	jclass stringClass;
	jobjectArray stringArray = NULL;
	jstring string;
	
	/*count the number of elements first*/
	while(args[++length] != NULL);
	
	stringClass = env->FindClass("java/lang/String");
	if(stringClass != NULL) {
		stringArray = env->NewObjectArray(length, stringClass, 0);
		if(stringArray != NULL) {
			for( index = 0; index < length; index++) {
				string = NewJavaString(env, args[index]);
				if(string != NULL) {
					env->SetObjectArrayElement(stringArray, index, string); 
					env->DeleteLocalRef(string);
				} else {
					env->DeleteLocalRef(stringArray);
					env->ExceptionDescribe();
					env->ExceptionClear();
					return NULL;
				}
			}
		}
	} 
	if(stringArray == NULL) {
		env->ExceptionDescribe();
		env->ExceptionClear();
	}
	return stringArray;
}


int JNI::RunMainClass( JNIEnv* env, TCHAR* mainClass, TCHAR* progArgs[] )
{
	/* JNI reflection */
	jclass mainClassCls = NULL;			/* The Main class to load */
	jmethodID runMethod = NULL;			/* Main.run(String[]) */
	jobjectArray methodArgs = NULL;		/* Arguments to pass to run */

	mainClassCls = env->FindClass(mainClass);
	if(mainClassCls != NULL) {
		runMethod = env->GetStaticMethodID(mainClassCls, "main", "([Ljava/lang/String;)V");
		if(runMethod != NULL) {
			methodArgs = CreateRunArgs(env, progArgs);
			if(methodArgs != NULL) {
				env->CallStaticVoidMethod(mainClassCls, runMethod, methodArgs);
				env->DeleteLocalRef(methodArgs);
			}
		}
	} 
	if(env->ExceptionOccurred())
	{
		env->ExceptionDescribe();
		env->ExceptionClear();
	}

	return 0;
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
	if(env && env->ExceptionCheck()) {
		env->ExceptionClear();
	}
}
