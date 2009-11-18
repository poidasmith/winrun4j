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

namespace 
{
	BOOL haveInit = FALSE;
	BOOL canUseConsole = FALSE;
	BOOL haveConsole = FALSE;
	HANDLE g_logfileHandle = NULL;
	HANDLE g_stdHandle = NULL;
	bool g_haveLogFile = false;
	bool g_logFileAndConsole = false;
	double g_logRollSize = 0;
	char* g_logFilename = NULL;
	char* g_logRollPrefix = NULL;
	char* g_logRollSuffix = NULL;
	bool g_logOverwrite = false;
	volatile bool g_logRolling = false;
	LoggingLevel g_logLevel = none; 
	bool g_error = false;
	char g_errorText[MAX_PATH];
}

typedef BOOL (_stdcall *FPTR_AttachConsole) ( DWORD );

#define LOG_OVERWRITE_OPTION ":log.overwrite"
#define LOG_FILE_AND_CONSOLE ":log.file.and.console"
#define LOG_ROLL_SIZE ":log.roll.size"
#define LOG_ROLL_PREFIX ":log.roll.prefix"
#define LOG_ROLL_SUFFIX ":log.roll.suffix"

void Log::Init(HINSTANCE hInstance, const char* logfile, const char* loglevel, dictionary* ini)
{
	if(loglevel == NULL) {
		g_logLevel = info;
	} else if(strcmp(loglevel,"none") == 0) {
		g_logLevel = none;
	} else if(strcmp(loglevel, "info") == 0) {
		g_logLevel = info;
	} else if(strcmp(loglevel, "warning") == 0) {
		g_logLevel = warning;
	} else if(strcmp(loglevel, "warn") == 0) {
		g_logLevel = warning;
	} else if(strcmp(loglevel, "error") == 0) {
		g_logLevel = error;
	} else if(strcmp(loglevel, "err") == 0) {
		g_logLevel = error;
	} else {
		g_logLevel = info;
		Warning("log.level unrecognized");
	}

	// If there is a log file specified redirect std streams to this file
	if(logfile != NULL) {
		char defWorkingDir[MAX_PATH];
		GetCurrentDirectory(MAX_PATH, defWorkingDir);
		char* workingDir = iniparser_getstr(ini, WORKING_DIR);
		if(workingDir) {
			SetCurrentDirectory(iniparser_getstr(ini, INI_DIR));
			SetCurrentDirectory(workingDir);
		}
		g_logFilename = strdup(logfile);
		g_logOverwrite = iniparser_getboolean(ini, LOG_OVERWRITE_OPTION, false);
		g_logfileHandle = CreateFile(logfile, GENERIC_READ|GENERIC_WRITE, FILE_SHARE_READ, NULL, 
				g_logOverwrite ? CREATE_ALWAYS : OPEN_ALWAYS, 
				FILE_ATTRIBUTE_NORMAL, NULL);
		if (g_logfileHandle != INVALID_HANDLE_VALUE) {
			SetFilePointer(g_logfileHandle, 0, NULL, g_logOverwrite ? FILE_BEGIN : FILE_END);
			g_stdHandle = GetStdHandle(STD_OUTPUT_HANDLE);
			SetStdHandle(STD_OUTPUT_HANDLE, g_logfileHandle);
			SetStdHandle(STD_ERROR_HANDLE, g_logfileHandle);
			g_haveLogFile = true;
			char* logFileAndConsole = iniparser_getstr(ini, LOG_FILE_AND_CONSOLE);
			if(logFileAndConsole) {
				g_logFileAndConsole = iniparser_getboolean(ini, LOG_FILE_AND_CONSOLE, false);
			}
			// Check for log rolling
			g_logRollSize = iniparser_getdouble(ini, LOG_ROLL_SIZE, 0) * 1000000;
			if(g_logRollSize > 0) {
				char fullLog[MAX_PATH];
				char logDir[MAX_PATH];
				char logPrefix[MAX_PATH];
				char logExtension[MAX_PATH];
				GetFullPathName(logfile, MAX_PATH, fullLog, 0);
				GetFileDirectory(fullLog, logDir);
				char* prefix = iniparser_getstr(ini, LOG_ROLL_PREFIX);
				if(prefix) {
					strcat(logDir, prefix);
				} else {
					GetFileNameSansExtension(fullLog, logPrefix);
					strcat(logDir, logPrefix);
				}
				g_logRollPrefix = strdup(logDir);
				char* suffix = iniparser_getstr(ini, LOG_ROLL_SUFFIX);
				if(suffix) {
					g_logRollSuffix = strdup(suffix);
				} else {
					GetFileExtension(fullLog, logExtension);
					g_logRollSuffix = strdup(logExtension);
				}
			}
		} else {
			Log::Error("Could not open log file");
			g_logfileHandle = GetStdHandle(STD_OUTPUT_HANDLE);
		}
		if(workingDir) {
			SetCurrentDirectory(defWorkingDir);
		}
	} else {
		g_logfileHandle = GetStdHandle(STD_OUTPUT_HANDLE);
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
						printf("\n\n");
					}
				}
			}
		}
		haveInit = TRUE;
	}
#endif
}

void Log::RollLog()
{
	char filename[MAX_PATH];
	SYSTEMTIME st;
	GetLocalTime(&st);
	sprintf(filename, "%s-%4d%02d%02d-%02d%02d%02d%s", g_logRollPrefix, st.wYear, st.wMonth, st.wDay,
		st.wHour, st.wMinute, st.wSecond, g_logRollSuffix);
	CloseHandle(g_logfileHandle);
	MoveFile(g_logFilename, filename);
	g_logfileHandle = CreateFile(g_logFilename, GENERIC_READ|GENERIC_WRITE, FILE_SHARE_READ, NULL, 
			g_logOverwrite ? CREATE_ALWAYS : OPEN_ALWAYS, 
			FILE_ATTRIBUTE_NORMAL, NULL);
	if (g_logfileHandle != INVALID_HANDLE_VALUE) {
		SetFilePointer(g_logfileHandle, 0, NULL, g_logOverwrite ? FILE_BEGIN : FILE_END);
	}
	Log::Info("Rolled log name: %s", filename);
}

// enum LoggingLevel { info = 0, warning = 1, error = 2, none = 3 };
void Log::LogIt(LoggingLevel loggingLevel, const char* marker, const char* format, va_list args) 
{
	if(g_logLevel > loggingLevel) return;
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
		WriteFile(g_logfileHandle, marker, strlen(marker), &dwRead, NULL);
		WriteFile(g_logfileHandle, " ", 1, &dwRead, NULL);
	}
	WriteFile(g_logfileHandle, tmp, strlen(tmp), &dwRead, NULL);
	WriteFile(g_logfileHandle, "\r\n", 2, &dwRead, NULL);
	FlushFileBuffers(g_logfileHandle);

	// Check if we also log to console if we have a log file
	if(g_haveLogFile && g_logFileAndConsole) {
		if(marker) {
			WriteFile(g_stdHandle, marker, strlen(marker), &dwRead, NULL);
			WriteFile(g_stdHandle, " ", 1, &dwRead, NULL);
		}
		WriteFile(g_stdHandle, tmp, strlen(tmp), &dwRead, NULL);
		WriteFile(g_stdHandle, "\r\n", 2, &dwRead, NULL);
		FlushFileBuffers(g_stdHandle);
	}

	// Check if we need to roll the log
	if(g_logRollSize > 0 && !g_logRolling) {
		g_logRolling = true;
		DWORD size = GetFileSize(g_logfileHandle, 0);
		if(size > g_logRollSize) {
			RollLog();
		}
		g_logRolling = false;
	}
}

void Log::SetLevel(LoggingLevel loggingLevel) 
{
	g_logLevel = loggingLevel;
}

LoggingLevel Log::GetLevel()
{
	return g_logLevel;
}

void Log::SetLogFileAndConsole(bool logAndConsole)
{
	g_logFileAndConsole = logAndConsole;
}

// enum LoggingLevel { info = 0, warning = 1, error = 2, none = 3 };
void Log::Info(const char* format, ...)
{
	if(g_logLevel <= info) {
		va_list args;
		va_start(args, format);
		LogIt(info, "[info]", format, args);
		va_end(args);
	}
}

void Log::Warning(const char* format, ...)
{
	if(g_logLevel <= warning) {
		va_list args;
		va_start(args, format);
		LogIt(warning, "[warn]", format, args);
		va_end(args);
	}
}

void Log::Error(const char* format, ...)
{
	if(g_logLevel <= error) {
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

#ifndef NO_JAVA
extern "C" __declspec(dllexport) void Log_LogIt(int level, const char* marker, const char* format, ...)
{
	va_list args;
	va_start(args, format);
	Log::LogIt((LoggingLevel) level, marker, format, args);
	va_end(args);
}
#endif
