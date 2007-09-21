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

jlong Registry::OpenKey(JNIEnv* env, jobject self, jlong rootKey, jstring subKey)
{
	jboolean iscopy = false;
	const char* sk = subKey == NULL ? 0 : env->GetStringUTFChars(subKey, &iscopy);
	HKEY key;

	LONG result = RegOpenKeyEx((HKEY) rootKey, sk, 0, KEY_ALL_ACCESS, &key);

	if(subKey) env->ReleaseStringUTFChars(subKey, sk);

	if(result == ERROR_SUCCESS) {
		return (jlong) key;	
	} else {
		return 0;
	}
}

void Registry::CloseKey(JNIEnv* env, jobject self, jlong handle)
{
	RegCloseKey((HKEY) handle);
}

jobjectArray Registry::GetSubKeyNames(JNIEnv* env, jobject self, jlong handle)
{
	DWORD keyCount = 0;
	LONG result = RegQueryInfoKey((HKEY) handle, NULL, NULL, NULL, &keyCount, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
	if(result != ERROR_SUCCESS) {
		return NULL;
	}

	char tmp[MAX_PATH];

	jclass clazz = env->FindClass("java/lang/String");
	jobjectArray arr = env->NewObjectArray(keyCount, clazz, NULL);

	for(int i = 0; i < keyCount; i++) {
		DWORD size = MAX_PATH;
		RegEnumKeyEx((HKEY) handle, i, tmp, &size, 0, 0, 0, 0);	
		env->SetObjectArrayElement(arr, i, env->NewStringUTF(tmp));
	}

	return arr;
}

jobjectArray Registry::GetValueNames(JNIEnv* env, jobject self, jlong handle)
{
	DWORD valueCount = 0;
	LONG result = RegQueryInfoKey((HKEY) handle, NULL, NULL, NULL, NULL, NULL, &valueCount, NULL, NULL, NULL, NULL, NULL);
	if(result != ERROR_SUCCESS) {
		return NULL;
	}

	char tmp[MAX_PATH];

	jclass clazz = env->FindClass("java/lang/String");
	jobjectArray arr = env->NewObjectArray(valueCount, clazz, NULL);

	for(int i = 0; i < valueCount; i++) {
		DWORD size = MAX_PATH;
		RegEnumValue((HKEY) handle, i, tmp, &size, 0, 0, 0, 0);	
		env->SetObjectArrayElement(arr, i, env->NewStringUTF(tmp));
	}

	return arr;
}

void Registry::DeleteKey(JNIEnv* env, jobject self, jlong handle)
{
}

void Registry::DeleteValue(JNIEnv* env, jobject self, jlong parent, jstring name)
{
}

jint Registry::GetType(JNIEnv* env, jobject self, jlong parent, jstring name)
{
	return 0;
}

jstring Registry::GetString(JNIEnv* env, jobject self, jlong parent, jstring name)
{
	return 0;
}

jarray Registry::GetBinary(JNIEnv* env, jobject self, jlong parent, jstring name)
{
	return 0;
}

jlong Registry::GetDoubleWord(JNIEnv* env, jobject self, jlong parent, jstring name)
{
	return 0;
}

jlong Registry::GetDoubleWordLittleEndian(JNIEnv* env, jobject self, jlong parent, jstring name)
{
	return 0;
}

jlong Registry::GetDoubleWordBigEndian(JNIEnv* env, jobject self, jlong parent, jstring name)
{
	return 0;
}

jstring Registry::GetExpandedString(JNIEnv* env, jobject self, jlong parent, jstring name)
{
	return 0;
}

jobjectArray Registry::GetMultiString(JNIEnv* env, jobject self, jlong parent, jstring name)
{
	return 0;
}

void Registry::SetString(JNIEnv* env, jobject self, jlong parent, jstring name, jstring value)
{
}

void Registry::SetBinary(JNIEnv* env, jobject self, jlong parent, jstring name, jarray value)
{
}

void Registry::SetDoubleWord(JNIEnv* env, jobject self, jlong parent, jstring name, jlong value)
{
}

void Registry::SetDoubleWordLittleEndian(JNIEnv* env, jobject self, jlong parent, jstring name, jlong value)
{
}

void Registry::SetDoubleWordBigEndian(JNIEnv* env, jobject self, jlong parent, jstring name, jlong value)
{
}

void Registry::SetMultiString(JNIEnv* env, jobject self, jlong parent, jstring name, jobjectArray value)
{
}

bool Registry::RegisterNatives(JNIEnv *env)
{
	return true;
}
