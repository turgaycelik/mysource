@echo off
set _PRG_DIR=%~dp0

set _PRGRUNMODE=false
if "%1" == "/fg" set _PRGRUNMODE=true
if "%1" == "run" set _PRGRUNMODE=true

if "%_PRGRUNMODE%" == "true" GOTO EXECSTART
	echo.
	echo To run JIRA in the foreground, start the server with start-jira.bat /fg


:EXECSTART
if "%_PRGRUNMODE%" == "true" GOTO EXECRUNMODE
	"%_PRG_DIR%startup.bat"  %1 %2 %3 %4 %5 %6 %7 %8 %9
	GOTO END

:EXECRUNMODE
	"%_PRG_DIR%catalina.bat"  run %2 %3 %4 %5 %6 %7 %8 %9
	GOTO END

:END