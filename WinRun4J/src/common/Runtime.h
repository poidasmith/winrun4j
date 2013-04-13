/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Peter Smith
 *******************************************************************************/

#ifndef RUNTIME_H
#define RUNTIME_H

#define WIN32_LEAN_MEAN
#include <windows.h>

// Tags for embedded resources
#define RT_INI_FILE MAKEINTRESOURCE(687)
#define RT_JAR_FILE MAKEINTRESOURCE(688)
#define RT_SPLASH_FILE MAKEINTRESOURCE(689)
#define RES_MAGIC_SIZE 4
#define INI_RES_MAGIC MAKEFOURCC('I','N','I',' ')
#define JAR_RES_MAGIC MAKEFOURCC('J','A','R',' ')

extern LPSTR _cdecl StripArg0(LPSTR lpCmdLine);
extern size_t _cdecl FindNextArg(LPSTR lpCmdLine, size_t start, size_t len);
extern bool _cdecl StartsWith(LPSTR str, LPSTR substr);
extern bool _cdecl StrContains(LPSTR str, char c);
extern void _cdecl StrReplace(LPSTR str, char old, char nu);
extern void _cdecl StrTrim(LPSTR str, LPSTR trimChars);
extern void _cdecl StrTruncate(LPSTR target, LPSTR source, size_t len);
extern void _cdecl ParseCommandLine(LPSTR lpCmdLine, TCHAR** args, UINT& count, bool includeFirst = false);
extern void _cdecl GetFileDirectory(LPSTR filename, LPSTR output);
extern void _cdecl GetFileName(LPSTR filename, LPSTR output);
extern void _cdecl GetFileExtension(LPSTR filename, LPSTR output);
extern void _cdecl GetFileNameSansExtension(LPSTR filename, LPSTR output);

#endif 