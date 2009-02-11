/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Peter Smith
 *******************************************************************************/

#include "Resource.h"
#include "Log.h"

// Set icon on original exe file
bool Resource::SetIcon(LPSTR exeFile, LPSTR iconFile)
{
	// Read icon file
	ICONHEADER* pHeader;
	ICONIMAGE** pIcons;
	GRPICONHEADER* pGrpHeader;
	bool res = LoadIcon(iconFile, pHeader, pIcons, pGrpHeader);
	if(!res) {
		return false;
	}

	// Copy in resources
	HANDLE hUpdate = BeginUpdateResource(exeFile, FALSE);

	// Copy in icon group resource
	UpdateResource(hUpdate, RT_GROUP_ICON, MAKEINTRESOURCE(1), MAKELANGID(LANG_NEUTRAL, SUBLANG_NEUTRAL),
		pGrpHeader, sizeof(WORD)*3+pHeader->count*sizeof(GRPICONENTRY));

	// Copy in icons
	for(int i = 0; i < pHeader->count; i++) {
		UpdateResource(hUpdate, RT_ICON, MAKEINTRESOURCE(i + 1), MAKELANGID(LANG_ENGLISH, SUBLANG_ENGLISH_US),
			pIcons[i], pHeader->entries[i].bytesInRes);
	}

	// Commit the changes
	EndUpdateResource(hUpdate, FALSE);

	return true;
}

// Load an icon image from a file
bool Resource::LoadIcon(LPSTR iconFile, ICONHEADER*& pHeader, ICONIMAGE**& pIcons, GRPICONHEADER*& pGrpHeader, int index)
{
	HANDLE hFile = CreateFile(TEXT(iconFile), GENERIC_READ, FILE_SHARE_READ, NULL, OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL, NULL);
	if(hFile == INVALID_HANDLE_VALUE) {
		Log::Error("ERROR: Could not open icon file: %s", iconFile);
		return false;
	}

	pHeader = (ICONHEADER*) malloc(sizeof(ICONHEADER));
	DWORD bytesRead;
	ReadFile(hFile, &(pHeader->reserved), sizeof(WORD), &bytesRead, NULL);
	ReadFile(hFile, &(pHeader->type), sizeof(WORD), &bytesRead, NULL);
	ReadFile(hFile, &(pHeader->count), sizeof(WORD), &bytesRead, NULL);
	pHeader = (ICONHEADER*) realloc(pHeader, sizeof(WORD)*3+sizeof(ICONENTRY)*pHeader->count);
	ReadFile(hFile, pHeader->entries, pHeader->count*sizeof(ICONENTRY), &bytesRead, NULL);
	pIcons = (ICONIMAGE**) malloc(sizeof(ICONIMAGE*)*pHeader->count);
	for(int i = 0 ; i < pHeader->count; i++) {
		pIcons[i] = (ICONIMAGE*) malloc(pHeader->entries[i].bytesInRes);
		SetFilePointer(hFile, pHeader->entries[i].imageOffset, NULL, FILE_BEGIN);
		ReadFile(hFile, pIcons[i], pHeader->entries[i].bytesInRes, &bytesRead, NULL);
	}

	// Convert to resource format
	pGrpHeader = (GRPICONHEADER*) malloc(sizeof(WORD)*3+pHeader->count*sizeof(GRPICONENTRY));
	pGrpHeader->reserved = 0;
	pGrpHeader->type = 1;
	pGrpHeader->count = pHeader->count;
	for(int i = 0; i < pHeader->count; i++) {
		ICONENTRY* icon = &pHeader->entries[i];
		GRPICONENTRY* entry = &pGrpHeader->entries[i];
		entry->bitCount = 0;
		entry->bytesInRes = icon->bitCount;
		entry->bytesInRes2 = (WORD)icon->bytesInRes;
		entry->colourCount = icon->colorCount;
		entry->height = icon->height;
		entry->id = (WORD)(i+1+index);
		entry->planes = (BYTE)icon->planes;
		entry->reserved = icon->reserved;
		entry->width = icon->width;
		entry->reserved2 = 0;
	}

	// Close handles
	CloseHandle(hFile);

	return true;
}

typedef struct
{
	LPCTSTR lpType;
	LPCTSTR lpName;
	WORD wLang;
} ResourceInfo;

typedef struct
{
	WORD count;
	WORD max;
	ResourceInfo* ri;
} ResourceInfoList;

BOOL ClearEnumLangsFunc(HANDLE hModule, LPCTSTR lpType, LPCTSTR lpName, WORD wLang, LONG lParam)
{
	ResourceInfoList* pRil = (ResourceInfoList*) lParam;
	pRil->ri[pRil->count].lpType = lpType;
	pRil->ri[pRil->count].lpName = lpName;
	pRil->ri[pRil->count].wLang = wLang;
	pRil->count++;
	return pRil->count < pRil->max;
}

BOOL ClearEnumNamesFunc(HANDLE hModule, LPCTSTR lpType, LPTSTR lpName, LONG lParam)
{
	EnumResourceLanguages((HMODULE) hModule, lpType, lpName, (ENUMRESLANGPROC) ClearEnumLangsFunc, lParam);
	return TRUE;
}

BOOL ClearEnumTypesFunc(HANDLE hModule, LPTSTR lpType, LONG lParam)
{
	EnumResourceNames((HMODULE) hModule, lpType, (ENUMRESNAMEPROC) ClearEnumNamesFunc, lParam);
	return TRUE;
}

int Resource::ClearResources(LPSTR exeFile)
{
	HMODULE hMod = LoadLibrary(exeFile);
	if(!hMod)
		return 1;

	ResourceInfoList ril;
	ril.ri = (ResourceInfo*) malloc(sizeof(ResourceInfo) * 100);
	ril.max = 100;
	ril.count = 0;
	EnumResourceTypes((HMODULE) hMod, (ENUMRESTYPEPROC) ClearEnumTypesFunc, (LONG_PTR) &ril);
	FreeLibrary(hMod);

	// Open exe for update
	HANDLE hUpdate = BeginUpdateResource(exeFile, FALSE);
	if(!hUpdate) {
		return 1;
	}

	for(int i = 0; i < ril.count; i++) {
		UpdateResource(hUpdate, ril.ri[i].lpType, ril.ri[i].lpName, ril.ri[i].wLang, 0, 0);
	}

	// Commit the changes
	EndUpdateResource(hUpdate, FALSE);

	// Free resources
	free(ril.ri);

	return 0;
}
