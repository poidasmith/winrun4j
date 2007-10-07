/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Peter Smith
 *******************************************************************************/

#ifndef SERVICE_H
#define SERVICE_H

#include "../common/Runtime.h"
#include "../common/INI.h"

class Service
{
public:
	static void Register(LPSTR lpCmdLine);
	static void Unregister(LPSTR lpCmdLine);
	static int Run(HINSTANCE hInstance, dictionary* ini, int argc, char* argv[]);

	// Internal methods
	static const char* GetName();
	static int GetControlsAccepted();
	static int Control(DWORD opCode);
	static int Main(DWORD argc, LPSTR* argv);
};

#endif // SERVICE_H