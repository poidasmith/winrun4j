/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Peter Smith
 *******************************************************************************/

#include <windows.h>
#include <stdio.h>
#include <string.h>
#include <jni.h>
#include <string>

#include "INI.h"
#include "JNI.h"
#include "Icon.h"
#include "Log.h"
#include "VM.h"
#include "Classpath.h"

class WinRun4J
{
public:
	static void SetWorkingDirectory(dictionary* ini);
	static void GetNumberedKeysFromIni(dictionary* ini, TCHAR* keyName, TCHAR** entries, int& index);
	static dictionary* LoadIniFile(HINSTANCE hInstance);
	static void ParseCommandLine(LPSTR lpCmdLine, TCHAR** args, int& count);

private:
	static bool StrTrimInChars(LPSTR trimChars, char c);
	static void StrTrim(LPSTR str, LPSTR trimChars);
};