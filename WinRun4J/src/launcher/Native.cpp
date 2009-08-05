/*******************************************************************************
* This program and the accompanying materials
* are made available under the terms of the Common Public License v1.0
* which accompanies this distribution, and is available at 
* http://www.eclipse.org/legal/cpl-v10.html
* 
* Contributors:
*     Peter Smith
*******************************************************************************/

#include "Native.h"
#include "../common/Log.h"
#include "../java/JNI.h"

bool Native::RegisterNatives(JNIEnv *env)
{
	Log::Info("Registering natives for Native class");

	jclass clazz = JNI::FindClass(env, "org/boris/winrun4j/Native");
	if(clazz == NULL) {
		JNI::ClearException(env);
		Log::Warning("Could not find Native class");
		return false;
	}
	
	JNINativeMethod nm[11];
	nm[0].name = "loadLibrary";
	nm[0].signature = "(Ljava/lang/String;)J";
	nm[0].fnPtr = (void*) LoadLibrary;
	nm[1].name = "freeLibrary";
	nm[1].signature = "(J)V";
	nm[1].fnPtr = (void*) FreeLibrary;
	nm[2].name = "getProcAddress";
	nm[2].signature = "(JLjava/lang/String;)J";
	nm[2].fnPtr = (void*) GetProcAddress;
	nm[3].name = "malloc";
	nm[3].signature = "(I)J";
	nm[3].fnPtr = (void*) Malloc;
	nm[4].name = "free";
	nm[4].signature = "(J)V";
	nm[4].fnPtr = (void*) Free;
	nm[5].name = "memcpy";
	nm[5].signature = "(J[BI)V";
	nm[5].fnPtr = (void*) MemCpy;
	nm[6].name = "fromPointer";
	nm[6].signature = "(JJ)Ljava/nio/ByteBuffer;";
	nm[6].fnPtr = (void*) FromPointer;
	nm[7].name = "intCall";
	nm[7].signature = "(J[II)J";
	nm[7].fnPtr = (void*) IntCall;
	nm[8].name = "doubleCall";
	nm[8].signature = "(J[II)D";
	nm[8].fnPtr = (void*) DoubleCall;
	env->RegisterNatives(clazz, nm, 9);

	if(env->ExceptionCheck()) {
		JNI::PrintStackTrace(env);
		return false;
	}

	return true;
}

jlong Native::LoadLibrary(JNIEnv* env, jobject self, jstring filename)
{
	if(!filename)
		return 0;
	jboolean iscopy;
	const jchar* str = env->GetStringChars(filename, &iscopy);
	return (jlong) ::LoadLibraryW((LPCWSTR) str);
}

void Native::FreeLibrary(JNIEnv* env, jobject self, jlong handle)
{
	::FreeLibrary((HMODULE) handle);
}

jlong Native::GetProcAddress(JNIEnv* env, jobject self, jlong handle, jstring name)
{
	if(!name)
		return 0;
	jboolean iscopy;
	const char* str = env->GetStringUTFChars(name, &iscopy);
	return (jlong) ::GetProcAddress((HMODULE) handle, str);
}

jlong Native::Malloc(JNIEnv* env, jobject self, jint size)
{
	return (jlong) ::malloc(size);
}

void Native::Free(JNIEnv* env, jobject self, jlong handle)
{
	::free((void*) handle);
}

void Native::MemCpy(JNIEnv* env, jobject self, jlong handle, jbyteArray buf, jint size)
{
	if(!buf) return;
	jboolean iscopy;
	void* p = env->GetPrimitiveArrayCritical(buf, &iscopy);
	::memcpy((void*) handle, p, size);
	env->ReleasePrimitiveArrayCritical(buf, p, 0);
}

jobject Native::FromPointer(JNIEnv* env, jobject self, jlong handle, jlong size)
{
	return env->NewDirectByteBuffer((void*) handle, size);
}

jlong Native::IntCall(JNIEnv* env, jobject self, jlong handle, jintArray stack, jint size)
{
	jboolean iscopy;
	int* p = !stack ? (int*) 0 : (int*)env->GetPrimitiveArrayCritical(stack, &iscopy);
	for(int i = 0; i < size; i++) {
		int v = *p;
		__asm {
			push v
		}
		p++;
	}
	int r;
	FARPROC fp = (FARPROC) handle;
	__asm {
		call fp
		mov dword ptr[r], eax
	}
	return (jlong) r;
}

jdouble Native::DoubleCall(JNIEnv* env, jobject self, jlong handle, jintArray stack, jint size)
{
	jboolean iscopy;
	int* p = !stack ? (int*) 0 : (int*) env->GetPrimitiveArrayCritical(stack, &iscopy);
	for(int i = 0; i < size; i++) {
		int v = *p;
		__asm {
			push v
		}
		p++;
	}
	double r;
	FARPROC fp = (FARPROC) handle;
	__asm {
		call fp
		fstp r
	}
	return (jdouble) r;
}
