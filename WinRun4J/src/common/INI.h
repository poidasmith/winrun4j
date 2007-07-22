/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Peter Smith
 *******************************************************************************/

#ifndef INI_H
#define INI_H

#include "Dictionary.h"
#include <windows.h>
#include <jni.h>

// Internal keys
#define MODULE_NAME "WinRun4J:ModuleName"
#define MODULE_INI "WinRun4J:ModuleIni"
#define MODULE_BASE "WinRun4J:ModuleBaseName"
#define MODULE_DIR "WinRun4J:ModuleDir"

// Ini keys
#define WORKING_DIR ":working.directory"
#define MAIN_CLASS ":main.class"
#define LOG_FILE ":log"
#define LOG_LEVEL ":log.level"
#define CLASS_PATH ":classpath"
#define VM_ARG ":vmarg"
#define PROG_ARG ":arg"

class INI
{
public:
	static void GetNumberedKeysFromIni(dictionary* ini, TCHAR* keyName, TCHAR** entries, int& index);
	static dictionary* LoadIniFile(HINSTANCE hInstance);
	static bool RegisterNatives(JNIEnv *env);

private:
	static bool StrTrimInChars(LPSTR trimChars, char c);
	static void StrTrim(LPSTR str, LPSTR trimChars);

	// JNI functions
	static jstring GetKey(JNIEnv* env, jobject self, jstring key);
	static jobjectArray GetKeys(JNIEnv* env, jobject self);
};

#endif // INI_H