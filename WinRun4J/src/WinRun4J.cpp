/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Peter Smith
 *******************************************************************************/

#include "WinRun4J.h"
#include "launcher/SplashScreen.h"
#include "launcher/Shell.h"
#include "launcher/DDE.h"
#include "launcher/Service.h"
#include "launcher/EventLog.h"
#include "launcher/Native.h"
#include "common/Registry.h"

#define CONSOLE_TITLE                       ":console.title"
#define PROCESS_PRIORITY                    ":process.priority"
#define DISABLE_NATIVE_METHODS              ":disable.native.methods"
#define ERROR_MESSAGES_SHOW_POPUP           "ErrorMessages:show.popup"
#define ERROR_MESSAGES_JAVA_NOT_FOUND       "ErrorMessages:java.not.found"
#define ERROR_MESSAGES_JAVA_START_FAILED    "ErrorMessages:java.failed"
#define ERROR_MESSAGES_MAIN_CLASS_NOT_FOUND "ErrorMessages:main.class.not.found"

namespace 
{
	TCHAR *vmargs[MAX_PATH];
	UINT vmargsCount = 0;
	bool progargsParsed = false;
	TCHAR *progargs[MAX_PATH];
	UINT progargsCount = 0;
	bool workingDirectorySet = false;
}

void WinRun4J::SetWorkingDirectory(dictionary* ini, bool defaultToIniDir)
{
	if(workingDirectorySet) 
		return;
	char* dir = iniparser_getstr(ini, WORKING_DIR);
	if(dir != NULL || defaultToIniDir) {
		// First set the current directory to the module (or ini) directory
		SetCurrentDirectory(iniparser_getstr(ini, INI_DIR));

		if(dir != NULL) {
			// Now set working directory to specified (this allows for a relative working directory)
			SetCurrentDirectory(dir);
		}

		// Inform the user of the absolute path
		if(Log::GetLevel() == info) {
			char temp[MAX_PATH];
			GetCurrentDirectory(MAX_PATH, temp);
			Log::Info("Working directory set to: %s", temp);
		}
	} 
	workingDirectorySet = true;
}

void WinRun4J::SetProcessPriority(dictionary* ini)
{
	char* priority = iniparser_getstr(ini, PROCESS_PRIORITY);
	if(!priority) 
		return;

	int p = -1;
	if(strcmp("idle", priority) == 0) {
		p = IDLE_PRIORITY_CLASS;
	} else if(strcmp("below_normal", priority) == 0) {
		p = BELOW_NORMAL_PRIORITY_CLASS;
	} else if(strcmp("normal", priority) == 0) {
		p = NORMAL_PRIORITY_CLASS;
	} else if(strcmp("above_normal", priority) == 0) {
		p = ABOVE_NORMAL_PRIORITY_CLASS;
	} else if(strcmp("high", priority) == 0) {
		p = HIGH_PRIORITY_CLASS;
	} else if(strcmp("realtime", priority) == 0) {
		p = REALTIME_PRIORITY_CLASS;
	} else {
		Log::Warning("Invalid process priority class: %s", priority);
	}

	if(p != -1) {
		SetPriorityClass(GetCurrentProcess(), p);
	}
}

int WinRun4J::DoBuiltInCommand(HINSTANCE hInstance, LPSTR lpCmdLine)
{
	// Remove any leading whitespace
	StrTrim(lpCmdLine, " ");

	// Make sure we also log to console
	Log::SetLogFileAndConsole(true);

	// Check for RegisterDDE util request
	if(StartsWith(lpCmdLine, "--WinRun4J:RegisterFileAssociations")) {
		return DDE::RegisterFileAssociations(WinRun4J::LoadIniFile(hInstance), lpCmdLine);
	}

	// Check for UnregisterDDE util request
	if(StartsWith(lpCmdLine, "--WinRun4J:UnregisterFileAssociations")) {
		return DDE::UnregisterFileAssociations(WinRun4J::LoadIniFile(hInstance), lpCmdLine);
	}

	// Check for Register Service util request
	if(StartsWith(lpCmdLine, "--WinRun4J:RegisterService")) {
		dictionary* ini = INI::LoadIniFile(hInstance);
		if(ini == NULL) 
			return 1;
		return Service::Register(ini);
	}

	// Check for Unregister Service util request
	if(StartsWith(lpCmdLine, "--WinRun4J:UnregisterService")) {
		dictionary* ini = INI::LoadIniFile(hInstance);
		if(ini == NULL) 
			return 1;
		return Service::Unregister(ini);
	}

	if(StartsWith(lpCmdLine, "--WinRun4J:PrintINI")) {
		dictionary* ini = INI::LoadIniFile(hInstance);
		if(ini == NULL) 
			return 1;
		for(int i = 0; i < ini->n; i++) 
			printf("%s=%s\n", ini->key[i], ini->val[i]);
		return 0;
	}

	if(StartsWith(lpCmdLine, "--WinRun4J:ExecuteINI")) {
		return WinRun4J::ExecuteINI(hInstance, lpCmdLine);
	}

	if(StartsWith(lpCmdLine, "--WinRun4J:Version")) {
		Log::Info("0.4.4\n");
		return 0;
	}

	Log::Error("Unrecognized command: %s", lpCmdLine);
	return 1;
}

dictionary* WinRun4J::LoadIniFile(HINSTANCE hInstance)
{
	dictionary* ini = INI::LoadIniFile(hInstance);
	if(!ini) {
		Log::Error("Failed to find or load ini file.");
		MessageBox(NULL, "Failed to find or load ini file.", "Startup Error", 0);
		Log::Close();
		return NULL;
	}

	return ini;
}

int WinRun4J::StartVM(LPSTR lpCmdLine, dictionary* ini)
{
	bool showErrorPopup = iniparser_getboolean(ini, ERROR_MESSAGES_SHOW_POPUP, 1);

	// Attempt to find an appropriate java VM
	char* vmlibrary = VM::FindJavaVMLibrary(ini);
	if(!vmlibrary) {
		char* javaNotFound = iniparser_getstring(ini, ERROR_MESSAGES_JAVA_NOT_FOUND, "Failed to find Java VM.");
		Log::Error(javaNotFound);
		if(showErrorPopup)
			MessageBox(NULL, javaNotFound, "Startup Error", 0);
		Log::Close();
		return 1;
	}

	Log::Info("Found VM: %s", vmlibrary);

	// Collect the VM args from the INI file
	INI::GetNumberedKeysFromIni(ini, VM_ARG, vmargs, vmargsCount);

	// Build up the classpath and add to vm args
	Classpath::BuildClassPath(ini, vmargs, vmargsCount);

	// Extract the specific VM args
	VM::ExtractSpecificVMArgs(ini, vmargs, vmargsCount);

	// Log the VM args
	if(vmargsCount > 0)
		Log::Info("VM Args:");
	TCHAR argl[MAX_PATH];
	for(UINT i = 0; i < vmargsCount; i++) {
		StrTruncate(argl, vmargs[i], MAX_PATH);
		Log::Info("vmarg.%d=%s", i, argl);
	}

	// Collect the program arguments from the INI file
	INI::GetNumberedKeysFromIni(ini, PROG_ARG, progargs, progargsCount);

	// Add the args from commandline
	if(!progargsParsed)
		ParseCommandLine(lpCmdLine, progargs, progargsCount, true);

	// Log the commandline args
	if(progargsCount > 0)
		Log::Info("Program Args");
	for(UINT i = 0; i < progargsCount; i++) {
		StrTruncate(argl, progargs[i], MAX_PATH);
		Log::Info("arg.%d=%s", i, argl);
	}

	// Make sure there is a NULL at the end of the args
	vmargs[vmargsCount] = NULL;
	progargs[progargsCount] = NULL;

	char* mainClass = iniparser_getstr(ini, MAIN_CLASS);

	// If we don't have a main class we might have a service class
	if(mainClass == NULL)
		mainClass = iniparser_getstr(ini, SERVICE_CLASS);

	// Fix main class - ie. replace x.y.z with x/y/z for use in jni
	if(mainClass != NULL) {
		int len = strlen(mainClass);
		for(int i = 0; i < len; i++) {
			if(mainClass[i] == '.') {
				mainClass[i] = '/';
			}
		}
	}

	if(mainClass == NULL) {
		Log::Error("No main class specified");
		return 1;
	} else {
		Log::Info("Main Class: %s", mainClass);
	}

	// Start the VM
	if(VM::StartJavaVM(vmlibrary, vmargs, NULL) != 0) {
		char* javaFailed = iniparser_getstring(ini, ERROR_MESSAGES_JAVA_START_FAILED, "Error starting Java VM.");
		Log::Error(javaFailed);
		if(showErrorPopup)
			MessageBox(NULL, javaFailed, "Startup Error", 0);
		Log::Close();
		return 1;
	}

	return 0;
}

void WinRun4J::FreeArgs()
{
	// Free vm args
	for(UINT i = 0; i < vmargsCount; i++) {
		free(vmargs[i]);
	}

	// Free program args
	for(UINT i = 0; i < progargsCount; i++) {
		free(progargs[i]);
	}
}

int WinRun4J::ExecuteINI(HINSTANCE hInstance, LPSTR lpCmdLine)
{
	ParseCommandLine(lpCmdLine, progargs, progargsCount, false);

	// The first arg should be the ini file
	if(progargsCount == 0) {
		Log::Error("INI file not specified");
		return 1;
	}

	// Load the INI
	dictionary* ini = INI::LoadIniFile(hInstance, progargs[0]);
	if(!ini) {
		return 1;
	}

	// Now shuffle any program args
	progargsParsed = true;
	if(progargsCount > 0) {
		free(progargs[0]);
		for(UINT i = 1; i < progargsCount; i++) {
			progargs[i-1] = progargs[i];
		}
		progargsCount--;
	} 

	return ExecuteINI(hInstance, ini, NULL);
}

int WinRun4J::ExecuteINI(HINSTANCE hInstance, dictionary* ini, LPSTR lpCmdLine)
{
	// Check for single instance option
	if(Shell::CheckSingleInstance(ini))
		return 0;

	char* serviceCls = iniparser_getstr(ini, SERVICE_CLASS);

	// If this is a service we want to default the working directory to the INI dir if not specified
	bool defaultToIniDir = (serviceCls != NULL);

	// Set the current working directory if specified
	WinRun4J::SetWorkingDirectory(ini, defaultToIniDir);

	// Display the splash screen if present
	SplashScreen::ShowSplashImage(hInstance, ini);

	// Check for process priority setting
	WinRun4J::SetProcessPriority(ini);

	// Start vm
	int result = WinRun4J::StartVM(lpCmdLine, ini);
	if(result) {
		return result;
	}

	JNIEnv* env = VM::GetJNIEnv();

	// Register native methods
	JNI::Init(env);
	if(!iniparser_getboolean(ini, DISABLE_NATIVE_METHODS, 0))
		Native::RegisterNatives(env);

	// Startup DDE if requested
	bool ddeInit = DDE::Initialize(hInstance, env, ini);

	// Set console title if required (and console mode)
#ifdef CONSOLE 
	char* title = iniparser_getstr(ini, CONSOLE_TITLE); 
	if(title)
		SetConsoleTitle(title); 
#endif 

	// Run the main class (or service class)
	if(serviceCls != NULL)
		result = Service::Run(hInstance, ini, progargsCount, progargs);
	else
		result = JNI::RunMainClass(env, iniparser_getstr(ini, MAIN_CLASS), progargs);
	
	// Check for exception - if not a service
	if(serviceCls == NULL)
		JNI::PrintStackTrace(env);

	if (ddeInit) DDE::Ready();
	
	// Free the args memory
	WinRun4J::FreeArgs();

	// Close VM (This will block until all non-daemon java threads finish).
	result |= VM::CleanupVM();

	// Close the log
	Log::Close();

	// Unitialize DDE
	if(ddeInit) DDE::Uninitialize();

	return result;
}

// Checks if the command is built in (ie. starts with --WinRun4J)
bool IsBuiltInCommand(LPSTR lpCmdLine)
{
	if(lpCmdLine == NULL)
		return false;

	int len = strlen(lpCmdLine);
	int i = 0;
	for(i = 0; i < len; i++) {
		char c = lpCmdLine[i];
		if(c != ' ' && c != '\"')
			break;
	}

	if(len - i > 11)
		return strncmp(&lpCmdLine[i], "--WinRun4J:", 11) == 0;
	else
		return false;
}

#ifdef CONSOLE
int main(int argc, char* argv[])
{
	HINSTANCE hInstance = (HINSTANCE) GetModuleHandle(NULL);
	LPSTR lpCmdLine = StripArg0(GetCommandLine());
#else
int __stdcall WinMain(HINSTANCE hInstance, HINSTANCE /*hPrevInstance*/, LPSTR lpCmdLine, int /*nCmdShow*/) 
{
	lpCmdLine = StripArg0(GetCommandLine());
#endif

	// Initialise the logger using std streams
	Log::Init(hInstance, NULL, NULL, NULL);

	// Check for Builtin commands
	if(IsBuiltInCommand(lpCmdLine)) {
		int res = WinRun4J::DoBuiltInCommand(hInstance, lpCmdLine);
		Log::Close();
		return res;
	}

	// Load the INI file based on module name
	dictionary* ini = WinRun4J::LoadIniFile(hInstance);
	if(ini == NULL) {
		return 1;
	}
	
	return WinRun4J::ExecuteINI(hInstance, ini, lpCmdLine);
}

