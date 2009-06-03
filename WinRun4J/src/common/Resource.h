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
	static bool AddIcon(LPSTR exeFile, LPSTR iconFile);
	static bool SetINI(LPSTR exeFile, LPSTR iniFile);
	static bool AddJar(LPSTR exeFile, LPSTR jarFile);
	static bool AddHTML(LPSTR exeFile, LPSTR htmlFile);
	static bool SetSplash(LPSTR exeFile, LPSTR splashFile);
	static bool SetManifest(LPSTR exeFile, LPSTR manifestFile);
	static bool ClearResources(LPSTR exeFile);
	static bool ListResources(LPSTR exeFile);
	static bool ListINI(LPSTR exeFile);

private:
	static bool SetFile(LPSTR exeFile, LPSTR resFile, LPCTSTR lpType, LPCTSTR lpName, DWORD magic, bool zeroTerminate);
	static bool LoadIcon(LPSTR iconFile, ICONHEADER*& pHeader, ICONIMAGE**& pIcons, GRPICONHEADER*& pGrpHeader, int index = 0);
};