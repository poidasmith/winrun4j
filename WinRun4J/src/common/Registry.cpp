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
#include "../java/JNI.h"

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
	if(handle == 0)
		return;
	RegCloseKey((HKEY) handle);
}

jobjectArray Registry::GetSubKeyNames(JNIEnv* env, jobject self, jlong handle)
{
	if(handle == 0)
		return 0;

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
	if(handle == 0)
		return 0;

	DWORD valueCount = 0;
	LONG result = RegQueryInfoKey((HKEY) handle, NULL, NULL, NULL, NULL, NULL, NULL, &valueCount, NULL, NULL, NULL, NULL);
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

jlong Registry::GetType(JNIEnv* env, jobject self, jlong parent, jstring name)
{
	if(parent == 0)
		return 0;

	jboolean iscopy = false;
	const char* str = env->GetStringUTFChars(name, &iscopy);
	DWORD type = 0;
	LONG result = RegQueryValueEx((HKEY) parent, str, NULL, &type, NULL, NULL);
	env->ReleaseStringUTFChars(name, str);
	if(result == ERROR_SUCCESS)
		return type;
	else 
		return 0;
}

jstring Registry::GetString(JNIEnv* env, jobject self, jlong parent, jstring name)
{
	if(parent == 0)
		return 0;

	jboolean iscopy = false;
	const char* str = env->GetStringUTFChars(name, &iscopy);
	DWORD type = 0;
	LONG result = RegQueryValueEx((HKEY) parent, str, NULL, &type, NULL, NULL);
	env->ReleaseStringUTFChars(name, str);
	if(result == ERROR_SUCCESS)
		return type;
	else 
		return 0;
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
	jclass clazz = env->FindClass("org/boris/winrun4j/RegistryKey");
	if(clazz == NULL) {
		Log::Warning("Could not find RegistryKey class\n");
		if(env->ExceptionOccurred())
			env->ExceptionClear();
		return false;
	}

	JNINativeMethod methods[20];
	methods[0].fnPtr = CloseKey;
	methods[0].name = "closeKeyHandle";
	methods[0].signature = "(J)V";
	methods[1].fnPtr = DeleteKey;
	methods[1].name = "deleteKey";
	methods[1].signature = "(J)V";
	methods[2].fnPtr = DeleteValue;
	methods[2].name = "deleteValue";
	methods[2].signature = "(JLjava/lang/String;)V";
	methods[3].fnPtr = GetBinary;
	methods[3].name = "getBinary";
	methods[3].signature = "(JLjava/lang/String;)[B";
	methods[4].fnPtr = GetDoubleWord;
	methods[4].name = "getDoubleWord";
	methods[4].signature = "(JLjava/lang/String;)J";
	methods[5].fnPtr = GetDoubleWordBigEndian;
	methods[5].name = "getDoubleWordBigEndian";
	methods[5].signature = "(JLjava/lang/String;)J";
	methods[6].fnPtr = GetDoubleWordLittleEndian;
	methods[6].name = "getDoubleWordLittleEndian";
	methods[6].signature = "(JLjava/lang/String;)J";
	methods[7].fnPtr = GetExpandedString;
	methods[7].name = "getExpandedString";
	methods[7].signature = "(JLjava/lang/String;)Ljava/lang/String;";
	methods[8].fnPtr = GetMultiString;
	methods[8].name = "getMultiString";
	methods[8].signature = "(JLjava/lang/String;)[Ljava/lang/String;";
	methods[9].fnPtr = GetString;
	methods[9].name = "getString";
	methods[9].signature = "(JLjava/lang/String;)Ljava/lang/String;";
	methods[10].fnPtr = GetSubKeyNames;
	methods[10].name = "getSubKeyNames";
	methods[10].signature = "(J)[Ljava/lang/String;";
	methods[11].fnPtr = GetType;
	methods[11].name = "getType";
	methods[11].signature = "(JLjava/lang/String;)J";
	methods[12].fnPtr = GetValueNames;
	methods[12].name = "getValueNames";
	methods[12].signature = "(J)[Ljava/lang/String;";
	methods[13].fnPtr = OpenKey;
	methods[13].name = "openKeyHandle";
	methods[13].signature = "(JLjava/lang/String;)J";
	methods[14].fnPtr = SetBinary;
	methods[14].name = "setBinary";
	methods[14].signature = "(JLjava/lang/String;[B)V";
	methods[15].fnPtr = SetDoubleWord;
	methods[15].name = "setDoubleWord";
	methods[15].signature = "(JLjava/lang/String;J)V";
	methods[16].fnPtr = SetDoubleWordBigEndian;
	methods[16].name = "setDoubleWordBigEndian";
	methods[16].signature = "(JLjava/lang/String;J)V";
	methods[17].fnPtr = SetDoubleWordLittleEndian;
	methods[17].name = "setDoubleWordLittleEndian";
	methods[17].signature = "(JLjava/lang/String;J)V";
	methods[18].fnPtr = SetMultiString;
	methods[18].name = "setMultiString";
	methods[18].signature = "(JLjava/lang/String;[Ljava/lang/String;)V";
	methods[19].fnPtr = SetString;
	methods[19].name = "setString";
	methods[19].signature = "(JLjava/lang/String;Ljava/lang/String;)V";
	
	env->RegisterNatives(clazz, methods, 20);
	if(env->ExceptionOccurred()) {
		char* msg = JNI::GetExceptionMessage(env);
		Log::Error(msg);
		env->ExceptionClear();
	}
	return true;
}
