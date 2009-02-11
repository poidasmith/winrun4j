/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Peter Smith
 *******************************************************************************/

/*
 * features:
 *   - set icon (ie. set main icon) /I
 *   - add icon (secondary - used for file associations) /+I
 *   - add a way to load icons from exe/dll for use as image in swt
 *   - set ini file
 *   - add/remove jar file /+J 
 *   - classpath.1=winrun4j.exe#test.jar
 *   - classpath.2=test.dll#other.jar
 *   - set splash image
 *   - set version information
 *   - clear all resources
 */

#include "common/Runtime.h"
#include "common/Resource.h"
#include <stdio.h>

// jar - magic[4bytes], name-len[4bytes], name[variable], data-len[4bytes], data[variable]
// ini - magic[4bytes], len[4bytes], data[variable]

int PrintUsage()
{
	printf("WinRun4J Resource Editor v1.0 (winrun4j.sf.net)\n\n");
	printf("Edits resources in executables (EXE) and dynamic link-libraries (DLL).\n\n");
	printf("RCEDIT [option] <filename> <resource>\n\n");
	printf("  filename\tSpecifies the filename of the EXE/DLL.\n");
	printf("  resource\tSpecifies the name of the resource to add to the EXE/DLL.\n");
	printf("  /I\t\tSet the icon as the default icon for the executable.\n");
	printf("  /A\t\tAdds an icon to the EXE/DLL.\n");
	printf("  /B\t\tSets the INI file.\n");
	printf("  /J\t\tAdds a JAR file.\n");
	printf("  /S\t\tSets the splash image.\n");
	printf("  /V\t\tSets the version information.\n");
	printf("  /C\t\tClears all resources from the EXE/DLL.\n");
	return 1;
}

int main(int argc, char* argv[])
{
	if(argc < 2) {
		return PrintUsage();
	}
	
	if(strcmp(argv[1], "/I") == 0) {
		if(argc != 4) return PrintUsage();
		LPSTR exeFile = argv[2];
		LPSTR iconFile = argv[3];
		Resource::SetIcon(exeFile, iconFile);
	}

	printf("%s\n", argv[0]);
	return 0;
}

