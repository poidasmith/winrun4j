/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Peter Smith
 *******************************************************************************/

#include "Icon.h"
#include "Log.h"

// Since we can't replace the icon resource while the program is running we do the following:
//  1. copy exe to a random filename.
//  2. execute random filename with the argument containing the original filename
//  3. replace the icon of the original filename
//  4. execute the original to delete the random filename

// WinRun4J.exe --seticon
// WinRun4J.Random.exe --seticon SetIcon WinRun4J.exe
// WinRun4J.exe --seticon Delete WinRun4J.Random.exe 

#define SET_ICON_CMD "--WinRun4J:SetIcon SetIcon"
#define SET_ICON_DELETE_EXE_CMD "--WinRun4J:SetIcon Delete"
#define ADD_ICON_CMD "--WinRun4J:AddIcon AddIcon"
#define ADD_ICON_DELETE_EXE_CMD "--WinRun4J:AddIcon Delete"
#define REMOVE_ICON_CMD "--WinRun4J:RemoveIcon RemoveIcon"
#define REMOVE_ICON_DELETE_EXE_CMD "--WinRun4J:RemoveIcon Delete"


void Icon::SetExeIcon(LPSTR commandLine)
{
	// Work out which operation
	if(strncmp(commandLine, SET_ICON_CMD, strlen(SET_ICON_CMD)) == 0) {
		SetIcon(commandLine);
	} else if(strncmp(commandLine, SET_ICON_DELETE_EXE_CMD, strlen(SET_ICON_DELETE_EXE_CMD)) == 0) {
		DeleteRandomFile(commandLine);
	} else {
		CopyToRandomAndRun();
	}
}

void Icon::SetIcon(LPSTR commandLine)
{
	// Assume the ico is named "appname.ico"
	TCHAR filename[MAX_PATH], iconfile[MAX_PATH];
	
	// Extract original file from commandline
	strcpy_s(filename, MAX_PATH, &commandLine[18]);

	// Make icon filename
	strcpy_s(iconfile, MAX_PATH, filename);
	int len = strlen(filename);
	iconfile[len - 1] = 'o';
	iconfile[len - 2] = 'c';
	iconfile[len - 3] = 'i';

	Log::Info("Setting icon file...\n");
	Log::Info("Icon File: %s\n", iconfile);
	Log::Info("Exe File: %s\n", filename);

	// Now set the icon
	SetIcon(filename, iconfile);

	// Create command line for deleting random file
	TCHAR random[MAX_PATH], cmd[MAX_PATH];
	GetModuleFileName(NULL, random, MAX_PATH);
	sprintf(cmd, "%s %s %s", filename, SET_ICON_DELETE_EXE_CMD, random);

	// Now delete the random exe
	STARTUPINFO si;
    PROCESS_INFORMATION pi;
    ZeroMemory( &si, sizeof(si) );
    si.cb = sizeof(si);
    ZeroMemory( &pi, sizeof(pi) );
	if(!CreateProcess(filename, cmd, NULL, NULL, FALSE, 0, NULL, NULL, &si, &pi))
		Log::Error("Could not run delete process\n");
}

// Create a random filename based on original and call set icon on this executable
void Icon::CopyToRandomAndRun() 
{
	TCHAR filename[MAX_PATH], random[MAX_PATH], cmdline[MAX_PATH];
	GetModuleFileName(NULL, filename, sizeof(filename));
	srand(GetTickCount());
	int r = rand();
	sprintf_s(random, MAX_PATH, "%s.%d.exe", filename, r);
	sprintf_s(cmdline, MAX_PATH, "%s %s %s", random, SET_ICON_CMD, filename);
	if(!CopyFile(filename, random, true)) {
		return;
	}

	STARTUPINFO si;
    PROCESS_INFORMATION pi;
    ZeroMemory( &si, sizeof(si) );
    si.cb = sizeof(si);
    ZeroMemory( &pi, sizeof(pi) );
	if(!CreateProcess(random, cmdline, NULL, NULL, FALSE, 0, NULL, NULL, &si, &pi)) 
		Log::Error("Could not run random process\n");
}

// Delete the random filename
void Icon::DeleteRandomFile(LPSTR cmdLine)
{
	TCHAR filename[MAX_PATH];
	strcpy_s(filename, MAX_PATH, &cmdLine[17]);
	DeleteFile(filename);
}

// Set icon on original exe file
bool Icon::SetIcon(LPSTR exeFile, LPSTR iconFile)
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
	UpdateResource(hUpdate, RT_GROUP_ICON, MAKEINTRESOURCE(1), MAKELANGID(LANG_ENGLISH, SUBLANG_ENGLISH_US),
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
bool Icon::LoadIcon(LPSTR iconFile, ICONHEADER*& pHeader, ICONIMAGE**& pIcons, GRPICONHEADER*& pGrpHeader)
{
	HANDLE hFile = CreateFile(TEXT(iconFile), GENERIC_READ, FILE_SHARE_READ, NULL, OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL, NULL);
	if(hFile == INVALID_HANDLE_VALUE) {
		Log::Error("ERROR: Could not open icon file: %s\n", iconFile);
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
		entry->id = (WORD)(i+1);
		entry->planes = (BYTE)icon->planes;
		entry->reserved = icon->reserved;
		entry->width = icon->width;
		entry->reserved2 = 0;
	}

	// Close handles
	CloseHandle(hFile);

	return true;
}

bool Icon::AddIcon(LPSTR exeFile, LPSTR iconFile)
{
	return false;
}

bool Icon::RemoveIcon(LPSTR exeFile, int index)
{
	return false;
}

bool Icon::RemoveIcons(LPSTR exeFile)
{
	return false;
}
