/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Peter Smith
 *******************************************************************************/

#include "LogUtils.h"
#include <stdio.h>
#include <fcntl.h>
#include <io.h>
#include <iostream>
#include <fstream>

using namespace std;

static const WORD MAX_CONSOLE_LINES = 500;
static BOOL haveInit = FALSE;
static BOOL haveConsole = FALSE;
static FILE* fp = NULL;

typedef BOOL (__stdcall *FPTR_AttachConsole) ( DWORD );

void RedirectIOToConsole()
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
	// make cout, wcout, cin, wcin, wcerr, cerr, wclog and clog
	// point to console as well
	ios::sync_with_stdio();
}

void LogInit(HINSTANCE hInstance, const char* logfile)
{
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
		freopen(logfile, "w", stdout);
		freopen(logfile, "a", stderr);
	}
}

extern void Log(const char* format, ...)
{
	va_list args;
	va_start(args, format);
	vprintf(format, args);
	fflush(stdout);
	fflush(stderr);
	va_end(args);
}

extern void LogClose() 
{
	if(fp != NULL) {
		fclose(fp);
	}
}