--- f:\eclipse\workspace\winrun4j\src\resourceeditor.cpp -----------------------

bool test(bool b, char c, short s, DWORD d, double dd, float f, mystruct t)
{
00405C70 55               push        ebp  
00405C71 8B EC            mov         ebp,esp 
	return true;
00405C73 B0 01            mov         al,1 
}
00405C75 5D               pop         ebp  
00405C76 C3               ret         


void main()
{
00405C80 55               push        ebp  
00405C81 8B EC            mov         ebp,esp 
00405C83 83 EC 18         sub         esp,18h 
	mystruct t;
	test(true, 'b', 1, 234, 234.3, 213.1, t);
00405C86 83 EC 18         sub         esp,18h 
00405C89 8B C4            mov         eax,esp 
00405C8B 8B 4D E8         mov         ecx,dword ptr [t] 
00405C8E 89 08            mov         dword ptr [eax],ecx 
00405C90 8B 55 EC         mov         edx,dword ptr [ebp-14h] 
00405C93 89 50 04         mov         dword ptr [eax+4],edx 
00405C96 8B 4D F0         mov         ecx,dword ptr [ebp-10h] 
00405C99 89 48 08         mov         dword ptr [eax+8],ecx 
00405C9C 8B 55 F4         mov         edx,dword ptr [ebp-0Ch] 
00405C9F 89 50 0C         mov         dword ptr [eax+0Ch],edx 
00405CA2 8B 4D F8         mov         ecx,dword ptr [ebp-8] 
00405CA5 89 48 10         mov         dword ptr [eax+10h],ecx 
00405CA8 8B 55 FC         mov         edx,dword ptr [ebp-4] 
00405CAB 89 50 14         mov         dword ptr [eax+14h],edx 
00405CAE 51               push        ecx  
00405CAF D9 05 0C A0 40 00 fld         dword ptr [__real@4355199a (40A00Ch)] 
00405CB5 D9 1C 24         fstp        dword ptr [esp] 
00405CB8 83 EC 08         sub         esp,8 
00405CBB DD 05 00 A0 40 00 fld         qword ptr [__real@406d49999999999a (40A000h)] 
00405CC1 DD 1C 24         fstp        qword ptr [esp] 
00405CC4 68 EA 00 00 00   push        0EAh 
00405CC9 6A 01            push        1    
00405CCB 6A 62            push        62h  
00405CCD 6A 01            push        1    
00405CCF E8 1B B5 FF FF   call        test (4011EFh) 
00405CD4 83 C4 34         add         esp,34h 
}
00405CD7 33 C0            xor         eax,eax 
00405CD9 8B E5            mov         esp,ebp 
00405CDB 5D               pop         ebp  
00405CDC C3               ret              
