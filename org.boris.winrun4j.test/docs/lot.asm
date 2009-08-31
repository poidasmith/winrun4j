
    jstring NewStringUTF(const char *utf) {
004035A0 55               push        ebp  
004035A1 8B EC            mov         ebp,esp 
004035A3 51               push        ecx  
004035A4 89 4D FC         mov         dword ptr [ebp-4],ecx 
        return functions->NewStringUTF(this,utf);
004035A7 8B 45 08         mov         eax,dword ptr [utf] 
004035AA 50               push        eax  
004035AB 8B 4D FC         mov         ecx,dword ptr [this] 
004035AE 51               push        ecx  
004035AF 8B 55 FC         mov         edx,dword ptr [this] 
004035B2 8B 02            mov         eax,dword ptr [edx] 
004035B4 8B 88 9C 02 00 00 mov         ecx,dword ptr [eax+29Ch] 
004035BA FF D1            call        ecx  
    }
004035BC 8B E5            mov         esp,ebp 
004035BE 5D               pop         ebp  
004035BF C2 04 00         ret         4    
--- No source file -------------------------------------------------------------

struct JNIEnv_ {
    const struct JNINativeInterface_ *functions;
#ifdef __cplusplus

    jint GetVersion() {
        return functions->GetVersion(this);
    }
    jclass DefineClass(const char *name, jobject loader, const jbyte *buf,
               jsize len) {
        return functions->DefineClass(this, name, loader, buf, len);
    }
    jclass FindClass(const char *name) {
004035D0 55               push        ebp  
004035D1 8B EC            mov         ebp,esp 
004035D3 51               push        ecx  
004035D4 89 4D FC         mov         dword ptr [ebp-4],ecx 
        return functions->FindClass(this, name);
004035D7 8B 45 08         mov         eax,dword ptr [name] 
004035DA 50               push        eax  
004035DB 8B 4D FC         mov         ecx,dword ptr [this] 
004035DE 51               push        ecx  
004035DF 8B 55 FC         mov         edx,dword ptr [this] 
004035E2 8B 02            mov         eax,dword ptr [edx] 
004035E4 8B 48 18         mov         ecx,dword ptr [eax+18h] 
004035E7 FF D1            call        ecx  
    }
004035E9 8B E5            mov         esp,ebp 
004035EB 5D               pop         ebp  
004035EC C2 04 00         ret         4    
--- No source file -------------------------------------------------------------
--- f:\eclipse\workspace\winrun4j\src\common\dictionary.cpp --------------------
#define DICTMINSZ   128
#define DICT_INVALID_KEY    ((char*)-1)

static void * mem_double(void * ptr, int size)
{
    void    *   newptr ;
 
    newptr = calloc(2*size, 1);
    memcpy(newptr, ptr, size);
    free(ptr);
    return newptr ;
}



unsigned dictionary_hash(char * key)
{
004036B0 55               push        ebp  
004036B1 8B EC            mov         ebp,esp 
004036B3 83 EC 0C         sub         esp,0Ch 
    int         len ;
    unsigned    hash ;
    int         i ;

    len = strlen(key);
004036B6 8B 45 08         mov         eax,dword ptr [key] 
004036B9 50               push        eax  
004036BA FF 15 68 86 41 00 call        dword ptr [__imp__lstrlenA@4 (418668h)] 
004036C0 89 45 F8         mov         dword ptr [len],eax 
    for (hash=0, i=0 ; i<len ; i++) {
004036C3 C7 45 F4 00 00 00 00 mov         dword ptr [hash],0 
004036CA C7 45 FC 00 00 00 00 mov         dword ptr [i],0 
004036D1 EB 09            jmp         dictionary_hash+2Ch (4036DCh) 
004036D3 8B 4D FC         mov         ecx,dword ptr [i] 
004036D6 83 C1 01         add         ecx,1 
004036D9 89 4D FC         mov         dword ptr [i],ecx 
004036DC 8B 55 FC         mov         edx,dword ptr [i] 
004036DF 3B 55 F8         cmp         edx,dword ptr [len] 
004036E2 7D 29            jge         dictionary_hash+5Dh (40370Dh) 
        hash += (unsigned)key[i] ;
004036E4 8B 45 08         mov         eax,dword ptr [key] 
004036E7 03 45 FC         add         eax,dword ptr [i] 
004036EA 0F BE 08         movsx       ecx,byte ptr [eax] 
004036ED 03 4D F4         add         ecx,dword ptr [hash] 
004036F0 89 4D F4         mov         dword ptr [hash],ecx 
        hash += (hash<<10);
004036F3 8B 55 F4         mov         edx,dword ptr [hash] 
004036F6 C1 E2 0A         shl         edx,0Ah 
004036F9 03 55 F4         add         edx,dword ptr [hash] 
004036FC 89 55 F4         mov         dword ptr [hash],edx 
        hash ^= (hash>>6) ;
004036FF 8B 45 F4         mov         eax,dword ptr [hash] 
00403702 C1 E8 06         shr         eax,6 
00403705 33 45 F4         xor         eax,dword ptr [hash] 
00403708 89 45 F4         mov         dword ptr [hash],eax 
    }
0040370B EB C6            jmp         dictionary_hash+23h (4036D3h) 
    hash += (hash <<3);
0040370D 8B 4D F4         mov         ecx,dword ptr [hash] 
00403710 8B 55 F4         mov         edx,dword ptr [hash] 
00403713 8D 04 CA         lea         eax,[edx+ecx*8] 
00403716 89 45 F4         mov         dword ptr [hash],eax 
    hash ^= (hash >>11);
00403719 8B 4D F4         mov         ecx,dword ptr [hash] 
0040371C C1 E9 0B         shr         ecx,0Bh 
0040371F 33 4D F4         xor         ecx,dword ptr [hash] 
00403722 89 4D F4         mov         dword ptr [hash],ecx 
    hash += (hash <<15);
00403725 8B 55 F4         mov         edx,dword ptr [hash] 
00403728 C1 E2 0F         shl         edx,0Fh 
0040372B 03 55 F4         add         edx,dword ptr [hash] 
0040372E 89 55 F4         mov         dword ptr [hash],edx 
    return hash ;
00403731 8B 45 F4         mov         eax,dword ptr [hash] 
}
00403734 8B E5            mov         esp,ebp 
00403736 5D               pop         ebp  
00403737 C2 04 00         ret         4    
--- No source file -------------------------------------------------------------
0040373A CC               int         3    
0040373B CC               int         3    
0040373C CC               int         3    
0040373D CC               int         3    
0040373E CC               int         3    
0040373F CC               int         3    
--- f:\eclipse\workspace\winrun4j\src\common\dictionary.cpp --------------------


dictionary * dictionary_new(int size)
{
00403740 55               push        ebp  
00403741 8B EC            mov         ebp,esp 
00403743 51               push        ecx  
    dictionary  *   d ;

    /* If no size was specified, allocate space for DICTMINSZ */
    if (size<DICTMINSZ) size=DICTMINSZ ;
00403744 81 7D 08 80 00 00 00 cmp         dword ptr [size],80h 
0040374B 7D 07            jge         dictionary_new+14h (403754h) 
0040374D C7 45 08 80 00 00 00 mov         dword ptr [size],80h 

    d = (dictionary *)calloc(1, sizeof(dictionary));
00403754 6A 14            push        14h  
00403756 6A 01            push        1    
00403758 E8 C3 A3 00 00   call        calloc (40DB20h) 
0040375D 83 C4 08         add         esp,8 
00403760 89 45 FC         mov         dword ptr [d],eax 
    d->size = size ;
00403763 8B 45 FC         mov         eax,dword ptr [d] 
00403766 8B 4D 08         mov         ecx,dword ptr [size] 
00403769 89 48 04         mov         dword ptr [eax+4],ecx 
    d->val  = (char **)calloc(size, sizeof(char*));
0040376C 6A 04            push        4    
0040376E 8B 55 08         mov         edx,dword ptr [size] 
00403771 52               push        edx  
00403772 E8 A9 A3 00 00   call        calloc (40DB20h) 
00403777 83 C4 08         add         esp,8 
0040377A 8B 4D FC         mov         ecx,dword ptr [d] 
0040377D 89 41 08         mov         dword ptr [ecx+8],eax 
    d->key  = (char **)calloc(size, sizeof(char*));
00403780 6A 04            push        4    
00403782 8B 55 08         mov         edx,dword ptr [size] 
00403785 52               push        edx  
00403786 E8 95 A3 00 00   call        calloc (40DB20h) 
0040378B 83 C4 08         add         esp,8 
0040378E 8B 4D FC         mov         ecx,dword ptr [d] 
00403791 89 41 0C         mov         dword ptr [ecx+0Ch],eax 
    d->hash = (unsigned int *)calloc(size, sizeof(unsigned));
00403794 6A 04            push        4    
00403796 8B 55 08         mov         edx,dword ptr [size] 
00403799 52               push        edx  
0040379A E8 81 A3 00 00   call        calloc (40DB20h) 
0040379F 83 C4 08         add         esp,8 
004037A2 8B 4D FC         mov         ecx,dword ptr [d] 
004037A5 89 41 10         mov         dword ptr [ecx+10h],eax 
    return d ;
004037A8 8B 45 FC         mov         eax,dword ptr [d] 
}
004037AB 8B E5            mov         esp,ebp 
004037AD 5D               pop         ebp  
004037AE C2 04 00         ret         4    
--- No source file -------------------------------------------------------------
004037B1 CC               int         3    
004037B2 CC               int         3    
004037B3 CC               int         3    
004037B4 CC               int         3    
004037B5 CC               int         3    
004037B6 CC               int         3    
004037B7 CC               int         3    
004037B8 CC               int         3    
004037B9 CC               int         3    
004037BA CC               int         3    
004037BB CC               int         3    
004037BC CC               int         3    
004037BD CC               int         3    
004037BE CC               int         3    
004037BF CC               int         3    
--- f:\eclipse\workspace\winrun4j\src\common\dictionary.cpp --------------------



void dictionary_del(dictionary * d)
{
004037C0 55               push        ebp  
004037C1 8B EC            mov         ebp,esp 
004037C3 51               push        ecx  
    int     i ;

    if (d==NULL) return ;
004037C4 83 7D 08 00      cmp         dword ptr [d],0 
004037C8 75 05            jne         dictionary_del+0Fh (4037CFh) 
004037CA E9 A0 00 00 00   jmp         dictionary_del+0AFh (40386Fh) 
    for (i=0 ; i<d->size ; i++) {
004037CF C7 45 FC 00 00 00 00 mov         dword ptr [i],0 
004037D6 EB 09            jmp         dictionary_del+21h (4037E1h) 
004037D8 8B 45 FC         mov         eax,dword ptr [i] 
004037DB 83 C0 01         add         eax,1 
004037DE 89 45 FC         mov         dword ptr [i],eax 
004037E1 8B 4D 08         mov         ecx,dword ptr [d] 
004037E4 8B 55 FC         mov         edx,dword ptr [i] 
004037E7 3B 51 04         cmp         edx,dword ptr [ecx+4] 
004037EA 7D 4A            jge         dictionary_del+76h (403836h) 
        if (d->key[i]!=NULL)
004037EC 8B 45 08         mov         eax,dword ptr [d] 
004037EF 8B 48 0C         mov         ecx,dword ptr [eax+0Ch] 
004037F2 8B 55 FC         mov         edx,dword ptr [i] 
004037F5 83 3C 91 00      cmp         dword ptr [ecx+edx*4],0 
004037F9 74 15            je          dictionary_del+50h (403810h) 
            free(d->key[i]);
004037FB 8B 45 08         mov         eax,dword ptr [d] 
004037FE 8B 48 0C         mov         ecx,dword ptr [eax+0Ch] 
00403801 8B 55 FC         mov         edx,dword ptr [i] 
00403804 8B 04 91         mov         eax,dword ptr [ecx+edx*4] 
00403807 50               push        eax  
00403808 E8 FB A2 00 00   call        free (40DB08h) 
0040380D 83 C4 04         add         esp,4 
        if (d->val[i]!=NULL)
00403810 8B 4D 08         mov         ecx,dword ptr [d] 
00403813 8B 51 08         mov         edx,dword ptr [ecx+8] 
00403816 8B 45 FC         mov         eax,dword ptr [i] 
00403819 83 3C 82 00      cmp         dword ptr [edx+eax*4],0 
0040381D 74 15            je          dictionary_del+74h (403834h) 
            free(d->val[i]);
0040381F 8B 4D 08         mov         ecx,dword ptr [d] 
00403822 8B 51 08         mov         edx,dword ptr [ecx+8] 
00403825 8B 45 FC         mov         eax,dword ptr [i] 
00403828 8B 0C 82         mov         ecx,dword ptr [edx+eax*4] 
0040382B 51               push        ecx  
0040382C E8 D7 A2 00 00   call        free (40DB08h) 
00403831 83 C4 04         add         esp,4 
    }
00403834 EB A2            jmp         dictionary_del+18h (4037D8h) 
    free(d->val);
00403836 8B 55 08         mov         edx,dword ptr [d] 
00403839 8B 42 08         mov         eax,dword ptr [edx+8] 
0040383C 50               push        eax  
0040383D E8               db          e8h  
0040383E ??               db          c6h  
0040383F A2 00 00 83 C4   mov         byte ptr ds:[C4830000h],al 
00403844 04 8B            add         al,8Bh 
00403846 4D               dec         ebp  
00403847 08 8B 51 0C 52 E8 or          byte ptr [ebx-17ADF3AFh],cl 
0040384D B7 A2            mov         bh,0A2h 
0040384F 00 00            add         byte ptr [eax],al 
00403851 83 C4 04         add         esp,4 
    free(d->hash);
00403854 8B 45 08         mov         eax,dword ptr [d] 
00403857 8B 48 10         mov         ecx,dword ptr [eax+10h] 
0040385A 51               push        ecx  
0040385B E8 A8 A2 00 00   call        free (40DB08h) 
00403860 83 C4 04         add         esp,4 
    free(d);
00403863 8B 55 08         mov         edx,dword ptr [d] 
00403866 52               push        edx  
00403867 E8 9C A2 00 00   call        free (40DB08h) 
0040386C 83 C4 04         add         esp,4 
    return ;
}
0040386F 8B E5            mov         esp,ebp 
00403871 5D               pop         ebp  
00403872 C2 04 00         ret         4    
--- No source file -------------------------------------------------------------
00403875 CC               int         3    
00403876 CC               int         3    
00403877 CC               int         3    
00403878 CC               int         3    
00403879 CC               int         3    
0040387A CC               int         3    
0040387B CC               int         3    
0040387C CC               int         3    
0040387D CC               int         3    
0040387E CC               int         3    
0040387F CC               int         3    
--- f:\eclipse\workspace\winrun4j\src\common\dictionary.cpp --------------------


char * dictionary_get(dictionary * d, char * key, char * def)
{
00403880 55               push        ebp  
00403881 8B EC            mov         ebp,esp 
00403883 83 EC 08         sub         esp,8 
    unsigned    hash ;
    int         i ;

    hash = dictionary_hash(key);
00403886 8B 45 0C         mov         eax,dword ptr [key] 
00403889 50               push        eax  
0040388A E8 13 DB FF FF   call        dictionary_hash (4013A2h) 
0040388F 89 45 F8         mov         dword ptr [hash],eax 
    for (i=0 ; i<d->size ; i++) {
00403892 C7 45 FC 00 00 00 00 mov         dword ptr [i],0 
00403899 EB 09            jmp         dictionary_get+24h (4038A4h) 
0040389B 8B 4D FC         mov         ecx,dword ptr [i] 
0040389E 83 C1 01         add         ecx,1 
004038A1 89 4D FC         mov         dword ptr [i],ecx 
004038A4 8B 55 08         mov         edx,dword ptr [d] 
004038A7 8B 45 FC         mov         eax,dword ptr [i] 
004038AA 3B 42 04         cmp         eax,dword ptr [edx+4] 
004038AD 7D 49            jge         dictionary_get+78h (4038F8h) 
        if (d->key==NULL)
004038AF 8B 4D 08         mov         ecx,dword ptr [d] 
004038B2 83 79 0C 00      cmp         dword ptr [ecx+0Ch],0 
004038B6 75 02            jne         dictionary_get+3Ah (4038BAh) 
            continue ;
004038B8 EB E1            jmp         dictionary_get+1Bh (40389Bh) 
        /* Compare hash */
        if (hash==d->hash[i]) {
004038BA 8B 55 08         mov         edx,dword ptr [d] 
004038BD 8B 42 10         mov         eax,dword ptr [edx+10h] 
004038C0 8B 4D FC         mov         ecx,dword ptr [i] 
004038C3 8B 55 F8         mov         edx,dword ptr [hash] 
004038C6 3B 14 88         cmp         edx,dword ptr [eax+ecx*4] 
004038C9 75 2B            jne         dictionary_get+76h (4038F6h) 
            /* Compare string, to avoid hash collisions */
            if (!strcmp(key, d->key[i])) {
004038CB 8B 45 08         mov         eax,dword ptr [d] 
004038CE 8B 48 0C         mov         ecx,dword ptr [eax+0Ch] 
004038D1 8B 55 FC         mov         edx,dword ptr [i] 
004038D4 8B 04 91         mov         eax,dword ptr [ecx+edx*4] 
004038D7 50               push        eax  
004038D8 8B 4D 0C         mov         ecx,dword ptr [key] 
004038DB 51               push        ecx  
004038DC E8 15 A2 00 00   call        strcmp (40DAF6h) 
004038E1 83 C4 08         add         esp,8 
004038E4 85 C0            test        eax,eax 
004038E6 75 0E            jne         dictionary_get+76h (4038F6h) 
                return d->val[i] ;
004038E8 8B 55 08         mov         edx,dword ptr [d] 
004038EB 8B 42 08         mov         eax,dword ptr [edx+8] 
004038EE 8B 4D FC         mov         ecx,dword ptr [i] 
004038F1 8B 04 88         mov         eax,dword ptr [eax+ecx*4] 
004038F4 EB 05            jmp         dictionary_get+7Bh (4038FBh) 
            }
        }
    }
004038F6 EB A3            jmp         dictionary_get+1Bh (40389Bh) 
    return def ;
004038F8 8B 45 10         mov         eax,dword ptr [def] 
}
004038FB 8B E5            mov         esp,ebp 
004038FD 5D               pop         ebp  
004038FE C2 0C 00         ret         0Ch  
--- No source file -------------------------------------------------------------
00403901 CC               int         3    
00403902 CC               int         3    
00403903 CC               int         3    
00403904 CC               int         3    
00403905 CC               int         3    
00403906 CC               int         3    
00403907 CC               int         3    
00403908 CC               int         3    
00403909 CC               int         3    
0040390A CC               int         3    
0040390B CC               int         3    
0040390C CC               int         3    
0040390D CC               int         3    
0040390E CC               int         3    
0040390F CC               int         3    
--- f:\eclipse\workspace\winrun4j\src\common\dictionary.cpp --------------------

char dictionary_getchar(dictionary * d, char * key, char def)
{
00403910 55               push        ebp  
00403911 8B EC            mov         ebp,esp 
00403913 51               push        ecx  
    char * v ;

    if ((v=dictionary_get(d,key,DICT_INVALID_KEY))==DICT_INVALID_KEY) {
00403914 6A FF            push        0FFFFFFFFh 
00403916 8B 45 0C         mov         eax,dword ptr [key] 
00403919 50               push        eax  
0040391A 8B 4D 08         mov         ecx,dword ptr [d] 
0040391D 51               push        ecx  
0040391E E8 7B D9 FF FF   call        dictionary_get (40129Eh) 
00403923 89 45 FC         mov         dword ptr [v],eax 
00403926 83 7D FC FF      cmp         dword ptr [v],0FFFFFFFFh 
0040392A 75 07            jne         dictionary_getchar+23h (403933h) 
        return def ;
0040392C 8A 45 10         mov         al,byte ptr [def] 
0040392F EB 07            jmp         dictionary_getchar+28h (403938h) 
    } else {
00403931 EB 05            jmp         dictionary_getchar+28h (403938h) 
        return v[0] ;
00403933 8B 55 FC         mov         edx,dword ptr [v] 
00403936 8A 02            mov         al,byte ptr [edx] 
    }
}
00403938 8B E5            mov         esp,ebp 
0040393A 5D               pop         ebp  
0040393B C2 0C 00         ret         0Ch  
--- No source file -------------------------------------------------------------
0040393E CC               int         3    
0040393F CC               int         3    
--- f:\eclipse\workspace\winrun4j\src\common\dictionary.cpp --------------------


int dictionary_getint(dictionary * d, char * key, int def)
{
00403940 55               push        ebp  
00403941 8B EC            mov         ebp,esp 
00403943 51               push        ecx  
    char * v ;

    if ((v=dictionary_get(d,key,DICT_INVALID_KEY))==DICT_INVALID_KEY) {
00403944 6A FF            push        0FFFFFFFFh 
00403946 8B 45 0C         mov         eax,dword ptr [key] 
00403949 50               push        eax  
0040394A 8B 4D 08         mov         ecx,dword ptr [d] 
0040394D 51               push        ecx  
0040394E E8 4B D9 FF FF   call        dictionary_get (40129Eh) 
00403953 89 45 FC         mov         dword ptr [v],eax 
00403956 83 7D FC FF      cmp         dword ptr [v],0FFFFFFFFh 
0040395A 75 07            jne         dictionary_getint+23h (403963h) 
        return def ;
0040395C 8B 45 10         mov         eax,dword ptr [def] 
0040395F EB 0E            jmp         dictionary_getint+2Fh (40396Fh) 
    } else {
00403961 EB 0C            jmp         dictionary_getint+2Fh (40396Fh) 
        return atoi(v);
00403963 8B 55 FC         mov         edx,dword ptr [v] 
00403966 52               push        edx  
00403967 E8 BA A1 00 00   call        atoi (40DB26h) 
0040396C 83 C4 04         add         esp,4 
    }
}
0040396F 8B E5            mov         esp,ebp 
00403971 5D               pop         ebp  
00403972 C2 0C 00         ret         0Ch  
--- No source file -------------------------------------------------------------
00403975 CC               int         3    
00403976 CC               int         3    
00403977 CC               int         3    
00403978 CC               int         3    
00403979 CC               int         3    
0040397A CC               int         3    
0040397B CC               int         3    
0040397C CC               int         3    
0040397D CC               int         3    
0040397E CC               int         3    
0040397F CC               int         3    
--- f:\eclipse\workspace\winrun4j\src\common\dictionary.cpp --------------------

double dictionary_getdouble(dictionary * d, char * key, double def)
{
00403980 55               push        ebp  
00403981 8B EC            mov         ebp,esp 
00403983 51               push        ecx  
    char * v ;

    if ((v=dictionary_get(d,key,DICT_INVALID_KEY))==DICT_INVALID_KEY) {
00403984 6A FF            push        0FFFFFFFFh 
00403986 8B 45 0C         mov         eax,dword ptr [key] 
00403989 50               push        eax  
0040398A 8B 4D 08         mov         ecx,dword ptr [d] 
0040398D 51               push        ecx  
0040398E E8 0B D9 FF FF   call        dictionary_get (40129Eh) 
00403993 89 45 FC         mov         dword ptr [v],eax 
00403996 83 7D FC FF      cmp         dword ptr [v],0FFFFFFFFh 
0040399A 75 07            jne         dictionary_getdouble+23h (4039A3h) 
        return def ;
0040399C DD 45 10         fld         qword ptr [def] 
0040399F EB 0E            jmp         dictionary_getdouble+2Fh (4039AFh) 
    } else {
004039A1 EB 0C            jmp         dictionary_getdouble+2Fh (4039AFh) 
        return atof(v);
004039A3 8B 55 FC         mov         edx,dword ptr [v] 
004039A6 52               push        edx  
004039A7 E8 80 A1 00 00   call        atof (40DB2Ch) 
004039AC 83 C4 04         add         esp,4 
    }
}
004039AF 8B E5            mov         esp,ebp 
004039B1 5D               pop         ebp  
004039B2 C2 10 00         ret         10h  
--- No source file -------------------------------------------------------------
004039B5 CC               int         3    
004039B6 CC               int         3    
004039B7 CC               int         3    
004039B8 CC               int         3    
004039B9 CC               int         3    
004039BA CC               int         3    
004039BB CC               int         3    
004039BC CC               int         3    
004039BD CC               int         3    
004039BE CC               int         3    
004039BF CC               int         3    
--- f:\eclipse\workspace\winrun4j\src\common\dictionary.cpp --------------------


void dictionary_set(dictionary * d, char * key, char * val)
{
004039C0 55               push        ebp  
004039C1 8B EC            mov         ebp,esp 
004039C3 83 EC 10         sub         esp,10h 
    int         i ;
    unsigned    hash ;

    if (d==NULL || key==NULL) return ;
004039C6 83 7D 08 00      cmp         dword ptr [d],0 
004039CA 74 06            je          dictionary_set+12h (4039D2h) 
004039CC 83 7D 0C 00      cmp         dword ptr [key],0 
004039D0 75 05            jne         dictionary_set+17h (4039D7h) 
004039D2 E9 D3 01 00 00   jmp         dictionary_set+1EAh (403BAAh) 
    
    /* Compute hash for this key */
    hash = dictionary_hash(key) ;
004039D7 8B 45 0C         mov         eax,dword ptr [key] 
004039DA 50               push        eax  
004039DB E8 C2 D9 FF FF   call        dictionary_hash (4013A2h) 
004039E0 89 45 F8         mov         dword ptr [hash],eax 
    /* Find if value is already in blackboard */
    if (d->n>0) {
004039E3 8B 4D 08         mov         ecx,dword ptr [d] 
004039E6 83 39 00         cmp         dword ptr [ecx],0 
004039E9 0F 8E BB 00 00 00 jle         dictionary_set+0EAh (403AAAh) 
        for (i=0 ; i<d->size ; i++) {
004039EF C7 45 FC 00 00 00 00 mov         dword ptr [i],0 
004039F6 EB 09            jmp         dictionary_set+41h (403A01h) 
004039F8 8B 55 FC         mov         edx,dword ptr [i] 
004039FB 83 C2 01         add         edx,1 
004039FE 89 55 FC         mov         dword ptr [i],edx 
00403A01 8B 45 08         mov         eax,dword ptr [d] 
00403A04 8B 4D FC         mov         ecx,dword ptr [i] 
00403A07 3B 48 04         cmp         ecx,dword ptr [eax+4] 
00403A0A 0F 8D 9A 00 00 00 jge         dictionary_set+0EAh (403AAAh) 
            if (d->key[i]==NULL)
00403A10 8B 55 08         mov         edx,dword ptr [d] 
00403A13 8B 42 0C         mov         eax,dword ptr [edx+0Ch] 
00403A16 8B 4D FC         mov         ecx,dword ptr [i] 
00403A19 83 3C 88 00      cmp         dword ptr [eax+ecx*4],0 
00403A1D 75 02            jne         dictionary_set+61h (403A21h) 
                continue ;
00403A1F EB D7            jmp         dictionary_set+38h (4039F8h) 
            if (hash==d->hash[i]) { /* Same hash value */
00403A21 8B 55 08         mov         edx,dword ptr [d] 
00403A24 8B 42 10         mov         eax,dword ptr [edx+10h] 
00403A27 8B 4D FC         mov         ecx,dword ptr [i] 
00403A2A 8B 55 F8         mov         edx,dword ptr [hash] 
00403A2D 3B 14 88         cmp         edx,dword ptr [eax+ecx*4] 
00403A30 75 73            jne         dictionary_set+0E5h (403AA5h) 
                if (!strcmp(key, d->key[i])) {   /* Same key */
00403A32 8B 45 08         mov         eax,dword ptr [d] 
00403A35 8B 48 0C         mov         ecx,dword ptr [eax+0Ch] 
00403A38 8B 55 FC         mov         edx,dword ptr [i] 
00403A3B 8B 04 91         mov         eax,dword ptr [ecx+edx*4] 
00403A3E 50               push        eax  
00403A3F 8B 4D 0C         mov         ecx,dword ptr [key] 
00403A42 51               push        ecx  
00403A43 E8 AE A0 00 00   call        strcmp (40DAF6h) 
00403A48 83 C4 08         add         esp,8 
00403A4B 85 C0            test        eax,eax 
00403A4D 75 56            jne         dictionary_set+0E5h (403AA5h) 
                    /* Found a value: modify and return */
                    if (d->val[i]!=NULL)
00403A4F 8B 55 08         mov         edx,dword ptr [d] 
00403A52 8B 42 08         mov         eax,dword ptr [edx+8] 
00403A55 8B 4D FC         mov         ecx,dword ptr [i] 
00403A58 83 3C 88 00      cmp         dword ptr [eax+ecx*4],0 
00403A5C 74 15            je          dictionary_set+0B3h (403A73h) 
                        free(d->val[i]);
00403A5E 8B 55 08         mov         edx,dword ptr [d] 
00403A61 8B 42 08         mov         eax,dword ptr [edx+8] 
00403A64 8B 4D FC         mov         ecx,dword ptr [i] 
00403A67 8B 14 88         mov         edx,dword ptr [eax+ecx*4] 
00403A6A 52               push        edx  
00403A6B E8 98 A0 00 00   call        free (40DB08h) 
00403A70 83 C4 04         add         esp,4 
                    d->val[i] = val ? strdup(val) : NULL ;
00403A73 83 7D 10 00      cmp         dword ptr [val],0 
00403A77 74 11            je          dictionary_set+0CAh (403A8Ah) 
00403A79 8B 45 10         mov         eax,dword ptr [val] 
00403A7C 50               push        eax  
00403A7D E8 6D D7 FF FF   call        @ILT+490(_strdup) (4011EFh) 
00403A82 83 C4 04         add         esp,4 
00403A85 89 45 F4         mov         dword ptr [ebp-0Ch],eax 
00403A88 EB 07            jmp         dictionary_set+0D1h (403A91h) 
00403A8A C7 45 F4 00 00 00 00 mov         dword ptr [ebp-0Ch],0 
00403A91 8B 4D 08         mov         ecx,dword ptr [d] 
00403A94 8B 51 08         mov         edx,dword ptr [ecx+8] 
00403A97 8B 45 FC         mov         eax,dword ptr [i] 
00403A9A 8B 4D F4         mov         ecx,dword ptr [ebp-0Ch] 
00403A9D 89 0C 82         mov         dword ptr [edx+eax*4],ecx 
                    /* Value has been modified: return */
                    return ;
00403AA0 E9 05 01 00 00   jmp         dictionary_set+1EAh (403BAAh) 
                }
            }
        }
00403AA5 E9 4E FF FF FF   jmp         dictionary_set+38h (4039F8h) 
    }
    /* Add a new value */
    /* See if dictionary needs to grow */
    if (d->n==d->size) {
00403AAA 8B 55 08         mov         edx,dword ptr [d] 
00403AAD 8B 45 08         mov         eax,dword ptr [d] 
00403AB0 8B 0A            mov         ecx,dword ptr [edx] 
00403AB2 3B 48 04         cmp         ecx,dword ptr [eax+4] 
00403AB5 75 62            jne         dictionary_set+159h (403B19h) 

        /* Reached maximum size: reallocate blackboard */
        d->val  = (char **)mem_double(d->val,  d->size * sizeof(char*)) ;
00403AB7 8B 55 08         mov         edx,dword ptr [d] 
00403ABA 8B 42 04         mov         eax,dword ptr [edx+4] 
00403ABD C1 E0 02         shl         eax,2 
00403AC0 50               push        eax  
00403AC1 8B 4D 08         mov         ecx,dword ptr [d] 
00403AC4 8B 51 08         mov         edx,dword ptr [ecx+8] 
00403AC7 52               push        edx  
00403AC8 E8 E3 00 00 00   call        mem_double (403BB0h) 
00403ACD 8B 4D 08         mov         ecx,dword ptr [d] 
00403AD0 89 41 08         mov         dword ptr [ecx+8],eax 
        d->key  = (char **)mem_double(d->key,  d->size * sizeof(char*)) ;
00403AD3 8B 55 08         mov         edx,dword ptr [d] 
00403AD6 8B 42 04         mov         eax,dword ptr [edx+4] 
00403AD9 C1 E0 02         shl         eax,2 
00403ADC 50               push        eax  
00403ADD 8B 4D 08         mov         ecx,dword ptr [d] 
00403AE0 8B 51 0C         mov         edx,dword ptr [ecx+0Ch] 
00403AE3 52               push        edx  
00403AE4 E8 C7 00 00 00   call        mem_double (403BB0h) 
00403AE9 8B 4D 08         mov         ecx,dword ptr [d] 
00403AEC 89 41 0C         mov         dword ptr [ecx+0Ch],eax 
        d->hash = (unsigned int *)mem_double(d->hash, d->size * sizeof(unsigned)) ;
00403AEF 8B 55 08         mov         edx,dword ptr [d] 
00403AF2 8B 42 04         mov         eax,dword ptr [edx+4] 
00403AF5 C1 E0 02         shl         eax,2 
00403AF8 50               push        eax  
00403AF9 8B 4D 08         mov         ecx,dword ptr [d] 
00403AFC 8B 51 10         mov         edx,dword ptr [ecx+10h] 
00403AFF 52               push        edx  
00403B00 E8 AB 00 00 00   call        mem_double (403BB0h) 
00403B05 8B 4D 08         mov         ecx,dword ptr [d] 
00403B08 89 41 10         mov         dword ptr [ecx+10h],eax 

        /* Double size */
        d->size *= 2 ;
00403B0B 8B 55 08         mov         edx,dword ptr [d] 
00403B0E 8B 42 04         mov         eax,dword ptr [edx+4] 
00403B11 D1 E0            shl         eax,1 
00403B13 8B 4D 08         mov         ecx,dword ptr [d] 
00403B16 89 41 04         mov         dword ptr [ecx+4],eax 
    }

    /* Insert key in the first empty slot */
    for (i=0 ; i<d->size ; i++) {
00403B19 C7 45 FC 00 00 00 00 mov         dword ptr [i],0 
00403B20 EB 09            jmp         dictionary_set+16Bh (403B2Bh) 
00403B22 8B 55 FC         mov         edx,dword ptr [i] 
00403B25 83 C2 01         add         edx,1 
00403B28 89 55 FC         mov         dword ptr [i],edx 
00403B2B 8B 45 08         mov         eax,dword ptr [d] 
00403B2E 8B 4D FC         mov         ecx,dword ptr [i] 
00403B31 3B 48 04         cmp         ecx,dword ptr [eax+4] 
00403B34 7D 13            jge         dictionary_set+189h (403B49h) 
        if (d->key[i]==NULL) {
00403B36 8B 55 08         mov         edx,dword ptr [d] 
00403B39 8B 42 0C         mov         eax,dword ptr [edx+0Ch] 
00403B3C 8B 4D FC         mov         ecx,dword ptr [i] 
00403B3F 83 3C 88 00      cmp         dword ptr [eax+ecx*4],0 
00403B43 75 02            jne         dictionary_set+187h (403B47h) 
            /* Add key here */
            break ;
00403B45 EB 02            jmp         dictionary_set+189h (403B49h) 
        }
    }
00403B47 EB D9            jmp         dictionary_set+162h (403B22h) 
    /* Copy key */
    d->key[i]  = strdup(key);
00403B49 8B 55 0C         mov         edx,dword ptr [key] 
00403B4C 52               push        edx  
00403B4D E8 9D D6 FF FF   call        @ILT+490(_strdup) (4011EFh) 
00403B52 83 C4 04         add         esp,4 
00403B55 8B 4D 08         mov         ecx,dword ptr [d] 
00403B58 8B 51 0C         mov         edx,dword ptr [ecx+0Ch] 
00403B5B 8B 4D FC         mov         ecx,dword ptr [i] 
00403B5E 89 04 8A         mov         dword ptr [edx+ecx*4],eax 
    d->val[i]  = val ? strdup(val) : NULL ;
00403B61 83 7D 10 00      cmp         dword ptr [val],0 
00403B65 74 11            je          dictionary_set+1B8h (403B78h) 
00403B67 8B 55 10         mov         edx,dword ptr [val] 
00403B6A 52               push        edx  
00403B6B E8 7F D6 FF FF   call        @ILT+490(_strdup) (4011EFh) 
00403B70 83 C4 04         add         esp,4 
00403B73 89 45 F0         mov         dword ptr [ebp-10h],eax 
00403B76 EB 07            jmp         dictionary_set+1BFh (403B7Fh) 
00403B78 C7 45 F0 00 00 00 00 mov         dword ptr [ebp-10h],0 
00403B7F 8B 45 08         mov         eax,dword ptr [d] 
00403B82 8B 48 08         mov         ecx,dword ptr [eax+8] 
00403B85 8B 55 FC         mov         edx,dword ptr [i] 
00403B88 8B 45 F0         mov         eax,dword ptr [ebp-10h] 
00403B8B 89 04 91         mov         dword ptr [ecx+edx*4],eax 
    d->hash[i] = hash;
00403B8E 8B 4D 08         mov         ecx,dword ptr [d] 
00403B91 8B 51 10         mov         edx,dword ptr [ecx+10h] 
00403B94 8B 45 FC         mov         eax,dword ptr [i] 
00403B97 8B 4D F8         mov         ecx,dword ptr [hash] 
00403B9A 89 0C 82         mov         dword ptr [edx+eax*4],ecx 
    d->n ++ ;
00403B9D 8B 55 08         mov         edx,dword ptr [d] 
00403BA0 8B 02            mov         eax,dword ptr [edx] 
00403BA2 83 C0 01         add         eax,1 
00403BA5 8B 4D 08         mov         ecx,dword ptr [d] 
00403BA8 89 01            mov         dword ptr [ecx],eax 
    return ;
}
00403BAA 8B E5            mov         esp,ebp 
00403BAC 5D               pop         ebp  
00403BAD C2 0C 00         ret         0Ch  

/*
Taken from http://ndevilla.free.fr/iniparser/

iniparser is a free stand-alone ini file parsing library.
It is written in portable ANSI C and should compile anywhere.
iniparser is distributed under an MIT license.

*/

#include "Dictionary.h"

#define strlen lstrlen

#define MAXVALSZ    1024
#define DICTMINSZ   128
#define DICT_INVALID_KEY    ((char*)-1)

static void * mem_double(void * ptr, int size)
{
00403BB0 55               push        ebp  
00403BB1 8B EC            mov         ebp,esp 
00403BB3 51               push        ecx  
    void    *   newptr ;
 
    newptr = calloc(2*size, 1);
00403BB4 6A 01            push        1    
00403BB6 8B 45 0C         mov         eax,dword ptr [size] 
00403BB9 D1 E0            shl         eax,1 
00403BBB 50               push        eax  
00403BBC E8 5F 9F 00 00   call        calloc (40DB20h) 
00403BC1 83 C4 08         add         esp,8 
00403BC4 89 45 FC         mov         dword ptr [newptr],eax 
    memcpy(newptr, ptr, size);
00403BC7 8B 4D 0C         mov         ecx,dword ptr [size] 
00403BCA 51               push        ecx  
00403BCB 8B 55 08         mov         edx,dword ptr [ptr] 
00403BCE 52               push        edx  
00403BCF 8B 45 FC         mov         eax,dword ptr [newptr] 
00403BD2 50               push        eax  
00403BD3 E8 42 9F 00 00   call        memcpy (40DB1Ah) 
00403BD8 83 C4 0C         add         esp,0Ch 
    free(ptr);
00403BDB 8B 4D 08         mov         ecx,dword ptr [ptr] 
00403BDE 51               push        ecx  
00403BDF E8 24 9F 00 00   call        free (40DB08h) 
00403BE4 83 C4 04         add         esp,4 
    return newptr ;
00403BE7 8B 45 FC         mov         eax,dword ptr [newptr] 
}
00403BEA 8B E5            mov         esp,ebp 
00403BEC 5D               pop         ebp  
00403BED C2 08 00         ret         8    

void dictionary_unset(dictionary * d, char * key)
{
00403BF0 55               push        ebp  
00403BF1 8B EC            mov         ebp,esp 
00403BF3 83 EC 08         sub         esp,8 
    unsigned    hash ;
    int         i ;

    hash = dictionary_hash(key);
00403BF6 8B 45 0C         mov         eax,dword ptr [key] 
00403BF9 50               push        eax  
00403BFA E8 A3 D7 FF FF   call        dictionary_hash (4013A2h) 
00403BFF 89 45 F8         mov         dword ptr [hash],eax 
    for (i=0 ; i<d->size ; i++) {
00403C02 C7 45 FC 00 00 00 00 mov         dword ptr [i],0 
00403C09 EB 09            jmp         dictionary_unset+24h (403C14h) 
00403C0B 8B 4D FC         mov         ecx,dword ptr [i] 
00403C0E 83 C1 01         add         ecx,1 
00403C11 89 4D FC         mov         dword ptr [i],ecx 
00403C14 8B 55 08         mov         edx,dword ptr [d] 
00403C17 8B 45 FC         mov         eax,dword ptr [i] 
00403C1A 3B 42 04         cmp         eax,dword ptr [edx+4] 
00403C1D 7D 43            jge         dictionary_unset+72h (403C62h) 
        if (d->key[i]==NULL)
00403C1F 8B 4D 08         mov         ecx,dword ptr [d] 
00403C22 8B 51 0C         mov         edx,dword ptr [ecx+0Ch] 
00403C25 8B 45 FC         mov         eax,dword ptr [i] 
00403C28 83 3C 82 00      cmp         dword ptr [edx+eax*4],0 
00403C2C 75 02            jne         dictionary_unset+40h (403C30h) 
            continue ;
00403C2E EB DB            jmp         dictionary_unset+1Bh (403C0Bh) 
        /* Compare hash */
        if (hash==d->hash[i]) {
00403C30 8B 4D 08         mov         ecx,dword ptr [d] 
00403C33 8B 51 10         mov         edx,dword ptr [ecx+10h] 
00403C36 8B 45 FC         mov         eax,dword ptr [i] 
00403C39 8B 4D F8         mov         ecx,dword ptr [hash] 
00403C3C 3B 0C 82         cmp         ecx,dword ptr [edx+eax*4] 
00403C3F 75 1F            jne         dictionary_unset+70h (403C60h) 
            /* Compare string, to avoid hash collisions */
            if (!strcmp(key, d->key[i])) {
00403C41 8B 55 08         mov         edx,dword ptr [d] 
00403C44 8B 42 0C         mov         eax,dword ptr [edx+0Ch] 
00403C47 8B 4D FC         mov         ecx,dword ptr [i] 
00403C4A 8B 14 88         mov         edx,dword ptr [eax+ecx*4] 
00403C4D 52               push        edx  
00403C4E 8B 45 0C         mov         eax,dword ptr [key] 
00403C51 50               push        eax  
00403C52 E8 9F 9E 00 00   call        strcmp (40DAF6h) 
00403C57 83 C4 08         add         esp,8 
00403C5A 85 C0            test        eax,eax 
00403C5C 75 02            jne         dictionary_unset+70h (403C60h) 
                /* Found key */
                break ;
00403C5E EB 02            jmp         dictionary_unset+72h (403C62h) 
            }
        }
    }
00403C60 EB A9            jmp         dictionary_unset+1Bh (403C0Bh) 
    if (i>=d->size)
00403C62 8B 4D 08         mov         ecx,dword ptr [d] 
00403C65 8B 55 FC         mov         edx,dword ptr [i] 
00403C68 3B 51 04         cmp         edx,dword ptr [ecx+4] 
00403C6B 7C 02            jl          dictionary_unset+7Fh (403C6Fh) 
        /* Key not found */
        return ;
00403C6D EB 76            jmp         dictionary_unset+0F5h (403CE5h) 

    free(d->key[i]);
00403C6F 8B 45 08         mov         eax,dword ptr [d] 
00403C72 8B 48 0C         mov         ecx,dword ptr [eax+0Ch] 
00403C75 8B 55 FC         mov         edx,dword ptr [i] 
00403C78 8B 04 91         mov         eax,dword ptr [ecx+edx*4] 
00403C7B 50               push        eax  
00403C7C E8 87 9E 00 00   call        free (40DB08h) 
00403C81 83 C4 04         add         esp,4 
    d->key[i] = NULL ;
00403C84 8B 4D 08         mov         ecx,dword ptr [d] 
00403C87 8B 51 0C         mov         edx,dword ptr [ecx+0Ch] 
00403C8A 8B 45 FC         mov         eax,dword ptr [i] 
00403C8D C7 04 82 00 00 00 00 mov         dword ptr [edx+eax*4],0 
    if (d->val[i]!=NULL) {
00403C94 8B 4D 08         mov         ecx,dword ptr [d] 
00403C97 8B 51 08         mov         edx,dword ptr [ecx+8] 
00403C9A 8B 45 FC         mov         eax,dword ptr [i] 
00403C9D 83 3C 82 00      cmp         dword ptr [edx+eax*4],0 
00403CA1 74 25            je          dictionary_unset+0D8h (403CC8h) 
        free(d->val[i]);
00403CA3 8B 4D 08         mov         ecx,dword ptr [d] 
00403CA6 8B 51 08         mov         edx,dword ptr [ecx+8] 
00403CA9 8B 45 FC         mov         eax,dword ptr [i] 
00403CAC 8B 0C 82         mov         ecx,dword ptr [edx+eax*4] 
00403CAF 51               push        ecx  
00403CB0 E8 53 9E 00 00   call        free (40DB08h) 
00403CB5 83 C4 04         add         esp,4 
        d->val[i] = NULL ;
00403CB8 8B 55 08         mov         edx,dword ptr [d] 
00403CBB 8B 42 08         mov         eax,dword ptr [edx+8] 
00403CBE 8B 4D FC         mov         ecx,dword ptr [i] 
00403CC1 C7 04 88 00 00 00 00 mov         dword ptr [eax+ecx*4],0 
    }
    d->hash[i] = 0 ;
00403CC8 8B 55 08         mov         edx,dword ptr [d] 
00403CCB 8B 42 10         mov         eax,dword ptr [edx+10h] 
00403CCE 8B 4D FC         mov         ecx,dword ptr [i] 
00403CD1 C7 04 88 00 00 00 00 mov         dword ptr [eax+ecx*4],0 
    d->n -- ;
00403CD8 8B 55 08         mov         edx,dword ptr [d] 
00403CDB 8B 02            mov         eax,dword ptr [edx] 
00403CDD 83 E8 01         sub         eax,1 
00403CE0 8B 4D 08         mov         ecx,dword ptr [d] 
00403CE3 89 01            mov         dword ptr [ecx],eax 
    return ;
}
00403CE5 8B E5            mov         esp,ebp 
00403CE7 5D               pop         ebp  
00403CE8 C2 08 00         ret         8    
--- No source file -------------------------------------------------------------
00403CEB CC               int         3    
00403CEC CC               int         3    
00403CED CC               int         3    
00403CEE CC               int         3    
00403CEF CC               int         3    
--- f:\eclipse\workspace\winrun4j\src\common\dictionary.cpp --------------------

void dictionary_setint(dictionary * d, char * key, int val)
{
00403CF0 55               push        ebp  
00403CF1 8B EC            mov         ebp,esp 
00403CF3 81 EC 00 04 00 00 sub         esp,400h 
    char sval[MAXVALSZ];
    sprintf(sval, "%d", val);
00403CF9 8B 45 10         mov         eax,dword ptr [val] 
00403CFC 50               push        eax  
00403CFD 68 14 0F 41 00   push        410F14h 
00403D02 8D 8D 00 FC FF FF lea         ecx,[sval] 
00403D08 51               push        ecx  
00403D09 E8 06 9E 00 00   call        sprintf (40DB14h) 
00403D0E 83 C4 0C         add         esp,0Ch 
    dictionary_set(d, key, sval);
00403D11 8D 95 00 FC FF FF lea         edx,[sval] 
00403D17 52               push        edx  
00403D18 8B 45 0C         mov         eax,dword ptr [key] 
00403D1B 50               push        eax  
00403D1C 8B 4D 08         mov         ecx,dword ptr [d] 
00403D1F 51               push        ecx  
00403D20 E8 7B D3 FF FF   call        dictionary_set (4010A0h) 
}
00403D25 8B E5            mov         esp,ebp 
00403D27 5D               pop         ebp  
00403D28 C2 0C 00         ret         0Ch  
--- No source file -------------------------------------------------------------
00403D2B CC               int         3    
00403D2C CC               int         3    
00403D2D CC               int         3    
00403D2E CC               int         3    
00403D2F CC               int         3    
--- f:\eclipse\workspace\winrun4j\src\common\dictionary.cpp --------------------

void dictionary_setdouble(dictionary * d, char * key, double val)
{
00403D30 55               push        ebp  
00403D31 8B EC            mov         ebp,esp 
00403D33 81 EC 00 04 00 00 sub         esp,400h 
    char    sval[MAXVALSZ];
    sprintf(sval, "%g", val);
00403D39 83 EC 08         sub         esp,8 
00403D3C DD 45 10         fld         qword ptr [val] 
00403D3F DD 1C 24         fstp        qword ptr [esp] 
00403D42 68 18 0F 41 00   push        410F18h 
00403D47 8D 85 00 FC FF FF lea         eax,[sval] 
00403D4D 50               push        eax  
00403D4E E8 C1 9D 00 00   call        sprintf (40DB14h) 
00403D53 83 C4 10         add         esp,10h 
    dictionary_set(d, key, sval);
00403D56 8D 8D 00 FC FF FF lea         ecx,[sval] 
00403D5C 51               push        ecx  
00403D5D 8B 55 0C         mov         edx,dword ptr [key] 
00403D60 52               push        edx  
00403D61 8B 45 08         mov         eax,dword ptr [d] 
00403D64 50               push        eax  
00403D65 E8 36 D3 FF FF   call        dictionary_set (4010A0h) 
}
00403D6A 8B E5            mov         esp,ebp 
00403D6C 5D               pop         ebp  
00403D6D C2 10 00         ret         10h  

void dictionary_dump(dictionary * d, FILE * out)
{
00403D70 55               push        ebp  
00403D71 8B EC            mov         ebp,esp 
00403D73 83 EC 08         sub         esp,8 
    int     i ;

    if (d==NULL || out==NULL) return ;
00403D76 83 7D 08 00      cmp         dword ptr [d],0 
00403D7A 74 06            je          dictionary_dump+12h (403D82h) 
00403D7C 83 7D 0C 00      cmp         dword ptr [out],0 
00403D80 75 05            jne         dictionary_dump+17h (403D87h) 
00403D82 E9 92 00 00 00   jmp         dictionary_dump+0A9h (403E19h) 
    if (d->n<1) {
00403D87 8B 45 08         mov         eax,dword ptr [d] 
00403D8A 83 38 01         cmp         dword ptr [eax],1 
00403D8D 7D 13            jge         dictionary_dump+32h (403DA2h) 
        fprintf(out, "empty dictionary\n");
00403D8F 68 1C 0F 41 00   push        410F1Ch 
00403D94 8B 4D 0C         mov         ecx,dword ptr [out] 
00403D97 51               push        ecx  
00403D98 E8 A3 9D 00 00   call        fprintf (40DB40h) 
00403D9D 83 C4 08         add         esp,8 
        return ;
00403DA0 EB 77            jmp         dictionary_dump+0A9h (403E19h) 
    }
    for (i=0 ; i<d->size ; i++) {
00403DA2 C7 45 FC 00 00 00 00 mov         dword ptr [i],0 
00403DA9 EB 09            jmp         dictionary_dump+44h (403DB4h) 
00403DAB 8B 55 FC         mov         edx,dword ptr [i] 
00403DAE 83 C2 01         add         edx,1 
00403DB1 89 55 FC         mov         dword ptr [i],edx 
00403DB4 8B 45 08         mov         eax,dword ptr [d] 
00403DB7 8B 4D FC         mov         ecx,dword ptr [i] 
00403DBA 3B 48 04         cmp         ecx,dword ptr [eax+4] 
00403DBD 7D 5A            jge         dictionary_dump+0A9h (403E19h) 
        if (d->key[i]) {
00403DBF 8B 55 08         mov         edx,dword ptr [d] 
00403DC2 8B 42 0C         mov         eax,dword ptr [edx+0Ch] 
00403DC5 8B 4D FC         mov         ecx,dword ptr [i] 
00403DC8 83 3C 88 00      cmp         dword ptr [eax+ecx*4],0 
00403DCC 74 49            je          dictionary_dump+0A7h (403E17h) 
            fprintf(out, "%20s\t[%s]\n",
                    d->key[i],
                    d->val[i] ? d->val[i] : "UNDEF");
00403DCE 8B 55 08         mov         edx,dword ptr [d] 
00403DD1 8B 42 08         mov         eax,dword ptr [edx+8] 
00403DD4 8B 4D FC         mov         ecx,dword ptr [i] 
00403DD7 83 3C 88 00      cmp         dword ptr [eax+ecx*4],0 
00403DDB 74 11            je          dictionary_dump+7Eh (403DEEh) 
00403DDD 8B 55 08         mov         edx,dword ptr [d] 
00403DE0 8B 42 08         mov         eax,dword ptr [edx+8] 
00403DE3 8B 4D FC         mov         ecx,dword ptr [i] 
00403DE6 8B 14 88         mov         edx,dword ptr [eax+ecx*4] 
00403DE9 89 55 F8         mov         dword ptr [ebp-8],edx 
00403DEC EB 07            jmp         dictionary_dump+85h (403DF5h) 
00403DEE C7 45 F8 30 0F 41 00 mov         dword ptr [ebp-8],410F30h 
00403DF5 8B 45 F8         mov         eax,dword ptr [ebp-8] 
00403DF8 50               push        eax  
00403DF9 8B 4D 08         mov         ecx,dword ptr [d] 
00403DFC 8B 51 0C         mov         edx,dword ptr [ecx+0Ch] 
00403DFF 8B 45 FC         mov         eax,dword ptr [i] 
00403E02 8B 0C 82         mov         ecx,dword ptr [edx+eax*4] 
00403E05 51               push        ecx  
00403E06 68 38 0F 41 00   push        410F38h 
00403E0B 8B 55 0C         mov         edx,dword ptr [out] 
00403E0E 52               push        edx  
00403E0F E8 2C 9D 00 00   call        fprintf (40DB40h) 
00403E14 83 C4 10         add         esp,10h 
        }
    }
00403E17 EB 92            jmp         dictionary_dump+3Bh (403DABh) 
    return ;
}
00403E19 8B E5            mov         esp,ebp 
00403E1B 5D               pop         ebp  
00403E1C C2 08 00         ret         8    
--- No source file -------------------------------------------------------------
00403E1F CC               int         3    
--- f:\eclipse\workspace\winrun4j\src\common\dictionary.cpp --------------------
    char longkey[2*ASCIILINESZ+1];

    /* Make a key as section:keyword */
    if (key!=NULL) {
        sprintf(longkey, "%s:%s", sec, key);
    } else {
        strcpy(longkey, sec);
    }

    /* Add (key,val) to dictionary */
    dictionary_set(d, longkey, val);
    return ;
}


int iniparser_getnsec(dictionary * d)
{
00403E20 55               push        ebp  
00403E21 8B EC            mov         ebp,esp 
00403E23 83 EC 08         sub         esp,8 
    int i ;
    int nsec ;

    if (d==NULL) return -1 ;
00403E26 83 7D 08 00      cmp         dword ptr [d],0 
00403E2A 75 05            jne         iniparser_getnsec+11h (403E31h) 
00403E2C 83 C8 FF         or          eax,0FFFFFFFFh 
00403E2F EB 5E            jmp         iniparser_getnsec+6Fh (403E8Fh) 
    nsec=0 ;
00403E31 C7 45 F8 00 00 00 00 mov         dword ptr [nsec],0 
    for (i=0 ; i<d->size ; i++) {
00403E38 C7 45 FC 00 00 00 00 mov         dword ptr [i],0 
00403E3F EB 09            jmp         iniparser_getnsec+2Ah (403E4Ah) 
00403E41 8B 45 FC         mov         eax,dword ptr [i] 
00403E44 83 C0 01         add         eax,1 
00403E47 89 45 FC         mov         dword ptr [i],eax 
00403E4A 8B 4D 08         mov         ecx,dword ptr [d] 
00403E4D 8B 55 FC         mov         edx,dword ptr [i] 
00403E50 3B 51 04         cmp         edx,dword ptr [ecx+4] 
00403E53 7D 37            jge         iniparser_getnsec+6Ch (403E8Ch) 
        if (d->key[i]==NULL)
00403E55 8B 45 08         mov         eax,dword ptr [d] 
00403E58 8B 48 0C         mov         ecx,dword ptr [eax+0Ch] 
00403E5B 8B 55 FC         mov         edx,dword ptr [i] 
00403E5E 83 3C 91 00      cmp         dword ptr [ecx+edx*4],0 
00403E62 75 02            jne         iniparser_getnsec+46h (403E66h) 
            continue ;
00403E64 EB DB            jmp         iniparser_getnsec+21h (403E41h) 
        if (strchr(d->key[i], ':')==NULL) {
00403E66 6A 3A            push        3Ah  
00403E68 8B 45 08         mov         eax,dword ptr [d] 
00403E6B 8B 48 0C         mov         ecx,dword ptr [eax+0Ch] 
00403E6E 8B 55 FC         mov         edx,dword ptr [i] 
00403E71 8B 04 91         mov         eax,dword ptr [ecx+edx*4] 
00403E74 50               push        eax  
00403E75 E8 3A D2 FF FF   call        strchr (4010B4h) 
00403E7A 83 C4 08         add         esp,8 
00403E7D 85 C0            test        eax,eax 
00403E7F 75 09            jne         iniparser_getnsec+6Ah (403E8Ah) 
            nsec ++ ;
00403E81 8B 4D F8         mov         ecx,dword ptr [nsec] 
00403E84 83 C1 01         add         ecx,1 
00403E87 89 4D F8         mov         dword ptr [nsec],ecx 
        }
    }
00403E8A EB B5            jmp         iniparser_getnsec+21h (403E41h) 
    return nsec ;
00403E8C 8B 45 F8         mov         eax,dword ptr [nsec] 
}
00403E8F 8B E5            mov         esp,ebp 
00403E91 5D               pop         ebp  
00403E92 C2 04 00         ret         4    
--- No source file -------------------------------------------------------------
00403E95 CC               int         3    
00403E96 CC               int         3    
00403E97 CC               int         3    
00403E98 CC               int         3    
00403E99 CC               int         3    
00403E9A CC               int         3    
00403E9B CC               int         3    
00403E9C CC               int         3    
00403E9D CC               int         3    
00403E9E CC               int         3    
00403E9F CC               int         3    
--- f:\eclipse\workspace\winrun4j\src\common\dictionary.cpp --------------------


char * iniparser_getsecname(dictionary * d, int n)
{
00403EA0 55               push        ebp  
00403EA1 8B EC            mov         ebp,esp 
00403EA3 83 EC 08         sub         esp,8 
    int i ;
    int foundsec ;

    if (d==NULL || n<0) return NULL ;
00403EA6 83 7D 08 00      cmp         dword ptr [d],0 
00403EAA 74 06            je          iniparser_getsecname+12h (403EB2h) 
00403EAC 83 7D 0C 00      cmp         dword ptr [n],0 
00403EB0 7D 04            jge         iniparser_getsecname+16h (403EB6h) 
00403EB2 33 C0            xor         eax,eax 
00403EB4 EB 7D            jmp         iniparser_getsecname+93h (403F33h) 
    foundsec=0 ;
00403EB6 C7 45 F8 00 00 00 00 mov         dword ptr [foundsec],0 
    for (i=0 ; i<d->size ; i++) {
00403EBD C7 45 FC 00 00 00 00 mov         dword ptr [i],0 
00403EC4 EB 09            jmp         iniparser_getsecname+2Fh (403ECFh) 
00403EC6 8B 45 FC         mov         eax,dword ptr [i] 
00403EC9 83 C0 01         add         eax,1 
00403ECC 89 45 FC         mov         dword ptr [i],eax 
00403ECF 8B 4D 08         mov         ecx,dword ptr [d] 
00403ED2 8B 55 FC         mov         edx,dword ptr [i] 
00403ED5 3B 51 04         cmp         edx,dword ptr [ecx+4] 
00403ED8 7D 41            jge         iniparser_getsecname+7Bh (403F1Bh) 
        if (d->key[i]==NULL)
00403EDA 8B 45 08         mov         eax,dword ptr [d] 
00403EDD 8B 48 0C         mov         ecx,dword ptr [eax+0Ch] 
00403EE0 8B 55 FC         mov         edx,dword ptr [i] 
00403EE3 83 3C 91 00      cmp         dword ptr [ecx+edx*4],0 
00403EE7 75 02            jne         iniparser_getsecname+4Bh (403EEBh) 
            continue ;
00403EE9 EB DB            jmp         iniparser_getsecname+26h (403EC6h) 
        if (strchr(d->key[i], ':')==NULL) {
00403EEB 6A 3A            push        3Ah  
00403EED 8B 45 08         mov         eax,dword ptr [d] 
00403EF0 8B 48 0C         mov         ecx,dword ptr [eax+0Ch] 
00403EF3 8B 55 FC         mov         edx,dword ptr [i] 
00403EF6 8B 04 91         mov         eax,dword ptr [ecx+edx*4] 
00403EF9 50               push        eax  
00403EFA E8 B5 D1 FF FF   call        strchr (4010B4h) 
00403EFF 83 C4 08         add         esp,8 
00403F02 85 C0            test        eax,eax 
00403F04 75 13            jne         iniparser_getsecname+79h (403F19h) 
            foundsec++ ;
00403F06 8B 4D F8         mov         ecx,dword ptr [foundsec] 
00403F09 83 C1 01         add         ecx,1 
00403F0C 89 4D F8         mov         dword ptr [foundsec],ecx 
            if (foundsec>n)
00403F0F 8B 55 F8         mov         edx,dword ptr [foundsec] 
00403F12 3B 55 0C         cmp         edx,dword ptr [n] 
00403F15 7E 02            jle         iniparser_getsecname+79h (403F19h) 
                break ;
00403F17 EB 02            jmp         iniparser_getsecname+7Bh (403F1Bh) 
        }
    }
00403F19 EB AB            jmp         iniparser_getsecname+26h (403EC6h) 
    if (foundsec<=n) {
00403F1B 8B 45 F8         mov         eax,dword ptr [foundsec] 
00403F1E 3B 45 0C         cmp         eax,dword ptr [n] 
00403F21 7F 04            jg          iniparser_getsecname+87h (403F27h) 
        return NULL ;
00403F23 33 C0            xor         eax,eax 
00403F25 EB 0C            jmp         iniparser_getsecname+93h (403F33h) 
    }
    return d->key[i] ;
00403F27 8B 4D 08         mov         ecx,dword ptr [d] 
00403F2A 8B 51 0C         mov         edx,dword ptr [ecx+0Ch] 
00403F2D 8B 45 FC         mov         eax,dword ptr [i] 
00403F30 8B 04 82         mov         eax,dword ptr [edx+eax*4] 
}
00403F33 8B E5            mov         esp,ebp 
00403F35 5D               pop         ebp  
00403F36 C2 08 00         ret         8    
--- No source file -------------------------------------------------------------
00403F39 CC               int         3    
00403F3A CC               int         3    
00403F3B CC               int         3    
00403F3C CC               int         3    
00403F3D CC               int         3    
00403F3E CC               int         3    
00403F3F CC               int         3    
--- f:\eclipse\workspace\winrun4j\src\common\dictionary.cpp --------------------

void iniparser_dump(dictionary * d, FILE * f)
{
00403F40 55               push        ebp  
00403F41 8B EC            mov         ebp,esp 
00403F43 51               push        ecx  
    int     i ;

    if (d==NULL || f==NULL) return ;
00403F44 83 7D 08 00      cmp         dword ptr [d],0 
00403F48 74 06            je          iniparser_dump+10h (403F50h) 
00403F4A 83 7D 0C 00      cmp         dword ptr [f],0 
00403F4E 75 05            jne         iniparser_dump+15h (403F55h) 
00403F50 E9 8D 00 00 00   jmp         iniparser_dump+0A2h (403FE2h) 
    for (i=0 ; i<d->size ; i++) {
00403F55 C7 45 FC 00 00 00 00 mov         dword ptr [i],0 
00403F5C EB 09            jmp         iniparser_dump+27h (403F67h) 
00403F5E 8B 45 FC         mov         eax,dword ptr [i] 
00403F61 83 C0 01         add         eax,1 
00403F64 89 45 FC         mov         dword ptr [i],eax 
00403F67 8B 4D 08         mov         ecx,dword ptr [d] 
00403F6A 8B 55 FC         mov         edx,dword ptr [i] 
00403F6D 3B 51 04         cmp         edx,dword ptr [ecx+4] 
00403F70 7D 70            jge         iniparser_dump+0A2h (403FE2h) 
        if (d->key[i]==NULL)
00403F72 8B 45 08         mov         eax,dword ptr [d] 
00403F75 8B 48 0C         mov         ecx,dword ptr [eax+0Ch] 
00403F78 8B 55 FC         mov         edx,dword ptr [i] 
00403F7B 83 3C 91 00      cmp         dword ptr [ecx+edx*4],0 
00403F7F 75 02            jne         iniparser_dump+43h (403F83h) 
            continue ;
00403F81 EB DB            jmp         iniparser_dump+1Eh (403F5Eh) 
        if (d->val[i]!=NULL) {
00403F83 8B 45 08         mov         eax,dword ptr [d] 
00403F86 8B 48 08         mov         ecx,dword ptr [eax+8] 
00403F89 8B 55 FC         mov         edx,dword ptr [i] 
00403F8C 83 3C 91 00      cmp         dword ptr [ecx+edx*4],0 
00403F90 74 2D            je          iniparser_dump+7Fh (403FBFh) 
            fprintf(f, "[%s]=[%s]\n", d->key[i], d->val[i]);
00403F92 8B 45 08         mov         eax,dword ptr [d] 
00403F95 8B 48 08         mov         ecx,dword ptr [eax+8] 
00403F98 8B 55 FC         mov         edx,dword ptr [i] 
00403F9B 8B 04 91         mov         eax,dword ptr [ecx+edx*4] 
00403F9E 50               push        eax  
00403F9F 8B 4D 08         mov         ecx,dword ptr [d] 
00403FA2 8B 51 0C         mov         edx,dword ptr [ecx+0Ch] 
00403FA5 8B 45 FC         mov         eax,dword ptr [i] 
00403FA8 8B 0C 82         mov         ecx,dword ptr [edx+eax*4] 
00403FAB 51               push        ecx  
00403FAC 68 4C 0F 41 00   push        410F4Ch 
00403FB1 8B 55 0C         mov         edx,dword ptr [f] 
00403FB4 52               push        edx  
00403FB5 E8 86 9B 00 00   call        fprintf (40DB40h) 
00403FBA 83 C4 10         add         esp,10h 
        } else {
00403FBD EB 1E            jmp         iniparser_dump+9Dh (403FDDh) 
            fprintf(f, "[%s]=UNDEF\n", d->key[i]);
00403FBF 8B 45 08         mov         eax,dword ptr [d] 
00403FC2 8B 48 0C         mov         ecx,dword ptr [eax+0Ch] 
00403FC5 8B 55 FC         mov         edx,dword ptr [i] 
00403FC8 8B 04 91         mov         eax,dword ptr [ecx+edx*4] 
00403FCB 50               push        eax  
00403FCC 68 58 0F 41 00   push        410F58h 
00403FD1 8B 4D 0C         mov         ecx,dword ptr [f] 
00403FD4 51               push        ecx  
00403FD5 E8 66 9B 00 00   call        fprintf (40DB40h) 
00403FDA 83 C4 0C         add         esp,0Ch 
        }
    }
00403FDD E9 7C FF FF FF   jmp         iniparser_dump+1Eh (403F5Eh) 
    return ;
}
00403FE2 8B E5            mov         esp,ebp 
00403FE4 5D               pop         ebp  
00403FE5 C2 08 00         ret         8    
--- No source file -------------------------------------------------------------
00403FE8 CC               int         3    
00403FE9 CC               int         3    
00403FEA CC               int         3    
00403FEB CC               int         3    
00403FEC CC               int         3    
00403FED CC               int         3    
00403FEE CC               int         3    
00403FEF CC               int         3    
--- f:\eclipse\workspace\winrun4j\src\common\dictionary.cpp --------------------


void iniparser_dump_ini(dictionary * d, FILE * f)
{
00403FF0 55               push        ebp  
00403FF1 8B EC            mov         ebp,esp 
00403FF3 81 EC 20 04 00 00 sub         esp,420h 
    int     i, j ;
    char    keym[ASCIILINESZ+1];
    int     nsec ;
    char *  secname ;
    int     seclen ;

    if (d==NULL || f==NULL) return ;
00403FF9 83 7D 08 00      cmp         dword ptr [d],0 
00403FFD 74 06            je          iniparser_dump_ini+15h (404005h) 
00403FFF 83 7D 0C 00      cmp         dword ptr [f],0 
00404003 75 05            jne         iniparser_dump_ini+1Ah (40400Ah) 
00404005 E9 D7 01 00 00   jmp         iniparser_dump_ini+1F1h (4041E1h) 

    nsec = iniparser_getnsec(d);
0040400A 8B 45 08         mov         eax,dword ptr [d] 
0040400D 50               push        eax  
0040400E E8 B3 D2 FF FF   call        iniparser_getnsec (4012C6h) 
00404013 89 85 EC FB FF FF mov         dword ptr [nsec],eax 
    if (nsec<1) {
00404019 83 BD EC FB FF FF 01 cmp         dword ptr [nsec],1 
00404020 7D 60            jge         iniparser_dump_ini+92h (404082h) 
        /* No section in file: dump all keys as they are */
        for (i=0 ; i<d->size ; i++) {
00404022 C7 45 F8 00 00 00 00 mov         dword ptr [i],0 
00404029 EB 09            jmp         iniparser_dump_ini+44h (404034h) 
0040402B 8B 4D F8         mov         ecx,dword ptr [i] 
0040402E 83 C1 01         add         ecx,1 
00404031 89 4D F8         mov         dword ptr [i],ecx 
00404034 8B 55 08         mov         edx,dword ptr [d] 
00404037 8B 45 F8         mov         eax,dword ptr [i] 
0040403A 3B 42 04         cmp         eax,dword ptr [edx+4] 
0040403D 7D 3E            jge         iniparser_dump_ini+8Dh (40407Dh) 
            if (d->key[i]==NULL)
0040403F 8B 4D 08         mov         ecx,dword ptr [d] 
00404042 8B 51 0C         mov         edx,dword ptr [ecx+0Ch] 
00404045 8B 45 F8         mov         eax,dword ptr [i] 
00404048 83 3C 82 00      cmp         dword ptr [edx+eax*4],0 
0040404C 75 02            jne         iniparser_dump_ini+60h (404050h) 
                continue ;
0040404E EB DB            jmp         iniparser_dump_ini+3Bh (40402Bh) 
            fprintf(f, "%s = %s\n", d->key[i], d->val[i]);
00404050 8B 4D 08         mov         ecx,dword ptr [d] 
00404053 8B 51 08         mov         edx,dword ptr [ecx+8] 
00404056 8B 45 F8         mov         eax,dword ptr [i] 
00404059 8B 0C 82         mov         ecx,dword ptr [edx+eax*4] 
0040405C 51               push        ecx  
0040405D 8B 55 08         mov         edx,dword ptr [d] 
00404060 8B 42 0C         mov         eax,dword ptr [edx+0Ch] 
00404063 8B 4D F8         mov         ecx,dword ptr [i] 
00404066 8B 14 88         mov         edx,dword ptr [eax+ecx*4] 
00404069 52               push        edx  
0040406A 68 64 0F 41 00   push        410F64h 
0040406F 8B 45 0C         mov         eax,dword ptr [f] 
00404072 50               push        eax  
00404073 E8 C8 9A 00 00   call        fprintf (40DB40h) 
00404078 83 C4 10         add         esp,10h 
        }
0040407B EB AE            jmp         iniparser_dump_ini+3Bh (40402Bh) 
        return ;
0040407D E9 5F 01 00 00   jmp         iniparser_dump_ini+1F1h (4041E1h) 
    }
    for (i=0 ; i<nsec ; i++) {
00404082 C7 45 F8 00 00 00 00 mov         dword ptr [i],0 
00404089 EB 09            jmp         iniparser_dump_ini+0A4h (404094h) 
0040408B 8B 4D F8         mov         ecx,dword ptr [i] 
0040408E 83 C1 01         add         ecx,1 
00404091 89 4D F8         mov         dword ptr [i],ecx 
00404094 8B 55 F8         mov         edx,dword ptr [i] 
00404097 3B 95 EC FB FF FF cmp         edx,dword ptr [nsec] 
0040409D 0F 8D 2D 01 00 00 jge         iniparser_dump_ini+1E0h (4041D0h) 
        secname = iniparser_getsecname(d, i) ;
004040A3 8B 45 F8         mov         eax,dword ptr [i] 
004040A6 50               push        eax  
004040A7 8B 4D 08         mov         ecx,dword ptr [d] 
004040AA 51               push        ecx  
004040AB E8 85 D1 FF FF   call        iniparser_getsecname (401235h) 
004040B0 89 85 E8 FB FF FF mov         dword ptr [secname],eax 
        seclen  = (int)strlen(secname);
004040B6 8B 95 E8 FB FF FF mov         edx,dword ptr [secname] 
004040BC 52               push        edx  
004040BD FF 15 68 86 41 00 call        dword ptr [__imp__lstrlenA@4 (418668h)] 
004040C3 89 45 FC         mov         dword ptr [seclen],eax 
        fprintf(f, "\n[%s]\n", secname);
004040C6 8B 85 E8 FB FF FF mov         eax,dword ptr [secname] 
004040CC 50               push        eax  
004040CD 68 70 0F 41 00   push        410F70h 
004040D2 8B 4D 0C         mov         ecx,dword ptr [f] 
004040D5 51               push        ecx  
004040D6 E8 65 9A 00 00   call        fprintf (40DB40h) 
004040DB 83 C4 0C         add         esp,0Ch 
        sprintf(keym, "%s:", secname);
004040DE 8B 95 E8 FB FF FF mov         edx,dword ptr [secname] 
004040E4 52               push        edx  
004040E5 68 78 0F 41 00   push        410F78h 
004040EA 8D 85 F0 FB FF FF lea         eax,[keym] 
004040F0 50               push        eax  
004040F1 E8 1E 9A 00 00   call        sprintf (40DB14h) 
004040F6 83 C4 0C         add         esp,0Ch 
        for (j=0 ; j<d->size ; j++) {
004040F9 C7 85 E4 FB FF FF 00 00 00 00 mov         dword ptr [j],0 
00404103 EB 0F            jmp         iniparser_dump_ini+124h (404114h) 
00404105 8B 8D E4 FB FF FF mov         ecx,dword ptr [j] 
0040410B 83 C1 01         add         ecx,1 
0040410E 89 8D E4 FB FF FF mov         dword ptr [j],ecx 
00404114 8B 55 08         mov         edx,dword ptr [d] 
00404117 8B 85 E4 FB FF FF mov         eax,dword ptr [j] 
0040411D 3B 42 04         cmp         eax,dword ptr [edx+4] 
00404120 0F 8D A5 00 00 00 jge         iniparser_dump_ini+1DBh (4041CBh) 
            if (d->key[j]==NULL)
00404126 8B 4D 08         mov         ecx,dword ptr [d] 
00404129 8B 51 0C         mov         edx,dword ptr [ecx+0Ch] 
0040412C 8B 85 E4 FB FF FF mov         eax,dword ptr [j] 
00404132 83 3C 82 00      cmp         dword ptr [edx+eax*4],0 
00404136 75 02            jne         iniparser_dump_ini+14Ah (40413Ah) 
                continue ;
00404138 EB CB            jmp         iniparser_dump_ini+115h (404105h) 
            if (!strncmp(d->key[j], keym, seclen+1)) {
0040413A 8B 4D FC         mov         ecx,dword ptr [seclen] 
0040413D 83 C1 01         add         ecx,1 
00404140 51               push        ecx  
00404141 8D 95 F0 FB FF FF lea         edx,[keym] 
00404147 52               push        edx  
00404148 8B 45 08         mov         eax,dword ptr [d] 
0040414B 8B 48 0C         mov         ecx,dword ptr [eax+0Ch] 
0040414E 8B 95 E4 FB FF FF mov         edx,dword ptr [j] 
00404154 8B 04 91         mov         eax,dword ptr [ecx+edx*4] 
00404157 50               push        eax  
00404158 E8 E9 99 00 00   call        strncmp (40DB46h) 
0040415D 83 C4 0C         add         esp,0Ch 
00404160 85 C0            test        eax,eax 
00404162 75 62            jne         iniparser_dump_ini+1D6h (4041C6h) 
                fprintf(f,
                        "%-30s = %s\n",
                        d->key[j]+seclen+1,
                        d->val[j] ? d->val[j] : "");
00404164 8B 4D 08         mov         ecx,dword ptr [d] 
00404167 8B 51 08         mov         edx,dword ptr [ecx+8] 
0040416A 8B 85 E4 FB FF FF mov         eax,dword ptr [j] 
00404170 83 3C 82 00      cmp         dword ptr [edx+eax*4],0 
00404174 74 17            je          iniparser_dump_ini+19Dh (40418Dh) 
00404176 8B 4D 08         mov         ecx,dword ptr [d] 
00404179 8B 51 08         mov         edx,dword ptr [ecx+8] 
0040417C 8B 85 E4 FB FF FF mov         eax,dword ptr [j] 
00404182 8B 0C 82         mov         ecx,dword ptr [edx+eax*4] 
00404185 89 8D E0 FB FF FF mov         dword ptr [ebp-420h],ecx 
0040418B EB 0A            jmp         iniparser_dump_ini+1A7h (404197h) 
0040418D C7 85 E0 FB FF FF 17 0F 41 00 mov         dword ptr [ebp-420h],410F17h 
00404197 8B 95 E0 FB FF FF mov         edx,dword ptr [ebp-420h] 
0040419D 52               push        edx  
0040419E 8B 45 08         mov         eax,dword ptr [d] 
004041A1 8B 48 0C         mov         ecx,dword ptr [eax+0Ch] 
004041A4 8B 95 E4 FB FF FF mov         edx,dword ptr [j] 
004041AA 8B 04 91         mov         eax,dword ptr [ecx+edx*4] 
004041AD 8B 4D FC         mov         ecx,dword ptr [seclen] 
004041B0 8D 54 08 01      lea         edx,[eax+ecx+1] 
004041B4 52               push        edx  
004041B5 68 7C 0F 41 00   push        410F7Ch 
004041BA 8B 45 0C         mov         eax,dword ptr [f] 
004041BD 50               push        eax  
004041BE E8 7D 99 00 00   call        fprintf (40DB40h) 
004041C3 83 C4 10         add         esp,10h 
            }
        }
004041C6 E9 3A FF FF FF   jmp         iniparser_dump_ini+115h (404105h) 
    }
004041CB E9 BB FE FF FF   jmp         iniparser_dump_ini+9Bh (40408Bh) 
    fprintf(f, "\n");
004041D0 68 88 0F 41 00   push        410F88h 
004041D5 8B 4D 0C         mov         ecx,dword ptr [f] 
004041D8 51               push        ecx  
004041D9 E8 62 99 00 00   call        fprintf (40DB40h) 
004041DE 83 C4 08         add         esp,8 
    return ;
}
004041E1 8B E5            mov         esp,ebp 
004041E3 5D               pop         ebp  
004041E4 C2 08 00         ret         8    
--- No source file -------------------------------------------------------------
004041E7 CC               int         3    
004041E8 CC               int         3    
004041E9 CC               int         3    
004041EA CC               int         3    
004041EB CC               int         3    
004041EC CC               int         3    
004041ED CC               int         3    
004041EE CC               int         3    
004041EF CC               int         3    
--- f:\eclipse\workspace\winrun4j\src\common\dictionary.cpp --------------------



char * iniparser_getstr(dictionary * d, const char * key)
{
004041F0 55               push        ebp  
004041F1 8B EC            mov         ebp,esp 
    return iniparser_getstring(d, key, NULL);
004041F3 6A 00            push        0    
004041F5 8B 45 0C         mov         eax,dword ptr [key] 
004041F8 50               push        eax  
004041F9 8B 4D 08         mov         ecx,dword ptr [d] 
004041FC 51               push        ecx  
004041FD E8 6E D1 FF FF   call        iniparser_getstring (401370h) 
}
00404202 5D               pop         ebp  
00404203 C2 08 00         ret         8    
--- No source file -------------------------------------------------------------
00404206 CC               int         3    
00404207 CC               int         3    
00404208 CC               int         3    
00404209 CC               int         3    
0040420A CC               int         3    
0040420B CC               int         3    
0040420C CC               int         3    
0040420D CC               int         3    
0040420E CC               int         3    
0040420F CC               int         3    
--- f:\eclipse\workspace\winrun4j\src\common\dictionary.cpp --------------------


char * iniparser_getstring(dictionary * d, const char * key, char * def)
{
00404210 55               push        ebp  
00404211 8B EC            mov         ebp,esp 
00404213 83 EC 08         sub         esp,8 
    char * lc_key ;
    char * sval ;

    if (d==NULL || key==NULL)
00404216 83 7D 08 00      cmp         dword ptr [d],0 
0040421A 74 06            je          iniparser_getstring+12h (404222h) 
0040421C 83 7D 0C 00      cmp         dword ptr [key],0 
00404220 75 05            jne         iniparser_getstring+17h (404227h) 
        return def ;
00404222 8B 45 10         mov         eax,dword ptr [def] 
00404225 EB 38            jmp         iniparser_getstring+4Fh (40425Fh) 

    lc_key = strdup(strlwc(key));
00404227 8B 45 0C         mov         eax,dword ptr [key] 
0040422A 50               push        eax  
0040422B E8 16 CE FF FF   call        strlwc (401046h) 
00404230 50               push        eax  
00404231 E8 B9 CF FF FF   call        @ILT+490(_strdup) (4011EFh) 
00404236 83 C4 04         add         esp,4 
00404239 89 45 FC         mov         dword ptr [lc_key],eax 
    sval = dictionary_get(d, lc_key, def);
0040423C 8B 4D 10         mov         ecx,dword ptr [def] 
0040423F 51               push        ecx  
00404240 8B 55 FC         mov         edx,dword ptr [lc_key] 
00404243 52               push        edx  
00404244 8B 45 08         mov         eax,dword ptr [d] 
00404247 50               push        eax  
00404248 E8 51 D0 FF FF   call        dictionary_get (40129Eh) 
0040424D 89 45 F8         mov         dword ptr [sval],eax 
    free(lc_key);
00404250 8B 4D FC         mov         ecx,dword ptr [lc_key] 
00404253 51               push        ecx  
00404254 E8 AF 98 00 00   call        free (40DB08h) 
00404259 83 C4 04         add         esp,4 
    return sval ;
0040425C 8B 45 F8         mov         eax,dword ptr [sval] 
}
0040425F 8B E5            mov         esp,ebp 
00404261 5D               pop         ebp  
00404262 C2 0C 00         ret         0Ch  
--- No source file -------------------------------------------------------------
00404265 CC               int         3    
00404266 CC               int         3    
00404267 CC               int         3    
00404268 CC               int         3    
00404269 CC               int         3    
0040426A CC               int         3    
0040426B CC               int         3    
0040426C CC               int         3    
0040426D CC               int         3    
0040426E CC               int         3    
0040426F CC               int         3    
--- f:\eclipse\workspace\winrun4j\src\common\dictionary.cpp --------------------


int iniparser_getint(dictionary * d, const char * key, int notfound)
{
00404270 55               push        ebp  
00404271 8B EC            mov         ebp,esp 
00404273 51               push        ecx  
    char    *   str ;

    str = iniparser_getstring(d, key, INI_INVALID_KEY);
00404274 6A FF            push        0FFFFFFFFh 
00404276 8B 45 0C         mov         eax,dword ptr [key] 
00404279 50               push        eax  
0040427A 8B 4D 08         mov         ecx,dword ptr [d] 
0040427D 51               push        ecx  
0040427E E8 ED D0 FF FF   call        iniparser_getstring (401370h) 
00404283 89 45 FC         mov         dword ptr [str],eax 
    if (str==INI_INVALID_KEY) return notfound ;
00404286 83 7D FC FF      cmp         dword ptr [str],0FFFFFFFFh 
0040428A 75 05            jne         iniparser_getint+21h (404291h) 
0040428C 8B 45 10         mov         eax,dword ptr [notfound] 
0040428F EB 10            jmp         iniparser_getint+31h (4042A1h) 
    return (int)strtol(str, NULL, 0);
00404291 6A 00            push        0    
00404293 6A 00            push        0    
00404295 8B 55 FC         mov         edx,dword ptr [str] 
00404298 52               push        edx  
00404299 E8 AE 98 00 00   call        strtol (40DB4Ch) 
0040429E 83 C4 0C         add         esp,0Ch 
}
004042A1 8B E5            mov         esp,ebp 
004042A3 5D               pop         ebp  
004042A4 C2 0C 00         ret         0Ch  
--- No source file -------------------------------------------------------------
004042A7 CC               int         3    
004042A8 CC               int         3    
004042A9 CC               int         3    
004042AA CC               int         3    
004042AB CC               int         3    
004042AC CC               int         3    
004042AD CC               int         3    
004042AE CC               int         3    
004042AF CC               int         3    
--- f:\eclipse\workspace\winrun4j\src\common\dictionary.cpp --------------------

double iniparser_getdouble(dictionary * d, char * key, double notfound)
{
004042B0 55               push        ebp  
004042B1 8B EC            mov         ebp,esp 
004042B3 51               push        ecx  
    char    *   str ;

    str = iniparser_getstring(d, key, INI_INVALID_KEY);
004042B4 6A FF            push        0FFFFFFFFh 
004042B6 8B 45 0C         mov         eax,dword ptr [key] 
004042B9 50               push        eax  
004042BA 8B 4D 08         mov         ecx,dword ptr [d] 
004042BD 51               push        ecx  
004042BE E8 AD D0 FF FF   call        iniparser_getstring (401370h) 
004042C3 89 45 FC         mov         dword ptr [str],eax 
    if (str==INI_INVALID_KEY) return notfound ;
004042C6 83 7D FC FF      cmp         dword ptr [str],0FFFFFFFFh 
004042CA 75 05            jne         iniparser_getdouble+21h (4042D1h) 
004042CC DD 45 10         fld         qword ptr [notfound] 
004042CF EB 0C            jmp         iniparser_getdouble+2Dh (4042DDh) 
    return atof(str);
004042D1 8B 55 FC         mov         edx,dword ptr [str] 
004042D4 52               push        edx  
004042D5 E8 52 98 00 00   call        atof (40DB2Ch) 
004042DA 83 C4 04         add         esp,4 
}
004042DD 8B E5            mov         esp,ebp 
004042DF 5D               pop         ebp  
004042E0 C2 10 00         ret         10h  
--- No source file -------------------------------------------------------------
004042E3 CC               int         3    
004042E4 CC               int         3    
004042E5 CC               int         3    
004042E6 CC               int         3    
004042E7 CC               int         3    
004042E8 CC               int         3    
004042E9 CC               int         3    
004042EA CC               int         3    
004042EB CC               int         3    
004042EC CC               int         3    
004042ED CC               int         3    
004042EE CC               int         3    
004042EF CC               int         3    
--- f:\eclipse\workspace\winrun4j\src\common\dictionary.cpp --------------------


int iniparser_getboolean(dictionary * d, const char * key, int notfound)
{
004042F0 55               push        ebp  
004042F1 8B EC            mov         ebp,esp 
004042F3 83 EC 08         sub         esp,8 
    char    *   c ;
    int         ret ;

    c = iniparser_getstring(d, key, INI_INVALID_KEY);
004042F6 6A FF            push        0FFFFFFFFh 
004042F8 8B 45 0C         mov         eax,dword ptr [key] 
004042FB 50               push        eax  
004042FC 8B 4D 08         mov         ecx,dword ptr [d] 
004042FF 51               push        ecx  
00404300 E8 6B D0 FF FF   call        iniparser_getstring (401370h) 
00404305 89 45 F8         mov         dword ptr [c],eax 
    if (c==INI_INVALID_KEY) return notfound ;
00404308 83 7D F8 FF      cmp         dword ptr [c],0FFFFFFFFh 
0040430C 75 08            jne         iniparser_getboolean+26h (404316h) 
0040430E 8B 45 10         mov         eax,dword ptr [notfound] 
00404311 E9 89 00 00 00   jmp         iniparser_getboolean+0AFh (40439Fh) 
    if (c[0]=='y' || c[0]=='Y' || c[0]=='1' || c[0]=='t' || c[0]=='T') {
00404316 8B 55 F8         mov         edx,dword ptr [c] 
00404319 0F BE 02         movsx       eax,byte ptr [edx] 
0040431C 83 F8 79         cmp         eax,79h 
0040431F 74 2C            je          iniparser_getboolean+5Dh (40434Dh) 
00404321 8B 4D F8         mov         ecx,dword ptr [c] 
00404324 0F BE 11         movsx       edx,byte ptr [ecx] 
00404327 83 FA 59         cmp         edx,59h 
0040432A 74 21            je          iniparser_getboolean+5Dh (40434Dh) 
0040432C 8B 45 F8         mov         eax,dword ptr [c] 
0040432F 0F BE 08         movsx       ecx,byte ptr [eax] 
00404332 83 F9 31         cmp         ecx,31h 
00404335 74 16            je          iniparser_getboolean+5Dh (40434Dh) 
00404337 8B 55 F8         mov         edx,dword ptr [c] 
0040433A 0F BE 02         movsx       eax,byte ptr [edx] 
0040433D 83 F8 74         cmp         eax,74h 
00404340 74 0B            je          iniparser_getboolean+5Dh (40434Dh) 
00404342 8B 4D F8         mov         ecx,dword ptr [c] 
00404345 0F BE 11         movsx       edx,byte ptr [ecx] 
00404348 83 FA 54         cmp         edx,54h 
0040434B 75 09            jne         iniparser_getboolean+66h (404356h) 
        ret = 1 ;
0040434D C7 45 FC 01 00 00 00 mov         dword ptr [ret],1 
00404354 EB 46            jmp         iniparser_getboolean+0ACh (40439Ch) 
    } else if (c[0]=='n' || c[0]=='N' || c[0]=='0' || c[0]=='f' || c[0]=='F') {
00404356 8B 45 F8         mov         eax,dword ptr [c] 
00404359 0F BE 08         movsx       ecx,byte ptr [eax] 
0040435C 83 F9 6E         cmp         ecx,6Eh 
0040435F 74 2C            je          iniparser_getboolean+9Dh (40438Dh) 
00404361 8B 55 F8         mov         edx,dword ptr [c] 
00404364 0F BE 02         movsx       eax,byte ptr [edx] 
00404367 83 F8 4E         cmp         eax,4Eh 
0040436A 74 21            je          iniparser_getboolean+9Dh (40438Dh) 
0040436C 8B 4D F8         mov         ecx,dword ptr [c] 
0040436F 0F BE 11         movsx       edx,byte ptr [ecx] 
00404372 83 FA 30         cmp         edx,30h 
00404375 74 16            je          iniparser_getboolean+9Dh (40438Dh) 
00404377 8B 45 F8         mov         eax,dword ptr [c] 
0040437A 0F BE 08         movsx       ecx,byte ptr [eax] 
0040437D 83 F9 66         cmp         ecx,66h 
00404380 74 0B            je          iniparser_getboolean+9Dh (40438Dh) 
00404382 8B 55 F8         mov         edx,dword ptr [c] 
00404385 0F BE 02         movsx       eax,byte ptr [edx] 
00404388 83 F8 46         cmp         eax,46h 
0040438B 75 09            jne         iniparser_getboolean+0A6h (404396h) 
        ret = 0 ;
0040438D C7 45 FC 00 00 00 00 mov         dword ptr [ret],0 
    } else {
00404394 EB 06            jmp         iniparser_getboolean+0ACh (40439Ch) 
        ret = notfound ;
00404396 8B 4D 10         mov         ecx,dword ptr [notfound] 
00404399 89 4D FC         mov         dword ptr [ret],ecx 
    }
    return ret;
0040439C 8B 45 FC         mov         eax,dword ptr [ret] 
}
0040439F 8B E5            mov         esp,ebp 
004043A1 5D               pop         ebp  
004043A2 C2 0C 00         ret         0Ch  
--- No source file -------------------------------------------------------------
004043A5 CC               int         3    
004043A6 CC               int         3    
004043A7 CC               int         3    
004043A8 CC               int         3    
004043A9 CC               int         3    
004043AA CC               int         3    
004043AB CC               int         3    
004043AC CC               int         3    
004043AD CC               int         3    
004043AE CC               int         3    
004043AF CC               int         3    
--- f:\eclipse\workspace\winrun4j\src\common\dictionary.cpp --------------------


int iniparser_find_entry(
    dictionary  *   ini,
    char        *   entry
)
{
004043B0 55               push        ebp  
004043B1 8B EC            mov         ebp,esp 
004043B3 51               push        ecx  
    int found=0 ;
004043B4 C7 45 FC 00 00 00 00 mov         dword ptr [found],0 
    if (iniparser_getstring(ini, entry, INI_INVALID_KEY)!=INI_INVALID_KEY) {
004043BB 6A FF            push        0FFFFFFFFh 
004043BD 8B 45 0C         mov         eax,dword ptr [entry] 
004043C0 50               push        eax  
004043C1 8B 4D 08         mov         ecx,dword ptr [ini] 
004043C4 51               push        ecx  
004043C5 E8 A6 CF FF FF   call        iniparser_getstring (401370h) 
004043CA 83 F8 FF         cmp         eax,0FFFFFFFFh 
004043CD 74 07            je          iniparser_find_entry+26h (4043D6h) 
        found = 1 ;
004043CF C7 45 FC 01 00 00 00 mov         dword ptr [found],1 
    }
    return found ;
004043D6 8B 45 FC         mov         eax,dword ptr [found] 
}
004043D9 8B E5            mov         esp,ebp 
004043DB 5D               pop         ebp  
004043DC C2 08 00         ret         8    
--- No source file -------------------------------------------------------------
004043DF CC               int         3    
--- f:\eclipse\workspace\winrun4j\src\common\dictionary.cpp --------------------



int iniparser_setstr(dictionary * ini, char * entry, char * val)
{
004043E0 55               push        ebp  
004043E1 8B EC            mov         ebp,esp 
    dictionary_set(ini, strlwc(entry), val);
004043E3 8B 45 10         mov         eax,dword ptr [val] 
004043E6 50               push        eax  
004043E7 8B 4D 0C         mov         ecx,dword ptr [entry] 
004043EA 51               push        ecx  
004043EB E8 56 CC FF FF   call        strlwc (401046h) 
004043F0 50               push        eax  
004043F1 8B 55 08         mov         edx,dword ptr [ini] 
004043F4 52               push        edx  
004043F5 E8 A6 CC FF FF   call        dictionary_set (4010A0h) 
    return 0 ;
004043FA 33 C0            xor         eax,eax 
}
004043FC 5D               pop         ebp  
004043FD C2 0C 00         ret         0Ch  

void iniparser_unset(dictionary * ini, char * entry)
{
00404400 55               push        ebp  
00404401 8B EC            mov         ebp,esp 
    dictionary_unset(ini, strlwc(entry));
00404403 8B 45 0C         mov         eax,dword ptr [entry] 
00404406 50               push        eax  
00404407 E8 3A CC FF FF   call        strlwc (401046h) 
0040440C 50               push        eax  
0040440D 8B 4D 08         mov         ecx,dword ptr [ini] 
00404410 51               push        ecx  
00404411 E8 87 CF FF FF   call        dictionary_unset (40139Dh) 
}
00404416 5D               pop         ebp  
00404417 C2 08 00         ret         8    
--- No source file -------------------------------------------------------------
0040441A CC               int         3    
0040441B CC               int         3    
0040441C CC               int         3    
0040441D CC               int         3    
0040441E CC               int         3    
0040441F CC               int         3    
--- f:\eclipse\workspace\winrun4j\src\common\dictionary.cpp --------------------

void parse_line(char * lin, dictionary * d)
{
00404420 55               push        ebp  
00404421 8B EC            mov         ebp,esp 
00404423 81 EC 20 0C 00 00 sub         esp,0C20h 
    char        sec[ASCIILINESZ+1];
    char        key[ASCIILINESZ+1];
    char        val[ASCIILINESZ+1];
    char    *   wher ;

    sec[0]=0;
00404429 C6 85 F8 FB FF FF 00 mov         byte ptr [sec],0 

    wher = strskp(lin); /* Skip leading spaces */
00404430 8B 45 08         mov         eax,dword ptr [lin] 
00404433 50               push        eax  
00404434 E8 9C CE FF FF   call        strskp (4012D5h) 
00404439 89 85 EC F7 FF FF mov         dword ptr [wher],eax 
    if (*wher==';' || *wher=='#' || *wher==0)
0040443F 8B 8D EC F7 FF FF mov         ecx,dword ptr [wher] 
00404445 0F BE 11         movsx       edx,byte ptr [ecx] 
00404448 83 FA 3B         cmp         edx,3Bh 
0040444B 74 1B            je          parse_line+48h (404468h) 
0040444D 8B 85 EC F7 FF FF mov         eax,dword ptr [wher] 
00404453 0F BE 08         movsx       ecx,byte ptr [eax] 
00404456 83 F9 23         cmp         ecx,23h 
00404459 74 0D            je          parse_line+48h (404468h) 
0040445B 8B 95 EC F7 FF FF mov         edx,dword ptr [wher] 
00404461 0F BE 02         movsx       eax,byte ptr [edx] 
00404464 85 C0            test        eax,eax 
00404466 75 0A            jne         parse_line+52h (404472h) 
        return ; /* Comment lines */
00404468 E9 68 01 00 00   jmp         parse_line+1B5h (4045D5h) 
    else {
0040446D E9 63 01 00 00   jmp         parse_line+1B5h (4045D5h) 
        if (sscanf(wher, "[%[^]]", sec)==1) {
00404472 8D 8D F8 FB FF FF lea         ecx,[sec] 
00404478 51               push        ecx  
00404479 68 8C 0F 41 00   push        410F8Ch 
0040447E 8B 95 EC F7 FF FF mov         edx,dword ptr [wher] 
00404484 52               push        edx  
00404485 E8 C8 96 00 00   call        sscanf (40DB52h) 
0040448A 83 C4 0C         add         esp,0Ch 
0040448D 83 F8 01         cmp         eax,1 
00404490 75 35            jne         parse_line+0A7h (4044C7h) 
            /* Valid section name */
            strcpy(sec, strlwc(sec));
00404492 8D 85 F8 FB FF FF lea         eax,[sec] 
00404498 50               push        eax  
00404499 E8 A8 CB FF FF   call        strlwc (401046h) 
0040449E 50               push        eax  
0040449F 8D 8D F8 FB FF FF lea         ecx,[sec] 
004044A5 51               push        ecx  
004044A6 E8 45 96 00 00   call        strcpy (40DAF0h) 
004044AB 83 C4 08         add         esp,8 
            iniparser_add_entry(d, sec, NULL, NULL);
004044AE 6A 00            push        0    
004044B0 6A 00            push        0    
004044B2 8D 95 F8 FB FF FF lea         edx,[sec] 
004044B8 52               push        edx  
004044B9 8B 45 0C         mov         eax,dword ptr [d] 
004044BC 50               push        eax  
004044BD E8 1E 01 00 00   call        iniparser_add_entry (4045E0h) 
        } else if (sscanf (wher, "%[^=] = \"%[^\"]\"", key, val) == 2
004044C2 E9 0E 01 00 00   jmp         parse_line+1B5h (4045D5h) 
               ||  sscanf (wher, "%[^=] = '%[^\']'",   key, val) == 2
               ||  sscanf (wher, "%[^=] = %[^;#]",     key, val) == 2) {
004044C7 8D 8D E0 F3 FF FF lea         ecx,[val] 
004044CD 51               push        ecx  
004044CE 8D 95 F0 F7 FF FF lea         edx,[key] 
004044D4 52               push        edx  
004044D5 68 94 0F 41 00   push        410F94h 
004044DA 8B 85 EC F7 FF FF mov         eax,dword ptr [wher] 
004044E0 50               push        eax  
004044E1 E8 6C 96 00 00   call        sscanf (40DB52h) 
004044E6 83 C4 10         add         esp,10h 
004044E9 83 F8 02         cmp         eax,2 
004044EC 74 52            je          parse_line+120h (404540h) 
004044EE 8D 8D E0 F3 FF FF lea         ecx,[val] 
004044F4 51               push        ecx  
004044F5 8D 95 F0 F7 FF FF lea         edx,[key] 
004044FB 52               push        edx  
004044FC 68 A4 0F 41 00   push        410FA4h 
00404501 8B 85 EC F7 FF FF mov         eax,dword ptr [wher] 
00404507 50               push        eax  
00404508 E8 45 96 00 00   call        sscanf (40DB52h) 
0040450D 83 C4 10         add         esp,10h 
00404510 83 F8 02         cmp         eax,2 
00404513 74 2B            je          parse_line+120h (404540h) 
00404515 8D 8D E0 F3 FF FF lea         ecx,[val] 
0040451B 51               push        ecx  
0040451C 8D 95 F0 F7 FF FF lea         edx,[key] 
00404522 52               push        edx  
00404523 68 B4 0F 41 00   push        410FB4h 
00404528 8B 85 EC F7 FF FF mov         eax,dword ptr [wher] 
0040452E 50               push        eax  
0040452F E8 1E 96 00 00   call        sscanf (40DB52h) 
00404534 83 C4 10         add         esp,10h 
00404537 83 F8 02         cmp         eax,2 
0040453A 0F 85 95 00 00 00 jne         parse_line+1B5h (4045D5h) 
            strcpy(key, strlwc(strcrop(key)));
00404540 8D 8D F0 F7 FF FF lea         ecx,[key] 
00404546 51               push        ecx  
00404547 E8 26 CC FF FF   call        strcrop (401172h) 
0040454C 50               push        eax  
0040454D E8 F4 CA FF FF   call        strlwc (401046h) 
00404552 50               push        eax  
00404553 8D 95 F0 F7 FF FF lea         edx,[key] 
00404559 52               push        edx  
0040455A E8 91 95 00 00   call        strcpy (40DAF0h) 
0040455F 83 C4 08         add         esp,8 
            /*
             * sscanf cannot handle "" or '' as empty value,
             * this is done here
             */
            if (!strcmp(val, "\"\"") || !strcmp(val, "''")) {
00404562 68 C4 0F 41 00   push        410FC4h 
00404567 8D 85 E0 F3 FF FF lea         eax,[val] 
0040456D 50               push        eax  
0040456E E8 83 95 00 00   call        strcmp (40DAF6h) 
00404573 83 C4 08         add         esp,8 
00404576 85 C0            test        eax,eax 
00404578 74 18            je          parse_line+172h (404592h) 
0040457A 68 C8 0F 41 00   push        410FC8h 
0040457F 8D 8D E0 F3 FF FF lea         ecx,[val] 
00404585 51               push        ecx  
00404586 E8 6B 95 00 00   call        strcmp (40DAF6h) 
0040458B 83 C4 08         add         esp,8 
0040458E 85 C0            test        eax,eax 
00404590 75 09            jne         parse_line+17Bh (40459Bh) 
                val[0] = (char)0;
00404592 C6 85 E0 F3 FF FF 00 mov         byte ptr [val],0 
            } else {
00404599 EB 1C            jmp         parse_line+197h (4045B7h) 
                strcpy(val, strcrop(val));
0040459B 8D 95 E0 F3 FF FF lea         edx,[val] 
004045A1 52               push        edx  
004045A2 E8 CB CB FF FF   call        strcrop (401172h) 
004045A7 50               push        eax  
004045A8 8D 85 E0 F3 FF FF lea         eax,[val] 
004045AE 50               push        eax  
004045AF E8 3C 95 00 00   call        strcpy (40DAF0h) 
004045B4 83 C4 08         add         esp,8 
            }
            iniparser_add_entry(d, sec, key, val);
004045B7 8D 8D E0 F3 FF FF lea         ecx,[val] 
004045BD 51               push        ecx  
004045BE 8D 95 F0 F7 FF FF lea         edx,[key] 
004045C4 52               push        edx  
004045C5 8D 85 F8 FB FF FF lea         eax,[sec] 
004045CB 50               push        eax  
004045CC 8B 4D 0C         mov         ecx,dword ptr [d] 
004045CF 51               push        ecx  
004045D0 E8 0B 00 00 00   call        iniparser_add_entry (4045E0h) 
        }
    }
}
004045D5 8B E5            mov         esp,ebp 
004045D7 5D               pop         ebp  
004045D8 C2 08 00         ret         8    
--- No source file -------------------------------------------------------------
004045DB CC               int         3    
004045DC CC               int         3    
004045DD CC               int         3    
004045DE CC               int         3    
004045DF CC               int         3    
--- f:\eclipse\workspace\winrun4j\src\common\dictionary.cpp --------------------




/*
Taken from http://ndevilla.free.fr/iniparser/

iniparser is a free stand-alone ini file parsing library.
It is written in portable ANSI C and should compile anywhere.
iniparser is distributed under an MIT license.

*/

#define ASCIILINESZ         1024
#define INI_INVALID_KEY     ((char*)-1)


/* Private: add an entry to the dictionary */
static void iniparser_add_entry(
    dictionary * d,
    char * sec,
    char * key,
    char * val)
{
004045E0 55               push        ebp  
004045E1 8B EC            mov         ebp,esp 
004045E3 81 EC 08 08 00 00 sub         esp,808h 
    char longkey[2*ASCIILINESZ+1];

    /* Make a key as section:keyword */
    if (key!=NULL) {
004045E9 83 7D 10 00      cmp         dword ptr [key],0 
004045ED 74 1E            je          iniparser_add_entry+2Dh (40460Dh) 
        sprintf(longkey, "%s:%s", sec, key);
004045EF 8B 45 10         mov         eax,dword ptr [key] 
004045F2 50               push        eax  
004045F3 8B 4D 0C         mov         ecx,dword ptr [sec] 
004045F6 51               push        ecx  
004045F7 68 44 0F 41 00   push        410F44h 
004045FC 8D 95 F8 F7 FF FF lea         edx,[longkey] 
00404602 52               push        edx  
00404603 E8 0C 95 00 00   call        sprintf (40DB14h) 
00404608 83 C4 10         add         esp,10h 
    } else {
0040460B EB 13            jmp         iniparser_add_entry+40h (404620h) 
        strcpy(longkey, sec);
0040460D 8B 45 0C         mov         eax,dword ptr [sec] 
00404610 50               push        eax  
00404611 8D 8D F8 F7 FF FF lea         ecx,[longkey] 
00404617 51               push        ecx  
00404618 E8 D3 94 00 00   call        strcpy (40DAF0h) 
0040461D 83 C4 08         add         esp,8 
    }

    /* Add (key,val) to dictionary */
    dictionary_set(d, longkey, val);
00404620 8B 55 14         mov         edx,dword ptr [val] 
00404623 52               push        edx  
00404624 8D 85 F8 F7 FF FF lea         eax,[longkey] 
0040462A 50               push        eax  
0040462B 8B 4D 08         mov         ecx,dword ptr [d] 
0040462E 51               push        ecx  
0040462F E8 6C CA FF FF   call        dictionary_set (4010A0h) 
    return ;
}
00404634 8B E5            mov         esp,ebp 
00404636 5D               pop         ebp  
00404637 C2 10 00         ret         10h  
--- No source file -------------------------------------------------------------
0040463A CC               int         3    
0040463B CC               int         3    
0040463C CC               int         3    
0040463D CC               int         3    
0040463E CC               int         3    
0040463F CC               int         3    
--- f:\eclipse\workspace\winrun4j\src\common\dictionary.cpp --------------------

dictionary * iniparser_load(char * ininame, bool isbuffer)
{
00404640 55               push        ebp  
00404641 8B EC            mov         ebp,esp 
00404643 81 EC 20 04 00 00 sub         esp,420h 
    dictionary  *   d ;
    char        lin[ASCIILINESZ+1];
    FILE    *   ini ;
    int         lineno ;
    memset(lin, 0, ASCIILINESZ);
00404649 68 00 04 00 00   push        400h 
0040464E 6A 00            push        0    
00404650 8D 85 F0 FB FF FF lea         eax,[lin] 
00404656 50               push        eax  
00404657 E8 0E 95 00 00   call        memset (40DB6Ah) 
0040465C 83 C4 0C         add         esp,0Ch 

    if (!isbuffer && (ini=fopen(ininame, "r"))==NULL) {
0040465F 0F B6 4D 0C      movzx       ecx,byte ptr [isbuffer] 
00404663 85 C9            test        ecx,ecx 
00404665 75 27            jne         iniparser_load+4Eh (40468Eh) 
00404667 68 CC 0F 41 00   push        410FCCh 
0040466C 8B 55 08         mov         edx,dword ptr [ininame] 
0040466F 52               push        edx  
00404670 E8 EF 94 00 00   call        fopen (40DB64h) 
00404675 83 C4 08         add         esp,8 
00404678 89 85 EC FB FF FF mov         dword ptr [ini],eax 
0040467E 83 BD EC FB FF FF 00 cmp         dword ptr [ini],0 
00404685 75 07            jne         iniparser_load+4Eh (40468Eh) 
        return NULL ;
00404687 33 C0            xor         eax,eax 
00404689 E9 01 01 00 00   jmp         iniparser_load+14Fh (40478Fh) 
    }

    if(isbuffer && !ininame) {
0040468E 0F B6 45 0C      movzx       eax,byte ptr [isbuffer] 
00404692 85 C0            test        eax,eax 
00404694 74 0D            je          iniparser_load+63h (4046A3h) 
00404696 83 7D 08 00      cmp         dword ptr [ininame],0 
0040469A 75 07            jne         iniparser_load+63h (4046A3h) 
        return NULL ;
0040469C 33 C0            xor         eax,eax 
0040469E E9 EC 00 00 00   jmp         iniparser_load+14Fh (40478Fh) 
    }

    /*
     * Initialize a new dictionary entry
     */
    d = dictionary_new(0);
004046A3 6A 00            push        0    
004046A5 E8 B9 CA FF FF   call        dictionary_new (401163h) 
004046AA 89 85 E8 FB FF FF mov         dword ptr [d],eax 
    lineno = 0 ;
004046B0 C7 45 FC 00 00 00 00 mov         dword ptr [lineno],0 
    int pos = 0;
004046B7 C7 85 E4 FB FF FF 00 00 00 00 mov         dword ptr [pos],0 
    while ((isbuffer ? sgets(ininame, &pos, lin, ASCIILINESZ) : fgets(lin, ASCIILINESZ, ini)) != NULL) {
004046C1 0F B6 4D 0C      movzx       ecx,byte ptr [isbuffer] 
004046C5 85 C9            test        ecx,ecx 
004046C7 74 24            je          iniparser_load+0ADh (4046EDh) 
004046C9 68 00 04 00 00   push        400h 
004046CE 8D 95 F0 FB FF FF lea         edx,[lin] 
004046D4 52               push        edx  
004046D5 8D 85 E4 FB FF FF lea         eax,[pos] 
004046DB 50               push        eax  
004046DC 8B 4D 08         mov         ecx,dword ptr [ininame] 
004046DF 51               push        ecx  
004046E0 E8 E7 CA FF FF   call        sgets (4011CCh) 
004046E5 89 85 E0 FB FF FF mov         dword ptr [ebp-420h],eax 
004046EB EB 21            jmp         iniparser_load+0CEh (40470Eh) 
004046ED 8B 95 EC FB FF FF mov         edx,dword ptr [ini] 
004046F3 52               push        edx  
004046F4 68 00 04 00 00   push        400h 
004046F9 8D 85 F0 FB FF FF lea         eax,[lin] 
004046FF 50               push        eax  
00404700 E8 59 94 00 00   call        fgets (40DB5Eh) 
00404705 83 C4 0C         add         esp,0Ch 
00404708 89 85 E0 FB FF FF mov         dword ptr [ebp-420h],eax 
0040470E 83 BD E0 FB FF FF 00 cmp         dword ptr [ebp-420h],0 
00404715 74 37            je          iniparser_load+10Eh (40474Eh) 
        lineno++;
00404717 8B 4D FC         mov         ecx,dword ptr [lineno] 
0040471A 83 C1 01         add         ecx,1 
0040471D 89 4D FC         mov         dword ptr [lineno],ecx 
        parse_line(lin, d);
00404720 8B 95 E8 FB FF FF mov         edx,dword ptr [d] 
00404726 52               push        edx  
00404727 8D 85 F0 FB FF FF lea         eax,[lin] 
0040472D 50               push        eax  
0040472E E8 01 CC FF FF   call        parse_line (401334h) 
        memset(lin, 0, ASCIILINESZ);
00404733 68 00 04 00 00   push        400h 
00404738 6A 00            push        0    
0040473A 8D 8D F0 FB FF FF lea         ecx,[lin] 
00404740 51               push        ecx  
00404741 E8 24 94 00 00   call        memset (40DB6Ah) 
00404746 83 C4 0C         add         esp,0Ch 
    }
00404749 E9 73 FF FF FF   jmp         iniparser_load+81h (4046C1h) 

    if(strlen(lin) != 0)
0040474E 8D 95 F0 FB FF FF lea         edx,[lin] 
00404754 52               push        edx  
00404755 FF 15 68 86 41 00 call        dword ptr [__imp__lstrlenA@4 (418668h)] 
0040475B 85 C0            test        eax,eax 
0040475D 74 13            je          iniparser_load+132h (404772h) 
        parse_line(lin, d);
0040475F 8B 85 E8 FB FF FF mov         eax,dword ptr [d] 
00404765 50               push        eax  
00404766 8D 8D F0 FB FF FF lea         ecx,[lin] 
0040476C 51               push        ecx  
0040476D E8 C2 CB FF FF   call        parse_line (401334h) 
    
    if(!isbuffer) fclose(ini);
00404772 0F B6 55 0C      movzx       edx,byte ptr [isbuffer] 
00404776 85 D2            test        edx,edx 
00404778 75 0F            jne         iniparser_load+149h (404789h) 
0040477A 8B 85 EC FB FF FF mov         eax,dword ptr [ini] 
00404780 50               push        eax  
00404781 E8 D2 93 00 00   call        fclose (40DB58h) 
00404786 83 C4 04         add         esp,4 

    return d ;
00404789 8B 85 E8 FB FF FF mov         eax,dword ptr [d] 
}
0040478F 8B E5            mov         esp,ebp 
00404791 5D               pop         ebp  
00404792 C2 08 00         ret         8    
--- No source file -------------------------------------------------------------
00404795 CC               int         3    
00404796 CC               int         3    
00404797 CC               int         3    
00404798 CC               int         3    
00404799 CC               int         3    
0040479A CC               int         3    
0040479B CC               int         3    
0040479C CC               int         3    
0040479D CC               int         3    
0040479E CC               int         3    
0040479F CC               int         3    
--- f:\eclipse\workspace\winrun4j\src\common\dictionary.cpp --------------------

void iniparser_freedict(dictionary * d)
{
004047A0 55               push        ebp  
004047A1 8B EC            mov         ebp,esp 
    dictionary_del(d);
004047A3 8B 45 08         mov         eax,dword ptr [d] 
004047A6 50               push        eax  
004047A7 E8 BA CB FF FF   call        dictionary_del (401366h) 
}
004047AC 5D               pop         ebp  
004047AD C2 04 00         ret         4    


#define ASCIILINESZ 1024



char * strlwc(const char * s)
{
004047B0 55               push        ebp  
004047B1 8B EC            mov         ebp,esp 
004047B3 51               push        ecx  
    static char l[ASCIILINESZ+1];
    int i ;

    if (s==NULL) return NULL ;
004047B4 83 7D 08 00      cmp         dword ptr [s],0 
004047B8 75 04            jne         strlwc+0Eh (4047BEh) 
004047BA 33 C0            xor         eax,eax 
004047BC EB 63            jmp         strlwc+71h (404821h) 
    memset(l, 0, ASCIILINESZ+1);
004047BE 68 01 04 00 00   push        401h 
004047C3 6A 00            push        0    
004047C5 68 60 5A 41 00   push        offset l (415A60h) 
004047CA E8 9B 93 00 00   call        memset (40DB6Ah) 
004047CF 83 C4 0C         add         esp,0Ch 
    i=0 ;
004047D2 C7 45 FC 00 00 00 00 mov         dword ptr [i],0 
    while (s[i] && i<ASCIILINESZ) {
004047D9 8B 45 08         mov         eax,dword ptr [s] 
004047DC 03 45 FC         add         eax,dword ptr [i] 
004047DF 0F BE 08         movsx       ecx,byte ptr [eax] 
004047E2 85 C9            test        ecx,ecx 
004047E4 74 2F            je          strlwc+65h (404815h) 
004047E6 81 7D FC 00 04 00 00 cmp         dword ptr [i],400h 
004047ED 7D 26            jge         strlwc+65h (404815h) 
        l[i] = (char)tolower((int)s[i]);
004047EF 8B 55 08         mov         edx,dword ptr [s] 
004047F2 03 55 FC         add         edx,dword ptr [i] 
004047F5 0F BE 02         movsx       eax,byte ptr [edx] 
004047F8 50               push        eax  
004047F9 E8 72 93 00 00   call        tolower (40DB70h) 
004047FE 83 C4 04         add         esp,4 
00404801 8B 4D FC         mov         ecx,dword ptr [i] 
00404804 88 81 60 5A 41 00 mov         byte ptr l (415A60h)[ecx],al 
        i++ ;
0040480A 8B 55 FC         mov         edx,dword ptr [i] 
0040480D 83 C2 01         add         edx,1 
00404810 89 55 FC         mov         dword ptr [i],edx 
    }
00404813 EB C4            jmp         strlwc+29h (4047D9h) 
    l[ASCIILINESZ]=(char)0;
00404815 C6 05 60 5E 41 00 00 mov         byte ptr [l+400h (415E60h)],0 
    return l ;
0040481C B8 60 5A 41 00   mov         eax,offset l (415A60h) 
}
00404821 8B E5            mov         esp,ebp 
00404823 5D               pop         ebp  
00404824 C2 04 00         ret         4    
--- No source file -------------------------------------------------------------
00404827 CC               int         3    
00404828 CC               int         3    
00404829 CC               int         3    
0040482A CC               int         3    
0040482B CC               int         3    
0040482C CC               int         3    
0040482D CC               int         3    
0040482E CC               int         3    
0040482F CC               int         3    
--- f:\eclipse\workspace\winrun4j\src\common\dictionary.cpp --------------------



char * strupc(char * s)
{
00404830 55               push        ebp  
00404831 8B EC            mov         ebp,esp 
00404833 51               push        ecx  
    static char l[ASCIILINESZ+1];
    int i ;

    if (s==NULL) return NULL ;
00404834 83 7D 08 00      cmp         dword ptr [s],0 
00404838 75 04            jne         strupc+0Eh (40483Eh) 
0040483A 33 C0            xor         eax,eax 
0040483C EB 63            jmp         strupc+71h (4048A1h) 
    memset(l, 0, ASCIILINESZ+1);
0040483E 68 01 04 00 00   push        401h 
00404843 6A 00            push        0    
00404845 68 30 5F 41 00   push        offset l (415F30h) 
0040484A E8 1B 93 00 00   call        memset (40DB6Ah) 
0040484F 83 C4 0C         add         esp,0Ch 
    i=0 ;
00404852 C7 45 FC 00 00 00 00 mov         dword ptr [i],0 
    while (s[i] && i<ASCIILINESZ) {
00404859 8B 45 08         mov         eax,dword ptr [s] 
0040485C 03 45 FC         add         eax,dword ptr [i] 
0040485F 0F BE 08         movsx       ecx,byte ptr [eax] 
00404862 85 C9            test        ecx,ecx 
00404864 74 2F            je          strupc+65h (404895h) 
00404866 81 7D FC 00 04 00 00 cmp         dword ptr [i],400h 
0040486D 7D 26            jge         strupc+65h (404895h) 
        l[i] = (char)toupper((int)s[i]);
0040486F 8B 55 08         mov         edx,dword ptr [s] 
00404872 03 55 FC         add         edx,dword ptr [i] 
00404875 0F BE 02         movsx       eax,byte ptr [edx] 
00404878 50               push        eax  
00404879 E8 F8 92 00 00   call        toupper (40DB76h) 
0040487E 83 C4 04         add         esp,4 
00404881 8B 4D FC         mov         ecx,dword ptr [i] 
00404884 88 81 30 5F 41 00 mov         byte ptr l (415F30h)[ecx],al 
        i++ ;
0040488A 8B 55 FC         mov         edx,dword ptr [i] 
0040488D 83 C2 01         add         edx,1 
00404890 89 55 FC         mov         dword ptr [i],edx 
    }
00404893 EB C4            jmp         strupc+29h (404859h) 
    l[ASCIILINESZ]=(char)0;
00404895 C6 05 30 63 41 00 00 mov         byte ptr [l+400h (416330h)],0 
    return l ;
0040489C B8 30 5F 41 00   mov         eax,offset l (415F30h) 
}
004048A1 8B E5            mov         esp,ebp 
004048A3 5D               pop         ebp  
004048A4 C2 04 00         ret         4    
--- No source file -------------------------------------------------------------
004048A7 CC               int         3    
004048A8 CC               int         3    
004048A9 CC               int         3    
004048AA CC               int         3    
004048AB CC               int         3    
004048AC CC               int         3    
004048AD CC               int         3    
004048AE CC               int         3    
004048AF CC               int         3    
--- f:\eclipse\workspace\winrun4j\src\common\dictionary.cpp --------------------



char * strskp(char * s)
{
004048B0 55               push        ebp  
004048B1 8B EC            mov         ebp,esp 
004048B3 51               push        ecx  
    char * skip = s;
004048B4 8B 45 08         mov         eax,dword ptr [s] 
004048B7 89 45 FC         mov         dword ptr [skip],eax 
    if (s==NULL) return NULL ;
004048BA 83 7D 08 00      cmp         dword ptr [s],0 
004048BE 75 04            jne         strskp+14h (4048C4h) 
004048C0 33 C0            xor         eax,eax 
004048C2 EB 2B            jmp         strskp+3Fh (4048EFh) 
    while (isspace((int)*skip) && *skip) skip++;
004048C4 8B 4D FC         mov         ecx,dword ptr [skip] 
004048C7 0F BE 11         movsx       edx,byte ptr [ecx] 
004048CA 52               push        edx  
004048CB E8 AC 92 00 00   call        isspace (40DB7Ch) 
004048D0 83 C4 04         add         esp,4 
004048D3 85 C0            test        eax,eax 
004048D5 74 15            je          strskp+3Ch (4048ECh) 
004048D7 8B 45 FC         mov         eax,dword ptr [skip] 
004048DA 0F BE 08         movsx       ecx,byte ptr [eax] 
004048DD 85 C9            test        ecx,ecx 
004048DF 74 0B            je          strskp+3Ch (4048ECh) 
004048E1 8B 55 FC         mov         edx,dword ptr [skip] 
004048E4 83 C2 01         add         edx,1 
004048E7 89 55 FC         mov         dword ptr [skip],edx 
004048EA EB D8            jmp         strskp+14h (4048C4h) 
    return skip ;
004048EC 8B 45 FC         mov         eax,dword ptr [skip] 
} 
004048EF 8B E5            mov         esp,ebp 
004048F1 5D               pop         ebp  
004048F2 C2 04 00         ret         4    
--- No source file -------------------------------------------------------------
004048F5 CC               int         3    
004048F6 CC               int         3    
004048F7 CC               int         3    
004048F8 CC               int         3    
004048F9 CC               int         3    
004048FA CC               int         3    
004048FB CC               int         3    
004048FC CC               int         3    
004048FD CC               int         3    
004048FE CC               int         3    
004048FF CC               int         3    
--- f:\eclipse\workspace\winrun4j\src\common\dictionary.cpp --------------------



char * strcrop(char * s)
{
00404900 55               push        ebp  
00404901 8B EC            mov         ebp,esp 
00404903 51               push        ecx  
    static char l[ASCIILINESZ+1];
    char * last ;

    if (s==NULL) return NULL ;
00404904 83 7D 08 00      cmp         dword ptr [s],0 
00404908 75 04            jne         strcrop+0Eh (40490Eh) 
0040490A 33 C0            xor         eax,eax 
0040490C EB 6D            jmp         strcrop+7Bh (40497Bh) 
    memset(l, 0, ASCIILINESZ+1);
0040490E 68 01 04 00 00   push        401h 
00404913 6A 00            push        0    
00404915 68 00 64 41 00   push        offset l (416400h) 
0040491A E8 4B 92 00 00   call        memset (40DB6Ah) 
0040491F 83 C4 0C         add         esp,0Ch 
    strcpy(l, s);
00404922 8B 45 08         mov         eax,dword ptr [s] 
00404925 50               push        eax  
00404926 68 00 64 41 00   push        offset l (416400h) 
0040492B E8 C0 91 00 00   call        strcpy (40DAF0h) 
00404930 83 C4 08         add         esp,8 
    last = l + strlen(l);
00404933 68 00 64 41 00   push        offset l (416400h) 
00404938 FF 15 68 86 41 00 call        dword ptr [__imp__lstrlenA@4 (418668h)] 
0040493E 05 00 64 41 00   add         eax,offset l (416400h) 
00404943 89 45 FC         mov         dword ptr [last],eax 
    while (last > l) {
00404946 81 7D FC 00 64 41 00 cmp         dword ptr [last],offset l (416400h) 
0040494D 76 21            jbe         strcrop+70h (404970h) 
        if (!isspace((int)*(last-1)))
0040494F 8B 4D FC         mov         ecx,dword ptr [last] 
00404952 0F BE 51 FF      movsx       edx,byte ptr [ecx-1] 
00404956 52               push        edx  
00404957 E8 20 92 00 00   call        isspace (40DB7Ch) 
0040495C 83 C4 04         add         esp,4 
0040495F 85 C0            test        eax,eax 
00404961 75 02            jne         strcrop+65h (404965h) 
            break ;
00404963 EB 0B            jmp         strcrop+70h (404970h) 
        last -- ;
00404965 8B 45 FC         mov         eax,dword ptr [last] 
00404968 83 E8 01         sub         eax,1 
0040496B 89 45 FC         mov         dword ptr [last],eax 
    }
0040496E EB D6            jmp         strcrop+46h (404946h) 
    *last = (char)0;
00404970 8B 4D FC         mov         ecx,dword ptr [last] 
00404973 C6 01 00         mov         byte ptr [ecx],0 
    return l ;
00404976 B8 00 64 41 00   mov         eax,offset l (416400h) 
}
0040497B 8B E5            mov         esp,ebp 
0040497D 5D               pop         ebp  
0040497E C2 04 00         ret         4    
--- No source file -------------------------------------------------------------

--- f:\eclipse\workspace\winrun4j\src\common\dictionary.cpp --------------------



char * strstrip(char * s)
{
00404990 55               push        ebp  
00404991 8B EC            mov         ebp,esp 
00404993 51               push        ecx  
    static char l[ASCIILINESZ+1];
    char * last ;
    
    if (s==NULL) return NULL ;
00404994 83 7D 08 00      cmp         dword ptr [s],0 
00404998 75 07            jne         strstrip+11h (4049A1h) 
0040499A 33 C0            xor         eax,eax 
0040499C E9 95 00 00 00   jmp         strstrip+0A6h (404A36h) 
    
    while (isspace((int)*s) && *s) s++;
004049A1 8B 45 08         mov         eax,dword ptr [s] 
004049A4 0F BE 08         movsx       ecx,byte ptr [eax] 
004049A7 51               push        ecx  
004049A8 E8 CF 91 00 00   call        isspace (40DB7Ch) 
004049AD 83 C4 04         add         esp,4 
004049B0 85 C0            test        eax,eax 
004049B2 74 15            je          strstrip+39h (4049C9h) 
004049B4 8B 55 08         mov         edx,dword ptr [s] 
004049B7 0F BE 02         movsx       eax,byte ptr [edx] 
004049BA 85 C0            test        eax,eax 
004049BC 74 0B            je          strstrip+39h (4049C9h) 
004049BE 8B 4D 08         mov         ecx,dword ptr [s] 
004049C1 83 C1 01         add         ecx,1 
004049C4 89 4D 08         mov         dword ptr [s],ecx 
004049C7 EB D8            jmp         strstrip+11h (4049A1h) 
    
    memset(l, 0, ASCIILINESZ+1);
004049C9 68 01 04 00 00   push        401h 
004049CE 6A 00            push        0    
004049D0 68 D0 68 41 00   push        offset l (4168D0h) 
004049D5 E8 90 91 00 00   call        memset (40DB6Ah) 
004049DA 83 C4 0C         add         esp,0Ch 
    strcpy(l, s);
004049DD 8B 55 08         mov         edx,dword ptr [s] 
004049E0 52               push        edx  
004049E1 68 D0 68 41 00   push        offset l (4168D0h) 
004049E6 E8 05 91 00 00   call        strcpy (40DAF0h) 
004049EB 83 C4 08         add         esp,8 
    last = l + strlen(l);
004049EE 68 D0 68 41 00   push        offset l (4168D0h) 
004049F3 FF 15 68 86 41 00 call        dword ptr [__imp__lstrlenA@4 (418668h)] 
004049F9 05 D0 68 41 00   add         eax,offset l (4168D0h) 
004049FE 89 45 FC         mov         dword ptr [last],eax 
    while (last > l) {
00404A01 81 7D FC D0 68 41 00 cmp         dword ptr [last],offset l (4168D0h) 
00404A08 76 21            jbe         strstrip+9Bh (404A2Bh) 
        if (!isspace((int)*(last-1)))
00404A0A 8B 45 FC         mov         eax,dword ptr [last] 
00404A0D 0F BE 48 FF      movsx       ecx,byte ptr [eax-1] 
00404A11 51               push        ecx  
00404A12 E8 65 91 00 00   call        isspace (40DB7Ch) 
00404A17 83 C4 04         add         esp,4 
00404A1A 85 C0            test        eax,eax 
00404A1C 75 02            jne         strstrip+90h (404A20h) 
            break ;
00404A1E EB 0B            jmp         strstrip+9Bh (404A2Bh) 
        last -- ;
00404A20 8B 55 FC         mov         edx,dword ptr [last] 
00404A23 83 EA 01         sub         edx,1 
00404A26 89 55 FC         mov         dword ptr [last],edx 
    }
00404A29 EB D6            jmp         strstrip+71h (404A01h) 
    *last = (char)0;
00404A2B 8B 45 FC         mov         eax,dword ptr [last] 
00404A2E C6 00 00         mov         byte ptr [eax],0 

    return (char*)l ;
00404A31 B8 D0 68 41 00   mov         eax,offset l (4168D0h) 
}
00404A36 8B E5            mov         esp,ebp 
00404A38 5D               pop         ebp  
00404A39 C2 04 00         ret         4    
--- No source file -------------------------------------------------------------
00404A3C CC               int         3    
00404A3D CC               int         3    
00404A3E CC               int         3    
00404A3F CC               int         3    
--- f:\eclipse\workspace\winrun4j\src\common\dictionary.cpp --------------------

char* sgets(char* buffer, int* pos, char * line, int maxsize)
{
00404A40 55               push        ebp  
00404A41 8B EC            mov         ebp,esp 
00404A43 51               push        ecx  
    if(buffer[*pos] == 0) return NULL;
00404A44 8B 45 0C         mov         eax,dword ptr [pos] 
00404A47 8B 08            mov         ecx,dword ptr [eax] 
00404A49 8B 55 08         mov         edx,dword ptr [buffer] 
00404A4C 0F BE 04 0A      movsx       eax,byte ptr [edx+ecx] 
00404A50 85 C0            test        eax,eax 
00404A52 75 07            jne         sgets+1Bh (404A5Bh) 
00404A54 33 C0            xor         eax,eax 
00404A56 E9 81 00 00 00   jmp         sgets+9Ch (404ADCh) 
    int i = *pos;
00404A5B 8B 4D 0C         mov         ecx,dword ptr [pos] 
00404A5E 8B 11            mov         edx,dword ptr [ecx] 
00404A60 89 55 FC         mov         dword ptr [i],edx 
00404A63 EB 09            jmp         sgets+2Eh (404A6Eh) 
    for(; i < maxsize; i++) {
00404A65 8B 45 FC         mov         eax,dword ptr [i] 
00404A68 83 C0 01         add         eax,1 
00404A6B 89 45 FC         mov         dword ptr [i],eax 
00404A6E 8B 4D FC         mov         ecx,dword ptr [i] 
00404A71 3B 4D 14         cmp         ecx,dword ptr [maxsize] 
00404A74 7D 1F            jge         sgets+55h (404A95h) 
        if(buffer[i] == '\n' || buffer[i] == 0)
00404A76 8B 55 08         mov         edx,dword ptr [buffer] 
00404A79 03 55 FC         add         edx,dword ptr [i] 
00404A7C 0F BE 02         movsx       eax,byte ptr [edx] 
00404A7F 83 F8 0A         cmp         eax,0Ah 
00404A82 74 0D            je          sgets+51h (404A91h) 
00404A84 8B 4D 08         mov         ecx,dword ptr [buffer] 
00404A87 03 4D FC         add         ecx,dword ptr [i] 
00404A8A 0F BE 11         movsx       edx,byte ptr [ecx] 
00404A8D 85 D2            test        edx,edx 
00404A8F 75 02            jne         sgets+53h (404A93h) 
            break;
00404A91 EB 02            jmp         sgets+55h (404A95h) 
    }
00404A93 EB D0            jmp         sgets+25h (404A65h) 
    memcpy(line, &buffer[*pos], i - (*pos));
00404A95 8B 45 0C         mov         eax,dword ptr [pos] 
00404A98 8B 4D FC         mov         ecx,dword ptr [i] 
00404A9B 2B 08            sub         ecx,dword ptr [eax] 
00404A9D 51               push        ecx  
00404A9E 8B 55 0C         mov         edx,dword ptr [pos] 
00404AA1 8B 45 08         mov         eax,dword ptr [buffer] 
00404AA4 03 02            add         eax,dword ptr [edx] 
00404AA6 50               push        eax  
00404AA7 8B 4D 10         mov         ecx,dword ptr [line] 
00404AAA 51               push        ecx  
00404AAB E8 6A 90 00 00   call        memcpy (40DB1Ah) 
00404AB0 83 C4 0C         add         esp,0Ch 
    line[i - (*pos)] = 0;
00404AB3 8B 55 0C         mov         edx,dword ptr [pos] 
00404AB6 8B 45 FC         mov         eax,dword ptr [i] 
00404AB9 2B 02            sub         eax,dword ptr [edx] 
00404ABB 8B 4D 10         mov         ecx,dword ptr [line] 
00404ABE C6 04 01 00      mov         byte ptr [ecx+eax],0 
    *pos = i + (buffer[i] == 0 ? 0 : 1);
00404AC2 8B 55 08         mov         edx,dword ptr [buffer] 
00404AC5 03 55 FC         add         edx,dword ptr [i] 
00404AC8 0F BE 02         movsx       eax,byte ptr [edx] 
00404ACB F7 D8            neg         eax  
00404ACD 1B C0            sbb         eax,eax 
00404ACF F7 D8            neg         eax  
00404AD1 03 45 FC         add         eax,dword ptr [i] 
00404AD4 8B 4D 0C         mov         ecx,dword ptr [pos] 
00404AD7 89 01            mov         dword ptr [ecx],eax 
    return line;
00404AD9 8B 45 10         mov         eax,dword ptr [line] 
}
00404ADC 8B E5            mov         esp,ebp 
00404ADE 5D               pop         ebp  
00404ADF C2 10 00         ret         10h  
--- No source file -------------------------------------------------------------

--- f:\eclipse\workspace\winrun4j\src\common\ini.cpp ---------------------------
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
#include "../java/JNI.h"

static dictionary* g_ini = NULL;

void INI::GetNumberedKeysFromIni(dictionary* ini, TCHAR* keyName, TCHAR** entries, int& index)
{
00404FF0 55               push        ebp  
00404FF1 8B EC            mov         ebp,esp 
00404FF3 81 EC 14 01 00 00 sub         esp,114h 
    int i = 0;
00404FF9 C7 45 FC 00 00 00 00 mov         dword ptr [i],0 
    TCHAR entryName[MAX_PATH];
    while(true) {
00405000 B8 01 00 00 00   mov         eax,1 
00405005 85 C0            test        eax,eax 
00405007 0F 84 84 00 00 00 je          INI::GetNumberedKeysFromIni+0A1h (405091h) 
        sprintf(entryName, "%s.%d", keyName, i+1);
0040500D 8B 4D FC         mov         ecx,dword ptr [i] 
00405010 83 C1 01         add         ecx,1 
00405013 51               push        ecx  
00405014 8B 55 0C         mov         edx,dword ptr [keyName] 
00405017 52               push        edx  
00405018 68 F4 0F 41 00   push        410FF4h 
0040501D 8D 85 F0 FE FF FF lea         eax,[entryName] 
00405023 50               push        eax  
00405024 E8 EB 8A 00 00   call        sprintf (40DB14h) 
00405029 83 C4 10         add         esp,10h 
        TCHAR* entry = iniparser_getstr(ini, entryName);
0040502C 8D 8D F0 FE FF FF lea         ecx,[entryName] 
00405032 51               push        ecx  
00405033 8B 55 08         mov         edx,dword ptr [ini] 
00405036 52               push        edx  
00405037 E8 58 C2 FF FF   call        iniparser_getstr (401294h) 
0040503C 89 85 EC FE FF FF mov         dword ptr [entry],eax 
        if(entry != NULL) {
00405042 83 BD EC FE FF FF 00 cmp         dword ptr [entry],0 
00405049 74 27            je          INI::GetNumberedKeysFromIni+82h (405072h) 
            entries[index++] = _strdup(entry);
0040504B 8B 85 EC FE FF FF mov         eax,dword ptr [entry] 
00405051 50               push        eax  
00405052 E8 29 C2 FF FF   call        @ILT+635(__strdup) (401280h) 
00405057 83 C4 04         add         esp,4 
0040505A 8B 4D 14         mov         ecx,dword ptr [index] 
0040505D 8B 11            mov         edx,dword ptr [ecx] 
0040505F 8B 4D 10         mov         ecx,dword ptr [entries] 
00405062 89 04 91         mov         dword ptr [ecx+edx*4],eax 
00405065 8B 55 14         mov         edx,dword ptr [index] 
00405068 8B 02            mov         eax,dword ptr [edx] 
0040506A 83 C0 01         add         eax,1 
0040506D 8B 4D 14         mov         ecx,dword ptr [index] 
00405070 89 01            mov         dword ptr [ecx],eax 
        }
        i++;
00405072 8B 55 FC         mov         edx,dword ptr [i] 
00405075 83 C2 01         add         edx,1 
00405078 89 55 FC         mov         dword ptr [i],edx 
        if(i > 10 && entry == NULL) {
0040507B 83 7D FC 0A      cmp         dword ptr [i],0Ah 
0040507F 7E 0B            jle         INI::GetNumberedKeysFromIni+9Ch (40508Ch) 
00405081 83 BD EC FE FF FF 00 cmp         dword ptr [entry],0 
00405088 75 02            jne         INI::GetNumberedKeysFromIni+9Ch (40508Ch) 
            break;
0040508A EB 05            jmp         INI::GetNumberedKeysFromIni+0A1h (405091h) 
        }
    }
0040508C E9 6F FF FF FF   jmp         INI::GetNumberedKeysFromIni+10h (405000h) 
    entries[index] = NULL;
00405091 8B 45 14         mov         eax,dword ptr [index] 
00405094 8B 08            mov         ecx,dword ptr [eax] 
00405096 8B 55 10         mov         edx,dword ptr [entries] 
00405099 C7 04 8A 00 00 00 00 mov         dword ptr [edx+ecx*4],0 
}
004050A0 8B E5            mov         esp,ebp 
004050A2 5D               pop         ebp  
004050A3 C2 10 00         ret         10h  
--- No source file -------------------------------------------------------------

--- f:\eclipse\workspace\winrun4j\src\common\ini.cpp ---------------------------

/* The ini filename is in the same directory as the executable and called the same (except with ini at the end). */
dictionary* INI::LoadIniFile(HINSTANCE hInstance)
{
004050B0 55               push        ebp  
004050B1 8B EC            mov         ebp,esp 
004050B3 81 EC 18 02 00 00 sub         esp,218h 
    TCHAR filename[MAX_PATH], inifile[MAX_PATH];
    GetModuleFileName(hInstance, filename, sizeof(filename));
004050B9 68 04 01 00 00   push        104h 
004050BE 8D 85 E8 FD FF FF lea         eax,[filename] 
004050C4 50               push        eax  
004050C5 8B 4D 08         mov         ecx,dword ptr [hInstance] 
004050C8 51               push        ecx  
004050C9 FF 15 6C 86 41 00 call        dword ptr [__imp__GetModuleFileNameA@12 (41866Ch)] 
    strcpy(inifile, filename);
004050CF 8D 95 E8 FD FF FF lea         edx,[filename] 
004050D5 52               push        edx  
004050D6 8D 85 F0 FE FF FF lea         eax,[inifile] 
004050DC 50               push        eax  
004050DD E8 0E 8A 00 00   call        strcpy (40DAF0h) 
004050E2 83 C4 08         add         esp,8 
    int len = strlen(inifile);
004050E5 8D 8D F0 FE FF FF lea         ecx,[inifile] 
004050EB 51               push        ecx  
004050EC E8 0B 8A 00 00   call        strlen (40DAFCh) 
004050F1 83 C4 04         add         esp,4 
004050F4 89 45 FC         mov         dword ptr [len],eax 
    // It is assumed the executable ends with "exe"
    inifile[len - 1] = 'i';
004050F7 8B 55 FC         mov         edx,dword ptr [len] 
004050FA C6 84 15 EF FE FF FF 69 mov         byte ptr [ebp+edx-111h],69h 
    inifile[len - 2] = 'n';
00405102 8B 45 FC         mov         eax,dword ptr [len] 
00405105 C6 84 05 EE FE FF FF 6E mov         byte ptr [ebp+eax-112h],6Eh 
    inifile[len - 3] = 'i';
0040510D 8B 4D FC         mov         ecx,dword ptr [len] 
00405110 C6 84 0D ED FE FF FF 69 mov         byte ptr [ebp+ecx-113h],69h 

    return LoadIniFile(hInstance, inifile);
00405118 8D 95 F0 FE FF FF lea         edx,[inifile] 
0040511E 52               push        edx  
0040511F 8B 45 08         mov         eax,dword ptr [hInstance] 
00405122 50               push        eax  
00405123 E8 59 C0 FF FF   call        INI::LoadIniFile (401181h) 
}
00405128 8B E5            mov         esp,ebp 
0040512A 5D               pop         ebp  
0040512B C2 04 00         ret         4    
--- No source file -------------------------------------------------------------
0040512E CC               int         3    
0040512F CC               int         3    
--- f:\eclipse\workspace\winrun4j\src\common\ini.cpp ---------------------------

dictionary* INI::LoadIniFile(HINSTANCE hInstance, LPSTR inifile)
{
00405130 55               push        ebp  
00405131 8B EC            mov         ebp,esp 
00405133 81 EC 34 02 00 00 sub         esp,234h 
    dictionary* ini = NULL;
00405139 C7 85 F0 FE FF FF 00 00 00 00 mov         dword ptr [ini],0 

    // First attempt to load INI from exe
    HRSRC hi = FindResource(hInstance, MAKEINTRESOURCE(1), RT_INI_FILE);
00405143 68 AF 02 00 00   push        2AFh 
00405148 6A 01            push        1    
0040514A 8B 45 08         mov         eax,dword ptr [hInstance] 
0040514D 50               push        eax  
0040514E FF 15 5C 86 41 00 call        dword ptr [__imp__FindResourceA@12 (41865Ch)] 
00405154 89 85 F4 FE FF FF mov         dword ptr [hi],eax 
    if(hi) {
0040515A 83 BD F4 FE FF FF 00 cmp         dword ptr [hi],0 
00405161 74 71            je          INI::LoadIniFile+0A4h (4051D4h) 
        HGLOBAL hg = LoadResource(hInstance, hi);
00405163 8B 8D F4 FE FF FF mov         ecx,dword ptr [hi] 
00405169 51               push        ecx  
0040516A 8B 55 08         mov         edx,dword ptr [hInstance] 
0040516D 52               push        edx  
0040516E FF 15 60 86 41 00 call        dword ptr [__imp__LoadResource@8 (418660h)] 
00405174 89 85 E0 FD FF FF mov         dword ptr [hg],eax 
        PBYTE pb = (PBYTE) LockResource(hg);
0040517A 8B 85 E0 FD FF FF mov         eax,dword ptr [hg] 
00405180 50               push        eax  
00405181 FF 15 64 86 41 00 call        dword ptr [__imp__LockResource@4 (418664h)] 
00405187 89 85 DC FD FF FF mov         dword ptr [pb],eax 
        DWORD* pd = (DWORD*) pb;
0040518D 8B 8D DC FD FF FF mov         ecx,dword ptr [pb] 
00405193 89 8D E4 FD FF FF mov         dword ptr [pd],ecx 
        if(*pd == INI_RES_MAGIC) {
00405199 8B 95 E4 FD FF FF mov         edx,dword ptr [pd] 
0040519F 81 3A 49 4E 49 20 cmp         dword ptr [edx],20494E49h 
004051A5 75 2D            jne         INI::LoadIniFile+0A4h (4051D4h) 
            ini = iniparser_load((char *) &pb[RES_MAGIC_SIZE], true);   
004051A7 6A 01            push        1    
004051A9 8B 85 DC FD FF FF mov         eax,dword ptr [pb] 
004051AF 83 C0 04         add         eax,4 
004051B2 50               push        eax  
004051B3 E8 22 C1 FF FF   call        iniparser_load (4012DAh) 
004051B8 89 85 F0 FE FF FF mov         dword ptr [ini],eax 
            if(!ini) {
004051BE 83 BD F0 FE FF FF 00 cmp         dword ptr [ini],0 
004051C5 75 0D            jne         INI::LoadIniFile+0A4h (4051D4h) 
                Log::Warning("Could not load embedded INI file");
004051C7 68 FC 0F 41 00   push        410FFCh 
004051CC E8 5D C2 FF FF   call        Log::Warning (40142Eh) 
004051D1 83 C4 04         add         esp,4 
            }
        }
    }

    // Check if we have already loaded an embedded INI file - if so 
    // then we only need to load and merge the INI file (if present)
    if(ini) {
004051D4 83 BD F0 FE FF FF 00 cmp         dword ptr [ini],0 
004051DB 0F 84 94 00 00 00 je          INI::LoadIniFile+145h (405275h) 
        dictionary* ini2 = iniparser_load(inifile);
004051E1 6A 00            push        0    
004051E3 8B 4D 0C         mov         ecx,dword ptr [inifile] 
004051E6 51               push        ecx  
004051E7 E8 EE C0 FF FF   call        iniparser_load (4012DAh) 
004051EC 89 85 D8 FD FF FF mov         dword ptr [ini2],eax 
        if(ini2) {
004051F2 83 BD D8 FD FF FF 00 cmp         dword ptr [ini2],0 
004051F9 74 78            je          INI::LoadIniFile+143h (405273h) 
            for(int i = 0; i < ini2->size; i++) {
004051FB C7 85 D4 FD FF FF 00 00 00 00 mov         dword ptr [i],0 
00405205 EB 0F            jmp         INI::LoadIniFile+0E6h (405216h) 
00405207 8B 95 D4 FD FF FF mov         edx,dword ptr [i] 
0040520D 83 C2 01         add         edx,1 
00405210 89 95 D4 FD FF FF mov         dword ptr [i],edx 
00405216 8B 85 D8 FD FF FF mov         eax,dword ptr [ini2] 
0040521C 8B 8D D4 FD FF FF mov         ecx,dword ptr [i] 
00405222 3B 48 04         cmp         ecx,dword ptr [eax+4] 
00405225 7D 4C            jge         INI::LoadIniFile+143h (405273h) 
                char* key = ini2->key[i];
00405227 8B 95 D8 FD FF FF mov         edx,dword ptr [ini2] 
0040522D 8B 42 0C         mov         eax,dword ptr [edx+0Ch] 
00405230 8B 8D D4 FD FF FF mov         ecx,dword ptr [i] 
00405236 8B 14 88         mov         edx,dword ptr [eax+ecx*4] 
00405239 89 95 CC FD FF FF mov         dword ptr [key],edx 
                char* value = ini2->val[i];
0040523F 8B 85 D8 FD FF FF mov         eax,dword ptr [ini2] 
00405245 8B 48 08         mov         ecx,dword ptr [eax+8] 
00405248 8B 95 D4 FD FF FF mov         edx,dword ptr [i] 
0040524E 8B 04 91         mov         eax,dword ptr [ecx+edx*4] 
00405251 89 85 D0 FD FF FF mov         dword ptr [value],eax 
                iniparser_setstr(ini, key, value);
00405257 8B 8D D0 FD FF FF mov         ecx,dword ptr [value] 
0040525D 51               push        ecx  
0040525E 8B 95 CC FD FF FF mov         edx,dword ptr [key] 
00405264 52               push        edx  
00405265 8B 85 F0 FE FF FF mov         eax,dword ptr [ini] 
0040526B 50               push        eax  
0040526C E8 D2 C0 FF FF   call        iniparser_setstr (401343h) 
            }       
00405271 EB 94            jmp         INI::LoadIniFile+0D7h (405207h) 
        }
    } else {
00405273 EB 32            jmp         INI::LoadIniFile+177h (4052A7h) 
        ini = iniparser_load(inifile);
00405275 6A 00            push        0    
00405277 8B 4D 0C         mov         ecx,dword ptr [inifile] 
0040527A 51               push        ecx  
0040527B E8 5A C0 FF FF   call        iniparser_load (4012DAh) 
00405280 89 85 F0 FE FF FF mov         dword ptr [ini],eax 
        if(ini == NULL) {
00405286 83 BD F0 FE FF FF 00 cmp         dword ptr [ini],0 
0040528D 75 18            jne         INI::LoadIniFile+177h (4052A7h) 
            Log::Error("Could not load INI file: %s", inifile);
0040528F 8B 55 0C         mov         edx,dword ptr [inifile] 
00405292 52               push        edx  
00405293 68 20 10 41 00   push        411020h 
00405298 E8 0F C1 FF FF   call        Log::Error (4013ACh) 
0040529D 83 C4 08         add         esp,8 
            return NULL;
004052A0 33 C0            xor         eax,eax 
004052A2 E9 3B 01 00 00   jmp         INI::LoadIniFile+2B2h (4053E2h) 
        }
    }

    // Expand environment variables
    ExpandVariables(ini);
004052A7 8B 85 F0 FE FF FF mov         eax,dword ptr [ini] 
004052AD 50               push        eax  
004052AE E8 18 C0 FF FF   call        INI::ExpandVariables (4012CBh) 

    iniparser_setstr(ini, MODULE_INI, inifile);
004052B3 8B 4D 0C         mov         ecx,dword ptr [inifile] 
004052B6 51               push        ecx  
004052B7 68 3C 10 41 00   push        41103Ch 
004052BC 8B 95 F0 FE FF FF mov         edx,dword ptr [ini] 
004052C2 52               push        edx  
004052C3 E8 7B C0 FF FF   call        iniparser_setstr (401343h) 

    // Add module name to ini
    TCHAR filename[MAX_PATH], filedir[MAX_PATH];
    GetModuleFileName(hInstance, filename, MAX_PATH);
004052C8 68 04 01 00 00   push        104h 
004052CD 8D 85 E8 FD FF FF lea         eax,[filename] 
004052D3 50               push        eax  
004052D4 8B 4D 08         mov         ecx,dword ptr [hInstance] 
004052D7 51               push        ecx  
004052D8 FF 15 6C 86 41 00 call        dword ptr [__imp__GetModuleFileNameA@12 (41866Ch)] 
    iniparser_setstr(ini, MODULE_NAME, filename);
004052DE 8D 95 E8 FD FF FF lea         edx,[filename] 
004052E4 52               push        edx  
004052E5 68 50 10 41 00   push        411050h 
004052EA 8B 85 F0 FE FF FF mov         eax,dword ptr [ini] 
004052F0 50               push        eax  
004052F1 E8 4D C0 FF FF   call        iniparser_setstr (401343h) 

    // strip off filename to get module directory
    GetFileDirectory(filename, filedir);
004052F6 8D 8D F8 FE FF FF lea         ecx,[filedir] 
004052FC 51               push        ecx  
004052FD 8D 95 E8 FD FF FF lea         edx,[filename] 
00405303 52               push        edx  
00405304 E8 DB BF FF FF   call        GetFileDirectory (4012E4h) 
00405309 83 C4 08         add         esp,8 
    iniparser_setstr(ini, MODULE_DIR, filedir);
0040530C 8D 85 F8 FE FF FF lea         eax,[filedir] 
00405312 50               push        eax  
00405313 68 64 10 41 00   push        411064h 
00405318 8B 8D F0 FE FF FF mov         ecx,dword ptr [ini] 
0040531E 51               push        ecx  
0040531F E8 1F C0 FF FF   call        iniparser_setstr (401343h) 

    // stip off filename to get ini directory
    GetFileDirectory(inifile, filedir);
00405324 8D 95 F8 FE FF FF lea         edx,[filedir] 
0040532A 52               push        edx  
0040532B 8B 45 0C         mov         eax,dword ptr [inifile] 
0040532E 50               push        eax  
0040532F E8 B0 BF FF FF   call        GetFileDirectory (4012E4h) 
00405334 83 C4 08         add         esp,8 
    iniparser_setstr(ini, INI_DIR, filedir);
00405337 8D 8D F8 FE FF FF lea         ecx,[filedir] 
0040533D 51               push        ecx  
0040533E 68 78 10 41 00   push        411078h 
00405343 8B 95 F0 FE FF FF mov         edx,dword ptr [ini] 
00405349 52               push        edx  
0040534A E8 F4 BF FF FF   call        iniparser_setstr (401343h) 

    // Log init
    Log::Init(hInstance, iniparser_getstr(ini, LOG_FILE), iniparser_getstr(ini, LOG_LEVEL), ini);
0040534F 8B 85 F0 FE FF FF mov         eax,dword ptr [ini] 
00405355 50               push        eax  
00405356 68 88 10 41 00   push        411088h 
0040535B 8B 8D F0 FE FF FF mov         ecx,dword ptr [ini] 
00405361 51               push        ecx  
00405362 E8 2D BF FF FF   call        iniparser_getstr (401294h) 
00405367 50               push        eax  
00405368 68 94 10 41 00   push        411094h 
0040536D 8B 95 F0 FE FF FF mov         edx,dword ptr [ini] 
00405373 52               push        edx  
00405374 E8 1B BF FF FF   call        iniparser_getstr (401294h) 
00405379 50               push        eax  
0040537A 8B 45 08         mov         eax,dword ptr [hInstance] 
0040537D 50               push        eax  
0040537E E8 5D BE FF FF   call        Log::Init (4011E0h) 
    Log::Info("Module Name: %s", filename);
00405383 8D 8D E8 FD FF FF lea         ecx,[filename] 
00405389 51               push        ecx  
0040538A 68 9C 10 41 00   push        41109Ch 
0040538F E8 9D BD FF FF   call        Log::Info (401131h) 
00405394 83 C4 08         add         esp,8 
    Log::Info("Module INI: %s", inifile);
00405397 8B 55 0C         mov         edx,dword ptr [inifile] 
0040539A 52               push        edx  
0040539B 68 AC 10 41 00   push        4110ACh 
004053A0 E8 8C BD FF FF   call        Log::Info (401131h) 
004053A5 83 C4 08         add         esp,8 
    Log::Info("Module Dir: %s", filedir);
004053A8 8D 85 F8 FE FF FF lea         eax,[filedir] 
004053AE 50               push        eax  
004053AF 68 BC 10 41 00   push        4110BCh 
004053B4 E8 78 BD FF FF   call        Log::Info (401131h) 
004053B9 83 C4 08         add         esp,8 
    Log::Info("INI Dir: %s", filedir);
004053BC 8D 8D F8 FE FF FF lea         ecx,[filedir] 
004053C2 51               push        ecx  
004053C3 68 CC 10 41 00   push        4110CCh 
004053C8 E8 64 BD FF FF   call        Log::Info (401131h) 
004053CD 83 C4 08         add         esp,8 

    // Store a reference to be used by JNI functions
    g_ini = ini;
004053D0 8B 95 F0 FE FF FF mov         edx,dword ptr [ini] 
004053D6 89 15 A0 6D 41 00 mov         dword ptr [g_ini (416DA0h)],edx 

    return ini;
004053DC 8B 85 F0 FE FF FF mov         eax,dword ptr [ini] 
}
004053E2 8B E5            mov         esp,ebp 
004053E4 5D               pop         ebp  
004053E5 C2 08 00         ret         8    
--- No source file -------------------------------------------------------------
--- f:\eclipse\workspace\winrun4j\src\common\ini.cpp ---------------------------

void INI::ExpandVariables(dictionary* ini)
{
004053F0 55               push        ebp  
004053F1 8B EC            mov         ebp,esp 
004053F3 B8 10 10 00 00   mov         eax,1010h 
004053F8 E8 93 87 00 00   call        _chkstk (40DB90h) 
    char tmp[4096];
    for(int i = 0; i < ini->size; i++) {
004053FD C7 85 FC EF FF FF 00 00 00 00 mov         dword ptr [i],0 
00405407 EB 0F            jmp         INI::ExpandVariables+28h (405418h) 
00405409 8B 85 FC EF FF FF mov         eax,dword ptr [i] 
0040540F 83 C0 01         add         eax,1 
00405412 89 85 FC EF FF FF mov         dword ptr [i],eax 
00405418 8B 4D 08         mov         ecx,dword ptr [ini] 
0040541B 8B 95 FC EF FF FF mov         edx,dword ptr [i] 
00405421 3B 51 04         cmp         edx,dword ptr [ecx+4] 
00405424 0F 8D 82 00 00 00 jge         INI::ExpandVariables+0BCh (4054ACh) 
        char* key = ini->key[i];
0040542A 8B 45 08         mov         eax,dword ptr [ini] 
0040542D 8B 48 0C         mov         ecx,dword ptr [eax+0Ch] 
00405430 8B 95 FC EF FF FF mov         edx,dword ptr [i] 
00405436 8B 04 91         mov         eax,dword ptr [ecx+edx*4] 
00405439 89 85 F4 EF FF FF mov         dword ptr [key],eax 
        char* value = ini->val[i];
0040543F 8B 4D 08         mov         ecx,dword ptr [ini] 
00405442 8B 51 08         mov         edx,dword ptr [ecx+8] 
00405445 8B 85 FC EF FF FF mov         eax,dword ptr [i] 
0040544B 8B 0C 82         mov         ecx,dword ptr [edx+eax*4] 
0040544E 89 8D F8 EF FF FF mov         dword ptr [value],ecx 
        int size = ExpandEnvironmentStrings(value, tmp, 4096);
00405454 68 00 10 00 00   push        1000h 
00405459 8D 95 00 F0 FF FF lea         edx,[tmp] 
0040545F 52               push        edx  
00405460 8B 85 F8 EF FF FF mov         eax,dword ptr [value] 
00405466 50               push        eax  
00405467 FF 15 10 87 41 00 call        dword ptr [__imp__ExpandEnvironmentStringsA@12 (418710h)] 
0040546D 89 85 F0 EF FF FF mov         dword ptr [size],eax 
        if(size == 0) {
00405473 83 BD F0 EF FF FF 00 cmp         dword ptr [size],0 
0040547A 75 14            jne         INI::ExpandVariables+0A0h (405490h) 
            Log::Warning("Could not expand variable: %s", value);
0040547C 8B 8D F8 EF FF FF mov         ecx,dword ptr [value] 
00405482 51               push        ecx  
00405483 68 D8 10 41 00   push        4110D8h 
00405488 E8 A1 BF FF FF   call        Log::Warning (40142Eh) 
0040548D 83 C4 08         add         esp,8 
        }
        iniparser_setstr(ini, key, tmp);
00405490 8D 95 00 F0 FF FF lea         edx,[tmp] 
00405496 52               push        edx  
00405497 8B 85 F4 EF FF FF mov         eax,dword ptr [key] 
0040549D 50               push        eax  
0040549E 8B 4D 08         mov         ecx,dword ptr [ini] 
004054A1 51               push        ecx  
004054A2 E8 9C BE FF FF   call        iniparser_setstr (401343h) 
004054A7 E9 5D FF FF FF   jmp         INI::ExpandVariables+19h (405409h) 
    }
}
004054AC 8B E5            mov         esp,ebp 
004054AE 5D               pop         ebp  
004054AF C2 04 00         ret         4    
--- No source file -------------------------------------------------------------
--- f:\eclipse\workspace\winrun4j\src\common\ini.cpp ---------------------------

#ifndef NO_JAVA
jobjectArray INI::GetKeys(JNIEnv* env, jobject self)
{
004054C0 55               push        ebp  
004054C1 8B EC            mov         ebp,esp 
004054C3 83 EC 0C         sub         esp,0Ch 
    jclass clazz = env->FindClass("java/lang/String");
004054C6 68 F8 10 41 00   push        4110F8h 
004054CB 8B 4D 08         mov         ecx,dword ptr [env] 
004054CE E8 64 BB FF FF   call        JNIEnv_::FindClass (401037h) 
004054D3 89 45 FC         mov         dword ptr [clazz],eax 
    jobjectArray keys = env->NewObjectArray(g_ini->n, clazz, NULL);
004054D6 6A 00            push        0    
004054D8 8B 45 FC         mov         eax,dword ptr [clazz] 
004054DB 50               push        eax  
004054DC 8B 0D A0 6D 41 00 mov         ecx,dword ptr [g_ini (416DA0h)] 
004054E2 8B 11            mov         edx,dword ptr [ecx] 
004054E4 52               push        edx  
004054E5 8B 4D 08         mov         ecx,dword ptr [env] 
004054E8 E8 95 BB FF FF   call        JNIEnv_::NewObjectArray (401082h) 
004054ED 89 45 F8         mov         dword ptr [keys],eax 
    for(int i = 0; i < g_ini->n; i++) {
004054F0 C7 45 F4 00 00 00 00 mov         dword ptr [i],0 
004054F7 EB 09            jmp         INI::GetKeys+42h (405502h) 
004054F9 8B 45 F4         mov         eax,dword ptr [i] 
004054FC 83 C0 01         add         eax,1 
004054FF 89 45 F4         mov         dword ptr [i],eax 
00405502 8B 0D A0 6D 41 00 mov         ecx,dword ptr [g_ini (416DA0h)] 
00405508 8B 55 F4         mov         edx,dword ptr [i] 
0040550B 3B 11            cmp         edx,dword ptr [ecx] 
0040550D 7D 2A            jge         INI::GetKeys+79h (405539h) 
        env->SetObjectArrayElement(keys, i, env->NewStringUTF(g_ini->key[i]));
0040550F A1 A0 6D 41 00   mov         eax,dword ptr [g_ini (416DA0h)] 
00405514 8B 48 0C         mov         ecx,dword ptr [eax+0Ch] 
00405517 8B 55 F4         mov         edx,dword ptr [i] 
0040551A 8B 04 91         mov         eax,dword ptr [ecx+edx*4] 
0040551D 50               push        eax  
0040551E 8B 4D 08         mov         ecx,dword ptr [env] 
00405521 E8 3C BD FF FF   call        JNIEnv_::NewStringUTF (401262h) 
00405526 50               push        eax  
00405527 8B 4D F4         mov         ecx,dword ptr [i] 
0040552A 51               push        ecx  
0040552B 8B 55 F8         mov         edx,dword ptr [keys] 
0040552E 52               push        edx  
0040552F 8B 4D 08         mov         ecx,dword ptr [env] 
00405532 E8 BB BE FF FF   call        JNIEnv_::SetObjectArrayElement (4013F2h) 
    }
00405537 EB C0            jmp         INI::GetKeys+39h (4054F9h) 
    return keys;
00405539 8B 45 F8         mov         eax,dword ptr [keys] 
}
0040553C 8B E5            mov         esp,ebp 
0040553E 5D               pop         ebp  
0040553F C2 08 00         ret         8    
--- No source file -------------------------------------------------------------
--- f:\eclipse\workspace\winrun4j\src\common\ini.cpp ---------------------------

jstring INI::GetKey(JNIEnv* env, jobject self, jstring key)
{
00405550 55               push        ebp  
00405551 8B EC            mov         ebp,esp 
00405553 83 EC 10         sub         esp,10h 
    jboolean iscopy = false;
00405556 C6 45 F7 00      mov         byte ptr [iscopy],0 
    const char* keyStr = key ? env->GetStringUTFChars(key, &iscopy) : 0;
0040555A 83 7D 10 00      cmp         dword ptr [key],0 
0040555E 74 15            je          INI::GetKey+25h (405575h) 
00405560 8D 45 F7         lea         eax,[iscopy] 
00405563 50               push        eax  
00405564 8B 4D 10         mov         ecx,dword ptr [key] 
00405567 51               push        ecx  
00405568 8B 4D 08         mov         ecx,dword ptr [env] 
0040556B E8 60 BD FF FF   call        JNIEnv_::GetStringUTFChars (4012D0h) 
00405570 89 45 F0         mov         dword ptr [ebp-10h],eax 
00405573 EB 07            jmp         INI::GetKey+2Ch (40557Ch) 
00405575 C7 45 F0 00 00 00 00 mov         dword ptr [ebp-10h],0 
0040557C 8B 55 F0         mov         edx,dword ptr [ebp-10h] 
0040557F 89 55 F8         mov         dword ptr [keyStr],edx 
    char* value = iniparser_getstr(g_ini, (char*) keyStr);
00405582 8B 45 F8         mov         eax,dword ptr [keyStr] 
00405585 50               push        eax  
00405586 8B 0D A0 6D 41 00 mov         ecx,dword ptr [g_ini (416DA0h)] 
0040558C 51               push        ecx  
0040558D E8 02 BD FF FF   call        iniparser_getstr (401294h) 
00405592 89 45 FC         mov         dword ptr [value],eax 
    if(value == NULL) {
00405595 83 7D FC 00      cmp         dword ptr [value],0 
00405599 75 06            jne         INI::GetKey+51h (4055A1h) 
        return NULL;
0040559B 33 C0            xor         eax,eax 
0040559D EB 0E            jmp         INI::GetKey+5Dh (4055ADh) 
    } else {
0040559F EB 0C            jmp         INI::GetKey+5Dh (4055ADh) 
        return env->NewStringUTF(value);
004055A1 8B 55 FC         mov         edx,dword ptr [value] 
004055A4 52               push        edx  
004055A5 8B 4D 08         mov         ecx,dword ptr [env] 
004055A8 E8 B5 BC FF FF   call        JNIEnv_::NewStringUTF (401262h) 
    }
}
004055AD 8B E5            mov         esp,ebp 
004055AF 5D               pop         ebp  
004055B0 C2 0C 00         ret         0Ch  
--- No source file -------------------------------------------------------------
--- f:\eclipse\workspace\winrun4j\src\common\ini.cpp ---------------------------

bool INI::RegisterNatives(JNIEnv *env, bool useExcel)
{
004055C0 55               push        ebp  
004055C1 8B EC            mov         ebp,esp 
004055C3 83 EC 1C         sub         esp,1Ch 
    Log::Info("Registering natives for INI class");
004055C6 68 0C 11 41 00   push        41110Ch 
004055CB E8 61 BB FF FF   call        Log::Info (401131h) 
004055D0 83 C4 04         add         esp,4 
    jclass clazz;
    JNINativeMethod methods[2];
    if(useExcel) {
004055D3 0F B6 45 0C      movzx       eax,byte ptr [useExcel] 
004055D7 85 C0            test        eax,eax 
004055D9 74 13            je          INI::RegisterNatives+2Eh (4055EEh) 
        clazz = JNI::FindClass(env, "org/excel4j/INI");
004055DB 68 30 11 41 00   push        411130h 
004055E0 8B 4D 08         mov         ecx,dword ptr [env] 
004055E3 51               push        ecx  
004055E4 E8 9E BA FF FF   call        JNI::FindClass (401087h) 
004055E9 89 45 E4         mov         dword ptr [clazz],eax 
    } else {
004055EC EB 11            jmp         INI::RegisterNatives+3Fh (4055FFh) 
        clazz = JNI::FindClass(env, "org/boris/winrun4j/INI");
004055EE 68 40 11 41 00   push        411140h 
004055F3 8B 55 08         mov         edx,dword ptr [env] 
004055F6 52               push        edx  
004055F7 E8 8B BA FF FF   call        JNI::FindClass (401087h) 
004055FC 89 45 E4         mov         dword ptr [clazz],eax 
    }
    if(clazz == NULL) {
004055FF 83 7D E4 00      cmp         dword ptr [clazz],0 
00405603 75 1A            jne         INI::RegisterNatives+5Fh (40561Fh) 
        Log::Warning("Could not find INI class");
00405605 68 58 11 41 00   push        411158h 
0040560A E8 1F BE FF FF   call        Log::Warning (40142Eh) 
0040560F 83 C4 04         add         esp,4 
        JNI::ClearException(env);
00405612 8B 45 08         mov         eax,dword ptr [env] 
00405615 50               push        eax  
00405616 E8 E6 BD FF FF   call        JNI::ClearException (401401h) 
        return false;
0040561B 32 C0            xor         al,al 
0040561D EB 67            jmp         INI::RegisterNatives+0C6h (405686h) 
    }
    methods[0].fnPtr = (void*) GetKeys;
0040561F C7 45 F0 7D 10 40 00 mov         dword ptr [ebp-10h],offset INI::GetKeys (40107Dh) 
    methods[0].name = "getPropertyKeys";
00405626 C7 45 E8 74 11 41 00 mov         dword ptr [methods],411174h 
    methods[0].signature = "()[Ljava/lang/String;";
0040562D C7 45 EC 84 11 41 00 mov         dword ptr [ebp-14h],411184h 
    methods[1].fnPtr = (void*) GetKey;
00405634 C7 45 FC D2 10 40 00 mov         dword ptr [ebp-4],offset INI::GetKey (4010D2h) 
    methods[1].name = "getProperty";
0040563B C7 45 F4 9C 11 41 00 mov         dword ptr [ebp-0Ch],41119Ch 
    methods[1].signature = "(Ljava/lang/String;)Ljava/lang/String;";
00405642 C7 45 F8 A8 11 41 00 mov         dword ptr [ebp-8],4111A8h 
    env->RegisterNatives(clazz, methods, 2);
00405649 6A 02            push        2    
0040564B 8D 4D E8         lea         ecx,[methods] 
0040564E 51               push        ecx  
0040564F 8B 55 E4         mov         edx,dword ptr [clazz] 
00405652 52               push        edx  
00405653 8B 4D 08         mov         ecx,dword ptr [env] 
00405656 E8 9F BA FF FF   call        JNIEnv_::RegisterNatives (4010FAh) 
    if(env->ExceptionCheck()) {
0040565B 8B 4D 08         mov         ecx,dword ptr [env] 
0040565E E8 27 BC FF FF   call        JNIEnv_::ExceptionCheck (40128Ah) 
00405663 0F B6 C0         movzx       eax,al 
00405666 85 C0            test        eax,eax 
00405668 74 1A            je          INI::RegisterNatives+0C4h (405684h) 
        Log::Error("Could not register natives methods for INI class");
0040566A 68 D0 11 41 00   push        4111D0h 
0040566F E8 38 BD FF FF   call        Log::Error (4013ACh) 
00405674 83 C4 04         add         esp,4 
        JNI::PrintStackTrace(env);
00405677 8B 4D 08         mov         ecx,dword ptr [env] 
0040567A 51               push        ecx  
0040567B E8 D2 BC FF FF   call        JNI::PrintStackTrace (401352h) 
        return false;
00405680 32 C0            xor         al,al 
00405682 EB 02            jmp         INI::RegisterNatives+0C6h (405686h) 
    }

    return true;
00405684 B0 01            mov         al,1 
}
00405686 8B E5            mov         esp,ebp 
00405688 5D               pop         ebp  
00405689 C2 08 00         ret         8    
--- No source file ------------------------------------------------------------- 
--- f:\program files\java\jdk1.6.0_05\include\jni.h ----------------------------
    }
    jsize GetStringUTFLength(jstring str) {
        return functions->GetStringUTFLength(this,str);
    }
    const char* GetStringUTFChars(jstring str, jboolean *isCopy) {
        return functions->GetStringUTFChars(this,str,isCopy);
    }
    void ReleaseStringUTFChars(jstring str, const char* chars) {
        functions->ReleaseStringUTFChars(this,str,chars);
    }

    jsize GetArrayLength(jarray array) {
        return functions->GetArrayLength(this,array);
    }

    jobjectArray NewObjectArray(jsize len, jclass clazz,
                jobject init) {
00405840 55               push        ebp  
00405841 8B EC            mov         ebp,esp 
00405843 51               push        ecx  
00405844 89 4D FC         mov         dword ptr [ebp-4],ecx 
        return functions->NewObjectArray(this,len,clazz,init);
00405847 8B 45 10         mov         eax,dword ptr [init] 
0040584A 50               push        eax  
0040584B 8B 4D 0C         mov         ecx,dword ptr [clazz] 
0040584E 51               push        ecx  
0040584F 8B 55 08         mov         edx,dword ptr [len] 
00405852 52               push        edx  
00405853 8B 45 FC         mov         eax,dword ptr [this] 
00405856 50               push        eax  
00405857 8B 4D FC         mov         ecx,dword ptr [this] 
0040585A 8B 11            mov         edx,dword ptr [ecx] 
0040585C 8B 82 B0 02 00 00 mov         eax,dword ptr [edx+2B0h] 
00405862 FF D0            call        eax  
    }
00405864 8B E5            mov         esp,ebp 
00405866 5D               pop         ebp  
00405867 C2 0C 00         ret         0Ch  
--- No source file -------------------------------------------------------------
0040587F CC               int         3    
--- f:\program files\java\jdk1.6.0_05\include\jni.h ----------------------------
    jobject GetObjectArrayElement(jobjectArray array, jsize index) {
        return functions->GetObjectArrayElement(this,array,index);
    }
    void SetObjectArrayElement(jobjectArray array, jsize index,
                   jobject val) {
00405880 55               push        ebp  
00405881 8B EC            mov         ebp,esp 
00405883 51               push        ecx  
00405884 89 4D FC         mov         dword ptr [ebp-4],ecx 
        functions->SetObjectArrayElement(this,array,index,val);
00405887 8B 45 10         mov         eax,dword ptr [val] 
0040588A 50               push        eax  
0040588B 8B 4D 0C         mov         ecx,dword ptr [index] 
0040588E 51               push        ecx  
0040588F 8B 55 08         mov         edx,dword ptr [array] 
00405892 52               push        edx  
00405893 8B 45 FC         mov         eax,dword ptr [this] 
00405896 50               push        eax  
00405897 8B 4D FC         mov         ecx,dword ptr [this] 
0040589A 8B 11            mov         edx,dword ptr [ecx] 
0040589C 8B 82 B8 02 00 00 mov         eax,dword ptr [edx+2B8h] 
004058A2 FF D0            call        eax  
    }
004058A4 8B E5            mov         esp,ebp 
004058A6 5D               pop         ebp  
004058A7 C2 0C 00         ret         0Ch  
--- No source file -------------------------------------------------------------
