@echo off

REM   Copyright (c) 2001-2002 The Apache Software Foundation.  All rights
REM   reserved.

if exist "%HOME%\antrc_pre.bat" call "%HOME%\antrc_pre.bat"

if "%OS%"=="Windows_NT" @setlocal

rem %~dp0 is expanded pathname of the current script under NT
set DEFAULT_JIRA_HOME=%~dp0..

if "%JIRA_HOME%"=="" set JIRA_HOME=%DEFAULT_JIRA_HOME%
set DEFAULT_JIRA_HOME=

rem Slurp the command line arguments. This loop allows for an unlimited number
rem of arguments (up to the command line limit, anyway).
set JIRA_CMD_LINE_ARGS=%1
if ""%1""=="""" goto doneStart
shift
:setupArgs
if ""%1""=="""" goto doneStart
set JIRA_CMD_LINE_ARGS=%JIRA_CMD_LINE_ARGS% %1
shift
goto setupArgs
rem This label provides a place for the argument list loop to break out 
rem and for NT handling to skip to.
:doneStart

rem find JIRA_HOME if it does not exist due to either an invalid value passed
rem by the user or the %0 problem on Windows 9x
if exist "%JIRA_HOME%" goto checkJava

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
echo JIRA_HOME is set incorrectly or ant could not be located. Please set JIRA_HOME.
goto end

:checkJava
set LOCALCLASSPATH=%CLASSPATH%
for %%i in ("%JIRA_HOME%\tools\ant\lib\*.jar") do call "%JIRA_HOME%\bin\lcp.bat" %%i
if "%JAVA_HOME%" == "" goto noJavaHome
if not exist "%JAVA_HOME%\bin\java.exe" goto noJavaHome
set _JAVACMD=%JAVA_HOME%\bin\java.exe
if exist "%JAVA_HOME%\lib\tools.jar" call "%JIRA_HOME%\bin\lcp.bat" "%JAVA_HOME%\lib\tools.jar"
if exist "%JAVA_HOME%\lib\classes.zip" call "%JIRA_HOME%\bin\lcp.bat" "%JAVA_HOME%\lib\classes.zip"
goto checkJikes

:noJavaHome
set _JAVACMD=java.exe
echo.
echo Warning: JAVA_HOME environment variable is not set.
echo   If build fails because sun.* classes could not be found
echo   you will need to set the JAVA_HOME environment variable
echo   to the installation directory of java.
echo.



:checkJikes
if not "%JIKESPATH%"=="" goto runAntWithJikes

:runAnt
set _APPLIEDLOG="%JIRA_HOME%\atlassian-jira\WEB-INF\classes\jira-updater.properties"
set _STANDARD_ARGS="-Djira.home=%JIRA_HOME% -Dupdater.home=%JIRA_HOME%\updater -Dupdater.appliedlog=%_APPLIEDLOG% -propertyfile %_APPLIEDLOG%"
"%_JAVACMD%" -classpath "%LOCALCLASSPATH%" "-Dant.home=%JIRA_HOME%" %JIRA_OPTS% org.apache.tools.ant.Main %JIRA_CMD_LINE_ARGS% "%_STANDARD_ARGS%"
goto end

:runAntWithJikes
set _APPLIEDLOG="%JIRA_HOME%\atlassian-jira\WEB-INF\classes\jira-updater.properties"
set _STANDARD_ARGS="-Djira.home=%JIRA_HOME% -Dupdater.home=%JIRA_HOME%\updater -Dupdater.appliedlog=%_APPLIEDLOG% -propertyfile %_APPLIEDLOG%"
"%_JAVACMD%" -classpath "%LOCALCLASSPATH%" "-Dant.home=%JIRA_HOME%" "-Djikes.class.path=%JIKESPATH%" %JIRA_OPTS% org.apache.tools.ant.Main %JIRA_CMD_LINE_ARGS% "%_STANDARD_ARGS%"
goto end

:end
set LOCALCLASSPATH=
set _JAVACMD=
set JIRA_CMD_LINE_ARGS=

if "%OS%"=="Windows_NT" @endlocal

:mainEnd
if exist "%HOME%\antrc_post.bat" call "%HOME%\antrc_post.bat"

