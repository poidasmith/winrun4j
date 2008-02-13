
#include "../src/common/Runtime.h"
#include "../src/java/JNIClient.h"
#include "../src/java/VM.h"
#include "../src/java/JNI.h"
#include <stdio.h>

int main()
{
	JavaVM* jvm = NULL;
	TCHAR* vmArgs[] = {
		"-Djava.class.path=C:\\Development\\Releases\\winrun4j\\bin\\WinRun4JTest.jar"
		,0
	};
	HRESULT hr = CreateJavaVM("C:\\Program Files\\Java\\jre1.6.0_03\\bin\\client\\jvm.dll", vmArgs, &jvm);
	if(FAILED(hr)) {
		printf("Could not create Java VM: %x\n", hr);
		return 1;
	}

	JNIEnv* env = 0;
	jvm->AttachCurrentThread((void**) &env, NULL);
	JNI::RunMainClass(env, "org/boris/winrun4j/test/WinRunTest", NULL);
	jvm->DestroyJavaVM();
}
