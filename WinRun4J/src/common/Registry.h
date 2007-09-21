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
	jlong OpenKey(JNIEnv* env, jobject self, jlong rootKey, jstring keyPath);
	void CloseKey(JNIEnv* env, jobject self, jlong handle);
	jobjectArray GetSubKeyNames(JNIEnv* env, jobject self, jlong handle);
	jobjectArray GetValueNames(JNIEnv* env, jobject self, jlong handle);
	jlong CreateSubKey(JNIEnv* env, jobject self, jlong handle, jstring name);
	jlong CreateValue(JNIEnv* env, jobject self, jlong handle, jstring name);
	void DeleteKey(JNIEnv* env, jobject self, jlong handle);
	void DeleteValue(JNIEnv* env, jobject self, jlong parent, jstring name);

	// Value methods
	jint GetType(JNIEnv* env, jobject self, jlong parent, jstring name);
	jstring GetString(JNIEnv* env, jobject self, jlong parent, jstring name);
	jarray GetBinary(JNIEnv* env, jobject self, jlong parent, jstring name);
	jlong GetDoubleWord(JNIEnv* env, jobject self, jlong parent, jstring name);
	jlong GetDoubleWordLittleEndian(JNIEnv* env, jobject self, jlong parent, jstring name);
	jlong GetDoubleWordBigEndian(JNIEnv* env, jobject self, jlong parent, jstring name);
	jstring GetExpandedString(JNIEnv* env, jobject self, jlong parent, jstring name);
	jobjectArray GetMultiString(JNIEnv* env, jobject self, jlong parent, jstring name);
	void SetString(JNIEnv* env, jobject self, jlong parent, jstring name, jstring value);
	void SetBinary(JNIEnv* env, jobject self, jlong parent, jstring name, jarray value);
	void SetDoubleWord(JNIEnv* env, jobject self, jlong parent, jstring name, jlong value);
	void SetDoubleWordLittleEndian(JNIEnv* env, jobject self, jlong parent, jstring name, jlong value);
	void SetDoubleWordBigEndian(JNIEnv* env, jobject self, jlong parent, jstring name, jlong value);
	void SetMultiString(JNIEnv* env, jobject self, jlong parent, jstring name, jobjectArray value);
};

#endif // REGISTRY_H