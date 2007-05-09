/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Peter Smith
 *******************************************************************************/

#ifndef LOG_UTILS_H
#define LOG_UTILS_H

#include <windows.h>

extern void LogInit(HINSTANCE hInstance, const char* logfile);
extern void Log(const char* format, ...);
extern void LogClose();

#endif // LOG_UTILS_H