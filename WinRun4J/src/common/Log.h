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

enum LoggingLevel { info = 0, warning, error, none };

struct Log {
	static void Init(HINSTANCE hInstance, const char* logfile, const char* loglevel);
	static void SetLevel(LoggingLevel level);
	static void Info(const char* format, ...);
	static void Warning(const char* format, ...);
	static void Error(const char* format, ...);
	static void Close();

	// Used for excel error function
	static void SetLastError(const char* format, ...);
	static const char* GetLastError();

private:
	static void LogIt(const char* format, ...);
	static void RedirectIOToConsole();
};

#endif // LOG_H