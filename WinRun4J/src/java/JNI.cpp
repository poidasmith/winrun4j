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

// Use to store a reference to our embedded classloader (if required)
static jclass g_classLoaderClass = NULL;
static jobject g_classLoader = NULL;
static jmethodID g_findClassMethod = NULL;

// Cache handles to class class
static jclass CLASS_CLASS;
static jmethodID CLASS_GETCTORS_METHOD;

// The java code for the EmbeddedClassLoader - used to load classes
// from jars embedded inside executabless
#include "EmbeddedClasses.cpp"

void JNI::Init(JNIEnv* env)
{
	// Cache handles to class class
	jclass c = env->FindClass("java/lang/Class");
	if(!c) {
		Log::Error("Could not find Class class");
		return;
	}
	CLASS_CLASS = (jclass) env->NewGlobalRef(c);
	CLASS_GETCTORS_METHOD = env->GetMethodID(CLASS_CLASS, "getConstructors", "()[Ljava/lang/reflect/Constructor;");
	if(!CLASS_GETCTORS_METHOD) {
		Log::Error("Could not find Class.getConstructors method");
		return;
	}

	// Simply attempt to load the embedded classloader if required
	LoadEmbeddedClassloader(env);
}

jclass JNI::FindClass(JNIEnv* env, TCHAR* classStr)
{
	if(g_classLoader == NULL) {
		return env->FindClass(classStr);
	}

	TCHAR t[1024];
	strcpy(t, classStr);
	int len = strlen(classStr);
	for(int i = 0; i < len; i++) 
		if(t[i] == '/') t[i] = '.';

	jclass cl = (jclass) env->CallObjectMethod(g_classLoader, g_findClassMethod, env->NewStringUTF(t));
	// Workaround for bug in sun 1.6 VMs
	if(cl && CLASS_GETCTORS_METHOD) {
		env->CallObjectMethod(cl, CLASS_GETCTORS_METHOD);
	}
	return cl;
}

bool JNI::RunMainClass( JNIEnv* env, TCHAR* mainClassStr, TCHAR* progArgs[] )
{
	if(!mainClassStr) {
		Log::Error("No main class specified");
		return false;
	}

	jclass mainClass = FindClass(env, mainClassStr);

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

char* JNI::CallStringMethod(JNIEnv* env, jclass clazz, jobject obj, char* name)
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
	const char* chars = str ? env->GetStringUTFChars(str, &iscopy) : 0;
	char* tmp = strdup(chars);
	env->ReleaseStringUTFChars(str, chars);
	return tmp;
}

const bool JNI::CallBooleanMethod(JNIEnv* env, jclass clazz, jobject obj, char* name)
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
	if(!env) return NULL;
	jthrowable thr = env->ExceptionOccurred();
	if(thr) {
		// Print out the stack trace for this exception
		jclass c = env->GetObjectClass(thr);
		jmethodID m = env->GetMethodID(c, "printStackTrace", "()V");
		if(m) 
			env->CallVoidMethod(thr, m);
		else {
			env->ExceptionClear();
			m = env->GetMethodID(c, "printStackTrace", "(Ljava/io/PrintStream;)V");
			jclass sc = env->FindClass("java/lang/System");
			jfieldID sof = env->GetStaticFieldID(sc, "out", "Ljava/io/PrintStream;");
			jobject so = env->GetStaticObjectField(sc, sof);
			env->CallVoidMethod(thr, m, so);
		}
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

jobjectArray JNI::ListJars(JNIEnv* env, jobject self, jstring library)
{
	HMODULE hm = NULL;
	if(library) {
		jboolean iscopy = false;
		const char* c = library ? env->GetStringUTFChars(library, &iscopy) : 0;
		hm = LoadLibrary(c);
		if(!hm)
			return NULL;
	}

	int resId = 1;
	HRSRC hs;
	while((hs = FindResource(hm, MAKEINTRESOURCE(resId), RT_JAR_FILE)) != NULL) {
		resId++;
	}
	jclass c = env->FindClass("java/lang/String");
	jobjectArray a = env->NewObjectArray(resId-1, c, 0);
	for(int i = 1; i < resId; i++) {
		hs = FindResource(hm, MAKEINTRESOURCE(i), RT_JAR_FILE);
		HGLOBAL hg = LoadResource(hm, hs);
		LPBYTE pb = (LPBYTE) LockResource(hg);
		DWORD* pd = (DWORD*) pb;
		if(*pd == JAR_RES_MAGIC) {
			const char* n = (const char *) &pb[RES_MAGIC_SIZE];
			env->SetObjectArrayElement(a, i-1, env->NewStringUTF(n));
		}
	}
	return a;
}

jobject JNI::GetJar(JNIEnv* env, jobject self, jstring library, jstring jarName)
{
	HMODULE hm = NULL;
	if(library) {
		jboolean iscopy = false;
		const char* c = library ?env->GetStringUTFChars(library, &iscopy) : 0; 
		hm = LoadLibrary(c);
		if(!hm)
			return NULL;
	}

	if(!jarName)
		return NULL;

	jboolean iscopy = false;
	const char* jn = jarName ? env->GetStringUTFChars(jarName, &iscopy) : 0;

	int resId = 1;
	HRSRC hs;
	while((hs = FindResource(hm, MAKEINTRESOURCE(resId), RT_JAR_FILE)) != NULL) {
		HGLOBAL hg = LoadResource(hm, hs);
		PBYTE pb = (PBYTE) LockResource(hg);
		DWORD* pd = (DWORD*) pb;
		if(*pd == JAR_RES_MAGIC) {
			int len = strlen((char*) &pb[RES_MAGIC_SIZE]);
			if(strcmp(jn, (char*) &pb[RES_MAGIC_SIZE]) == 0) {
				DWORD offset = RES_MAGIC_SIZE + len + 1;
				DWORD s = SizeofResource(NULL, hs);
				return env->NewDirectByteBuffer(&pb[offset], s - offset);
			}
		}
		resId++;
	}

	return NULL;
}

jclass JNI::DefineClass(JNIEnv* env, const char* filename, const char* name, jobject loader) 
{
	// Read in file from temp source
	HANDLE hFile = CreateFile(filename, GENERIC_READ, FILE_SHARE_READ, NULL, OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL, NULL);
	DWORD cbBuffer = GetFileSize(hFile, 0);
	PBYTE pBuffer = (PBYTE) malloc(cbBuffer);
	ReadFile(hFile, pBuffer, cbBuffer, &cbBuffer, 0);
	CloseHandle(hFile);
	jclass cl = env->DefineClass(name, loader, (const jbyte*) pBuffer, cbBuffer);
	free(pBuffer);
	return cl;
}

void JNI::LoadEmbeddedClassloader(JNIEnv* env)
{
	// First we check if there are any embedded jars
	if(!FindResource(NULL, MAKEINTRESOURCE(1), RT_JAR_FILE))
		return;

	// We need to grab a reference to the system clasloader via the 
	// ClassLoader.getSystemClassLoader method
	jclass loaderClass = env->FindClass("java/lang/ClassLoader"); 
	if(!loaderClass) {
		Log::Error("Could not access classloader");
		return;
	}

	jmethodID loaderMethod = env->GetStaticMethodID(loaderClass, "getSystemClassLoader", "()Ljava/lang/ClassLoader;");
	if(!loaderMethod) {
		Log::Error("Could not access classloader method");
		return;
	}

	// Grab the system loader and create a global ref
	jobject loader = env->CallStaticObjectMethod(loaderClass, loaderMethod);
	loader = env->NewGlobalRef(loader);

	// Load class from static memory
	jclass bb = env->DefineClass("org/boris/winrun4j/classloader/ByteBufferInputStream", loader, (const jbyte*) g_byteBufferISCode, sizeof(g_byteBufferISCode));
	jclass cl = env->DefineClass("org/boris/winrun4j/classloader/EmbeddedClassLoader", loader, (const jbyte*) g_classLoaderCode, sizeof(g_classLoaderCode));

	/*
	jclass bb = DefineClass(env, "F:/eclipse/workspace/org.boris.winrun4j.classloader/bin/org/boris/winrun4j/classloader/ByteBufferInputStream.class",
		"org/boris/winrun4j/classloader/ByteBufferInputStream", loader);
	jclass cl = DefineClass(env, "F:/eclipse/workspace/org.boris.winrun4j.classloader/bin/org/boris/winrun4j/classloader/EmbeddedClassLoader.class",
		"org/boris/winrun4j/classloader/EmbeddedClassLoader", loader);
	*/

	if(!cl) {
		PrintStackTrace(env);
		Log::Error("Could not load embedded classloader");
		return;
	}

	g_classLoaderClass = (jclass) env->NewGlobalRef(cl);

	// Workaround for JDK bug
	env->CallObjectMethod(g_classLoaderClass, CLASS_GETCTORS_METHOD);

	// Now link in native methods
	JNINativeMethod m[2];
	m[0].fnPtr = ListJars;
	m[0].name = "listJars";
	m[0].signature = "(Ljava/lang/String;)[Ljava/lang/String;";
	m[1].fnPtr = GetJar;
	m[1].name = "getJar";
	m[1].signature = "(Ljava/lang/String;Ljava/lang/String;)Ljava/nio/ByteBuffer;";
	env->RegisterNatives(g_classLoaderClass, m, 2);
	if(env->ExceptionCheck()) {
		Log::Error("Could not register classloader native methods");
		return;
	}

	jmethodID ctor = env->GetMethodID(g_classLoaderClass, "<init>", "()V");
	if(!ctor) {
		Log::Error("Could not access classloader constructor");
		return;
	}

	jobject o = env->NewObject(g_classLoaderClass, ctor);
	if(!o) {
		PrintStackTrace(env);
		Log::Error("Could not create classloader instance");
		return;
	}

	g_classLoader = env->NewGlobalRef(o);

	// Grab a reference to the find class method
	g_findClassMethod = env->GetMethodID(g_classLoaderClass, "findClass", "(Ljava/lang/String;)Ljava/lang/Class;");
	if(!g_findClassMethod) {
		PrintStackTrace(env);
		Log::Error("Could not access find ClassLoader.findClass method");
		g_classLoader = NULL;
		return;
	}
}
