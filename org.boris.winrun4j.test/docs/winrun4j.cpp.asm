--- f:\eclipse\workspace\winrun4j\src\winrun4j.cpp -----------------------------

#ifdef CONSOLE
int main(int argc, char* argv[])
{
	HINSTANCE hInstance = (HINSTANCE) GetModuleHandle(NULL);
	LPSTR lpCmdLine = StripArg0(GetCommandLine());
#else
int __stdcall WinMain(HINSTANCE hInstance, HINSTANCE hPrevInstance, LPSTR lpCmdLine, int nCmdShow) 
{
0040D7D0 55               push        ebp  
0040D7D1 8B EC            mov         ebp,esp 
0040D7D3 83 EC 08         sub         esp,8 
	lpCmdLine = StripArg0(GetCommandLine());
0040D7D6 FF 15 F4 86 41 00 call        dword ptr [__imp__GetCommandLineA@0 (4186F4h)] 
0040D7DC 50               push        eax  
0040D7DD E8 5A 38 FF FF   call        StripArg0 (40103Ch) 
0040D7E2 83 C4 04         add         esp,4 
0040D7E5 89 45 10         mov         dword ptr [lpCmdLine],eax 
#endif

	// Initialise the logger using std streams
	Log::Init(hInstance, NULL, NULL, NULL);
0040D7E8 6A 00            push        0    
0040D7EA 6A 00            push        0    
0040D7EC 6A 00            push        0    
0040D7EE 8B 45 08         mov         eax,dword ptr [hInstance] 
0040D7F1 50               push        eax  
0040D7F2 E8 E9 39 FF FF   call        Log::Init (4011E0h) 

	// Check for Builtin commands
	if(IsBuiltInCommand(lpCmdLine)) {
0040D7F7 8B 4D 10         mov         ecx,dword ptr [lpCmdLine] 
0040D7FA 51               push        ecx  
0040D7FB E8 2A 3B FF FF   call        IsBuiltInCommand (40132Ah) 
0040D800 0F B6 D0         movzx       edx,al 
0040D803 85 D2            test        edx,edx 
0040D805 74 1A            je          WinMain+51h (40D821h) 
		int res = WinRun4J::DoBuiltInCommand(hInstance, lpCmdLine);
0040D807 8B 45 10         mov         eax,dword ptr [lpCmdLine] 
0040D80A 50               push        eax  
0040D80B 8B 4D 08         mov         ecx,dword ptr [hInstance] 
0040D80E 51               push        ecx  
0040D80F E8 3B 39 FF FF   call        WinRun4J::DoBuiltInCommand (40114Fh) 
0040D814 89 45 F8         mov         dword ptr [res],eax 
		Log::Close();
0040D817 E8 24 39 FF FF   call        Log::Close (401140h) 
		return res;
0040D81C 8B 45 F8         mov         eax,dword ptr [res] 
0040D81F EB 2A            jmp         WinMain+7Bh (40D84Bh) 
	}

	// Load the INI file based on module name
	dictionary* ini = WinRun4J::LoadIniFile(hInstance);
0040D821 8B 55 08         mov         edx,dword ptr [hInstance] 
0040D824 52               push        edx  
0040D825 E8 18 3C FF FF   call        WinRun4J::LoadIniFile (401442h) 
0040D82A 89 45 FC         mov         dword ptr [ini],eax 
	if(ini == NULL) {
0040D82D 83 7D FC 00      cmp         dword ptr [ini],0 
0040D831 75 07            jne         WinMain+6Ah (40D83Ah) 
		return 1;
0040D833 B8 01 00 00 00   mov         eax,1 
0040D838 EB 11            jmp         WinMain+7Bh (40D84Bh) 
	}
	
	return WinRun4J::ExecuteINI(hInstance, ini, lpCmdLine);
0040D83A 8B 45 10         mov         eax,dword ptr [lpCmdLine] 
0040D83D 50               push        eax  
0040D83E 8B 4D FC         mov         ecx,dword ptr [ini] 
0040D841 51               push        ecx  
0040D842 8B 55 08         mov         edx,dword ptr [hInstance] 
0040D845 52               push        edx  
0040D846 E8 DD 37 FF FF   call        WinRun4J::ExecuteINI (401028h) 
}
0040D84B 8B E5            mov         esp,ebp 
0040D84D 5D               pop         ebp  
0040D84E C2 10 00         ret         10h  
