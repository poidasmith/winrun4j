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
	static bool SetIcon(LPSTR exeFile, LPSTR iconFile);

private:	
	static void CopyToRandomAndRun() ;
	static void DeleteRandomFile(LPSTR commandLine);
	static void SetIcon(LPSTR commandLine);
};

#endif // ICON_UTILS_H