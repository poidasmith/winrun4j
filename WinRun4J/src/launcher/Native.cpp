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
#include "../java/VM.h"
#include "../libffi/ffi.h"

bool Native::RegisterNatives(JNIEnv *env)
{
	Log::Info("Registering natives for Native class");

	jclass clazz = JNI::FindClass(env, "org/boris/winrun4j/Native");
	if(clazz == NULL) {
		JNI::ClearException(env);
		Log::Warning("Could not find Native class");
		return false;
	}
	
	JNINativeMethod nm[12];
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
	nm[6].name = "bind";
	nm[6].signature = "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/String;J)Z";
	nm[6].fnPtr = (void*) Bind;
	nm[7].name = "newGlobalRef";
	nm[7].signature = "(Ljava/lang/Object;)J";
	nm[7].fnPtr = (void*) NewGlobalRef;
	nm[8].name = "deleteGlobalRef";
	nm[8].signature = "(J)V";
	nm[8].fnPtr = (void*) DeleteGlobalRef;
	nm[9].name = "getMethodId";
	nm[9].signature = "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/String;Z)J";
	nm[9].fnPtr = (void*) GetMethodID;
	nm[10].name = "getObjectId";
	nm[10].signature = "(Ljava/lang/Object;)J";
	nm[10].fnPtr = (void*) GetObjectID;
	nm[11].name = "getObject";
	nm[11].signature = "(J)Ljava/lang/Object;";
	nm[11].fnPtr = (void*) GetObject;

	env->RegisterNatives(clazz, nm, 12);

	if(env->ExceptionCheck()) {
		JNI::PrintStackTrace(env);
		return false;
	}

	Log::Info("Registering natives for FFI class");

	jclass clazz2 = JNI::FindClass(env, "org/boris/winrun4j/FFI");
	if(clazz2 == NULL) {
		JNI::ClearException(env);
		Log::Warning("Could not find FFI class");
		return false;
	}
	
	JNINativeMethod nm2[4];
	nm2[0].name = "prepare";
	nm2[0].signature = "(JIIJJ)I";
	nm2[0].fnPtr = (void*) FFIPrepare;
	nm2[1].name = "call";
	nm2[1].signature = "(JJJJ)V";
	nm2[1].fnPtr = (void*) FFICall;
	nm2[2].name = "prepareClosure";
	nm2[2].signature = "(JJJ)J";
	nm2[2].fnPtr = (void*) FFIPrepareClosure;
	nm2[3].name = "freeClosure";
	nm2[3].signature = "(J)V";
	nm2[3].fnPtr = (void*) FFIFreeClosure;

	env->RegisterNatives(clazz2, nm2, 4);

	if(env->ExceptionCheck()) {
		JNI::PrintStackTrace(env);
		return false;
	}

	return true;
}

jlong Native::LoadLibrary(JNIEnv* env, jobject /*self*/, jstring filename)
{
	if(!filename)
		return 0;
	jboolean iscopy;
	const jchar* str = env->GetStringChars(filename, &iscopy);
	jlong res = (jlong) ::LoadLibraryW((LPCWSTR) str);
	env->ReleaseStringChars(filename, str);
	return res;
}

void Native::FreeLibrary(JNIEnv* /*env*/, jobject /*self*/, jlong handle)
{
	::FreeLibrary((HMODULE) handle);
}

jlong Native::GetProcAddress(JNIEnv* env, jobject /*self*/, jlong handle, jstring name)
{
	if(!name)
		return 0;
	jboolean iscopy;
	const char* str = env->GetStringUTFChars(name, &iscopy);
	jlong res = (jlong) ::GetProcAddress((HMODULE) handle, str);
	env->ReleaseStringUTFChars(name, str);
	return res;
}

jlong Native::Malloc(JNIEnv* /*env*/, jobject /*self*/, jint size)
{
	void* ptr = malloc(size);
	memset(ptr, 0, size);
	return (jlong) ptr;
}

void Native::Free(JNIEnv* /*env*/, jobject /*self*/, jlong handle)
{
	::free((void*) handle);
}

jobject Native::FromPointer(JNIEnv* env, jobject /*self*/, jlong handle, jlong size)
{
	return env->NewDirectByteBuffer((void*) handle, size);
}

jboolean Native::Bind(JNIEnv* env, jobject /*self*/, jclass clazz, jstring fn, jstring sig, jlong ptr)
{
	if(!clazz || !fn || !sig || !ptr)
		return false;

	jboolean iscopy = false;
	const char* cf = env->GetStringUTFChars(fn, &iscopy);
	const char* cs = env->GetStringUTFChars(sig, &iscopy);

	JNINativeMethod nm[1];
	nm[0].name = (char*) cf;
	nm[0].signature = (char*) cs;
	nm[0].fnPtr = (void*) ptr;

	env->RegisterNatives(clazz, nm, 1);

	if(env->ExceptionCheck()) {
		JNI::PrintStackTrace(env);
		env->ReleaseStringUTFChars(fn, cf);
		env->ReleaseStringUTFChars(sig, cs);
		return false;
	}
	env->ReleaseStringUTFChars(fn, cf);
	env->ReleaseStringUTFChars(sig, cs);

	return true;
}

jlong Native::NewGlobalRef(JNIEnv* env, jobject /*self*/, jobject obj)
{
	return (jlong) env->NewGlobalRef(obj);
}

void Native::DeleteGlobalRef(JNIEnv* env, jobject /*self*/, jlong handle)
{
	env->DeleteGlobalRef((jobject) handle);
}

jlong Native::GetMethodID(JNIEnv* env, jobject /*self*/, jclass clazz, jstring name, jstring sig, jboolean isStatic)
{
	const char* ns = env->GetStringUTFChars(name, 0);
	const char* ss = env->GetStringUTFChars(sig, 0);
	jmethodID res = isStatic ? env->GetStaticMethodID(clazz, ns, ss) : 
		env->GetMethodID(clazz, ns, ss);
	env->ReleaseStringUTFChars(name, ns);
	env->ReleaseStringUTFChars(sig, ss);
	return (jlong) res;
}

jlong Native::GetObjectID(JNIEnv* /*env*/, jobject /*self*/, jobject obj)
{
	return (jlong) obj;
}

jobject Native::GetObject(JNIEnv* /*env*/, jobject /*self*/, jlong obj)
{
	return (jobject) obj;
}

jint Native::FFIPrepare(JNIEnv* /*env*/, jobject /*self*/, jlong cif, jint abi, jint nargs, jlong rtype, jlong atypes)
{
	return ffi_prep_cif((ffi_cif *) cif, (ffi_abi) abi, nargs, (ffi_type *) rtype, (ffi_type **) atypes);
}

void Native::FFICall(JNIEnv* /*env*/, jobject /*self*/, jlong cif, jlong fn, jlong rvalue, jlong avalue)
{
	ffi_call((ffi_cif *) cif, (void (__cdecl *)(void)) fn, (void *) rvalue, (void **) avalue);
}

typedef struct {
	void* codeloc;
	ffi_closure* closure;
	jobject objectId;
	jmethodID methodId;
} FFI_CLOSURE_DATA;

void Closure(ffi_cif* /*cif*/, void *resp, void **arg_area, void* user_data)
{
	JNIEnv* env = VM::GetJNIEnv(true);
	// Sanity check here to avoid JVM crash
	if(env->ExceptionCheck()) {
		env->ExceptionDescribe();
		env->ExceptionClear();
	}
	FFI_CLOSURE_DATA* fd = (FFI_CLOSURE_DATA*) user_data;
	env->CallVoidMethod(fd->objectId, fd->methodId, (jlong) resp, (jlong) arg_area);
	if(env->ExceptionCheck()) {
		env->Throw(env->ExceptionOccurred());
	}
}

jlong Native::FFIPrepareClosure(JNIEnv* /*env*/, jobject /*self*/, jlong cif, jlong objectId, jlong methodId)
{
	FFI_CLOSURE_DATA* fd = (FFI_CLOSURE_DATA*) malloc(sizeof(FFI_CLOSURE_DATA));
	fd->closure = (ffi_closure*) ffi_closure_alloc(sizeof(ffi_closure), &(fd->codeloc));
	fd->objectId = (jobject) objectId;
	fd->methodId = (jmethodID) methodId;
	ffi_prep_closure_loc (fd->closure,
                      (ffi_cif*) cif,
                      Closure,
                      (void*) fd,
                      fd->codeloc);
	return (jlong) fd;
}

void Native::FFIFreeClosure(JNIEnv* /*env*/, jobject /*self*/, jlong closure)
{
	FFI_CLOSURE_DATA* fd = (FFI_CLOSURE_DATA*) closure;
	ffi_closure_free(fd->closure);
	free(fd);
}


