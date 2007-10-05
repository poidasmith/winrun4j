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

#ifdef TINY

extern "C" char * _cdecl strdup(const char *str)
{
    char *r;
    if ((r = (char *)malloc(strlen(str) + 1)) == NULL)
		return 0;
    return strcpy (r, str);
}

extern "C" errno_t _cdecl strcpy_s(char *dest, rsize_t size, const char *source)
{
	strcpy(dest, source);
	return 0;
}

extern "C" char * _cdecl _strdup(const char *src)
{
	return strdup(src);
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
	return 0;
}

extern "C" FILE* _cdecl _fdopen(int fd, const char *mode)
{
	return 0;
}

extern "C" int _cdecl _open_osfhandle(int c)
{
	return 0;
}

#endif 

#ifdef TINY_DEPRECATED

extern "C" char * _cdecl strcat(char *dest, const char *source)
{
	return lstrcat(dest, source);
}

extern "C" int _cdecl strcmp(const char *str1, const char *str2)
{
	int len1 = strlen(str1);
	int len2 = strlen(str2);
	for(int i = 0; i < len1 && i < len2; i++) {
		if(str1[i] != str2[i])
			return str1[i] - str2[i];
	}
	if(len1 > len2) {
		return -1;
	} else if(len2 > len1) {
		return 1;
	} else {
		return 0;
	}
}

extern "C" size_t _cdecl strlen(const char *str)
{
	return lstrlen(str);
}

extern "C" const char * _cdecl strrchr(const char *str, int ch)
{
	for(int i = strlen(str) - 1; i >= 0; i--) 
		if(str[i] == ch)
			return &str[i];
	return 0;
}

extern "C" const char * _cdecl strchr(const char *str, int ch)
{
	int len = strlen(str);
	for(int i = 0; i < len; i++)
		if(str[i] == ch)
			return &str[i];
	return 0;
}

extern "C" char * _cdecl strcpy(char *dest, const char *source)
{
	return lstrcpy(dest, source);
}

extern "C" double _cdecl atof(const char *str)
{
	return 0.;
}

extern "C" int _cdecl strncmp(const char *str1, const char *str2, size_t count)
{
	for(int i = 0; i < count; i++) {
		if(str1[i] != str2[i])
			return str1[i] - str2[i];
	}
	return 0;
}

extern "C" void * _cdecl memcpy(void *dest, const void *source, size_t size)
{
	CopyMemory(dest, source, size);
	return dest;
}

extern "C" int _cdecl fclose(FILE *stream)
{
	return CloseHandle(stream);
}

extern "C" int _cdecl isdigit(int c)
{
	return 0;
}

extern "C" int _cdecl isspace(int c)
{
	return 0;
}

extern "C" char* _cdecl fgets(char *str, int n, FILE *stream)
{
	ReadFile(
	return 0;
}

extern "C" FILE* _cdecl fopen(const char* filename, const char* mode)
{
	DWORD desiredAccess;
	DWORD shareMode;
	switch(*mode) {
		case 'a':
			desiredAccess = FILE_WRITE_DATA;
			shareMode = FILE_SHARE_WRITE;
			break;
		case 'w':
			desiredAccess = FILE_WRITE_DATA;
			shareMode = FILE_SHARE_WRITE;
			break;
		case 'r':
			desiredAccess = FILE_READ_DATA;
			shareMode = FILE_SHARE_READ;
			break;
	}
	HANDLE hFile = CreateFile(filename, desiredAccess, shareMode, 0, 0, 0, 0);
	return hFile;
}

extern "C" int _cdecl _chkstk(int c)
{
	return 0;
}

extern "C" int _cdecl tolower(int c)
{
	return (int) CharLower((LPSTR) c);
}

extern "C" void * _cdecl memset(void *dst, int val, size_t size)
{
	return 0;
}

extern "C" char * _cdecl strtok(char *str, const char *delim)
{
	return 0;
}

extern "C" FILE* _cdecl freopen(const char* filename, const char* mode, FILE* filea)
{
	return 0;
}

extern "C" long _cdecl strtol(const char *str, char **endptr, int radix)
{
	return 0;
}

extern "C" int _cdecl sscanf(const char* src, const char* format, ...)
{
	return 0;
}

extern "C" int _cdecl toupper(int c)
{
	return (int) CharUpper((LPSTR) c);
}

extern "C" int _cdecl rand()
{
	return 0;
}

extern "C" int _cdecl _fltused()
{
	return 0;
}

extern "C" int _cdecl fprintf(FILE* file, const char *format, ...)
{
	va_list args;
	va_start(args, format);
	return wsprintf((LPSTR) format, args);
}

extern "C" int _cdecl vprintf(const char *format, va_list argptr)
{
	return printf(format, argptr);
}

extern "C" void _cdecl srand(unsigned int seed)
{
}

extern "C" int _cdecl fflush(FILE* file)
{
	return 0;
}

extern "C" int _cdecl vsprintf(char *buffer, const char *format, va_list argptr)
{
	return wvsprintf(buffer, format, argptr);
}

#endif 
