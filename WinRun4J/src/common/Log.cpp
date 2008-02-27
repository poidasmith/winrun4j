/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Peter Smith
 *******************************************************************************/

#include "Log.h"
#include <stdio.h>
#include <fcntl.h>
#include <io.h>
#include <iostream>
#include <fstream>
#include "../java/VM.h"
#include "../java/JNI.h"

using namespace std;

static const WORD MAX_CONSOLE_LINES = 500;
static BOOL haveInit = FALSE;
static BOOL canUseConsole = FALSE;
static BOOL haveConsole = FALSE;
static FILE* fp = NULL;
static LoggingLevel level = none; 
static bool g_error = false;
static char g_errorText[MAX_PATH];

typedef BOOL (__stdcall *FPTR_AttachConsole) ( DWORD );

void Log::RedirectIOToConsole()
{
	// redirect unbuffered STDOUT to the console
	HANDLE lStdHandle = GetStdHandle(STD_OUTPUT_HANDLE);
	int hConHandle = _open_osfhandle((long) lStdHandle, _O_TEXT);
	FILE* fp = _fdopen( hConHandle, "w" );
	*stdout = *fp;
	setvbuf( stdout, NULL, _IONBF, 0 );

	// redirect unbuffered STDIN to the console
	lStdHandle = GetStdHandle(STD_INPUT_HANDLE);
	hConHandle = _open_osfhandle((long) lStdHandle, _O_TEXT);
	fp = _fdopen( hConHandle, "r" );
	*stdin = *fp;
	setvbuf( stdin, NULL, _IONBF, 0 );

	// redirect unbuffered STDERR to the console
	lStdHandle = GetStdHandle(STD_ERROR_HANDLE);
	hConHandle = _open_osfhandle((long) lStdHandle, _O_TEXT);
	fp = _fdopen( hConHandle, "w" );
	*stderr = *fp;
	setvbuf( stderr, NULL, _IONBF, 0 );
}

void Log::Init(HINSTANCE hInstance, const char* logfile, const char* loglevel)
{
	if(loglevel == NULL) {
		level = info;
	} else if(strcmp(loglevel,"none") == 0) {
		level = none;
	} else if(strcmp(loglevel, "info") == 0) {
		level = info;
	} else if(strcmp(loglevel, "warning") == 0) {
		level = warning;
	} else if(strcmp(loglevel, "error") == 0) {
		level = error;
	}

#ifndef CONSOLE
	OSVERSIONINFO ver;
	ver.dwOSVersionInfoSize = sizeof(OSVERSIONINFO);
	BOOL result = GetVersionEx(&ver);	
	if (result && ver.dwMajorVersion > 5 || (ver.dwMajorVersion == 5 && ver.dwMinorVersion > 0))
		canUseConsole = TRUE;

	if(!haveInit) {
		if(canUseConsole) {
			// Attempt to attach to parent console (if function is present)
			HMODULE hModule = GetModuleHandle("kernel32");
			if(hModule != NULL) {
				FPTR_AttachConsole AttachConsole = (FPTR_AttachConsole) GetProcAddress(hModule, "AttachConsole");
				if(AttachConsole != NULL) {
					haveConsole = AttachConsole(-1);
					if(haveConsole) {
						AllocConsole();
						RedirectIOToConsole();
						printf("\n\n");
					}
				}
			}
		}
		haveInit = TRUE;
	}
#endif

	// If there is a log file specified redirect std streams to this file
	if(logfile != NULL) {
		freopen(logfile, "a", stdout);
		freopen(logfile, "a", stderr);
	}
}

#ifdef DEBUG_LOG						
#define LOG_IT						\
	if(format) {                    \
		char tmp[4096];				\
		va_list args;				\
		va_start(args, format);		\
		vsprintf(tmp, format, args);\
		OutputDebugString(tmp);		\
		va_end(args);				\
	}
#else
#define LOG_IT						\
	if(format) {                    \
		char tmp[4096];				\
		va_list args;				\
		va_start(args, format);		\
		vsprintf(tmp, format, args);\
		puts(tmp);                  \
		fflush(stdout);				\
		fflush(stderr);				\
		va_end(args);				\
	}
#endif

void Log::SetLevel(LoggingLevel logingLevel) 
{
	level = logingLevel;
}

void Log::LogIt(LoggingLevel loggingLevel, const char* format, ...)
{
	if(level <= loggingLevel) {
		LOG_IT
	}
}

void Log::Info(const char* format, ...)
{
	if(level <= info) {
		LOG_IT
	}
}

void Log::Warning(const char* format, ...)
{
	if(level <= warning) {
		LOG_IT
	}
}

void Log::Error(const char* format, ...)
{
	if(level <= error) {
		LOG_IT
	}
}

void Log::Close() 
{
	if(fp != NULL) {
		fclose(fp);
	}
}

// Set the error
void Log::SetLastError(const char* format, ...)
{
	JNI::ClearException(VM::GetJNIEnv());
	g_error = true;
	va_list args;
	va_start(args, format);
	Log::Error(format, args);
	vsprintf_s(g_errorText, MAX_PATH, format, args);
	fflush(stdout);
	fflush(stderr);
	va_end(args);
}

const char* Log::GetLastError()
{
	return g_error ? g_errorText : NULL;
}

bool Log::RegisterNatives(JNIEnv* env)
{
	// Register Log functions
	jclass clazz = env->FindClass("org/boris/winrun4j/Log");
	if(clazz == NULL) {
		Log::SetLastError("Could not find Log class");
		return false;
	}
	
	JNINativeMethod nm[3];
	nm[0].name = "log";
	nm[0].signature = "(ILjava/lang/String;)V";
	nm[0].fnPtr = (void*) Log::LogJ;
	nm[1].name = "setLastError";
	nm[1].signature = "(Ljava/lang/String;)V";
	nm[1].fnPtr = (void*) Log::SetLastErrorJ;
	nm[2].name = "getLastError";
	nm[2].signature = "()Ljava/lang/String;";
	nm[2].fnPtr = (void*) Log::GetLastErrorJ;
	env->RegisterNatives(clazz, nm, 3);

	if(env->ExceptionCheck()) {
		Log::SetLastError(JNI::GetExceptionMessage(env));
		return false;
	}
	
	return true;
}

void JNICALL Log::LogJ(JNIEnv* env, jobject self, jint jlevel, jstring str)
{
	if(str == NULL)
		return;

	jboolean iscopy = false;
	const char* format = env->GetStringUTFChars(str, &iscopy);
	LogIt((LoggingLevel) jlevel, format);
}

void JNICALL Log::SetLastErrorJ(JNIEnv* env, jobject self, jstring str)
{
	if(str == NULL)
		return;

	jboolean iscopy = false;
	if(env->ExceptionOccurred())
		env->ExceptionClear();
	const char* chars = env->GetStringUTFChars(str, &iscopy);
	Log::SetLastError(chars);
	env->ReleaseStringUTFChars(str, chars); 
}

jstring JNICALL Log::GetLastErrorJ(JNIEnv* env, jobject self)
{
	const char* err = Log::GetLastError();
	if(err == NULL)
		return NULL;
	else
		return env->NewStringUTF(err);
}
