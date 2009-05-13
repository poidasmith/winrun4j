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

static const WORD MAX_CONSOLE_LINES = 500;
static BOOL haveInit = FALSE;
static BOOL canUseConsole = FALSE;
static BOOL haveConsole = FALSE;
static FILE* fp = NULL;
static LoggingLevel level = none; 
static bool g_error = false;
static char g_errorText[MAX_PATH];

typedef BOOL (__stdcall *FPTR_AttachConsole) ( DWORD );

#define LOG_OVERWRITE_OPTION ":log.overwrite"

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

void Log::Init(HINSTANCE hInstance, const char* logfile, const char* loglevel, dictionary* ini)
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
	} else {
		level = info;
		Warning("log.level unrecognized");
	}

	// If there is a log file specified redirect std streams to this file
	if(logfile != NULL) {
		char* logOverwriteOption;
		logOverwriteOption = iniparser_getstring(ini, LOG_OVERWRITE_OPTION, "no");
		DWORD dwCreationDisp = OPEN_ALWAYS;
		if (!stricmp(logOverwriteOption, "y") || 
			!stricmp(logOverwriteOption, "yes") ||
			!stricmp(logOverwriteOption, "true")) dwCreationDisp = TRUNCATE_EXISTING;
		HANDLE h = CreateFile(logfile, GENERIC_ALL, FILE_SHARE_WRITE, NULL, dwCreationDisp, FILE_ATTRIBUTE_NORMAL, NULL);
		if (h != INVALID_HANDLE_VALUE) {
			SetStdHandle(STD_OUTPUT_HANDLE, h);
			SetStdHandle(STD_ERROR_HANDLE, h);
			int oh = _open_osfhandle((long) h, _O_TEXT);
			FILE* fp = _fdopen(oh, dwCreationDisp == OPEN_ALWAYS ? "a+" : "w");
			*stdout = *fp;
			*stderr = *fp;
		}

		setvbuf( stdout, NULL, _IONBF, 0 );
		setvbuf( stderr, NULL, _IONBF, 0 );
		//setvbuf(log, NULL, _IONBF, 0 );
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
}

// enum LoggingLevel { info = 0, warning = 1, error = 2, none = 3 };
void Log::LogIt(LoggingLevel loggingLevel, const char* marker, const char* format, va_list args) 
{
	if(level > loggingLevel) return;
	if(!format) return;

	char tmp[4096];
	vsprintf(tmp, format, args);
#ifdef DEBUG_LOG
	if(marker) {
		OutputDebugString(marker);
		OutputDebugString(" ");
	}
	OutputDebugString(tmp);
	OutputDebugString("\n");
#endif
	if(marker) {
		printf(marker);
		printf(" ");
	}
	puts(tmp);
	fflush(stdout);
	fflush(stderr);
}

void Log::SetLevel(LoggingLevel loggingLevel) 
{
	level = loggingLevel;
}

LoggingLevel Log::GetLevel()
{
	return level;
}

void Log::Info(const char* format, ...)
{
	if(level <= info) {
		va_list args;
		va_start(args, format);
		LogIt(info, "[info]", format, args);
		va_end(args);
	}
}

void Log::Warning(const char* format, ...)
{
	if(level <= warning) {
		va_list args;
		va_start(args, format);
		LogIt(warning, "[warn]", format, args);
		va_end(args);
	}
}

void Log::Error(const char* format, ...)
{
	if(level <= error) {
		va_list args;
		va_start(args, format);
		LogIt(error, " [err]", format, args);
		va_end(args);
	}
}

void Log::Close() 
{
	if(fp != NULL) {
		fflush(fp);
		fclose(fp);
	}
}

// Set the error
void Log::SetLastError(const char* format, ...)
{
#ifndef NO_JAVA
	JNI::ClearException(VM::GetJNIEnv());
#endif
	g_error = true;
	va_list args;
	va_start(args, format);
	Log::Error(format, args);
	vsprintf_s(g_errorText, MAX_PATH, format, args);
	fflush(stdout);
	fflush(stderr);
	va_end(args);
}

#ifndef NO_JAVA

const char* Log::GetLastError()
{
	return g_error ? g_errorText : NULL;
}

bool Log::RegisterNatives(JNIEnv* env)
{
	// Register Log functions
	Log::Info("Registering natives for Log class");
	jclass clazz = JNI::FindClass(env, "org/boris/winrun4j/Log");
	if(clazz == NULL) {
		Log::Warning("Could not find Log class");
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
		JNI::PrintStackTrace(env);
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
	// TODO - logging level to string
	switch(jlevel) {
		case info: Info(format); break;
		case warning: Warning(format); break;
		case error: Error(format); break;
	}
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

#endif