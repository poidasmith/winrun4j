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
static jclass g_class = 0;
static jmethodID g_executeMethodID = 0;
static jmethodID g_activateMethodID = 0;
static bool g_ready = 0;
static LPSTR *g_buffer = NULL;
static int g_buffer_ix = 0;
static int g_buffer_siz = 0;

// INI keys
#define DDE_CLASS ":dde.class"
#define DDE_ENABLED ":dde.enabled"
#define DDE_WINDOW_CLASS ":dde.window.class"
#define DDE_SERVER_NAME ":dde.server.name"
#define DDE_TOPIC ":dde.topic"

// Single instance
#define DDE_EXECUTE_ACTIVATE "ACTIVATE"

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
		break;

	case XTYP_EXECUTE: {
		UINT size = DdeGetData(hdata, NULL, 0, 0);
		LPSTR execData = (LPSTR) malloc(size);
		DdeGetData(hdata, (LPBYTE) execData, size, 0);
		DDE::Execute(execData);
		free(execData);
		return (HDDEDATA) 1;
					   }
		break;
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
	g_topic = DdeCreateStringHandle(g_pidInst, topic == NULL ? "system" : topic, CP_WINANSI);

	// Register the server
	DdeNameService(g_pidInst, g_serverName, NULL, DNS_REGISTER);
	return true;
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
	Log::Info("Initializing DDE");
	g_ini = ini;

	// Attach JNI methods
	if (!RegisterNatives(env, ini))
		return false;

	// Create Thread to manage the window
	CreateThread(0, 0, DdeWindowThreadProc, (LPVOID) hInstance, 0, 0);
	return true;
}

void DDE::Uninitialize()
{
	if(g_serverName) DdeFreeStringHandle(g_pidInst, g_serverName);
	if(g_topic) DdeFreeStringHandle(g_pidInst, g_topic);

	// Shutdown DDE library
	DdeUninitialize(g_pidInst);
}

bool DDE::NotifySingleInstance(dictionary* ini)
{
	g_ini = ini;

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
	g_topic = DdeCreateStringHandle(g_pidInst, topic == NULL ? "system" : topic, CP_WINANSI);

	HCONV conv = DdeConnect(g_pidInst, g_serverName, g_topic, NULL);
	if (conv != NULL) {
		LPSTR cmdline = StripArg0(GetCommandLine());
		char* activate = (char*) malloc(strlen(DDE_EXECUTE_ACTIVATE) + strlen(cmdline) + 2);
		strcpy(activate, DDE_EXECUTE_ACTIVATE);
		strcat(activate, " ");
		strcat(activate, cmdline);
		HDDEDATA result = DdeClientTransaction((LPBYTE)activate, strlen(activate) + 1, conv, NULL, 0, XTYP_EXECUTE, TIMEOUT_ASYNC, NULL);
		if (result == 0) {
			Log::Error("Failed to send DDE single instance notification");
			return false;
		}
	} else{
		Log::Error("Unable to create DDE conversation");
	}

	DDE::Uninitialize();
	return true;
}

void DDE::Execute(LPSTR lpExecuteStr)
{
	JNIEnv* env = VM::GetJNIEnv(true);
	if(env == NULL) return;
	if(g_class == NULL) return;
	if(g_executeMethodID == NULL) return;

	if (g_ready) {
		if (g_class != NULL) {
			Log::Info("DDE Execute: %s", lpExecuteStr);

			if (memcmp(lpExecuteStr, DDE_EXECUTE_ACTIVATE, 8) == 0) {
				if (g_activateMethodID != NULL) {
					jstring str = 0;
					if(lpExecuteStr) str = env->NewStringUTF(&lpExecuteStr[9]);
					env->CallStaticVoidMethod(g_class, g_activateMethodID, str);
				} else {
					Log::Error("Ignoring DDE single instance activate message");
				}
			} else {
				jstring str = 0;
				if(lpExecuteStr) str = env->NewStringUTF(lpExecuteStr);
				env->CallStaticVoidMethod(g_class, g_executeMethodID, str);
			}

			if(env->ExceptionOccurred()) {
				env->ExceptionDescribe();
				env->ExceptionClear();
			}
		}
	} else {
		/* Allocate a copy of the string */
		LPSTR buffered = (LPSTR) malloc(MAX_PATH);
		strcpy(buffered, lpExecuteStr);

		if (g_buffer == NULL) {
			/* Create buffer */
			g_buffer_siz = 10;
			g_buffer = (LPSTR*) malloc(sizeof(LPSTR) * g_buffer_siz);
		} else if (g_buffer_ix >= g_buffer_siz) {
			/* Enlarge buffer */
			g_buffer_siz += 10;
			LPSTR *new_buffer = (LPSTR*) malloc(sizeof(LPSTR) * g_buffer_siz);
			memcpy(new_buffer, g_buffer, sizeof(LPSTR) * g_buffer_ix);
			free(g_buffer);
			g_buffer = new_buffer;
		}
		g_buffer[g_buffer_ix++] = buffered;
	}
}

void DDE::Ready() {
	/* Check if we're already marked ready. Ready is now called possibly from a native callback
	* and after the main() method has executed.
	*/
	if (g_ready == 1)
		return;

	g_ready = 1;

	for (int i = 0; i < g_buffer_ix; i++) {
		LPSTR lpExecuteStr = g_buffer[i];
		DDE::Execute(lpExecuteStr);
		free(lpExecuteStr);
	}
	free(g_buffer);
	g_buffer = NULL;
}

extern "C" __declspec(dllexport) void DDE_Ready() 
{
	DDE::Ready();
}

void DDE::RegisterWindow(HINSTANCE hInstance)
{
	// Create window class for DDE
	WNDCLASSEX wcx;
	wcx.cbSize = sizeof(wcx);
	wcx.style = CS_BYTEALIGNCLIENT | CS_BYTEALIGNWINDOW;
	wcx.lpfnWndProc = DdeMainWndProc;
	wcx.cbClsExtra = 0;
	wcx.cbWndExtra = DLGWINDOWEXTRA;
	wcx.hInstance = hInstance;
	wcx.hIcon = 0;
	wcx.hCursor = LoadCursor(NULL, IDC_WAIT);
	wcx.hbrBackground = (HBRUSH) GetStockObject(LTGRAY_BRUSH);
	wcx.lpszMenuName = 0;
	char* clsName = iniparser_getstr(g_ini, DDE_WINDOW_CLASS);
	wcx.lpszClassName = clsName == NULL ? "WinRun4J.DDEWndClass" : clsName;
	wcx.hIconSm = 0;

	if(!RegisterClassEx(&wcx)) {
		Log::Error("Could not register DDE window class");
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
		Log::Error("Could not find DDE class.");
		if(env->ExceptionCheck()) env->ExceptionClear();
		return false;
	}
	// Global ref in case of garbage collection
	g_class = (jclass) env->NewGlobalRef(g_class);

	g_executeMethodID = env->GetStaticMethodID(g_class, "execute", "(Ljava/lang/String;)V");
	if(g_executeMethodID == NULL) {
		Log::Error("Could not find execute method");
		if(env->ExceptionCheck()) env->ExceptionClear();
		return false;
	}

	g_activateMethodID = env->GetStaticMethodID(g_class, "activate", "(Ljava/lang/String;)V");
	if(env->ExceptionCheck()) {
		env->ExceptionClear();
	}

	return true;
}

void DDE::EnumFileAssocations(dictionary* ini, LPSTR lpCmdLine, bool isRegister, void (*CallbackFunc)(DDEInfo&))
{
	// For the moment just register all
	char key[MAX_PATH];
	for(int i = 1;; i++) {
		DDEInfo info;
		info.ini = ini;

		sprintf(key, "FileAssociations:file.%d.extension", i);
		info.extension = iniparser_getstr(ini, key);
		if(info.extension == NULL) break;

		Log::Info(isRegister ? "Registering %s" : "Unregistering %s", info.extension);

		sprintf(key, "FileAssociations:file.%d.name", i);
		info.name = iniparser_getstr(ini, key);
		if(info.name == NULL) {
			Log::Error("Name not specified for extension: %s", info.extension);
			break;
		}

		sprintf(key, "FileAssociations:file.%d.description", i);
		info.description = iniparser_getstr(ini, key);
		if(info.description == NULL) {
			Log::Warning("Description not specified for extension: %s", info.extension);
		}

		CallbackFunc(info);
	}
}

void DDE::RegisterFileAssociations(dictionary* ini, LPSTR lpCmdLine)
{
	EnumFileAssocations(ini, lpCmdLine, true, RegisterFileAssociation);
}

void DDE::RegisterFileAssociation(DDEInfo& info)
{
	DWORD dwDisp;
	HKEY hKey;
	if(RegCreateKeyEx(HKEY_CLASSES_ROOT, info.extension, 0, NULL, REG_OPTION_NON_VOLATILE, KEY_WRITE, NULL, &hKey, &dwDisp)) {
		Log::Error("ERROR: Could not create extension key: %s", info.extension);
		return;
	}

	if(RegSetValueEx(hKey, NULL, 0, REG_SZ, (BYTE *) info.name, strlen(info.name) + 1)) {
		Log::Error("ERROR: Could not set name for extension: %s", info.extension);
		return;
	}

	if(RegCreateKeyEx(HKEY_CLASSES_ROOT, info.name, 0, NULL, REG_OPTION_NON_VOLATILE, KEY_WRITE, NULL, &hKey, &dwDisp)) {
		Log::Error("ERROR: Could not create name key: %s", info.name);
		return;
	}

	if(info.description) {
		if(RegSetValueEx(hKey, NULL, 0, REG_SZ, (BYTE *) info.description, strlen(info.description) + 1)) {
			Log::Error("ERROR: Could not set description for extension: %s", info.extension);
			return;
		}
	}

	if(RegCreateKeyEx(HKEY_CLASSES_ROOT, info.name, 0, NULL, REG_OPTION_NON_VOLATILE, KEY_WRITE, NULL, &hKey, &dwDisp)) {
		Log::Error("ERROR: Could not create name key: %s", info.name);
		return;
	}

	HKEY hDep;
	if(RegCreateKeyEx(hKey, "DefaultIcon", 0, NULL, REG_OPTION_NON_VOLATILE, KEY_WRITE, NULL, &hDep, &dwDisp)) {
		Log::Error("ERROR: Could not create shell key: %s", info.name);
		return;
	}

	char path[MAX_PATH];
	GetModuleFileName(NULL, path, MAX_PATH);
	if(RegSetValueEx(hDep, NULL, 0, REG_SZ, (BYTE *) path, strlen(path) + 1)) {
		Log::Error("ERROR: Could not set command for extension: %s", info.extension);
		return;
	}

	if(RegCreateKeyEx(hKey, "shell", 0, NULL, REG_OPTION_NON_VOLATILE, KEY_WRITE, NULL, &hKey, &dwDisp)) {
		Log::Error("ERROR: Could not create shell key: %s", info.name);
		return;
	}

	if(RegCreateKeyEx(hKey, "Open", 0, NULL, REG_OPTION_NON_VOLATILE, KEY_WRITE, NULL, &hKey, &dwDisp)) {
		Log::Error("ERROR: Could not create Open key: %s", info.name);
		return;
	}

	HKEY hCmd;
	if(RegCreateKeyEx(hKey, "command", 0, NULL, REG_OPTION_NON_VOLATILE, KEY_WRITE, NULL, &hCmd, &dwDisp)) {
		Log::Error("ERROR: Could not create command key: %s", info.name);
		return;
	}

	strcat(path, " \"%1\"");
	if(RegSetValueEx(hCmd, NULL, 0, REG_SZ, (BYTE *) path, strlen(path) + 1)) {
		Log::Error("ERROR: Could not set command for extension: %s", info.extension);
		return;
	}

	HKEY hDde;
	if(RegCreateKeyEx(hKey, "ddeexec", 0, NULL, REG_OPTION_NON_VOLATILE, KEY_WRITE, NULL, &hDde, &dwDisp)) {
		Log::Error("ERROR: Could not create ddeexec key: %s", info.name);
		return;
	}

	char* cmd = "%1";
	if(RegSetValueEx(hDde, NULL, 0, REG_SZ, (BYTE *) cmd, strlen(cmd) + 1)) {
		Log::Error("ERROR: Could not set command string for extension: %s", info.extension);
		return;
	}

	HKEY hApp;
	if(RegCreateKeyEx(hDde, "application", 0, NULL, REG_OPTION_NON_VOLATILE, KEY_WRITE, NULL, &hApp, &dwDisp)) {
		Log::Error("ERROR: Could not create ddeexec->application key: %s", info.name);
		return;
	}

	char* appname = iniparser_getstr(info.ini, DDE_SERVER_NAME);
	if(appname == NULL) appname = "WinRun4J";
	if(RegSetValueEx(hApp, NULL, 0, REG_SZ, (BYTE *) appname, strlen(appname) + 1)) {
		Log::Error("ERROR: Could not set appname for extension: %s", info.extension);
		return;
	}

	HKEY hTopic;
	if(RegCreateKeyEx(hDde, "topic", 0, NULL, REG_OPTION_NON_VOLATILE, KEY_WRITE, NULL, &hTopic, &dwDisp)) {
		Log::Error("ERROR: Could not create ddeexec->application key: %s", info.name);
		return;
	}

	char* topic = iniparser_getstr(info.ini, DDE_TOPIC);
	if(topic == NULL) topic = "system";
	if(RegSetValueEx(hTopic, NULL, 0, REG_SZ, (BYTE *) topic, strlen(topic) + 1)) {
		Log::Error("ERROR: Could not set topic for extension: %s", info.extension);
		return;
	}
}

void DDE::UnregisterFileAssociation(DDEInfo& info)
{
	RegDeleteKey(HKEY_CLASSES_ROOT, info.name);
}

void DDE::UnregisterFileAssociations(dictionary* ini, LPSTR lpCmdLine)
{
	EnumFileAssocations(ini, lpCmdLine, false, UnregisterFileAssociation);
}

