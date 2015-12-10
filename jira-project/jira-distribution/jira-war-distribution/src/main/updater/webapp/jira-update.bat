@echo off

if exist "%HOME%\antrc_pre.bat" call "%HOME%\antrc_pre.bat"

if not "%OS%"=="Windows_NT" goto win9xStart
:winNTStart
@setlocal

rem %~dp0 is name of current script under NT
set DEFAULT_JIRA_HOME=%~dp0

rem : operator works similar to make : operator
set DEFAULT_JIRA_HOME=%DEFAULT_JIRA_HOME%\..

if "%JIRA_HOME%"=="" set JIRA_HOME=%DEFAULT_JIRA_HOME%
set DEFAULT_JIRA_HOME=

rem Need to check if we are using the 4NT shell...
if "%eval[2+2]" == "4" goto setup4NT

rem On NT/2K grab all arguments at once
set JIRA_CMD_LINE_ARGS=%*
goto doneStart

:setup4NT
set JIRA_CMD_LINE_ARGS=%$
goto doneStart

:win9xStart
rem Slurp the command line arguments.  This loop allows for an unlimited number of 
rem agruments (up to the command line limit, anyway).

set JIRA_CMD_LINE_ARGS=

:setupArgs
if %1a==a goto doneStart
set JIRA_CMD_LINE_ARGS=%JIRA_CMD_LINE_ARGS% %1
shift
goto setupArgs

:doneStart
rem This label provides a place for the argument list loop to break out 
rem and for NT handling to skip to.

rem find JIRA_HOME
if not "%JIRA_HOME%"=="" goto checkJava

rem check for ant in Program Files on system drive
if not exist "%SystemDrive%\Program Files\ant" goto checkSystemDrive
set JIRA_HOME=%SystemDrive%\Program Files\ant
goto checkJava

:checkSystemDrive
rem check for ant in root directory of system drive
if not exist %SystemDrive%\ant\nul goto checkCDrive
set JIRA_HOME=%SystemDrive%\ant
goto checkJava

:checkCDrive
rem check for ant in C:\ant for Win9X users
if not exist C:\ant\nul goto noAntHome
set JIRA_HOME=C:\ant
goto checkJava

:noAntHome
echo JIRA_HOME is not set and ant could not be located. Please set JIRA_HOME.
goto end

:checkJava

echo "Fetching list of available JIRA updates..."

call %JIRA_HOME%\updater\ant.bat -emacs -f %JIRA_HOME%\updater\scripts\get-updater.xml
call %JIRA_HOME%\updater\ant.bat -emacs -f %JIRA_HOME%\updater\scripts\updater.xml %JIRA_CMD_LINE_ARGS%
