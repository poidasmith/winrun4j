set L=F:\eclipse\workspace\WinRun4J\build\WinRun4J-Debug\WinRun4J.exe
set H=F:\eclipse\workspace\org.boris.winrun4j.test\launchers\TestHTML.exe
set RES="F:\eclipse\workspace\WinRun4J\build\ResourceEditor-Debug - Console\ResourceEditor.exe"
set HTML=F:\eclipse\workspace\org.boris.winrun4j.test\launchers\test.html
copy %L% %H%
%RES% /H %H% %HTML%
%RES% /L %H%
start iexplore.exe  res://%H%/test.html
pause
