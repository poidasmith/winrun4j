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
#include <stdio.h>

// Set icon on exe file
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
	if(!hUpdate) {
		Log::Error("Could not load exe to set icon: %s", exeFile);
		return false;
	}

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

// Add icon to exe file
bool Resource::AddIcon(LPSTR exeFile, LPSTR iconFile)
{
	// Find the resource indices that are available
	HMODULE hm = LoadLibrary(exeFile);
	if(!hm) {
		Log::Error("Could not load exe to add icon: %s", exeFile);
		return false;
	}
	int gresId = 1;
	HRSRC hr = 0;
	while((hr = FindResource(hm, MAKEINTRESOURCE(gresId), RT_GROUP_ICON)) != NULL)
		gresId++;
	int iresId = 1;
	while((hr = FindResource(hm, MAKEINTRESOURCE(iresId), RT_ICON)) != NULL)
		iresId++;
	FreeLibrary(hm);

	// Read icon file
	ICONHEADER* pHeader;
	ICONIMAGE** pIcons;
	GRPICONHEADER* pGrpHeader;
	bool res = LoadIcon(iconFile, pHeader, pIcons, pGrpHeader, iresId);
	if(!res) {
		return false;
	}

	// Copy in resources
	HANDLE hUpdate = BeginUpdateResource(exeFile, FALSE);
	if(!hUpdate) {
		Log::Error("Could not load exe to add icon: %s", exeFile);
		return false;
	}

	// Copy in icon group resource
	if(!UpdateResource(hUpdate, RT_GROUP_ICON, MAKEINTRESOURCE(gresId), MAKELANGID(LANG_NEUTRAL, SUBLANG_NEUTRAL),
		pGrpHeader, sizeof(WORD)*3+pHeader->count*sizeof(GRPICONENTRY)))
		Log::Error("Could not insert group icon into binary");

	// Copy in icons
	for(int i = 0; i < pHeader->count; i++) {
		if(!UpdateResource(hUpdate, RT_ICON, MAKEINTRESOURCE(i + iresId), MAKELANGID(LANG_ENGLISH, SUBLANG_ENGLISH_US),
			pIcons[i], pHeader->entries[i].bytesInRes))
			Log::Error("Could not insert icon into binary");
	}

	// Commit the changes
	EndUpdateResource(hUpdate, FALSE);

	return true;
}

// Set the INI file
bool Resource::SetINI(LPSTR exeFile, LPSTR iniFile)
{
	return SetFile(exeFile, iniFile, RT_INI_FILE, MAKEINTRESOURCE(1), INI_RES_MAGIC, true);
}

// Set the splash file
bool Resource::SetSplash(LPSTR exeFile, LPSTR splashFile)
{
	return SetFile(exeFile, splashFile, RT_SPLASH_FILE, MAKEINTRESOURCE(1), 0, false);
}

// Set the manifest file
bool Resource::SetManifest(LPSTR exeFile, LPSTR manifestFile)
{
	return SetFile(exeFile, manifestFile, RT_MANIFEST, MAKEINTRESOURCE(1), 0, true);
}

// Prints the contents of the  INI  file
bool Resource::ListINI(LPSTR exeFile)
{
	HMODULE hm = LoadLibrary(exeFile);
	if(!hm) {
		Log::Error("Could not load exe to list INI contents: %s", exeFile);
		return false;
	}
	
	HRSRC h = FindResource(hm, MAKEINTRESOURCE(1), RT_INI_FILE);
	if(!h) {
		Log::Error("Could not find INI resource", exeFile);
		return false;
	}
	HGLOBAL hg = LoadResource(hm, h);
	PBYTE pb = (PBYTE) LockResource(hg);
	DWORD* pd = (DWORD*) pb;
	if(*pd == INI_RES_MAGIC) {
		puts((char *) &pb[4]);
		puts("\n");
	} else {
		printf("Unknown resource\n");
	}

	FreeLibrary(hm);
	return true;
}

// Add JAR file
bool Resource::AddJar(LPSTR exeFile, LPSTR jarFile)
{
	// Extract just the filename from the jar file path
	char jarName[MAX_PATH];
	int len = strlen(jarFile) - 1;
	while(len > 0) {
		if(jarFile[len] == '\\' || jarFile[len] == '/') 
			break;
		len--;
	}
	if(len == 0) len--;
	strcpy(jarName, &jarFile[len+1]);

	// Find next available slot for JAR file
	HMODULE hm = LoadLibrary(exeFile);
	if(!hm) {
		Log::Error("Could not load exe to add JAR: %s", exeFile);
		return false;
	}
	int resId = 1;
	HRSRC hr = 0;
	while((hr = FindResource(hm, MAKEINTRESOURCE(resId), RT_JAR_FILE)) != NULL) {
		// Check for jar with matching name - if found we want to overwrite
		HGLOBAL hg = LoadResource(hm, hr);
		PBYTE pb = (PBYTE) LockResource(hg);
		DWORD* pd = (DWORD*) pb;
		if(*pd == JAR_RES_MAGIC) {
			int len = strlen((char*) &pb[RES_MAGIC_SIZE]);
			if(strcmp(jarName, (char*) &pb[RES_MAGIC_SIZE]) == 0) {
				break;
			}
		}

		resId++;
	}
	FreeLibrary(hm);

	// Read the JAR file
	HANDLE hFile = CreateFile(TEXT(jarFile), GENERIC_READ, FILE_SHARE_READ, NULL, OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL, NULL);
	if(hFile == INVALID_HANDLE_VALUE) {
		Log::Error("Could not open JAR file: %s", jarFile);
		return false;
	}
	DWORD cbBuffer = GetFileSize(hFile, 0);
	DWORD cbPadding = RES_MAGIC_SIZE + strlen(jarName) + 1;
	PBYTE pBuffer = (PBYTE) malloc(cbBuffer + cbPadding);
	ReadFile(hFile, &pBuffer[cbPadding], cbBuffer, &cbBuffer, 0);

	// Create binary structure for jar file
	DWORD* pMagic = (DWORD*) pBuffer;
	*pMagic = JAR_RES_MAGIC;
	memcpy(&pBuffer[RES_MAGIC_SIZE], jarName, strlen(jarName) + 1);

	// Copy in resources
	HANDLE hUpdate = BeginUpdateResource(exeFile, FALSE);
	if(!hUpdate) {
		Log::Error("Could not load exe to add JAR: %s", exeFile);
		return false;
	}

	// Copy in JAR file
	UpdateResource(hUpdate, RT_JAR_FILE, MAKEINTRESOURCE(resId), MAKELANGID(LANG_NEUTRAL, SUBLANG_NEUTRAL),
		pBuffer, cbBuffer + cbPadding);

	// Commit the changes
	EndUpdateResource(hUpdate, FALSE);

	return true;
}

// Add JAR file
bool Resource::AddHTML(LPSTR exeFile, LPSTR htmlFile)
{
	// Extract just the filename from the jar file path
	char htmlName[MAX_PATH];
	int len = strlen(htmlFile) - 1;
	while(len > 0) {
		if(htmlFile[len] == '\\' || htmlFile[len] == '/') 
			break;
		len--;
	}
	if(len == 0) len--;
	strcpy(htmlName, &htmlFile[len+1]);

	// Named resources must be in uppercase...
	len = strlen(htmlName);
	for(int i = 0; i < len; i++) {
		htmlName[i] = toupper(htmlName[i]);
	}

	return SetFile(exeFile, htmlFile, RT_HTML, htmlName, 0, false);
}

bool Resource::SetFile(LPSTR exeFile, LPSTR resFile, LPCTSTR lpType, LPCTSTR lpName, DWORD magic, bool zeroTerminate)
{
	// Read the INI file
	HANDLE hFile = CreateFile(TEXT(resFile), GENERIC_READ, FILE_SHARE_READ, NULL, OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL, NULL);
	if(hFile == INVALID_HANDLE_VALUE) {
		Log::Error("Could not open resource file: %s", resFile);
		return false;
	}
	DWORD cbBuffer = GetFileSize(hFile, 0);
	DWORD ztPadding = zeroTerminate ? 1 : 0;
	DWORD magicSize = magic == 0 ? 0 : RES_MAGIC_SIZE;
	PBYTE pBuffer = (PBYTE) malloc(cbBuffer + magicSize + ztPadding);
	BOOL rfRes = ReadFile(hFile, &pBuffer[magicSize], cbBuffer, &cbBuffer, 0);
	if(!rfRes) {
		Log::Error("Could not read in resource file: %s", resFile);
		return false;
	}
	if(magic) {
		DWORD* pMagic = (DWORD*) pBuffer;
		*pMagic = magic;
	}
	if(zeroTerminate) 
		pBuffer[cbBuffer + magicSize] = 0;

	// Copy in resources
	HANDLE hUpdate = BeginUpdateResource(exeFile, FALSE);
	if(!hUpdate) {
		Log::Error("Could not load exe to load resource: %s", exeFile);
		return false;
	}

	// Copy in resource file
	if(!UpdateResource(hUpdate, lpType, lpName, MAKELANGID(LANG_NEUTRAL, SUBLANG_NEUTRAL),
		pBuffer, cbBuffer + RES_MAGIC_SIZE + ztPadding))
		Log::Error("Could not insert resource into binary");

	// Commit the changes
	EndUpdateResource(hUpdate, FALSE);

	return true;
}

// Load an icon image from a file
bool Resource::LoadIcon(LPSTR iconFile, ICONHEADER*& pHeader, ICONIMAGE**& pIcons, GRPICONHEADER*& pGrpHeader, int index)
{
	HANDLE hFile = CreateFile(TEXT(iconFile), GENERIC_READ, FILE_SHARE_READ, NULL, OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL, NULL);
	if(hFile == INVALID_HANDLE_VALUE) {
		Log::Error("Could not open icon file: %s", iconFile);
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

BOOL EnumLangsFunc(HANDLE hModule, LPCTSTR lpType, LPCTSTR lpName, WORD wLang, LONG lParam)
{
	ResourceInfoList* pRil = (ResourceInfoList*) lParam;
	pRil->ri[pRil->count].lpType = lpType;
	if(IS_INTRESOURCE(lpName)) {
		pRil->ri[pRil->count].lpName = lpName;
	} else {
		pRil->ri[pRil->count].lpName = strdup(lpName);
	}
	pRil->ri[pRil->count].wLang = wLang;
	pRil->count++;
	return pRil->count < pRil->max;
}

BOOL EnumNamesFunc(HANDLE hModule, LPCTSTR lpType, LPTSTR lpName, LONG lParam)
{
	return EnumResourceLanguages((HMODULE) hModule, lpType, lpName, (ENUMRESLANGPROC) EnumLangsFunc, lParam);
}

BOOL EnumTypesFunc(HANDLE hModule, LPTSTR lpType, LONG lParam)
{
	return EnumResourceNames((HMODULE) hModule, lpType, (ENUMRESNAMEPROC) EnumNamesFunc, lParam);
}

bool Resource::ClearResources(LPSTR exeFile)
{
	HMODULE hMod = LoadLibrary(exeFile);
	if(!hMod) {
		Log::Error("Could not load exe to clear resources: %s", exeFile);
		return false;
	}

	ResourceInfoList ril;
	ril.ri = (ResourceInfo*) malloc(sizeof(ResourceInfo) * 100);
	ril.max = 100;
	ril.count = 0;
	EnumResourceTypes((HMODULE) hMod, (ENUMRESTYPEPROC) EnumTypesFunc, (LONG_PTR) &ril);
	FreeLibrary(hMod);

	// Open exe for update
	HANDLE hUpdate = BeginUpdateResource(exeFile, FALSE);
	if(!hUpdate) {
		Log::Error("Could not load exe to clear resources: %s", exeFile);
		return false;
	}

	for(int i = 0; i < ril.count; i++) {
		UpdateResource(hUpdate, ril.ri[i].lpType, ril.ri[i].lpName, ril.ri[i].wLang, 0, 0);
	}

	// Commit the changes
	EndUpdateResource(hUpdate, FALSE);

	// Free resources
	free(ril.ri);

	return true;
}

bool Resource::ListResources(LPSTR exeFile)
{
	HMODULE hMod = LoadLibrary(exeFile);
	if(!hMod) {
		Log::Error("Could not load exe to list resources: %s", exeFile);
		return false;
	}

	ResourceInfoList ril;
	ril.ri = (ResourceInfo*) malloc(sizeof(ResourceInfo) * 100);
	ril.max = 100;
	ril.count = 0;
	EnumResourceTypes((HMODULE) hMod, (ENUMRESTYPEPROC) EnumTypesFunc, (LONG_PTR) &ril);

	for(int i = 0; i < ril.count; i++) {
		LPCTSTR lpType = ril.ri[i].lpType;
		LPCTSTR lpName = ril.ri[i].lpName;
		if(lpType == RT_GROUP_ICON) {
			printf("Group Icon\t%04x\n", lpName);
		} else if(lpType == RT_ICON) {
			printf("Icon      \t%04x\n", lpName);
		} else if(lpType == RT_JAR_FILE) {
			HRSRC h = FindResource(hMod, lpName, lpType);
			HGLOBAL hg = LoadResource(hMod, h);
			PBYTE pb = (PBYTE) LockResource(hg);
			DWORD* pd = (DWORD*) pb;
			if(*pd == JAR_RES_MAGIC) {
				printf("JAR File  \t%s\n", &pb[4]);
			} else {
				printf("Unknown   \t%04x, %04x\n", lpType, lpName);
			}
		} else if(lpType == RT_INI_FILE) {
			printf("INI File\n");
		} else if(lpType == RT_SPLASH_FILE) {
			printf("Splash File\n");
		} else if(lpType == RT_ACCELERATOR) {
			printf("Accelerator\t%04x\n", lpName);
		} else if(lpType == RT_ANICURSOR) {
			printf("Ani Cursor\t%04x\n", lpName);
		} else if(lpType == RT_ANIICON) {
			printf("Ani Icon\t%04x\n", lpName);
		} else if(lpType == RT_BITMAP) {
			printf("Bitmap\t%04x\n", lpName);
		} else if(lpType == RT_CURSOR) {
			printf("Cursor\t%04x\n", lpName);
		} else if(lpType == RT_DIALOG) {
			printf("Dialog\t%04x\n", lpName);
		} else if(lpType == RT_DLGINCLUDE) {
			printf("Dialog Include\t%04x\n", lpName);
		} else if(lpType == RT_FONT) {
			printf("Font\t%04x\n", lpName);
		} else if(lpType == RT_FONTDIR) {
			printf("Font Dir\t%04x\n", lpName);
		} else if(lpType == RT_HTML) {
			printf("HTML\t\t%s\n", lpName);
		} else if(lpType == RT_GROUP_CURSOR) {
			printf("Group Cursor\t%04x\n", lpName);
		} else if(lpType == RT_MANIFEST) {
			printf("Manifest\t%04x\n", lpName);
		} else if(lpType == RT_MENU) {
			printf("Menu\t%04x\n", lpName);
		} else if(lpType == RT_MESSAGETABLE) {
			printf("Message Table\t%04x\n", lpName);
		} else if(lpType == RT_PLUGPLAY) {
			printf("Plug Play\t%04x\n", lpName);
		} else if(lpType == RT_RCDATA) {
			printf("RC Data\t%04x\n", lpName);
		} else if(lpType == RT_STRING) {
			printf("String\t%04x\n", lpName);
		} else if(lpType == RT_VERSION) {
			printf("Version\t%04x\n", lpName);
		} else if(lpType == RT_VXD) {
			printf("VXD\t%04x\n", lpName);
		} else {
			printf("Unknown   \t%04x, %04x\n", lpType, lpName);
		}
	}

	free(ril.ri);
	FreeLibrary(hMod);

	return true;
}
