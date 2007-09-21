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
#include "common/Registry.h"

#define ERROR_MESSAGES_JAVA_NOT_FOUND "ErrorMessages:java.not.found"

using namespace std;

void WinRun4J::SetWorkingDirectory(dictionary* ini)
{
	char* dir = iniparser_getstr(ini, WORKING_DIR);
	if(dir != NULL) {
		// First set the current directory to the module directory
		SetCurrentDirectory(iniparser_getstr(ini, MODULE_DIR));

		// Now set working directory to specified (this allows for a relative working directory)
		SetCurrentDirectory(dir);
	} 
}

bool WinRun4J::StrTrimInChars(LPSTR trimChars, char c)
{
	unsigned int len = strlen(trimChars);
	for(unsigned int i = 0; i < len; i++) {
		if(c == trimChars[i]) {
			return true;
		}
	}
	return false;
}

void WinRun4J::StrTrim(LPSTR str, LPSTR trimChars)
{
	unsigned int start = 0;
	unsigned int end = strlen(str) - 1;
	for(unsigned int i = 0; i < end; i++) {
		char c = str[i];
		if(!StrTrimInChars(trimChars, c)) {
			start = i;
			break;
		}
	}
	for(int i = end; i >= 0; i--) {
		char c = str[i];
		if(!StrTrimInChars(trimChars, c)) {
			end = i;
			break;
		}
	}
	if(start != 0 || end != strlen(str) - 1) {
		int k = 0;
		for(unsigned int i = start; i <= end; i++, k++) {
			str[k] = str[i];
		}
		str[k] = 0;
	}
}

void WinRun4J::ParseCommandLine(LPSTR lpCmdLine, TCHAR** args, int& count, bool includeFirst)
{
	StrTrim(lpCmdLine, " ");
	int len = strlen(lpCmdLine);
	if(len == 0) {
		return;
	}

	int start = 0;
	bool quote = false;
	bool first = true;
	TCHAR arg[4096];
	for(int i = 0; i < len; i++) {
		char c = lpCmdLine[i];
		if(c == '\"') {
			quote = !quote;
		} else if(!quote && c == ' ') {
			if(!first || includeFirst) {
				int k = 0;
				for(int j = start; j < i; j++, k++) {
					arg[k] = lpCmdLine[j];
				}
				arg[k] = 0;
				args[count] = strdup(arg);
				StrTrim(args[count], " ");
				StrTrim(args[count], "\"");
				count++;
			}
			start = i;
			first = false;
		}
	}

	// Add the last one
	if(!first || includeFirst) {
		int k = 0;
		for(int j = start; j < len; j++, k++) {
			arg[k] = lpCmdLine[j];
		}
		arg[k] = 0;
		args[count] = _strdup(arg);
		StrTrim(args[count], " ");
		StrTrim(args[count], "\"");
		count++;
	}
}

void WinRun4J::DoBuiltInCommand(HINSTANCE hInstance, LPSTR lpCmdLine)
{
	// Check for SetIcon util request
	if(strncmp(lpCmdLine, "--WinRun4J:SetIcon", 20) == 0) {
		Icon::SetExeIcon(lpCmdLine);
		return;
	}

	// Check for RegisterDDE util request
	if(strncmp(lpCmdLine, "--WinRun4J:RegisterFileAssociations", 23) == 0) {
		DDE::RegisterFileAssociations(WinRun4J::LoadIniFile(hInstance), lpCmdLine);
		return;
	}

	// Check for UnregisterDDE util request
	if(strncmp(lpCmdLine, "--WinRun4J:UnregisterFileAssociations", 25) == 0) {
		DDE::UnregisterFileAssociations(WinRun4J::LoadIniFile(hInstance), lpCmdLine);
		return;
	}

	// Check for Register Service util request
	if(strncmp(lpCmdLine, "--WinRun4J:RegisterService", 27) == 0) {
		Service::Register(lpCmdLine);
		return;
	}

	// Check for Unregister Service util request
	if(strncmp(lpCmdLine, "--WinRun4J:UnregisterService", 29) == 0) {
		Service::Unregister(lpCmdLine);
		return;
	}
}

dictionary* WinRun4J::LoadIniFile(HINSTANCE hInstance)
{
	dictionary* ini = INI::LoadIniFile(hInstance);
	if(ini == NULL) {
		MessageBox(NULL, "Failed to find or load ini file.", "Startup Error", 0);
		Log::Close();
		return NULL;
	}

	return ini;
}


#ifdef CONSOLE
int main(int argc, char* argv[])
{
	char lpCmdLine[4096];
	for(int i = 0; i < argc; i++) {
		strcat(lpCmdLine, "\"");
		strcat(lpCmdLine, argv[i]);
		strcat(lpCmdLine, "\" ");
	}
	HINSTANCE hInstance = (HINSTANCE) GetModuleHandle(NULL);
#else
int __stdcall WinMain(HINSTANCE hInstance, HINSTANCE hPrevInstance, LPSTR lpCmdLine, int nCmdShow) 
{
	int argc = 0;
	char** argv = 0;
#endif

	// Initialise the logger using std streams
	Log::Init(hInstance, NULL, NULL);

	// Check for Builtin commands
	if(strncmp(lpCmdLine, "--WinRun4J:", 11) == 0) {
		WinRun4J::DoBuiltInCommand(hInstance, lpCmdLine);
		Log::Close();
		return 0;
	}

	// Load the INI file based on module name
	dictionary* ini = WinRun4J::LoadIniFile(hInstance);
	if(ini == NULL) {
		return 1;
	}

	// Set the current working directory if specified
	WinRun4J::SetWorkingDirectory(ini);

	// Now initialise the logger using std streams + specified log dir
	Log::Init(hInstance, iniparser_getstr(ini, LOG_FILE), iniparser_getstr(ini, LOG_LEVEL));

	// Display the splash screen if present
	SplashScreen::ShowSplashImage(hInstance, ini);

	// Attempt to find an appropriate java VM
	char* vmlibrary = VM::FindJavaVMLibrary(ini);
	if(!vmlibrary) {
		char* javaNotFound = iniparser_getstr(ini, ERROR_MESSAGES_JAVA_NOT_FOUND);
		MessageBox(NULL, (javaNotFound == NULL ? "Failed to find Java VM." : javaNotFound), "Startup Error", 0);
		Log::Close();
		return 1;
	}

	// Collect the VM args from the INI file
	TCHAR *vmargs[MAX_PATH];
	int vmargsCount = 0;
	INI::GetNumberedKeysFromIni(ini, VM_ARG, vmargs, vmargsCount);

	// Build up the classpath and add to vm args
	Classpath::BuildClassPath(ini, vmargs, vmargsCount);

	// Extract the specific VM args
	VM::ExtractSpecificVMArgs(ini, vmargs, vmargsCount);

	// Log the VM args
	for(int i = 0; i < vmargsCount; i++) {
		Log::Info("vmarg.%d=%s\n", i, vmargs[i]);
	}

	// Collect the program arguments from the INI file
	TCHAR *progargs[MAX_PATH];
	int progargsCount = 0;
	INI::GetNumberedKeysFromIni(ini, PROG_ARG, progargs, progargsCount);

	// Add the args from commandline
	WinRun4J::ParseCommandLine(lpCmdLine, progargs, progargsCount);

	// Log the commandline args
	for(int i = 0; i < progargsCount; i++) {
		Log::Info("arg.%d=%s\n", i, progargs[i]);
	}

	// Make sure there is a NULL at the end of the args
	vmargs[vmargsCount] = NULL;
	progargs[progargsCount] = NULL;

	// Fix main class - ie. replace x.y.z with x/y/z for use in jni
	char* mainClass = iniparser_getstr(ini, MAIN_CLASS);
	if(mainClass != NULL) {
		int len = strlen(mainClass);
		for(int i = 0; i < len; i++) {
			if(mainClass[i] == '.') {
				mainClass[i] = '/';
			}
		}
	}
	if(mainClass == NULL) {
		Log::Error("ERROR: no main class specified\n");
		return 1;
	} else {
		Log::Info("Main Class: %s\n", mainClass);
	}

	// Start the VM
	if(VM::StartJavaVM(vmlibrary, vmargs) != 0) {
		Log::Error("Error starting java VM\n");
		return 1;
	}

	JNIEnv* env = VM::GetJNIEnv();

	// Register native methods
	INI::RegisterNatives(env);
	SplashScreen::RegisterNatives(env);
	Registry::RegisterNatives(env);
	Shell::RegisterNatives(env);
	DDE::Initialize(env, ini);

	// Run the main class
	JNI::RunMainClass(env, iniparser_getstr(ini, MAIN_CLASS), progargs);
	
	// Free vm args
	for(int i = 0; i < vmargsCount; i++) {
		free(vmargs[i]);
	}

	// Free program args
	for(int i = 0; i < progargsCount; i++) {
		free(progargs[i]);
	}

	// Close VM (This will block until all non-daemon java threads finish).
	int result = VM::CleanupVM();

	// Close the log
	Log::Close();

	// Unitialize DDE
	DDE::Uninitialize();

	return result;
}

