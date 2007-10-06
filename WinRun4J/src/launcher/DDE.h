/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Peter Smith
 *******************************************************************************/

#ifndef DDE_H
#define DDE_H

#include "../common/Runtime.h"
#include "../common/INI.h"
#include <jni.h>

class DDE
{
public:
	// Lifecycle
	static bool Initialize(HINSTANCE hInstance, JNIEnv* env, dictionary* ini);
	static void Uninitialize();
	static bool RegisterDDE();
	static void RegisterWindow(HINSTANCE hInstance);

	// Registration helpers
	static void RegisterFileAssociations(dictionary* ini, LPSTR lpCmdLine);
	static void UnregisterFileAssociations(dictionary* ini, LPSTR lpCmdLine);

	// Execute
	static void Execute(LPSTR lpExecuteStr);

private:
	static bool RegisterNatives(JNIEnv* env, dictionary* ini);
};

#endif // DDE_H