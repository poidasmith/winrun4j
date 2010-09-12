
set DIR=%1
set FILE=%2
set OUT=%3

echo %DIR%
echo %FILE%

set CPP=cl.exe -EP
set CFLAGS=-nologo -Zi -D_MD -W3 -DWIN32 -DWINNT -D_WIN32 -D_WINDOWS -D_WINNT -D_WIN32_WINNT=0x0501 -D_WIN32_IE=0x0600 -D_X86_=1 -DDEBUG -D_DEBUG -MDd -Od -Oy-
set AS=ml.exe
set ASFLAGS=-coff -W3 -Cx -Zm -Di386 -DQUIET -D?QUIET

%CPP% /I %DIR% %CFLAGS% %DIR%\%FILE%.S > %DIR%\%FILE%.asm

%AS% -c /Fo %OUT%\%FILE%.obj %DIR%\%FILE%.asm 