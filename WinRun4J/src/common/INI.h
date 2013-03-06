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

#include "Runtime.h"
#include "Dictionary.h"
#include <jni.h>

// Internal keys
#define MODULE_NAME "WinRun4J:module.name"
#define MODULE_INI  "WinRun4J:module.ini"
#define MODULE_DIR  "WinRun4J:module.dir"
#define INI_DIR     "WinRun4J:ini.dir"

// Ini keys
#define WORKING_DIR   ":working.directory"
#define LOG_FILE      ":log"
#define LOG_LEVEL     ":log.level"
#define CLASS_PATH    ":classpath"
#define VM_ARG        ":vmarg"
#define PROG_ARG      ":arg"
#define MAIN_CLASS    ":main.class"
#define SERVICE_CLASS ":service.class"

class INI
{
public:
	static void GetNumberedKeysFromIni(dictionary* ini, TCHAR* keyName, TCHAR** entries, UINT& index);
	static dictionary* LoadIniFile(HINSTANCE hInstance);
	static dictionary* LoadIniFile(HINSTANCE hInstance, LPSTR inifile);

	static char* GetString(dictionary* ini, const TCHAR* section, const TCHAR* key, TCHAR* defValue, bool defFromMainSection = true);
	static int   GetInteger(dictionary* ini, const TCHAR* section, const TCHAR* key, int defValue, bool defFromMainSection = true);
	static bool  GetBoolean(dictionary* ini, const TCHAR* section, const TCHAR* key, bool defValue, bool defFromMainSection = true);

private:
	static bool StrTrimInChars(LPSTR trimChars, char c);
	static void StrTrim(LPSTR str, LPSTR trimChars);
	static void ExpandVariables(dictionary* ini);
	static void ExpandRegistryVariables(dictionary* ini);
	static int GetRegistryValue(char* input, char* output, int len);
	static void ParseRegistryKeys(dictionary* ini);
	static HKEY GetHKey(char* key);
};

#endif // INI_H
