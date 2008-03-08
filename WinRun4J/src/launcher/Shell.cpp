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
#include <tlhelp32.h>
#include <psapi.h>

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

	if(strcmp(singleInstance, "window") == 0)
		processOnly = false;
	else if(strcmp(singleInstance, "process") != 0) {
		Log::Warning("Invalid single instance mode: %s\n", singleInstance);
		return 0;
	}

	char thisModule[MAX_PATH];
	DWORD thisProcessId = GetCurrentProcessId();
	GetModuleFileName(0, thisModule, MAX_PATH);
	HANDLE h = CreateToolhelp32Snapshot(TH32CS_SNAPPROCESS, 0);
	PROCESSENTRY32 e;
	char otherModule[MAX_PATH];

	if(Process32First(h, &e)) {
		HANDLE hProcess = OpenProcess(PROCESS_QUERY_INFORMATION | PROCESS_VM_READ,	FALSE, e.th32ProcessID);
		GetModuleFileNameEx(hProcess, 0, otherModule, MAX_PATH);
		CloseHandle(hProcess);
		if(thisProcessId != e.th32ProcessID && strcmp(thisModule, otherModule) == 0) {
			if(processOnly) {
				return 1;
			}
			return !EnumWindows(EnumWindowsProcSingleInstance, e.th32ProcessID);
		}
		while(Process32Next(h, &e)) {
			HANDLE hProcess = OpenProcess(PROCESS_QUERY_INFORMATION | PROCESS_VM_READ, FALSE, e.th32ProcessID);
			GetModuleFileNameEx(hProcess, 0, otherModule, MAX_PATH);
			CloseHandle(hProcess);
			if(thisProcessId != e.th32ProcessID && strcmp(thisModule, otherModule) == 0) {
				if(processOnly) {
					return 1;
				}
				return !EnumWindows(EnumWindowsProcSingleInstance, e.th32ProcessID);
			}
		}
	}

	return 0;
}

bool Shell::RegisterNatives(JNIEnv *env)
{
	Log::Info("Registering natives for Shell class\n");
	return false;
}