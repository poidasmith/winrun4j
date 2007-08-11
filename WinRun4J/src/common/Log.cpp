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
static BOOL haveConsole = FALSE;
static FILE* fp = NULL;
static LoggingLevel level = LoggingLevel::none; 
static bool g_error = false;
static char g_errorText[MAX_PATH];

typedef BOOL (__stdcall *FPTR_AttachConsole) ( DWORD );

void Log::RedirectIOToConsole()
{
	// redirect unbuffered STDOUT to the console
	long lStdHandle = (long)GetStdHandle(STD_OUTPUT_HANDLE);
	int hConHandle = _open_osfhandle(lStdHandle, _O_TEXT);
	FILE* fp = _fdopen( hConHandle, "w" );
	*stdout = *fp;
	setvbuf( stdout, NULL, _IONBF, 0 );

	// redirect unbuffered STDIN to the console
	lStdHandle = (long)GetStdHandle(STD_INPUT_HANDLE);
	hConHandle = _open_osfhandle(lStdHandle, _O_TEXT);
	fp = _fdopen( hConHandle, "r" );
	*stdin = *fp;
	setvbuf( stdin, NULL, _IONBF, 0 );

	// redirect unbuffered STDERR to the console
	lStdHandle = (long)GetStdHandle(STD_ERROR_HANDLE);
	hConHandle = _open_osfhandle(lStdHandle, _O_TEXT);
	fp = _fdopen( hConHandle, "w" );
	*stderr = *fp;
	setvbuf( stderr, NULL, _IONBF, 0 );
}

void Log::Init(HINSTANCE hInstance, const char* logfile, const char* loglevel)
{
	if(loglevel == NULL) {
		level = LoggingLevel::info;
	} else if(strcmp(loglevel,"none") == 0) {
		level = LoggingLevel::none;
	} else if(strcmp(loglevel, "info") == 0) {
		level = LoggingLevel::info;
	} else if(strcmp(loglevel, "warning") == 0) {
		level = LoggingLevel::warning;
	} else if(strcmp(loglevel, "error")) {
		level = LoggingLevel::error;
	}

	if(!haveInit) {
		// Attempt to attach to parent console (if function is present)
		HMODULE hModule = GetModuleHandle("kernel32");
		if(hModule != NULL) {
			FPTR_AttachConsole AttachConsole = (FPTR_AttachConsole) GetProcAddress(hModule, "AttachConsole");
			haveConsole = AttachConsole(-1);
			if(haveConsole) {
				AllocConsole();
				RedirectIOToConsole();
				printf("\n\n");
			}
		}
	}

	// If there is a log file specified redirect std streams to this file
	if(logfile != NULL) {
		freopen(logfile, "a", stdout);
		freopen(logfile, "a", stderr);
	}
}

#define LOG_IT               \
	va_list args;            \
	va_start(args, format);  \
	vprintf(format, args);   \
	fflush(stdout);          \
	fflush(stderr);          \
	va_end(args);

void Log::SetLevel(LoggingLevel logingLevel) 
{
	level = logingLevel;
}


void Log::Info(const char* format, ...)
{
	if(level <= LoggingLevel::info) {
		LOG_IT
	}
}

void Log::Warning(const char* format, ...)
{
	if(level <= LoggingLevel::warning) {
		LOG_IT
	}
}

void Log::Error(const char* format, ...)
{
	if(level <= LoggingLevel::error) {
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
	JNI::ClearJavaException(VM::GetJNIEnv());
	g_error = true;
	va_list args;
	va_start(args, format);
	vsprintf_s(g_errorText, MAX_PATH, format, args);
	fflush(stdout);
	fflush(stderr);
	va_end(args);
}

const char* Log::GetLastError()
{
	return g_error ? g_errorText : NULL;
}