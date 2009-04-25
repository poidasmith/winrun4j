/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Peter Smith
 *******************************************************************************/

#ifndef WINRUN4J_H
#define WINRUN4J_H

#include "common/Runtime.h"
#include <stdio.h>
#include <string.h>
#include <jni.h>
#include <string>
#include <commctrl.h>

#include "common/Log.h"
#include "common/INI.h"
#include "common/Dictionary.h"
#include "java/JNI.h"
#include "java/VM.h"
#include "java/Classpath.h"

class WinRun4J
{
public:
	static void SetWorkingDirectory(dictionary* ini);
	static int DoBuiltInCommand(HINSTANCE hInstance, LPSTR lpCmdLine);
	static dictionary* LoadIniFile(HINSTANCE hInstance);
	static int StartVM(LPSTR lpCmdLine, dictionary* ini);
	static void FreeArgs();
	static int ExecuteINI(HINSTANCE hInstance, LPSTR lpCmdLine);
	static int ExecuteINI(HINSTANCE hInstance, dictionary* ini, LPSTR lpCmdLine);
};

#endif // WINRUN4J_H