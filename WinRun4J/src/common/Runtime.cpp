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