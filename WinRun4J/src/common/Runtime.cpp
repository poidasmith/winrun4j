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

#ifdef TINY

extern "C" char * _cdecl strdup(const char *str)
{
    char *r;

    if ((r = (char *)malloc(strlen(str) + 1)) == NULL) {
		return 0;
    }
    strcpy (r, str);
    return r;
}

extern "C" char * _cdecl strcat(char *dest, const char *source)
{
	return 0;
}

extern "C" int _cdecl strcmp(const char *str1, const char *str2)
{
	return 0;
}

extern "C" size_t _cdecl strlen(const char *str)
{
	return lstrlen(str);
}

extern "C" void _cdecl sprintf_s(char *str, const char *fmt, ...)
{
}

extern "C" const char * _cdecl strrchr(const char *str, int ch)
{
	return 0;
}

extern "C" const char * _cdecl strchr(const char *str, int ch)
{
	return 0;
}

extern "C" char * _cdecl strcpy(char *dest, const char *source)
{
	return 0;
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
	return 0;
}

extern "C" void _cdecl fclose()
{
}

extern "C" int _cdecl isdigit(int c)
{
	return 0;
}

extern "C" int _cdecl isspace(int c)
{
	return 0;
}

extern "C" int _cdecl fgets(int c)
{
	return 0;
}

extern "C" int _cdecl fopen(int c)
{
	return 0;
}

extern "C" int _cdecl _chkstk(int c)
{
	return 0;
}

extern "C" int _cdecl tolower(int c)
{
	return 0;
}

extern "C" void * _cdecl memset(void *dst, int val, size_t size)
{
	return 0;
}

extern "C" int _cdecl vsprintf_s(int c)
{
	return 0;
}

extern "C" char * _cdecl strtok(char *str, const char *delim)
{
	return 0;
}

extern "C" int _cdecl _ftol_sse(int c)
{
	return 0;
}

extern "C" int _cdecl _fdopen(int c)
{
	return 0;
}

extern "C" int _cdecl _open_osfhandle(int c)
{
	return 0;
}

extern "C" int _cdecl freopen(int c)
{
	return 0;
}

extern "C" long _cdecl strtol(const char *str, char **endptr, int radix)
{
	return 0;
}

extern "C" int _cdecl sscanf(int c)
{
	return 0;
}

extern "C" int _cdecl toupper(int c)
{
	return 0;
}

extern "C" int _cdecl rand()
{
	return 0;
}

extern "C" int _cdecl _fltused()
{
	return 0;
}

extern "C" int _cdecl fprintf()
{
	return 0;
}

extern "C" int _cdecl vprintf()
{
	return 0;
}

extern "C" void _cdecl srand(unsigned int seed)
{
}

extern "C" int _cdecl setvbuf()
{
	return 0;
}

extern "C" int _cdecl fflush()
{
	return 0;
}

extern "C" int _cdecl vsprintf()
{
	return 0;
}

extern "C" int _cdecl _ftol2_sse()
{
	return 0;
}

extern "C" int _cdecl __iob_func()
{
	return 0;
}

#endif 
