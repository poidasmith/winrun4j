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
#include "Log.h"

static JavaVM *jvm = 0;
static JNIEnv *env = 0;

typedef jint (JNICALL *JNI_createJavaVM)(JavaVM **pvm, JNIEnv **env, void *args);

JNIEnv* JNI::GetJNIEnv()
{
	JNIEnv* env = 0;
	jvm->GetEnv((void **) &env, JNI_VERSION_1_1);
	return env;
}

jstring JNI::NewJavaString(JNIEnv *env, TCHAR * str)
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

int JNI::StartJavaVM( TCHAR* libPath, TCHAR* vmArgs[] )
{
	int i;
	int numVMArgs = -1;
	HMODULE jniLibrary;
	JNI_createJavaVM createJavaVM;
	JavaVMInitArgs init_args;
	JavaVMOption * options;
	
	jniLibrary = LoadLibrary(libPath);
	if(jniLibrary == NULL) {
		Log::Info("ERROR: Could not load library: %s\n", libPath);
		return -1; /*error*/
	}

	createJavaVM = (JNI_createJavaVM)GetProcAddress(jniLibrary, "JNI_CreateJavaVM");
	if(createJavaVM == NULL) {
		Log::Info("ERROR: Could not find JNI_CreateJavaVM function\n");
		return -1; /*error*/
	}
	
	/* count the vm args */
	while(vmArgs[++numVMArgs] != NULL) {}
	
	options = (JavaVMOption*) malloc((numVMArgs) * sizeof(JavaVMOption));
	for(i = 0; i < numVMArgs; i++){
		options[i].optionString = _strdup(vmArgs[i]);
		options[i].extraInfo = 0;
	}
		
	init_args.version = JNI_VERSION_1_2;
	init_args.options = options;
	init_args.nOptions = numVMArgs;
	init_args.ignoreUnrecognized = JNI_TRUE;
	
	int result = createJavaVM(&jvm, &env, &init_args);

	/* toNarrow allocated new strings, free them */
	for(i = 0; i < numVMArgs; i++){
		free( options[i].optionString );
	}
	free(options);

	return result;
}

int JNI::RunMainClass( TCHAR* mainClass, TCHAR* progArgs[] )
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

int JNI::CleanupVM() 
{
	if (jvm == 0 || env == 0)
		return 1;

	if (env->ExceptionOccurred()) 
	{
		env->ExceptionDescribe();
		env->ExceptionClear();
	}

	return jvm->DestroyJavaVM();
}