
#include "../src/common/Runtime.h"
#include "../src/common/COMHelper.h"
#include "../src/java/JNIClient.h"
#include "../src/java/VM.h"
#include "../src/java/JNI.h"
#include <stdio.h>

int main2()
{
	CoInitialize(0);
	BSTR bstr = ConvertCharToBSTR("TEST 1\n");
	//wprintf(bstr);
	char* test = ConvertBSTRToChar(bstr);
	printf(test);
	delete test;
	FreeBSTR(bstr);

	TCHAR* vmArgs[] = {
		"-Djava.class.path=C:\\Development\\Releases\\winrun4j\\bin\\WinRun4JTest.jar"
		,0
	};

	SAFEARRAY *psa = ConvertCharArrayToSafeArray(vmArgs);
	TCHAR** carr = ConvertSafeArrayToCharArray(psa);

	for(int i = 0; carr[i] != 0; i++) {
		printf("%s\n", carr[i]);
	}

	FreeSafeArray(psa);
	delete [] carr;

	return 0;
}

int main()
{
	JavaVM* jvm = NULL;
	TCHAR* vmArgs[] = {
		"-Djava.class.path=C:\\Development\\Releases\\winrun4j\\bin\\WinRun4JTest.jar"
		,0
	};
	HRESULT hr = VM::StartJavaVM("C:\\Program Files\\Java\\jre1.6.0_03\\bin\\client\\jvm.dll", vmArgs, NULL, true);
	if(FAILED(hr)) {
		printf("Could not create Java VM: %x\n", hr);
		return 1;
	}

	JNIEnv* env = VM::GetJNIEnv();
	JNI::RunMainClass(env, "org/boris/winrun4j/test/WinRunTest", NULL);
	VM::CleanupVM();
}
