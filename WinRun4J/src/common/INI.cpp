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
		sprintf_s(entryName, sizeof(entryName), "%s.%d", keyName, i+1);
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
	TCHAR filename[MAX_PATH], inifile[MAX_PATH], filedir[MAX_PATH];
	GetModuleFileName(hInstance, filename, sizeof(filename));
	strcpy_s(inifile, sizeof(inifile), filename);
	strcpy_s(filedir, sizeof(filedir), filename);
	int len = strlen(inifile);
	// It is assumed the executable ends with "exe"
	inifile[len - 1] = 'i';
	inifile[len - 2] = 'n';
	inifile[len - 3] = 'i';
	dictionary* ini = iniparser_load(inifile);
	iniparser_setstr(ini, MODULE_NAME, filename);
	iniparser_setstr(ini, MODULE_INI, inifile);
	Log::Info("Module Name: %s\n", filename);
	Log::Info("Module INI: %s\n", inifile);

	// strip off filename to get module directory
	for(int i = len - 1; i >= 0; i--) {
		if(filedir[i] == '\\') {
			filedir[i] = 0;
			break;
		}
	}
	iniparser_setstr(ini, MODULE_DIR, filedir);
	Log::Info("Module Dir: %s\n", filedir);

	// Store a reference to be used by JNI functions
	g_ini = ini;

	return ini;
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

void INI::RegisterNatives(JNIEnv *env)
{
	jclass clazz;
	JNINativeMethod methods[2];
	clazz = env->FindClass("org/boris/winrun4j/INI");
	if(clazz == NULL) {
		Log::Warning("org.boris.WinRun4J not found in classpath\n");
		if(env->ExceptionOccurred())
			env->ExceptionClear();
		return;
	}
	methods[0].fnPtr = GetKeys;
	methods[0].name = "getPropertyKeys";
	methods[0].signature = "()[Ljava/lang/String;";
	methods[1].fnPtr = GetKey;
	methods[1].name = "getProperty";
	methods[1].signature = "(Ljava/lang/String;)Ljava/lang/String;";
	env->RegisterNatives(clazz, methods, 2);
}

