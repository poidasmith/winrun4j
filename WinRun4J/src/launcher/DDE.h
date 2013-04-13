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

struct DDEInfo
{
	dictionary* ini;
	char* extension;
	char* name;
	char* description;
};

class DDE
{
public:
	// Lifecycle
	static bool Initialize(HINSTANCE hInstance, JNIEnv* env, dictionary* ini);
	static void Uninitialize();
	static bool RegisterDDE();
	static void RegisterWindow(HINSTANCE hInstance);
	static void Ready();

	// Registration helpers
	static int RegisterFileAssociations(dictionary* ini);
	static int UnregisterFileAssociations(dictionary* ini);

	// Execute
	static void Execute(LPSTR lpExecuteStr);

	// Client
	static bool NotifySingleInstance(dictionary* ini);

private:
	static bool RegisterNatives(JNIEnv* env, dictionary* ini);
	static int EnumFileAssocations(dictionary* ini, bool isRegister, int (*CallbackFunc)(DDEInfo&));
	static int RegisterFileAssociation(DDEInfo&);
	static int UnregisterFileAssociation(DDEInfo&);
};

#endif // DDE_H