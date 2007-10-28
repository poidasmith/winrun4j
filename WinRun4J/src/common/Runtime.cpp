/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Peter Smith
 *******************************************************************************/

#include "Runtime.h"
#include <stdio.h>

extern bool _cdecl StartsWith(LPSTR str, LPSTR substr)
{
	return strncmp(str, substr, strlen(substr)) == 0;
}

extern LPSTR _cdecl StripArg0(LPSTR lpCmdLine)
{
	int len = strlen(lpCmdLine);
	int point = FindNextArg(lpCmdLine, 0, len);

	return &lpCmdLine[point];
}

extern size_t _cdecl FindNextArg(LPSTR lpCmdLine, size_t start, size_t len)
{
	bool found = false;

	for(; start < len; start++) {
		char c = lpCmdLine[start];
		if(c == '\"') {
			found = !found;
		} else if(c == ' ') {
			if(!found) break;
		}
	}
	return start == len ? start : start + 1;
}

extern bool _cdecl StrTrimInChars(LPSTR trimChars, char c)
{
	unsigned int len = strlen(trimChars);
	for(unsigned int i = 0; i < len; i++) {
		if(c == trimChars[i]) {
			return true;
		}
	}
	return false;
}

extern void _cdecl StrTrim(LPSTR str, LPSTR trimChars)
{
	unsigned int start = 0;
	unsigned int end = strlen(str) - 1;
	for(unsigned int i = 0; i < end; i++) {
		char c = str[i];
		if(!StrTrimInChars(trimChars, c)) {
			start = i;
			break;
		}
	}
	for(int i = end; i >= 0; i--) {
		char c = str[i];
		if(!StrTrimInChars(trimChars, c)) {
			end = i;
			break;
		}
	}
	if(start != 0 || end != strlen(str) - 1) {
		int k = 0;
		for(unsigned int i = start; i <= end; i++, k++) {
			str[k] = str[i];
		}
		str[k] = 0;
	}
}

extern void _cdecl ParseCommandLine(LPSTR lpCmdLine, TCHAR** args, int& count, bool includeFirst)
{
	StrTrim(lpCmdLine, " ");
	int len = strlen(lpCmdLine);
	if(len == 0) {
		return;
	}

	int start = 0;
	bool quote = false;
	bool first = true;
	TCHAR arg[4096];
	for(int i = 0; i < len; i++) {
		char c = lpCmdLine[i];
		if(c == '\"') {
			quote = !quote;
		} else if(!quote && c == ' ') {
			if(!first || includeFirst) {
				int k = 0;
				for(int j = start; j < i; j++, k++) {
					arg[k] = lpCmdLine[j];
				}
				arg[k] = 0;
				args[count] = strdup(arg);
				StrTrim(args[count], " ");
				StrTrim(args[count], "\"");
				count++;
			}
			start = i;
			first = false;
		}
	}

	// Add the last one
	if(!first || includeFirst) {
		int k = 0;
		for(int j = start; j < len; j++, k++) {
			arg[k] = lpCmdLine[j];
		}
		arg[k] = 0;
		args[count] = _strdup(arg);
		StrTrim(args[count], " ");
		StrTrim(args[count], "\"");
		count++;
	}
}

#ifdef TINY

extern "C" void * __cdecl malloc(size_t size)
{
    return HeapAlloc( GetProcessHeap(), 0, size );
}

extern "C" void __cdecl free(void * p)
{
    HeapFree( GetProcessHeap(), 0, p );
}

extern "C" char * _cdecl strdup(const char *str)
{
    char *r;
    if ((r = (char *)malloc(strlen(str) + 1)) == NULL)
		return 0;
    return strcpy (r, str);
}

extern "C" char * _cdecl _strdup(const char *src)
{
	return strdup(src);
}

extern "C" errno_t _cdecl strcpy_s(char *dest, rsize_t size, const char *source)
{
	strcpy(dest, source);
	return 0;
}

extern "C" int _cdecl vsprintf_s(char *buffer, size_t sizeInBytes, const char *format, va_list argptr)
{
	return vsprintf(buffer, format, argptr);
}

extern "C" int _cdecl _ftol_sse(int c)
{
	return 0;
}

extern "C" int _cdecl setvbuf(FILE* file, char* buf, int mode, size_t size)
{
	return 0;
}

extern "C" int _cdecl _ftol2_sse()
{
	return 0;
}

extern "C" FILE* _cdecl __iob_func()
{
	static FILE _iob[3] = {
	  { NULL, 0, NULL, 0, 0, 0, 0 },
	  { NULL, 0, NULL, 0, 1, 0, 0 },
	  { NULL, 0, NULL, 0, 2, 0, 0 }
	};

	return _iob;
}

extern "C" FILE* _cdecl _fdopen(int fd, const char *mode)
{
	FILE* ret = (FILE *) sizeof(FILE);
	ret->_file = fd;
	ret->_base = 0;
	ret->_cnt = 0;
	ret->_ptr = NULL;
	ret->_flag = _IOREAD | _IOWRT;

	return ret;
}

extern "C" int _cdecl _open_osfhandle(int c)
{
	return c;
}

#endif 
