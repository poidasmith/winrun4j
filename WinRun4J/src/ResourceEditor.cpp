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
#ifdef X64
	printf("WinRun4J 64-bit Resource Editor v1.0 (winrun4j.sf.net)\n\n");
#else
	printf("WinRun4J Resource Editor v1.0 (winrun4j.sf.net)\n\n");
#endif
	printf("Edits resources in executables (EXE) and dynamic link-libraries (DLL).\n\n");
#ifdef X64
	printf("RCEDIT64 <option> <exe/dll> [resource]\n\n");
#else
	printf("RCEDIT <option> <exe/dll> [resource]\n\n");
#endif
	printf("  filename\tSpecifies the filename of the EXE/DLL.\n");
	printf("  resource\tSpecifies the name of the resource to add to the EXE/DLL.\n");
	printf("  /I\t\tSet the icon as the default icon for the executable.\n");
	printf("  /A\t\tAdds an icon to the EXE/DLL.\n");
	printf("  /N\t\tSets the INI file.\n");
	printf("  /J\t\tAdds a JAR file.\n");
	printf("  /E\t\tExtracts a JAR file from the EXE/DLL.\n");
	printf("  /S\t\tSets the splash image.\n");
/*
	printf("  /M\t\tSets the manifest.\n");
*/
	printf("  /H\t\tAdds an HTML file the EXE/DLL.\n");
	printf("  /C\t\tClears all resources from the EXE/DLL.\n");
	printf("  /L\t\tLists the resources in the EXE/DLL.\n");
	printf("  /P\t\tOutputs the contents of the INI file in the EXE.\n");
	printf("  /R\t\tLoads a script file listing resource settings.\n");
	printf("  /W\t\tSame as /R but clears all resources first.\n");
	printf("  /D\t\tFurther help on /R command.\n");

	return 1;
}

int PrintScriptHelp() 
{
	printf("Use /R to set a series of resource options on a single executable.\n\n");
	printf("RCEDIT /R <exe/dll> <script file>\n\n");
	printf("ini=<ini file>\n");
	printf("icon.1=<main icon file>\n");
	printf("icon.2=<extra icon file>\n");
	printf("icon.n=<extra icon file>\n");
	printf("jar.1=<jar file>\n");
	printf("html.1=<html file>\n");

/*
	printf("version.FileVersion=x,y,z,a\n");
	printf("version.ProductVersion=x,y,z,a\n");
	printf("version.info.Comments=...\n");
	printf("version.info.CompanyName=...\n");
	printf("version.info.FileDescription=...\n");
	printf("version.info.FileVersion=...\n");
	printf("version.info.InternalName=...\n");
	printf("version.info.LegalCopyright=...\n");
	printf("version.info.LegalTrademarks=...\n");
	printf("version.info.OriginalFilename=...\n");
	printf("version.info.PrivateBuild=...\n");
	printf("version.info.ProductName=...\n");
	printf("version.info.ProductVersion=...\n");
	printf("version.info.SpecialBuild=...\n");
*/

	return 0;
}

int ExecuteResourceScript(LPSTR exeFile, LPSTR iniFile, bool clear)
{
	// Clear existing if required
	if(clear) {
		if(!Resource::ClearResources(exeFile))
			return 1;
	}

	dictionary* ini = iniparser_load(iniFile);
	if(!ini) {
		Log::Error("Could not load INI file: %s", iniFile);
		return 1;
	}

	// Store INI file
	char* appIni = iniparser_getstr(ini, ":ini");
	if(appIni) {
		if(!Resource::SetINI(exeFile, appIni))
			return 1;
	}

	// Store icons
	TCHAR key[MAX_PATH];
	for(int i = 1; i <= 100; i++) {
		sprintf(key, ":icon.%d", i);
		char* iconFile = iniparser_getstr(ini, key);
		if(iconFile) {
			if(i == 1) {
				if(!Resource::SetIcon(exeFile, iconFile))
					return 1;
			} else {
				if(!Resource::AddIcon(exeFile, iconFile))
					return 1;
			}
		} else if(i > 10) {
			break;
		}
	}

	// Store jars
	for(int i = 1; i <= 100; i++) {
		sprintf(key, ":jar.%d", i);
		char* jarFile = iniparser_getstr(ini, key);
		if(jarFile) {
			if(!Resource::AddJar(exeFile, jarFile))
				return 1;
		} else if(i > 10) {
			break;
		}
	}

	// Store HTML
	for(int i = 1; i <= 100; i++) {
		sprintf(key, ":html.%d", i);
		char* htmlFile = iniparser_getstr(ini, key);
		if(htmlFile) {
			if(!Resource::AddHTML(exeFile, htmlFile))
				return 1;
		} else if(i > 10) {
			break;
		}
	}

	// Check for version information (we require version.FileVersion)
	char* fileVer = iniparser_getstr(ini, ":version.FileVersion");
	if(fileVer) {
	}

	return 0;
}

int main5(int argc, char* argv[])
{
	// Initialize the logger to dump to stdout
	Log::Init(GetModuleHandle(NULL), 0, 0, 0);

	if(argc < 2) {
		return PrintUsage();
	}

	// TODO 
	//  - add checks on icon import to ensure a valid icon

	bool ok = true;
	
	if(strcmp(argv[1], "/I") == 0) {
		if(argc != 4) return PrintUsage();
		LPSTR exeFile = argv[2];
		LPSTR iconFile = argv[3];
		ok = Resource::SetIcon(exeFile, iconFile);
	} else if(strcmp(argv[1], "/A") == 0) {
		if(argc != 4) return PrintUsage();
		LPSTR exeFile = argv[2];
		LPSTR iconFile = argv[3];
		ok = Resource::AddIcon(exeFile, iconFile);
	} else if(strcmp(argv[1], "/N") == 0) {
		if(argc != 4) return PrintUsage();
		LPSTR exeFile = argv[2];
		LPSTR iniFile = argv[3];
		ok = Resource::SetINI(exeFile, iniFile);
	} else if(strcmp(argv[1], "/J") == 0) {
		if(argc != 4) return PrintUsage();
		LPSTR exeFile = argv[2];
		LPSTR jarFile = argv[3];
		ok = Resource::AddJar(exeFile, jarFile);
	} else if(strcmp(argv[1], "/H") == 0) {
		if(argc != 4) return PrintUsage();
		LPSTR exeFile = argv[2];
		LPSTR htmlFile = argv[3];
		ok = Resource::AddHTML(exeFile, htmlFile);
	} else if(strcmp(argv[1], "/S") == 0) {
		if(argc != 4) return PrintUsage();
		LPSTR exeFile = argv[2];
		LPSTR splashFile = argv[3];
		ok = Resource::SetSplash(exeFile, splashFile);
	} else if(strcmp(argv[1], "/M") == 0) {
		if(argc != 4) return PrintUsage();
		LPSTR exeFile = argv[2];
		LPSTR manifestFile = argv[3];
		ok = Resource::SetManifest(exeFile, manifestFile);
	} else if(strcmp(argv[1], "/R") == 0) {
		if(argc != 4) return PrintUsage();
		LPSTR exeFile = argv[2];
		LPSTR iniFile = argv[3];
		return ExecuteResourceScript(exeFile, iniFile, false);
	} else if(strcmp(argv[1], "/W") == 0) {
		if(argc != 4) return PrintUsage();
		LPSTR exeFile = argv[2];
		LPSTR iniFile = argv[3];
		return ExecuteResourceScript(exeFile, iniFile, true);
	} else if(strcmp(argv[1], "/C") == 0) {
		if(argc != 3) return PrintUsage();
		LPSTR exeFile = argv[2];
		ok = Resource::ClearResources(exeFile);
	} else if(strcmp(argv[1], "/L") == 0) {
		if(argc != 3) return PrintUsage();
		LPSTR exeFile = argv[2];
		ok = Resource::ListResources(exeFile);
	} else if(strcmp(argv[1], "/P") == 0) {
		if(argc != 3) return PrintUsage();
		LPSTR exeFile = argv[2];
		ok = Resource::ListINI(exeFile);
	} else if(strcmp(argv[1], "/D") == 0) {
		return PrintScriptHelp();
	} else {
		return PrintUsage();
	}

	if(ok) {
		Log::Info("OK");
	}
		
	return ok ? 0 : 1;
}

typedef struct _s {
	int p;
	double v;
	void* x;
} mystruct;

mystruct test()
{
	mystruct t;
	t.p = 1;
	return t;
}

double testD()
{
	return 1.0;
}

void test(bool b, char c, short s, DWORD d, double dd, float f, mystruct t)
{
}

void main()
{
	mystruct t;
	bool b = false;
	if(12 > 3)
		b = true;
	OSVERSIONINFOEX ex;
	ZeroMemory(&ex, sizeof(OSVERSIONINFOEX));
	ex.dwOSVersionInfoSize = sizeof(OSVERSIONINFOEX);
	GetVersionEx((LPOSVERSIONINFO) &ex);
	int* p = (int*) &ex;
	test(b, 'b', 1, 234, 234.3, 213.1, t);
}