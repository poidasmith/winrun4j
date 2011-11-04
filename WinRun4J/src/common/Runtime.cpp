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

extern void _cdecl StrTruncate(LPSTR target, LPSTR source, size_t len)
{
	if(source == NULL) return;
	if(strlen(source) < len) {
		strcpy(target, source);
		return;
	}

	int i = 0;
	for(; i < len - 1; i++) {
		target[i] = source[i];
	}
	target[i] = 0;
}

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

extern bool _cdecl StrContains(LPSTR str, char c)
{
	unsigned int len = strlen(str);
	for(unsigned int i = 0; i < len; i++) {
		if(c == str[i]) {
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
		if(!StrContains(trimChars, c)) {
			start = i;
			break;
		}
	}
	for(int i = end; i >= 0; i--) {
		char c = str[i];
		if(!StrContains(trimChars, c)) {
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

extern void _cdecl ParseCommandLine(LPSTR lpCmdLine, TCHAR** args, UINT& count, bool includeFirst)
{
	// Bug fix here provided by Frederic.Canut@kxen.com 
	if(lpCmdLine == NULL || *lpCmdLine == 0) return;

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
/*
extern void _cdecl ParseCommandLine(LPSTR lpCmdLine, TCHAR** args, UINT& count, bool includeFirst)
{
	// Bug fix here provided by Frederic.Canut@kxen.com 
	if(lpCmdLine == NULL || *lpCmdLine == 0) return;

	StrTrim(lpCmdLine, " ");
	int len = strlen(lpCmdLine);
	if(len == 0) {
		return;
	}

	//values positions (like java - startPos inclusive, endPos exclusive)
	int startPos[1024], endPos[1024];

	//current index of value borders
	int currentIndex = -1;

	//if we are inside quotes so space/tab are no arg separators
	bool insideQuotes = false;

	//if we are between values so space/tab are arg separators
	bool insideArgSeparator = true;

	int i;
	//let's find value borders
	for(i = 0; i < len; i++) {

		char c = lpCmdLine[i];
		if (c == ' ' || c == '\t') {

			//space is no arg separator in quotes
			if (insideQuotes) {continue;};

			//ignore multiple arg separators
			if (insideArgSeparator) {continue;};

			//it is first arg separator so save the end border of value
			endPos[currentIndex] = i;

			//set we are in separator seeking mode
			insideArgSeparator = true;
		}
		else {

			if (insideArgSeparator) {

				//save start of value
				startPos[++currentIndex] = i;

				//set unknown end
				endPos[currentIndex] = -1;

				//set we are in value seeking mode
				insideArgSeparator = false;
			}

			if (c == '"') {

				insideQuotes = !insideQuotes;
			}
		}
	}//end of for(i = 0; i < len; i++) {

	if (endPos[currentIndex] < 0) {

		//set end position if it is still unknown
		endPos[currentIndex] = i;
	}

	int index = (includeFirst) ? count : 0;
	for (i = 0; i <= currentIndex; i++) {

		int begin = startPos[i];
		int end = endPos[i];
		
		if (lpCmdLine[begin] == '"' && lpCmdLine[end - 1] == '"') {

			//remove quotes
			begin++;
			end--;
		}
		int valueLen = end - begin;
		
		if (valueLen > 0) {
		
			TCHAR *value = (TCHAR *)malloc(sizeof(TCHAR) * (valueLen + 1));
			for (int a = 0; a < valueLen; a++) {
			
				value[a] = lpCmdLine[begin + a];
			}
			value[valueLen] = '\0';
				
			args[index++] = value;
		}
	}
	count = index;
}
*/
extern void _cdecl GetFileDirectory(LPSTR filename, LPSTR output)
{
	int len = strlen(filename);
	if(len == 0) {
		output[0] = 0;
		return;
	}
	int i = len-1;
	bool found = false;
	while(true) {
		if(filename[i] == '\\' || filename[i] == '/') {
			found = true;
			break;
		}
		if(i == 0)
			break;
		i--;
	}

	if(found) {
		i++;
		memcpy(output, filename, i);
		output[i] = 0;
	} else {
		output[0] = 0;
	}
}

extern void _cdecl GetFileName(LPSTR filename, LPSTR output)
{
	int len = strlen(filename);
	if(len == 0) {
		output[0] = 0;
		return;
	}
	int i = len-1;
	bool found = false;
	while(true) {
		if(filename[i] == '\\' || filename[i] == '/') {
			found = true;
			break;
		}
		if(i == 0)
			break;
		i--;
	}

	if(found) i++;
	strcpy(output, &filename[i]);
}

extern void _cdecl GetFileExtension(LPSTR filename, LPSTR output)
{
	int len = strlen(filename);
	if(len == 0) {
		output[0] = 0;
		return;
	}
	int i = len-1;
	bool found = false;
	while(true) {
		if(filename[i] == '.') {
			found = true;
			break;
		}
		if(i == 0)
			break;
		i--;
	}

	if(found)
		strcpy(output, &filename[i]);
	else
		output[0] = 0;
}

extern void _cdecl GetFileNameSansExtension(LPSTR filename, LPSTR output)
{
	int len = strlen(filename);
	int i = len-1;
	if(len == 0) {
		output[0] = 0;
		return;
	}
	int dotPos = -1;
	while(true) {
		if(dotPos == -1 && filename[i] == '.') 
			dotPos = i;
		if(dotPos != -1 && (filename[i] == '/' || filename[i] == '\\'))
			break;
		if(i == 0)
			break;
		i--;
	}

	if(dotPos != -1) {
		if(i > 0) i++;
		memcpy(output, &filename[i], dotPos - i);
		output[dotPos - i] = 0;
	} else {
		if(i > 0) i++;
		strcpy(output, &filename[i]);
	}
}

extern "C" char * _cdecl strrev(char *str)
{
	if(!str) return 0;
	char* rstr = str + strlen(str) - 1;
	char c;
	while(str<rstr) {
		c=*str;
		*str=*rstr;
		*rstr=c;
		str++;
		rstr--;
	}
	return str;
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

extern "C" void __cdecl _wassert(int e)
{
}

#ifdef TINY

extern "C" int __cdecl _purecall()
{
	return 0;
}

extern "C" int* _errno()
{
	return 0;
}

extern "C" void * __cdecl malloc(size_t size)
{
    return HeapAlloc( GetProcessHeap(), 0, size );
}

extern "C" void __cdecl free(void * p)
{
    HeapFree( GetProcessHeap(), 0, p );
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
	FILE* ret = (FILE *) malloc(sizeof(FILE));
	ret->_file = fd;
	ret->_base = 0;
	ret->_cnt = 0;
	ret->_ptr = NULL;
	ret->_flag = _IOREAD | _IOWRT;
	ret->_bufsiz = 0;
	ret->_charbuf = 0;

	return ret;
}

extern "C" int _cdecl _open_osfhandle(int c)
{
	return c;
}

extern "C" int __cdecl _fileno(FILE* _File)
{
	return _File->_file;
}

HANDLE __cdecl _get_osfhandle(int _FileHandle)
{
	return (HANDLE) _FileHandle;
}

#endif 
