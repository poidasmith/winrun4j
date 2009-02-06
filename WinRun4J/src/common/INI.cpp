/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Peter Smith
 *******************************************************************************/

#include "INI.h"
#include "Log.h"

static dictionary* g_ini = NULL;

void INI::GetNumberedKeysFromIni(dictionary* ini, TCHAR* keyName, TCHAR** entries, int& index)
{
	int i = 0;
	TCHAR entryName[MAX_PATH];
	while(true) {
		sprintf(entryName, "%s.%d", keyName, i+1);
		TCHAR* entry = iniparser_getstr(ini, entryName);
		if(entry != NULL) {
			entries[index++] = _strdup(entry);
		}
		i++;
		if(i > 10 && entry == NULL) {
			break;
		}
	}
	entries[index] = NULL;
}

/* The ini filename is in the same directory as the executable and called the same (except with ini at the end). */
dictionary* INI::LoadIniFile(HINSTANCE hInstance)
{
	TCHAR filename[MAX_PATH], inifile[MAX_PATH];
	GetModuleFileName(hInstance, filename, sizeof(filename));
	strcpy(inifile, filename);
	int len = strlen(inifile);
	// It is assumed the executable ends with "exe"
	inifile[len - 1] = 'i';
	inifile[len - 2] = 'n';
	inifile[len - 3] = 'i';

	return LoadIniFile(hInstance, inifile);
}

dictionary* INI::LoadIniFile(HINSTANCE hInstance, LPSTR inifile)
{
	dictionary* ini = iniparser_load(inifile);
	if(ini == NULL) {
		Log::Error("Could not load INI file: %s", inifile);
		return NULL;
	}

	// Expand environment variables
	ExpandVariables(ini);

	iniparser_setstr(ini, MODULE_INI, inifile);

	// Add module name to ini
	TCHAR filename[MAX_PATH], filedir[MAX_PATH];
	GetModuleFileName(hInstance, filename, MAX_PATH);
	iniparser_setstr(ini, MODULE_NAME, filename);

	// Log init
	Log::Init(hInstance, iniparser_getstr(ini, LOG_FILE), iniparser_getstr(ini, LOG_LEVEL));
	Log::Info("Module Name: %s", filename);
	Log::Info("Module INI: %s", inifile);

	// strip off filename to get module directory
	strcpy(filedir, filename);
	for(int i = strlen(filename) - 1; i >= 0; i--) {
		if(filedir[i] == '\\') {
			filedir[i] = 0;
			break;
		}
	}
	iniparser_setstr(ini, MODULE_DIR, filedir);
	Log::Info("Module Dir: %s", filedir);

	// stip off filename to get ini directory
	strcpy(filedir, inifile);
	for(int i = strlen(inifile) - 1; i >= 0; i--) {
		if(filedir[i] == '\\' || filedir[i] == '/') {
			filedir[i] = 0;
			break;
		}
	}
	iniparser_setstr(ini, INI_DIR, filedir);
	Log::Info("INI Dir: %s", filedir);

	// Store a reference to be used by JNI functions
	g_ini = ini;

	return ini;
}

void INI::ExpandVariables(dictionary* ini)
{
	char tmp[4096];
	for(int i = 0; i < ini->size; i++) {
		char* key = ini->key[i];
		char* value = ini->val[i];
		int size = ExpandEnvironmentStrings(value, tmp, 4096);
		if(size == 0) {
			Log::Warning("Could not expand variable: %s", value);
		}
		iniparser_setstr(ini, key, tmp);
	}
}

jobjectArray INI::GetKeys(JNIEnv* env, jobject self)
{
	jclass clazz = env->FindClass("java/lang/String");
	jobjectArray keys = env->NewObjectArray(g_ini->n, clazz, NULL);
	for(int i = 0; i < g_ini->n; i++) {
		env->SetObjectArrayElement(keys, i, env->NewStringUTF(g_ini->key[i]));
	}
	return keys;
}

jstring INI::GetKey(JNIEnv* env, jobject self, jstring key)
{
	jboolean iscopy = false;
	const char* keyStr = env->GetStringUTFChars(key, &iscopy);
	char* value = iniparser_getstr(g_ini, (char*) keyStr);
	if(value == NULL) {
		return NULL;
	} else {
		return env->NewStringUTF(value);
	}
}

bool INI::RegisterNatives(JNIEnv *env, bool useExcel)
{
	Log::Info("Registering natives for INI class");
	jclass clazz;
	JNINativeMethod methods[2];
	if(useExcel) {
		clazz = env->FindClass("org/excel4j/INI");
	} else {
		clazz = env->FindClass("org/boris/winrun4j/INI");
	}
	if(clazz == NULL) {
		Log::Warning("Could not find INI class");
		if(env->ExceptionOccurred())
			env->ExceptionClear();
		return false;
	}
	methods[0].fnPtr = (void*) GetKeys;
	methods[0].name = "getPropertyKeys";
	methods[0].signature = "()[Ljava/lang/String;";
	methods[1].fnPtr = (void*) GetKey;
	methods[1].name = "getProperty";
	methods[1].signature = "(Ljava/lang/String;)Ljava/lang/String;";
	env->RegisterNatives(clazz, methods, 2);

	return true;
}

