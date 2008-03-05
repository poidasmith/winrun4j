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
#include "common/Registry.h"

#define ERROR_MESSAGES_JAVA_NOT_FOUND "ErrorMessages:java.not.found"
#define ERROR_MESSAGES_JAVA_START_FAILED "ErrorMessages:java.failed"
#define SINGLE_INSTANCE_OPTION ":single.instance"

using namespace std;

static TCHAR *vmargs[MAX_PATH];
static int vmargsCount = 0;
static TCHAR *progargs[MAX_PATH];
static int progargsCount = 0;

void WinRun4J::SetWorkingDirectory(dictionary* ini)
{
	char* dir = iniparser_getstr(ini, WORKING_DIR);
	if(dir != NULL) {
		// First set the current directory to the module directory
		SetCurrentDirectory(iniparser_getstr(ini, INI_DIR));

		// Now set working directory to specified (this allows for a relative working directory)
		SetCurrentDirectory(dir);
	} 
}

int WinRun4J::DoBuiltInCommand(HINSTANCE hInstance, LPSTR lpCmdLine)
{
	// Remove any leading whitespace
	StrTrim(lpCmdLine, " ");

	// Check for SetIcon util request
	if(StartsWith(lpCmdLine, "--WinRun4J:SetIcon")) {
		Icon::SetExeIcon(lpCmdLine);
		return 0;
	}

	// Check for AddIcon util request
	if(StartsWith(lpCmdLine, "--WinRun4J:AddIcon")) {
		Icon::AddExeIcon(lpCmdLine);
		return 0;
	}

	// Check for DeleteIcon util request
	if(StartsWith(lpCmdLine, "--WinRun4J:RemoveIcon")) {
		Icon::RemoveExeIcons(lpCmdLine);
		return 0;
	}

	// Check for RegisterDDE util request
	if(StartsWith(lpCmdLine, "--WinRun4J:RegisterFileAssociations")) {
		DDE::RegisterFileAssociations(WinRun4J::LoadIniFile(hInstance), lpCmdLine);
		return 0;
	}

	// Check for UnregisterDDE util request
	if(StartsWith(lpCmdLine, "--WinRun4J:UnregisterFileAssociations")) {
		DDE::UnregisterFileAssociations(WinRun4J::LoadIniFile(hInstance), lpCmdLine);
		return 0;
	}

	// Check for Register Service util request
	if(StartsWith(lpCmdLine, "--WinRun4J:RegisterService")) {
		dictionary* ini = INI::LoadIniFile(hInstance);
		if(ini == NULL) 
			return 1;
		Service::Register(ini);
		return 0;
	}

	// Check for Unregister Service util request
	if(StartsWith(lpCmdLine, "--WinRun4J:UnregisterService")) {
		dictionary* ini = INI::LoadIniFile(hInstance);
		if(ini == NULL) 
			return 1;
		Service::Unregister(ini);
		return 0;
	}

	if(StartsWith(lpCmdLine, "--WinRun4J:ExecuteINI")) {
		return WinRun4J::ExecuteINI(hInstance, lpCmdLine);
	}

	Log::Error("Unrecognized command: %s", lpCmdLine);
	return 1;
}

dictionary* WinRun4J::LoadIniFile(HINSTANCE hInstance)
{
	dictionary* ini = INI::LoadIniFile(hInstance);
	if(ini == NULL) {
#ifdef CONSOLE
		Log::Error("Failed to find or load ini file.");
#else
		MessageBox(NULL, "Failed to find or load ini file.", "Startup Error", 0);
#endif
		Log::Close();
		return NULL;
	}

	return ini;
}

int WinRun4J::StartVM(LPSTR lpCmdLine, dictionary* ini)
{
	// Attempt to find an appropriate java VM
	char* vmlibrary = VM::FindJavaVMLibrary(ini);
	if(!vmlibrary) {
		char* javaNotFound = iniparser_getstr(ini, ERROR_MESSAGES_JAVA_NOT_FOUND);
		Log::Error("Failed to find Java VM.");
		MessageBox(NULL, (javaNotFound == NULL ? "Failed to find Java VM." : javaNotFound), "Startup Error", 0);
		Log::Close();
		return 1;
	}

	Log::Info("Found VM: %s\n", vmlibrary);

	// Collect the VM args from the INI file
	INI::GetNumberedKeysFromIni(ini, VM_ARG, vmargs, vmargsCount);

	// Build up the classpath and add to vm args
	Classpath::BuildClassPath(ini, vmargs, vmargsCount);

	// Extract the specific VM args
	VM::ExtractSpecificVMArgs(ini, vmargs, vmargsCount);

	// Log the VM args
	Log::Info("VM Args\n");
	TCHAR argl[MAX_PATH];
	for(int i = 0; i < vmargsCount; i++) {
		StrTruncate(argl, vmargs[i], MAX_PATH);
		Log::Info("vmarg.%d=%s\n", i, argl);
	}

	// Collect the program arguments from the INI file
	INI::GetNumberedKeysFromIni(ini, PROG_ARG, progargs, progargsCount);

	// Add the args from commandline
	ParseCommandLine(lpCmdLine, progargs, progargsCount, true);

	// Log the commandline args
	Log::Info("Program Args\n");
	for(int i = 0; i < progargsCount; i++) {
		StrTruncate(argl, progargs[i], MAX_PATH);
		Log::Info("arg.%d=%s\n", i, argl);
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
		Log::Error("No main class specified\n");
		return 1;
	} else {
		Log::Info("Main Class: %s\n", mainClass);
	}

	// Start the VM
	if(VM::StartJavaVM(vmlibrary, vmargs, NULL) != 0) {
		Log::Error("Error starting Java VM\n");
		char* javaFailed = iniparser_getstr(ini, ERROR_MESSAGES_JAVA_START_FAILED);
		MessageBox(NULL, (javaFailed == NULL ? "Error starting Java VM." : javaFailed), "Startup Error", 0);
		Log::Close();
		return 1;
	}

	return 0;
}

void WinRun4J::FreeArgs()
{
	// Free vm args
	for(int i = 0; i < vmargsCount; i++) {
		free(vmargs[i]);
	}

	// Free program args
	for(int i = 0; i < progargsCount; i++) {
		free(progargs[i]);
	}
}

int WinRun4J::ExecuteINI(HINSTANCE hInstance, LPSTR lpCmdLine)
{
	TCHAR *tmpProgargs[MAX_PATH];
	int tmpProgargsCount = 0;
	ParseCommandLine(lpCmdLine, tmpProgargs, tmpProgargsCount, false);

	// The first arg should be the ini file
	if(tmpProgargsCount == 0) {
		Log::Error("INI file not specified");
		return 1;
	}

	dictionary* ini = INI::LoadIniFile(hInstance, tmpProgargs[0]);

	return 1;
}

int WinRun4J::ExecuteINI(HINSTANCE hInstance, dictionary* ini, LPSTR lpCmdLine)
{
	// Check for single instance option
	char* singleInstance = iniparser_getstr(ini, SINGLE_INSTANCE_OPTION);
	if(singleInstance != NULL && strcmp(singleInstance, "true") == 0) {
		if(Shell::CheckSingleInstance())
			return 1;
	}

	// Set the current working directory if specified
	WinRun4J::SetWorkingDirectory(ini);

	// Now initialise the logger using std streams + specified log dir
	Log::Init(hInstance, iniparser_getstr(ini, LOG_FILE), iniparser_getstr(ini, LOG_LEVEL));

	// Display the splash screen if present
	SplashScreen::ShowSplashImage(hInstance, ini);

	// Start vm
	int result = WinRun4J::StartVM(lpCmdLine, ini);
	if(result) {
		return result;
	}

	JNIEnv* env = VM::GetJNIEnv();

	// Register native methods
	Log::RegisterNatives(env);
	INI::RegisterNatives(env);
	SplashScreen::RegisterNatives(env);
	Registry::RegisterNatives(env);
	Shell::RegisterNatives(env);
	EventLog::RegisterNatives(env);

	// Startup DDE if requested
	bool ddeInit = DDE::Initialize(hInstance, env, ini);

	// Run the main class (or service class)
	char* serviceCls = iniparser_getstr(ini, SERVICE_CLASS);
	if(serviceCls != NULL)
		Service::Run(hInstance, ini, progargsCount, progargs);
	else
		JNI::RunMainClass(env, iniparser_getstr(ini, MAIN_CLASS), progargs);
	
	if (ddeInit) DDE::Ready();
	
	// Free the args memory
	WinRun4J::FreeArgs();

	// Close VM (This will block until all non-daemon java threads finish).
	result = VM::CleanupVM();

	// Close the log
	Log::Close();

	// Unitialize DDE
	if(ddeInit) DDE::Uninitialize();

	return result;
}

// Checks if the command is built in - strips off whitespace and quotes at the start.
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
int __stdcall WinMain(HINSTANCE hInstance, HINSTANCE hPrevInstance, LPSTR lpCmdLine, int nCmdShow) 
{
	lpCmdLine = StripArg0(GetCommandLine());
#endif

	// Initialise the logger using std streams
	Log::Init(hInstance, NULL, NULL);

	// Check for Builtin commands
	if(IsBuiltInCommand(lpCmdLine)) {
		WinRun4J::DoBuiltInCommand(hInstance, lpCmdLine);
		Log::Close();
		return 0;
	}

	// Load the INI file based on module name
	dictionary* ini = WinRun4J::LoadIniFile(hInstance);
	if(ini == NULL) {
		return 1;
	}
	
	return WinRun4J::ExecuteINI(hInstance, ini, lpCmdLine);
}

