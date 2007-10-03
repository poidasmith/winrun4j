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

extern "C" char * _cdecl strdup(const char *s)
{
    char *r;

    if ((r = (char *)malloc(strlen(s) + 1)) == NULL) {
		return 0;
    }
    strcpy (r, s);
    return r;
}

extern "C" char * _cdecl strcat(char *dest, const char *source)
{
	return 0;
}

extern "C" int _cdecl strcmp(const char *s1, const char *s2)
{
	return 0;
}

extern "C" size_t _cdecl strlen(const char *s)
{
	return lstrlen(s);
}

extern "C" void _cdecl sprintf_s(char *_Str, const char *_Fmt, ...)
{
}

extern "C" const char * _cdecl strrchr(const char *_Str, int _Ch)
{
	return 0;
}

extern "C" const char * _cdecl strchr(const char *_Str, int _Ch)
{
	return 0;
}

extern "C" char * _cdecl strcpy(char *_Dest, const char *_Source)
{
	return 0;
}

extern "C" errno_t _cdecl strcpy_s(char *_Dst, rsize_t _DstSize, const char *_Src)
{
	return 0;
}

extern "C" char * _cdecl _strdup(const char *_Src)
{
	return 0;
}

extern "C" double _cdecl atof(const char *_String)
{
	return 0.;
}

extern "C" int _cdecl strncmp(const char *_Str1, const char *_Str2, size_t _MaxCount)
{
	return 0;
}

extern "C" void * _cdecl memcpy(void *_Dst, const void *_Src, size_t _Size)
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
