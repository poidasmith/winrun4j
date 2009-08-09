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
	
	JNINativeMethod nm[7];
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
	nm[5].name = "fromPointer";
	nm[5].signature = "(JJ)Ljava/nio/ByteBuffer;";
	nm[5].fnPtr = (void*) FromPointer;
	nm[6].name = "call";
	nm[6].signature = "(J[BI)J";
	nm[6].fnPtr = (void*) Call;
	env->RegisterNatives(clazz, nm, 7);

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

jobject Native::FromPointer(JNIEnv* env, jobject self, jlong handle, jlong size)
{
	return env->NewDirectByteBuffer((void*) handle, size);
}

jlong Native::Call(JNIEnv* env, jobject self, jlong handle, jbyteArray stack, jint size)
{
	jboolean iscopy;
	int* p = !stack ? (int*) 0 : (int*)env->GetPrimitiveArrayCritical(stack, &iscopy);
	if(!p && size > 0)
		return 0;
	for(int i = 0; i < size; i+=4) {
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
	env->ReleasePrimitiveArrayCritical(stack, p, 0);
	return (jlong) r;
}

jboolean Native::Bind(JNIEnv* env, jstring clazz, jstring fn, jstring sig, jlong ptr)
{
	if(!clazz || !fn || !sig || !ptr)
		return false;

	jboolean iscopy = false;
	const char* cc = env->GetStringUTFChars(clazz, &iscopy);
	const char* cf = env->GetStringUTFChars(fn, &iscopy);
	const char* cs = env->GetStringUTFChars(sig, &iscopy);

	jclass jc = env->FindClass(cc);
	if(jc == NULL) {
		JNI::ClearException(env);
		Log::Warning("Could not find [%s] class", cc);
		return false;
	}

	JNINativeMethod nm[1];
	nm[0].name = (char*) cf;
	nm[0].signature = (char*) cs;
	nm[0].fnPtr = (void*) ptr;

	env->RegisterNatives(jc, nm, 1);

	if(env->ExceptionCheck()) {
		JNI::PrintStackTrace(env);
		return false;
	}

	return true;
}
