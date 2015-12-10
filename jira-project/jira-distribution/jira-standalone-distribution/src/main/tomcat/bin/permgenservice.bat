set _PRG_DIR=%~dp0
rem Perm Gen size needs to be increased if encountering OutOfMemoryError: PermGen problems. Specifying PermGen size is not valid on IBM JDKs
set JIRA_MAX_PERM_SIZE=384m
IF EXIST "%_PRG_DIR%\permgen.bat" goto startPermGenCheck
goto skipPermGenCheck
:startPermGenCheck
call "%_PRG_DIR%\permgen.bat"
if ERRORLEVEL 1 goto endPermGenCheck
"%EXECUTABLE%" //US//%SERVICE_NAME% ++JvmOptions "-XX:MaxPermSize=%JIRA_MAX_PERM_SIZE%"
:endPermGenCheck
set JIRA_MAX_PERM_SIZE=
rem Clear the errorlevel which may have been set by permgen.bat
cmd /c
:skipPermGenCheck
