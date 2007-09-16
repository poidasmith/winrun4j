/*******************************************************************************
* This program and the accompanying materials
* are made available under the terms of the Common Public License v1.0
* which accompanies this distribution, and is available at 
* http://www.eclipse.org/legal/cpl-v10.html
* 
* Contributors:
*     Peter Smith
*******************************************************************************/

#include "Registry.h"
#include "Log.h"

jlong Registry::CreateKeyHandle(JNIEnv* env, jobject self, jstring keyPath)
{
	return 0;
}

jarray Registry::GetSubKeys(JNIEnv* env, jobject self, jlong handle)
{
	return 0;
}

jlong Registry::GetSubKey(JNIEnv* env, jobject self, jlong handle, jstring name)
{
	return 0;
}

jarray Registry::GetValues(JNIEnv* env, jobject self, jlong handle)
{
	return 0;
}

jlong Registry::GetValue(JNIEnv* env, jobject self, jlong handle, jstring name)
{
	return 0;
}

jstring Registry::GetKeyName(JNIEnv* env, jobject self, jlong handle)
{
	return 0;
}

jlong Registry::GetParent(JNIEnv* env, jobject self, jlong handle)
{
	return 0;
}

jlong Registry::CreateSubKey(JNIEnv* env, jobject self, jlong handle, jstring name)
{
	return 0;
}

jlong Registry::CreateValue(JNIEnv* env, jobject self, jlong handle, jstring name)
{
	return 0;
}

void Registry::DeleteKey(JNIEnv* env, jobject self, jlong handle)
{
}

jstring Registry::GetValueName(JNIEnv* env, jobject self, jlong parent, jlong handle)
{
	return 0;
}

jint Registry::GetType(JNIEnv* env, jobject self, jlong parent, jlong handle)
{
	return 0;
}

void Registry::DeleteValue(JNIEnv* env, jobject self, jlong parent, jlong handle)
{
}

jstring Registry::GetString(JNIEnv* env, jobject self, jlong parent, jlong handle)
{
	return 0;
}

jarray Registry::GetBinary(JNIEnv* env, jobject self, jlong parent, jlong handle)
{
	return 0;
}

jlong Registry::GetDoubleWord(JNIEnv* env, jobject self, jlong parent, jlong handle)
{
	return 0;
}

jlong Registry::GetDoubleWordLittleEndian(JNIEnv* env, jobject self, jlong parent, jlong handle)
{
	return 0;
}

jlong Registry::GetDoubleWordBigEndian(JNIEnv* env, jobject self, jlong parent, jlong handle)
{
	return 0;
}

jstring Registry::GetExpandedString(JNIEnv* env, jobject self, jlong parent, jlong handle)
{
	return 0;
}

jobjectArray Registry::GetMultiString(JNIEnv* env, jobject self, jlong parent, jlong handle)
{
	return 0;
}

void Registry::SetString(JNIEnv* env, jobject self, jlong parent, jlong handle, jstring value)
{
}

void Registry::SetBinary(JNIEnv* env, jobject self, jlong parent, jlong handle, jarray value)
{
}

void Registry::SetDoubleWord(JNIEnv* env, jobject self, jlong parent, jlong handle, jlong value)
{
}

void Registry::SetDoubleWordLittleEndian(JNIEnv* env, jobject self, jlong parent, jlong handle, jlong value)
{
}

void Registry::SetDoubleWordBigEndian(JNIEnv* env, jobject self, jlong parent, jlong handle, jlong value)
{
}

void Registry::SetMultiString(JNIEnv* env, jobject self, jlong parent, jlong handle, jobjectArray value)
{
}

bool Registry::RegisterNatives(JNIEnv *env)
{
	return true;
}
