/*******************************************************************************
* This program and the accompanying materials
* are made available under the terms of the Common Public License v1.0
* which accompanies this distribution, and is available at 
* http://www.eclipse.org/legal/cpl-v10.html
* 
* Contributors:
*     Peter Smith
*******************************************************************************/

#include "Service.h"
#include "../common/INI.h"
#include "../common/Log.h"

SERVICE_STATUS g_serviceStatus;
SERVICE_STATUS_HANDLE g_serviceStatusHandle;

void WINAPI ServiceCtrlHandler(DWORD opCode)
{
	int result;

	switch(opCode)
	{
	case SERVICE_CONTROL_PAUSE:
		Service::Pause();
		g_serviceStatus.dwCurrentState = SERVICE_PAUSED;
		break;

	case SERVICE_CONTROL_CONTINUE:
		Service::Start();
		g_serviceStatus.dwCurrentState = SERVICE_RUNNING;
		break;

	case SERVICE_CONTROL_STOP:
		result = Service::Stop();
		g_serviceStatus.dwWin32ExitCode = result;
		g_serviceStatus.dwCurrentState = SERVICE_STOPPED;
		g_serviceStatus.dwCheckPoint = 0;
		g_serviceStatus.dwWaitHint = 0;
		
		if(!SetServiceStatus(g_serviceStatusHandle, &g_serviceStatus)) {
			Log::Error("Error in SetServiceStatus: %d\n", GetLastError());
		}

		return;
	
	case SERVICE_INTERROGATE:
		break;
	}

	if(!SetServiceStatus(g_serviceStatusHandle, &g_serviceStatus)) {
		Log::Error("Error in SetServiceStatus: %d\n", GetLastError());
	}
}

void WINAPI ServiceStart(DWORD argc, LPTSTR *argv)
{
	g_serviceStatus.dwServiceType = SERVICE_WIN32;
	g_serviceStatus.dwCurrentState = SERVICE_START_PENDING;
	g_serviceStatus.dwControlsAccepted = 0;
	if(Service::CanHandlePowerEvent()) g_serviceStatus.dwControlsAccepted |= SERVICE_ACCEPT_POWEREVENT;
	if(Service::CanPauseAndContinue()) g_serviceStatus.dwControlsAccepted |= SERVICE_ACCEPT_PAUSE_CONTINUE;
	if(Service::CanStop()) g_serviceStatus.dwControlsAccepted |= SERVICE_ACCEPT_STOP;
	if(Service::CanShutdown()) g_serviceStatus.dwControlsAccepted |= SERVICE_ACCEPT_SHUTDOWN;
	g_serviceStatus.dwWin32ExitCode = 0;
	g_serviceStatus.dwServiceSpecificExitCode = 0;
	g_serviceStatus.dwWaitHint = 0;

	g_serviceStatusHandle = RegisterServiceCtrlHandler(Service::GetName(), ServiceCtrlHandler);

	if(g_serviceStatusHandle == (SERVICE_STATUS_HANDLE)0)
	{
		Log::Error("Error registering service control handler: %d\n", GetLastError());
		return;
	}
}

int Service::Run(HINSTANCE hInstance, dictionary* ini, int argc, char* argv[])
{
	char* serviceName = GetName();
	if(serviceName == NULL) {
		Log::Error("Could not find service name\n");
		return 1;
	}
	
	SERVICE_TABLE_ENTRY dispatchTable[] = { { serviceName, ServiceStart }, { NULL, NULL } };

	if(!StartServiceCtrlDispatcher(dispatchTable)) {
		Log::Error("Service control dispatcher error: %d\n", GetLastError());
		return 2;
	}
}

void Service::Register(LPSTR lpCmdLine)
{
}

void Service::Unregister(LPSTR lpCmdLine)
{
}

char* Service::GetName()
{
	return 0;
}

bool Service::CanHandlePowerEvent()
{
	return false;
}

bool Service::CanPauseAndContinue()
{
	return false;
}

bool Service::CanShutdown()
{
	return false;
}

bool Service::CanStop()
{
	return false;
}

int Service::Pause()
{
	return 0;
}

int Service::Start()
{
	return 0;
}

int Service::Stop()
{
	return 0;
}

int Service::Shutdown()
{
	return 0;
}
