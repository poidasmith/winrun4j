/*******************************************************************************
* This program and the accompanying materials
* are made available under the terms of the Common Public License v1.0
* which accompanies this distribution, and is available at 
* http://www.eclipse.org/legal/cpl-v10.html
* 
* Contributors:
*     Peter Smith
*******************************************************************************/

#include "DDE.h"
#include "../common/Log.h"
#include "../java/VM.h"

static dictionary* g_ini = 0;
static HWND g_hWnd;
static DWORD g_pidInst = 0;
static HSZ g_serverName = 0;
static HSZ g_topic = 0;
static char g_execute[MAX_PATH];
static jclass g_class;
static jmethodID g_methodID;

// INI keys
#define DDE_CLASS ":dde.class"
#define DDE_ENABLED ":dde.enabled"
#define DDE_WINDOW_CLASS ":dde.window.class"
#define DDE_SERVER_NAME ":dde.server.name"
#define DDE_TOPIC ":dde.topic"

LRESULT CALLBACK DdeMainWndProc(HWND hWnd, UINT uMsg, WPARAM wParam, LPARAM lParam)
{
	return DefWindowProc(hWnd, uMsg, wParam, lParam);
}

HDDEDATA CALLBACK DdeCallback(UINT uType, UINT /*uFmt*/, HCONV /*hconv*/, HDDEDATA hsz1,
    HDDEDATA hsz2, HDDEDATA hdata, HDDEDATA /*dwData1*/, HDDEDATA /*dwData2*/)
{
	switch (uType)
	{     
		case XTYP_CONNECT: 
			if(hsz2 == (HDDEDATA) g_serverName && hsz1 == (HDDEDATA) g_topic)
			 	return (HDDEDATA) 1;
		case XTYP_EXECUTE: {
			DdeGetData(hdata, (LPBYTE) g_execute, MAX_PATH, 0); 
			DDE::Execute(g_execute);
			break;
	   }
	}

	return 0;
}

bool DDE::RegisterDDE()
{
	// Startup DDE library
	UINT result = DdeInitialize(&g_pidInst, (PFNCALLBACK) &DdeCallback, 0, 0);
	if(result != DMLERR_NO_ERROR) {
		Log::Error("Unable to initialize DDE: %d", result);
		return false;
	}

	// Check for app/topic override
	char* appName = iniparser_getstr(g_ini, DDE_SERVER_NAME);
	char* topic = iniparser_getstr(g_ini, DDE_TOPIC);

	g_serverName = DdeCreateStringHandle(g_pidInst, appName == NULL ? "WinRun4J" : appName, CP_WINANSI);
	g_topic = DdeCreateStringHandle(g_pidInst, topic == NULL ? "System" : topic, CP_WINANSI);    

	// Register the server
	DdeNameService(g_pidInst, g_serverName, NULL, DNS_REGISTER);
	return false;
}

DWORD WINAPI DdeWindowThreadProc(LPVOID lpParam)
{
	// Register Window
	DDE::RegisterWindow((HINSTANCE) lpParam);
	
	bool initDde = DDE::RegisterDDE();
	if(!initDde)
		return 1;

	char* clsName = iniparser_getstr(g_ini, DDE_WINDOW_CLASS);

	// Create window
	g_hWnd = CreateWindowEx(
		0, 
		clsName == NULL ? "WinRun4J.DDEWndClass" : clsName, 
		"WinRun4J.DDEWindow", 
		0, 
		0, 0,
		0, 0, 
		NULL, NULL, NULL, NULL);

	// Listen for messages
	MSG msg;
	while (GetMessage (&msg, NULL, 0, 0))
	{
		TranslateMessage (&msg);
		DispatchMessage (&msg);
	}

	return 0;
}

bool DDE::Initialize(HINSTANCE hInstance, JNIEnv* env, dictionary* ini)
{
	// Check for enabled flag
	char* ddeEnabled = iniparser_getstr(ini, DDE_ENABLED);
	if(ddeEnabled == NULL || strcmp("true", ddeEnabled) != 0)
		return false;

	// Store ini file reference
	g_ini = ini;

	// Create Thread to manage the window
	CreateThread(0, 0, DdeWindowThreadProc, (LPVOID) hInstance, 0, 0);

	// Attach JNI methods
	return RegisterNatives(env, ini);
}

void DDE::Uninitialize()
{
	if(g_serverName) DdeFreeStringHandle(g_pidInst, g_serverName);
	if(g_topic) DdeFreeStringHandle(g_pidInst, g_topic);

	// Shutdown DDE library
	DdeUninitialize(g_pidInst); 
}

void DDE::Execute(LPSTR lpExecuteStr)
{
	JNIEnv* env = VM::GetJNIEnv();
	jstring str = 0;
	if(lpExecuteStr) str = env->NewStringUTF(lpExecuteStr);
	env->CallStaticVoidMethod(g_class, g_methodID, str);
}

void DDE::RegisterWindow(HINSTANCE hInstance)
{
	// Create window class for splash image
	WNDCLASSEX wcx;
	wcx.cbSize = sizeof(wcx);
	wcx.style = CS_BYTEALIGNCLIENT | CS_BYTEALIGNWINDOW;
	wcx.lpfnWndProc = DdeMainWndProc;
	wcx.cbClsExtra = 0;
	wcx.cbWndExtra = DLGWINDOWEXTRA;
	wcx.hInstance = hInstance;
	wcx.hIcon = 0;
	wcx.hCursor = ::LoadCursor(NULL, IDC_WAIT);
	wcx.hbrBackground = (HBRUSH)::GetStockObject(LTGRAY_BRUSH);
	wcx.lpszMenuName = 0;
	char* clsName = iniparser_getstr(g_ini, DDE_WINDOW_CLASS);
	wcx.lpszClassName = clsName == NULL ? "WinRun4J.DDEWndClass" : clsName;
	wcx.hIconSm = 0;

	if(!RegisterClassEx(&wcx)) {
		Log::Error("Could not register DDE window class\n");
		return;
	}
}

bool DDE::RegisterNatives(JNIEnv* env, dictionary* ini)
{
	char* ddeClassName = iniparser_getstr(ini, DDE_CLASS);
	if(ddeClassName != NULL) {
		int strl = strlen(ddeClassName);
		for(int i = 0; i < strl; i++) {
			if(ddeClassName[i] == '.')
				ddeClassName[i] = '/';
		}
		g_class = env->FindClass(ddeClassName);
	} else {
		g_class = env->FindClass("org/boris/winrun4j/DDE");
	}
	if(g_class == NULL) {
		Log::SetLastError("Could not find DDE class.");
		return false;
	}

	g_methodID = env->GetStaticMethodID(g_class, "execute", "(Ljava/lang/String;)V");
	if(g_methodID == NULL) {
		Log::SetLastError("Could not find execute method");
		return false;
	}

	return true;
}

void DDE::RegisterFileAssociations(dictionary* ini, LPSTR lpCmdLine)
{
}

void DDE::UnregisterFileAssociations(dictionary* ini, LPSTR lpCmdLine)
{
}