/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Peter Smith
 *******************************************************************************/

#ifndef REGISTRY_H
#define REGISTRY_H

#include "Runtime.h"
#include "INI.h"

class Registry {
public:
	static bool RegisterNatives(JNIEnv* env);

private:
	// Key methods
	static jlong OpenKey(JNIEnv* env, jobject self, jlong rootKey, jstring keyPath);
	static void CloseKey(JNIEnv* env, jobject self, jlong handle);
	static jobjectArray GetSubKeyNames(JNIEnv* env, jobject self, jlong handle);
	static jobjectArray GetValueNames(JNIEnv* env, jobject self, jlong handle);
	static jlong CreateSubKey(JNIEnv* env, jobject self, jlong handle, jstring name);
	static jlong CreateValue(JNIEnv* env, jobject self, jlong handle, jstring name);
	static void DeleteKey(JNIEnv* env, jobject self, jlong handle);
	static void DeleteValue(JNIEnv* env, jobject self, jlong parent, jstring name);

	// Value methods
	static jint GetType(JNIEnv* env, jobject self, jlong parent, jstring name);
	static jstring GetString(JNIEnv* env, jobject self, jlong parent, jstring name);
	static jarray GetBinary(JNIEnv* env, jobject self, jlong parent, jstring name);
	static jlong GetDoubleWord(JNIEnv* env, jobject self, jlong parent, jstring name);
	static jlong GetDoubleWordLittleEndian(JNIEnv* env, jobject self, jlong parent, jstring name);
	static jlong GetDoubleWordBigEndian(JNIEnv* env, jobject self, jlong parent, jstring name);
	static jstring GetExpandedString(JNIEnv* env, jobject self, jlong parent, jstring name);
	static jobjectArray GetMultiString(JNIEnv* env, jobject self, jlong parent, jstring name);
	static void SetString(JNIEnv* env, jobject self, jlong parent, jstring name, jstring value);
	static void SetBinary(JNIEnv* env, jobject self, jlong parent, jstring name, jarray value);
	static void SetDoubleWord(JNIEnv* env, jobject self, jlong parent, jstring name, jlong value);
	static void SetDoubleWordLittleEndian(JNIEnv* env, jobject self, jlong parent, jstring name, jlong value);
	static void SetDoubleWordBigEndian(JNIEnv* env, jobject self, jlong parent, jstring name, jlong value);
	static void SetMultiString(JNIEnv* env, jobject self, jlong parent, jstring name, jobjectArray value);
};

#endif // REGISTRY_H