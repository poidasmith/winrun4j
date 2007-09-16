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
	jlong CreateKeyHandle(JNIEnv* env, jobject self, jstring keyPath);
	jarray GetSubKeys(JNIEnv* env, jobject self, jlong handle);
	jlong GetSubKey(JNIEnv* env, jobject self, jlong handle, jstring name);
	jarray GetValues(JNIEnv* env, jobject self, jlong handle);
	jlong GetValue(JNIEnv* env, jobject self, jlong handle, jstring name);
	jstring GetKeyName(JNIEnv* env, jobject self, jlong handle);
	jlong GetParent(JNIEnv* env, jobject self, jlong handle);
	jlong CreateSubKey(JNIEnv* env, jobject self, jlong handle, jstring name);
	jlong CreateValue(JNIEnv* env, jobject self, jlong handle, jstring name);
	void DeleteKey(JNIEnv* env, jobject self, jlong handle);

	// Value methods
	jstring GetValueName(JNIEnv* env, jobject self, jlong parent, jlong handle);
	jint GetType(JNIEnv* env, jobject self, jlong parent, jlong handle);
	void DeleteValue(JNIEnv* env, jobject self, jlong parent, jlong handle);
	jstring GetString(JNIEnv* env, jobject self, jlong parent, jlong handle);
	jarray GetBinary(JNIEnv* env, jobject self, jlong parent, jlong handle);
	jlong GetDoubleWord(JNIEnv* env, jobject self, jlong parent, jlong handle);
	jlong GetDoubleWordLittleEndian(JNIEnv* env, jobject self, jlong parent, jlong handle);
	jlong GetDoubleWordBigEndian(JNIEnv* env, jobject self, jlong parent, jlong handle);
	jstring GetExpandedString(JNIEnv* env, jobject self, jlong parent, jlong handle);
	jobjectArray GetMultiString(JNIEnv* env, jobject self, jlong parent, jlong handle);
	void SetString(JNIEnv* env, jobject self, jlong parent, jlong handle, jstring value);
	void SetBinary(JNIEnv* env, jobject self, jlong parent, jlong handle, jarray value);
	void SetDoubleWord(JNIEnv* env, jobject self, jlong parent, jlong handle, jlong value);
	void SetDoubleWordLittleEndian(JNIEnv* env, jobject self, jlong parent, jlong handle, jlong value);
	void SetDoubleWordBigEndian(JNIEnv* env, jobject self, jlong parent, jlong handle, jlong value);
	void SetMultiString(JNIEnv* env, jobject self, jlong parent, jlong handle, jobjectArray value);
};

#endif // REGISTRY_H