set L=c:\eclipse\workspace\WinRun4J\build\WinRun4J-Debug\WinRun4J.exe
set H=c:\eclipse\workspace\org.boris.winrun4j.test\launchers\TestHTML.exe
set RES="c:\eclipse\workspace\WinRun4J\build\ResourceEditor-Debug - Console\ResourceEditor.exe"
set HTML=c:\eclipse\workspace\org.boris.winrun4j.test\launchers\test.html
set HTML2=c:\eclipse\workspace\org.boris.winrun4j.test\launchers\test2.html
set HTML3=c:\eclipse\workspace\org.boris.winrun4j.test\launchers\WinRun4J-logobig.gif
copy %L% %H%
%RES% /H %H% %HTML%
%RES% /H %H% %HTML2%
%RES% /H %H% %HTML3%
%RES% /L %H%
start iexplore.exe  res://%H%/test.html
pause
