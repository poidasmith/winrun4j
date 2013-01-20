
set DIR=%1
set FILE=%2
set OUT=%3

echo dir=%DIR%
echo file=%FILE%
echo out=%OUT%

set CPP=cl.exe -EP
set CFLAGS=-nologo -Zi -D_MD -W3 -DWIN32 -DWINNT -D_WIN32 -D_WINDOWS -D_WINNT -D_WIN32_WINNT=0x0501 -D_WIN32_IE=0x0600 -D_X86_=1 -DDEBUG -D_DEBUG -MDd -Od -Oy-
set AS=ml.exe
set ASFLAGS=-coff -W3 -Cx -Zm -Di386 -DQUIET -D?QUIET

echo %CPP% /I %DIR% %CFLAGS% %DIR%\%FILE%.S %OUT%\%FILE%.asm
%CPP% /I %DIR% %CFLAGS% %DIR%\%FILE%.S > %OUT%\%FILE%.asm"

echo %AS% -c /Fo %OUT%\%FILE%.obj %OUT%\%FILE%.asm 
%AS% -c /Fo %OUT%\%FILE%.obj %OUT%\%FILE%.asm" 