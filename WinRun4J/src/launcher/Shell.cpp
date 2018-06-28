/*******************************************************************************
* This program and the accompanying materials
* are made available under the terms of the Common Public License v1.0
* which accompanies this distribution, and is available at 
* http://www.eclipse.org/legal/cpl-v10.html
* 
* Contributors:
*     Peter Smith
*******************************************************************************/

#include "Shell.h"
#include "../common/Log.h"
#include "../java/JNI.h"
#include "../java/VM.h"
#include "DDE.h"
#include <tlhelp32.h>
#include <psapi.h>
#include <shlobj.h>

#define SINGLE_INSTANCE_OPTION ":single.instance"

BOOL CALLBACK EnumWindowsProcSingleInstance(HWND hWnd, LPARAM lParam)
{
	DWORD procId = 0;
	GetWindowThreadProcessId(hWnd, &procId);
	if((DWORD)lParam == procId) {
		WINDOWINFO wi;
		wi.cbSize = sizeof(WINDOWINFO);
		GetWindowInfo(hWnd, &wi);
		if((wi.dwStyle & WS_VISIBLE) != 0) {
			SetForegroundWindow(hWnd);
			Log::Warning("Single Instance Shutdown");
			return FALSE;
		}
	}

	return TRUE;
}

int Shell::CheckSingleInstance(dictionary* ini)
{
	char* singleInstance = iniparser_getstr(ini, SINGLE_INSTANCE_OPTION);
	if(singleInstance == NULL) {
		return 0;
	}

	// Check for single instance mode
	bool processOnly = true;
	bool dde = false;

	if(strcmp(singleInstance, "window") == 0)
		processOnly = false;
	else if (strcmp(singleInstance, "dde") == 0) {
		processOnly = false;
		dde = true;
	} else if(strcmp(singleInstance, "process") != 0) {		
		Log::Warning("Invalid single instance mode: %s", singleInstance);
		return 0;
	}

	char thisModule[MAX_PATH];
	DWORD thisProcessId = GetCurrentProcessId();
	GetModuleFileName(0, thisModule, MAX_PATH);
	HANDLE h = CreateToolhelp32Snapshot(TH32CS_SNAPPROCESS, 0);
	PROCESSENTRY32 e;
	e.dwSize = sizeof(PROCESSENTRY32);
	char otherModule[MAX_PATH];

	if(Process32First(h, &e)) {
		HANDLE hProcess = OpenProcess(PROCESS_QUERY_INFORMATION | PROCESS_VM_READ,	FALSE, e.th32ProcessID);
		GetModuleFileNameEx(hProcess, 0, otherModule, MAX_PATH);
		CloseHandle(hProcess);
		if(thisProcessId != e.th32ProcessID && strcmp(thisModule, otherModule) == 0) {
			if (dde && DDE::NotifySingleInstance(ini)) {
				Log::Warning("Single Instance Shutdown");
				return 1;
			}
			if(processOnly) {
				Log::Warning("Single Instance Shutdown");
				return 1;
			}
			return !EnumWindows(EnumWindowsProcSingleInstance, e.th32ProcessID);
		}
		while(Process32Next(h, &e)) {
			HANDLE hProcess = OpenProcess(PROCESS_QUERY_INFORMATION | PROCESS_VM_READ, FALSE, e.th32ProcessID);
			GetModuleFileNameEx(hProcess, 0, otherModule, MAX_PATH);
			CloseHandle(hProcess);
			if(thisProcessId != e.th32ProcessID && strcmp(thisModule, otherModule) == 0) {
				if (dde && DDE::NotifySingleInstance(ini)) {
					Log::Warning("Single Instance Shutdown");
					return 1;
				}
				if(processOnly) {
					Log::Warning("Single Instance Shutdown");
					return 1;
				}
				return !EnumWindows(EnumWindowsProcSingleInstance, e.th32ProcessID);
			}
		}
	} 

	return 0;
}

