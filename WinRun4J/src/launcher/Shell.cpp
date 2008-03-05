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

BOOL CALLBACK EnumWindowsProcSingleInstance(HWND hWnd, LPARAM lParam)
{
	DWORD procId = 0;
	GetWindowThreadProcessId(hWnd, &procId);
	if(lParam == procId) {
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

int Shell::CheckSingleInstance()
{
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
			return !EnumWindows(EnumWindowsProcSingleInstance, e.th32ProcessID);
		}
		while(Process32Next(h, &e)) {
			HANDLE hProcess = OpenProcess(PROCESS_QUERY_INFORMATION | PROCESS_VM_READ, FALSE, e.th32ProcessID);
			GetModuleFileNameEx(hProcess, 0, otherModule, MAX_PATH);
			CloseHandle(hProcess);
			if(thisProcessId != e.th32ProcessID && strcmp(thisModule, otherModule) == 0) {
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