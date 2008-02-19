
#include "../src/common/Runtime.h"
#include "../src/common/COMHelper.h"
#include "../src/java/VM.h"
#include "../src/java/JNI.h"
#include <stdio.h>

int main()
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

