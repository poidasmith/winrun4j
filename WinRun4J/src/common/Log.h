/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Peter Smith
 *******************************************************************************/

#ifndef LOG_H
#define LOG_H

#include "Runtime.h"
#include "INI.h"

enum LoggingLevel { info = 0, warning = 1, error = 2, none = 3 };

struct Log {
	static void Init(HINSTANCE hInstance, const char* logfile, const char* loglevel, dictionary* ini);
	static void SetLevel(LoggingLevel level);
	static void SetLogFileAndConsole(bool logAndConsole);
	static LoggingLevel GetLevel();
	static void Info(const char* format, ...);
	static void Warning(const char* format, ...);
	static void Error(const char* format, ...);
	static void Close();
	static void LogIt(LoggingLevel loggingLevel, const char* marker, const char* format, va_list args);

private:
	static void RedirectIOToConsole();
	static void RollLog();
};

#endif // LOG_H