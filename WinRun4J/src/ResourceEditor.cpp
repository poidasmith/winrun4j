/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Peter Smith
 *******************************************************************************/

#include "common/Runtime.h"
#include "common/Resource.h"
#include "common/Log.h"
#include <stdio.h>

int PrintUsage()
{
	printf("WinRun4J Resource Editor v1.0 (winrun4j.sf.net)\n\n");
	printf("Edits resources in executables (EXE) and dynamic link-libraries (DLL).\n\n");
	printf("RCEDIT <option> <exe/dll> [resource]\n\n");
	printf("  filename\tSpecifies the filename of the EXE/DLL.\n");
	printf("  resource\tSpecifies the name of the resource to add to the EXE/DLL.\n");
	printf("  /I\t\tSet the icon as the default icon for the executable.\n");
	printf("  /A\t\tAdds an icon to the EXE/DLL.\n");
	printf("  /N\t\tSets the INI file.\n");
	printf("  /J\t\tAdds a JAR file.\n");
	printf("  /E\t\tExtracts a JAR file from the EXE/DLL.\n");
	printf("  /S\t\tSets the splash image.\n");
	//printf("  /V\t\tSets the version information.\n");
	printf("  /C\t\tClears all resources from the EXE/DLL.\n");
	printf("  /L\t\tLists the resources in the EXE/DLL.\n");
	printf("  /P\t\tOutputs the contents of the INI file in the EXE.\n");
	return 1;
}

int main(int argc, char* argv[])
{
	// Initialize the logger to dump to stdout
	Log::Init(GetModuleHandle(NULL), 0, 0, 0);

	if(argc < 2) {
		return PrintUsage();
	}

	// TODO 
	//  - add checks on icon import to ensure a valid icon
	//  - recognize more resource types when listing
	//  - 
	
	if(strcmp(argv[1], "/I") == 0) {
		if(argc != 4) return PrintUsage();
		LPSTR exeFile = argv[2];
		LPSTR iconFile = argv[3];
		Resource::SetIcon(exeFile, iconFile);
	} else if(strcmp(argv[1], "/A") == 0) {
		if(argc != 4) return PrintUsage();
		LPSTR exeFile = argv[2];
		LPSTR iconFile = argv[3];
		Resource::AddIcon(exeFile, iconFile);
	} else if(strcmp(argv[1], "/N") == 0) {
		if(argc != 4) return PrintUsage();
		LPSTR exeFile = argv[2];
		LPSTR iniFile = argv[3];
		Resource::SetINI(exeFile, iniFile);
	} else if(strcmp(argv[1], "/J") == 0) {
		if(argc != 4) return PrintUsage();
		LPSTR exeFile = argv[2];
		LPSTR jarFile = argv[3];
		Resource::AddJar(exeFile, jarFile);
	} else if(strcmp(argv[1], "/S") == 0) {
		if(argc != 4) return PrintUsage();
		LPSTR exeFile = argv[2];
		LPSTR splashFile = argv[3];
		Resource::SetSplash(exeFile, splashFile);
	} else if(strcmp(argv[1], "/C") == 0) {
		if(argc != 3) return PrintUsage();
		LPSTR exeFile = argv[2];
		Resource::ClearResources(exeFile);
	} else if(strcmp(argv[1], "/L") == 0) {
		if(argc != 3) return PrintUsage();
		LPSTR exeFile = argv[2];
		Resource::ListResources(exeFile);
	} else if(strcmp(argv[1], "/P") == 0) {
		if(argc != 3) return PrintUsage();
		LPSTR exeFile = argv[2];
		Resource::ListINI(exeFile);
	} else {
		return PrintUsage();
	}

	return 0;
}

