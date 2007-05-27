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

#include <windows.h>

enum LoggingLevel { info = 0, warning, error };

struct Log {
	static void Init(HINSTANCE hInstance, const char* logfile);
	static void SetLevel(LoggingLevel level);
	static void Info(const char* format, ...);
	static void Warning(const char* format, ...);
	static void Error(const char* format, ...);
	static void Close();

private:
	static void LogIt(const char* format, ...);
	static void RedirectIOToConsole();
};

#endif // LOG_H