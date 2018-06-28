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
#define ARGS_ALLOW_OVERRIDES                ":args.allow.overrides"
#define ARGS_ALLOW_VMARGS                   ":args.allow.vmargs"
#define ARGS_OVERRIDE_PREFIX                ":args.override.prefix"
#define ERROR_MESSAGES_SHOW_POPUP           "ErrorMessages:show.popup"
#define ERROR_MESSAGES_JAVA_NOT_FOUND       "ErrorMessages:java.not.found"
#define ERROR_MESSAGES_JAVA_START_FAILED    "ErrorMessages:java.failed"
#define ERROR_MESSAGES_MAIN_CLASS_NOT_FOUND "ErrorMessages:main.class.not.found"

namespace 
{
	TCHAR *vmargs[MAX_PATH];
	UINT vmargsCount = 0;
	TCHAR *progargs[MAX_PATH];
	UINT progargsCount = 0;
	UINT progargsOffset = 0;
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

int WinRun4J::DoBuiltInCommand(HINSTANCE hInstance)
{
	LPSTR lpArg1 = progargs[0];

	// Make sure we also log to console
	Log::SetLogFileAndConsole(true);

	// Check for RegisterDDE util request
	if(StartsWith(lpArg1, "--WinRun4J:RegisterFileAssociations")) {
		return DDE::RegisterFileAssociations(WinRun4J::LoadIniFile(hInstance));
	}

	// Check for UnregisterDDE util request
	if(StartsWith(lpArg1, "--WinRun4J:UnregisterFileAssociations")) {
		return DDE::UnregisterFileAssociations(WinRun4J::LoadIniFile(hInstance));
	}

	// Check for Register Service util request
	if(StartsWith(lpArg1, "--WinRun4J:RegisterService")) {
		dictionary* ini = INI::LoadIniFile(hInstance);
		if(ini == NULL) 
			return 1;
		return Service::Register(ini);
	}

	// Check for Unregister Service util request
	if(StartsWith(lpArg1, "--WinRun4J:UnregisterService")) {
		dictionary* ini = INI::LoadIniFile(hInstance);
		if(ini == NULL) 
			return 1;
		return Service::Unregister(ini);
	}

	if(StartsWith(lpArg1, "--WinRun4J:PrintINI")) {
		dictionary* ini = INI::LoadIniFile(hInstance);
		if(ini == NULL) 
			return 1;
		for(int i = 0; i < ini->n; i++) 
			printf("%s=%s\n", ini->key[i], ini->val[i]);
		return 0;
	}

	if(StartsWith(lpArg1, "--WinRun4J:ExecuteINI")) {
		if(progargsCount < 2) {
			Log::Error("INI file not specified");
			return 1;
		}
		dictionary* ini = INI::LoadIniFile(hInstance, progargs[1]);
		progargsOffset = 2;
		return WinRun4J::ExecuteINI(hInstance, ini);
	}

	if(StartsWith(lpArg1, "--WinRun4J:Version")) {
		Log::Info("0.4.5\n");
		return 0;
	}

	Log::Error("Unrecognized command: %s", lpArg1);
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

int WinRun4J::StartVM(dictionary* ini)
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
	char java_home[MAX_PATH];
	if (strstr(vmlibrary, "\\jdk") != NULL) {
		strncpy(java_home, vmlibrary, strlen(vmlibrary) - strlen(strstr(strstr(vmlibrary, "\\jdk") + 1, "\\")));
		SetEnvironmentVariable("JDK_HOME", java_home);
		Log::Info("Setting JDK to: %s", java_home);
	} else if (strstr(vmlibrary, "\\jre") != NULL) {
		strncpy(java_home, vmlibrary, strlen(vmlibrary) - strlen(strstr(strstr(vmlibrary, "\\jre") + 1, "\\")));
		SetEnvironmentVariable("JRE_HOME", java_home);
		Log::Info("Setting JRE_HOME to: %s", java_home);
	}

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

	// Make sure there is a NULL at the end of the args
	vmargs[vmargsCount] = NULL;

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

void WinRun4J::ProcessCommandLineArgs(dictionary* ini)
{
	bool allowOverrides = iniparser_getboolean(ini, ARGS_ALLOW_OVERRIDES, true);
	bool allowVmargs    = iniparser_getboolean(ini, ARGS_ALLOW_VMARGS, true);
	LPSTR overrideArg   = iniparser_getstring(ini, ARGS_OVERRIDE_PREFIX, "-W");
	UINT oaLen          = strlen(overrideArg);

	// Get the max index for prog args so we can add from there
	UINT paMax = INI::GetNumberedKeysMax(ini, ":arg");
	UINT vmMax = INI::GetNumberedKeysMax(ini, ":vmarg");

	// Loop through each program argument - check if we have an override and apply
	// otherwise add to INI arg.N
	TCHAR entryName[MAX_PATH];
	for(UINT i = progargsOffset; i < progargsCount; i++) {
		if(allowOverrides && StartsWith(progargs[i], overrideArg)) {
			char* nmptr = &(progargs[i][oaLen]);
			char* eqptr = strchr(nmptr, '=');
			char* scptr = strchr(nmptr, ':');
			bool inSection = scptr && (scptr < eqptr || !eqptr);
			int offset = 0;
			if(!inSection) {
				entryName[0] = ':';
				offset = 1;
			}
			if(eqptr) {
				strncpy((char *) (entryName + offset), nmptr, eqptr - nmptr);
				entryName[eqptr - nmptr + offset] = 0;
				iniparser_setstr(ini, entryName, eqptr+1);
			} else {
				// Remove the entry
				strcpy((char *) (entryName + offset), nmptr);
				iniparser_unset(ini, entryName);
			}
		} else if(allowVmargs && (StartsWith(progargs[i], "-X") || StartsWith(progargs[i], "-D"))) {
			sprintf(entryName, ":vmarg.%d", ++vmMax);
			iniparser_setstr(ini, entryName, progargs[i]);
		} else {
			sprintf(entryName, ":arg.%d", ++paMax);
			iniparser_setstr(ini, entryName, progargs[i]);
		}
	}
}

int WinRun4J::ExecuteINI(HINSTANCE hInstance, dictionary* ini)
{
	// Merge in command line args and overrides
	ProcessCommandLineArgs(ini);

	// Check for single instance option
	if(Shell::CheckSingleInstance(ini))
		return 0;

	// Check if we are in service or main mode
	char* serviceCls = iniparser_getstr(ini, SERVICE_CLASS);
	char* mainCls    = iniparser_getstr(ini, MAIN_CLASS);
	bool serviceMode = iniparser_getboolean(ini, SERVICE_MODE, serviceCls != NULL);

	// If this is a service we want to default the working directory to the INI dir if not specified
	bool defaultToIniDir = serviceMode;

	// Set the current working directory if specified
	WinRun4J::SetWorkingDirectory(ini, defaultToIniDir);

	// Display the splash screen if present (only for main mode)
	if(!serviceMode)
		SplashScreen::ShowSplashImage(hInstance, ini);

	// Check for process priority setting
	WinRun4J::SetProcessPriority(ini);

	// Start vm
	int result = WinRun4J::StartVM(ini);
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

	// Pull out the command line args (plus any existing INI args)
	TCHAR *argv[MAX_PATH];
	UINT argc = 0;
	INI::GetNumberedKeysFromIni(ini, ":arg", argv, argc);

	// Run the main class (or service class)
	if(serviceMode)
		result = Service::Run(hInstance, ini, argc, argv);
	else
		result = JNI::RunMainClass(env, mainCls, argc, argv);
	
	// Check for exception - if not a service
	if(serviceCls == NULL)
		JNI::PrintStackTrace(env);

	if (ddeInit) DDE::Ready();
	
	// Free the args memory
	WinRun4J::FreeArgs();

	// Close VM (This will block until all non-daemon java threads finish).
	result |= VM::CleanupVM();
	Log::Info("VM closed and cleaned up");

	// Close the log
	Log::Close();

	// Unitialize DDE
	if(ddeInit) DDE::Uninitialize();

	return result;
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

	// Parse cmd line so we can check for built ins and overrides
	ParseCommandLine(lpCmdLine, progargs, progargsCount, true);

	// Check for Builtin commands
	if(progargsCount && strncmp(progargs[0], "--WinRun4J:", 11) == 0) {
		int res = WinRun4J::DoBuiltInCommand(hInstance);
		Log::Close();
		return res;
	}

	// Load the INI file based on module name
	dictionary* ini = WinRun4J::LoadIniFile(hInstance);
	if(ini == NULL) {
		return 1;
	}
	return WinRun4J::ExecuteINI(hInstance, ini);
}

