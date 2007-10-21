/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Peter Smith
 *******************************************************************************/

#ifndef ICON_H
#define ICON_H

#include "Runtime.h"
#include <stdio.h>

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

struct Icon {
	static void SetExeIcon(LPSTR commandLine);
	static void AddExeIcon(LPSTR commandLine);
	static void RemoveExeIcons(LPSTR commandLine);
	static bool SetIcon(LPSTR exeFile, LPSTR iconFile);
	static bool AddIcon(LPSTR exeFile, LPSTR iconFile);
	static bool RemoveIcon(LPSTR exeFile, int index);

private:	
	static void CopyToRandomAndRun(LPSTR command) ;
	static void DeleteRandomFile(LPSTR commandLine);
	static void SetIcon(LPSTR commandLine);
	static void AddIcon(LPSTR commandLine);
	static void RemoveIcons(LPSTR commandLine);
	static bool LoadIcon(LPSTR iconFile, ICONHEADER*& pHeader, ICONIMAGE**& pIcons, GRPICONHEADER*& pGrpHeader);
	static int FindNextId(HMODULE hModule);
	static void RunDeleteRandom(LPSTR filename, LPSTR cmd);
	static void GetFilenames(LPSTR commandLine, LPSTR filename, LPSTR iconfile);
	static bool RemoveIconResources(LPSTR exeFile);
};

#endif // ICON_UTILS_H