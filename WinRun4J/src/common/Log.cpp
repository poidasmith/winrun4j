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
static HANDLE g_logfileHandle = NULL;
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
		char defWorkingDir[MAX_PATH];
		GetCurrentDirectory(MAX_PATH, defWorkingDir);
		char* workingDir = iniparser_getstr(ini, WORKING_DIR);
		if(workingDir) {
			SetCurrentDirectory(iniparser_getstr(ini, MODULE_DIR));
			SetCurrentDirectory(workingDir);
		}
		char* logOverwriteOption;
		logOverwriteOption = iniparser_getstr(ini, LOG_OVERWRITE_OPTION);
		bool overwrite = false;
		if (logOverwriteOption || stricmp(logOverwriteOption, "y")==0 || 
			stricmp(logOverwriteOption, "yes")==0 ||
			stricmp(logOverwriteOption, "true")==0) {
				overwrite = true;
		}
		g_logfileHandle = CreateFile(logfile, GENERIC_READ|GENERIC_WRITE, FILE_SHARE_READ, NULL, 
				overwrite ? CREATE_ALWAYS : OPEN_ALWAYS, 
				FILE_ATTRIBUTE_NORMAL, NULL);
		if (g_logfileHandle != INVALID_HANDLE_VALUE) {
			SetFilePointer(g_logfileHandle, 0, NULL, overwrite ? FILE_BEGIN : FILE_END);
			// Redirect STD streams to log file
			SetStdHandle(STD_OUTPUT_HANDLE, g_logfileHandle);
			SetStdHandle(STD_ERROR_HANDLE, g_logfileHandle);
		}
		if(workingDir) {
			SetCurrentDirectory(defWorkingDir);
		}
	}

#ifndef CONSOLE
	OSVERSIONINFO ver;
	ver.dwOSVersionInfoSize = sizeof(OSVERSIONINFO);
	BOOL result = GetVersionEx(&ver);	
	if (result && ver.dwMajorVersion > 5 || (ver.dwMajorVersion == 5 && ver.dwMinorVersion > 0))
		canUseConsole = TRUE;

	if(!haveInit && !logfile) {
		if(canUseConsole) {
			// Attempt to attach to parent console (if function is present)
			HMODULE hModule = GetModuleHandle("kernel32");
			if(hModule != NULL) {
				FPTR_AttachConsole AttachConsole = (FPTR_AttachConsole) GetProcAddress(hModule, "AttachConsole");
				if(AttachConsole != NULL) {
					haveConsole = AttachConsole(-1);
					if(haveConsole) {
						AllocConsole();
						//RedirectIOToConsole();
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
	DWORD dwRead;
	if(marker) {
		if(g_logfileHandle) {
			WriteFile(g_logfileHandle, marker, strlen(marker), &dwRead, NULL);
			WriteFile(g_logfileHandle, " ", 1, &dwRead, NULL);
		} else {
			printf(marker);
			printf(" ");
		}
	}
	if(g_logfileHandle) {
		WriteFile(g_logfileHandle, tmp, strlen(tmp), &dwRead, NULL);
		WriteFile(g_logfileHandle, "\r\n", 2, &dwRead, NULL);
		FlushFileBuffers(g_logfileHandle);
	}
	else {
		puts(tmp);
		fflush(stdout);
		fflush(stderr);
	}
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
	if(g_logfileHandle) {
		CloseHandle(g_logfileHandle);
		g_logfileHandle = NULL;
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