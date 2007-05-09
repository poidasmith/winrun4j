/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Peter Smith
 *******************************************************************************/

#include "IconUtils.h"
#include "LogUtils.h"

void SetExeIcon()
{
	// Assume the ico is named "appname.ico"
	TCHAR filename[MAX_PATH], tempfile[MAX_PATH], iconfile[MAX_PATH];
	GetModuleFileName(NULL, filename, sizeof(filename));
	int len = strlen(filename);
	strcpy_s(tempfile, sizeof(tempfile), filename);
	tempfile[len - 4]=0;
	strcat_s(tempfile, sizeof(tempfile), ".seticon.exe");
	strcpy_s(iconfile, sizeof(iconfile), filename);
	iconfile[len - 1] = 'o';
	iconfile[len - 2] = 'c';
	iconfile[len - 3] = 'i';

	Log("Setting icon file...\n");
	Log("Icon File: %s\n", iconfile);
	Log("Exe File: %s\n", tempfile);

	// Copy executable to a temporary location for editing
	BOOL result = CopyFile(filename, tempfile, false);
	if(!result) {
		Log("ERROR: Could not copy tempfile\n");
		return;
	}

	// Read icon file
	SECURITY_ATTRIBUTES attrs;
	HANDLE hFile = CreateFile(TEXT(iconfile), GENERIC_READ, FILE_SHARE_READ, NULL, OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL, NULL);
	if(hFile == INVALID_HANDLE_VALUE) {
		Log("ERROR: Could not open icon file\n");
		return;
	}
	ICONHEADER* pHeader = (ICONHEADER*) malloc(sizeof(ICONHEADER));
	DWORD bytesRead;
	ReadFile(hFile, &(pHeader->reserved), sizeof(WORD), &bytesRead, NULL);
	ReadFile(hFile, &(pHeader->type), sizeof(WORD), &bytesRead, NULL);
	ReadFile(hFile, &(pHeader->count), sizeof(WORD), &bytesRead, NULL);
	pHeader = (ICONHEADER*) realloc(pHeader, sizeof(WORD)*3+sizeof(ICONENTRY)*pHeader->count);
	ReadFile(hFile, pHeader->entries, pHeader->count*sizeof(ICONENTRY), &bytesRead, NULL);
	ICONIMAGE** pIcons = (ICONIMAGE**) malloc(sizeof(ICONIMAGE*)*pHeader->count);
	for(int i = 0 ; i < pHeader->count; i++) {
		pIcons[i] = (ICONIMAGE*) malloc(pHeader->entries[i].bytesInRes);
		SetFilePointer(hFile, pHeader->entries[i].imageOffset, NULL, FILE_BEGIN);
		ReadFile(hFile, pIcons[i], pHeader->entries[i].bytesInRes, &bytesRead, NULL);
	}

	// Convert to resource format
	GRPICONHEADER* pGrpHeader = (GRPICONHEADER*) malloc(sizeof(WORD)*3+pHeader->count*sizeof(GRPICONENTRY));
	pGrpHeader->reserved = 0;
	pGrpHeader->type = 1;
	pGrpHeader->count = pHeader->count;
	for(int i = 0; i < pHeader->count; i++) {
		ICONENTRY* icon = &pHeader->entries[i];
		GRPICONENTRY* entry = &pGrpHeader->entries[i];
		entry->bitCount = 0;
		entry->bytesInRes = icon->bitCount;
		entry->bytesInRes2 = icon->bytesInRes;
		entry->colourCount = icon->colorCount;
		entry->height = icon->height;
		entry->id = (i+1);
		entry->planes = icon->planes;
		entry->reserved = icon->reserved;
		entry->width = icon->width;
		entry->reserved2 = 0;
	}

	// Copy in resources
	HANDLE hUpdate = BeginUpdateResource(tempfile, FALSE);

	// Copy in icon group resource
	UpdateResource(hUpdate, RT_GROUP_ICON, MAKEINTRESOURCE(1), MAKELANGID(LANG_ENGLISH, SUBLANG_ENGLISH_US),
		pGrpHeader, sizeof(WORD)*3+pHeader->count*sizeof(GRPICONENTRY));

	// Copy in icons
	for(int i = 0; i < pHeader->count; i++) {
		UpdateResource(hUpdate, RT_ICON, MAKEINTRESOURCE(i + 1), MAKELANGID(LANG_ENGLISH, SUBLANG_ENGLISH_US),
			pIcons[i], pHeader->entries[i].bytesInRes);
	}

	// Commit the changes
	EndUpdateResource(hUpdate, FALSE);

	// Close handles
	CloseHandle(hFile);
}
