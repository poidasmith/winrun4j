/*******************************************************************************
* This program and the accompanying materials
* are made available under the terms of the Common Public License v1.0
* which accompanies this distribution, and is available at 
* http://www.eclipse.org/legal/cpl-v10.html
* 
* Contributors:
*     Peter Smith
*******************************************************************************/

#include "DDE.h"
#include "../common/Log.h"
#include "../java/VM.h"

static DWORD g_pidInst = 0;
static HSZ g_serverName = 0;
static HSZ g_topic = 0;
static char g_execute[MAX_PATH];
static jclass g_class;
static jmethodID g_methodID;

HDDEDATA CALLBACK DdeCallback(UINT uType, UINT uFmt, HCONV hconv, HDDEDATA hsz1,
    HDDEDATA hsz2, HDDEDATA hdata, HDDEDATA dwData1, HDDEDATA dwData2)
{
	switch (uType)
	{     
		case XTYP_CONNECT: 
			if(hsz2 == (HDDEDATA) g_serverName && hsz1 == (HDDEDATA) g_topic)
			 	return (HDDEDATA) 1;
		case XTYP_EXECUTE: {
			DdeGetData(hdata, (LPBYTE) g_execute, MAX_PATH, 0); 
			DDE::Execute(g_execute);
			break;
	   }
	}

	return 0;
}

bool DDE::Initialize(JNIEnv* env, dictionary* ini)
{
	// Startup DDE library
	UINT result = DdeInitialize(&g_pidInst, (PFNCALLBACK) DdeCallback, APPCMD_FILTERINITS, 0);
	if(result != DMLERR_NO_ERROR) {
		Log::Error("Unable to initialize DDE: %d", result);
		return false;
	}

	g_serverName = DdeCreateStringHandle(g_pidInst, "WinRun4J", CP_WINANSI);
	g_topic = DdeCreateStringHandle(g_pidInst, "System", CP_WINANSI);    

	// Register the server
	DdeNameService(g_pidInst, g_serverName, NULL, DNS_REGISTER);

	// Attach JNI methods
	return RegisterNatives(env);
}

void DDE::Uninitialize()
{
	if(g_serverName) DdeFreeStringHandle(g_pidInst, g_serverName);
	if(g_topic) DdeFreeStringHandle(g_pidInst, g_topic);

	// Shutdown DDE library
	DdeUninitialize(g_pidInst); 
}

void DDE::Execute(LPSTR lpExecuteStr)
{
	JNIEnv* env = VM::GetJNIEnv();
	jstring str = 0;
	if(lpExecuteStr) str = env->NewStringUTF(lpExecuteStr);
	env->CallStaticVoidMethod(g_class, g_methodID, str);
}

bool DDE::RegisterNatives(JNIEnv* env)
{
	g_class = env->FindClass("org/boris/winrun4j/DDE");
	if(g_class == NULL) {
		Log::SetLastError("Could not find DDE class.");
		return false;
	}

	g_methodID = env->GetStaticMethodID(g_class, "execute", "(Ljava/lang/String;)V");
	if(g_methodID == NULL) {
		Log::SetLastError("Could not find execute method");
		return false;
	}

	return true;
}

void DDE::RegisterFileAssociations(dictionary* ini, LPSTR lpCmdLine)
{
}

void DDE::UnregisterFileAssociations(dictionary* ini, LPSTR lpCmdLine)
{
}