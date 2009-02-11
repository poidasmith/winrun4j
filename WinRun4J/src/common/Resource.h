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

typedef struct 
{
	const TCHAR* comments;
	const TCHAR* companyName;
	const TCHAR* fileDescription;
	const TCHAR* fileVersion;
	const TCHAR* internalName;
	const TCHAR* legalCopyright;
	const TCHAR* legalTrademarks;
	const TCHAR* originalFilename;
	const TCHAR* privateBuild;
	const TCHAR* productName;
	const TCHAR* productVersion;
	const TCHAR* specialBuild;
} VERINFO;

typedef struct 
{
	BYTE width;
	BYTE height;
	BYTE colorCount;
	BYTE reserved;
	WORD planes;
	WORD bitCount;
	DWORD bytesInRes;
	DWORD imageOffset;
} ICONENTRY;

typedef struct  
{
	WORD reserved;
	WORD type;
	WORD count;
	ICONENTRY entries[1];
} ICONHEADER;

typedef struct 
{
	BITMAPINFOHEADER header;
	RGBQUAD colors;
	BYTE xors[1];
	BYTE ands[1];
} ICONIMAGE;

#pragma pack(push)
#pragma pack(2)

typedef struct
{
	BYTE width;
	BYTE height;
	BYTE colourCount;
	BYTE reserved;
	BYTE planes;
	BYTE bitCount;
	WORD bytesInRes;
	WORD bytesInRes2;
	WORD reserved2;
	WORD id;
} GRPICONENTRY;

typedef struct 
{
	WORD reserved;
	WORD type;
	WORD count;
	GRPICONENTRY entries[1];
} GRPICONHEADER;

#pragma pack(pop)

class Resource
{
public:
	static bool SetIcon(LPSTR exeFile, LPSTR iconFile);
	static int AddIcon(LPSTR exeFile, LPSTR iconFile);
	static int SetINI(LPSTR exeFile, LPSTR iniFile);
	static int AddJar(LPSTR exeFile, LPSTR jarFile);
	static int SetSplash(LPSTR exeFile, LPSTR splashFile);
	static int SetVersionInformation(LPSTR exeFile, VERINFO& info);

private:
	static bool LoadIcon(LPSTR iconFile, ICONHEADER*& pHeader, ICONIMAGE**& pIcons, GRPICONHEADER*& pGrpHeader);
};