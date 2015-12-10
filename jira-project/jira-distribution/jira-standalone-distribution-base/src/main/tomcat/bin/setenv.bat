rem --------------------------------------------------------------------------
rem Note: If running JIRA as a Service, settings in this file have no
rem effect. See http://confluence.atlassian.com/display/JIRA/Increasing+JIRA+memory
rem
rem --------------------------------------------------------------------------

rem --------------------------------------------------------------------------
rem
rem One way to set the JIRA HOME path is here via this variable.  Simply uncomment it and set a valid path like c:\jira\home.  You can of course set it outside in the command terminal.  That will also work.
rem
rem JIRA_HOME=""
rem --------------------------------------------------------------------------



rem --------------------------------------------------------------------------
rem
rem  Occasionally Atlassian Support may recommend that you set some specific JVM arguments.  You can use this variable below to do that.
rem
rem --------------------------------------------------------------------------
set JVM_SUPPORT_RECOMMENDED_ARGS=


rem --------------------------------------------------------------------------
rem
rem The following 2 settings control the minimum and maximum given to the JIRA Java virtual machine.  In larger JIRA instances, the maximum amount will need to be increased.
rem
rem --------------------------------------------------------------------------
set JVM_MINIMUM_MEMORY=384m
set JVM_MAXIMUM_MEMORY=768m

rem --------------------------------------------------------------------------
rem
rem The following are the required arguments for JIRA.
rem
rem --------------------------------------------------------------------------
set JVM_REQUIRED_ARGS=${jvm.required.args}

rem --------------------------------------------------------------------------
rem Uncomment this setting if you want to import data without notifications
rem
rem --------------------------------------------------------------------------
rem set DISABLE_NOTIFICATIONS= -Datlassian.mail.senddisabled=true -Datlassian.mail.fetchdisabled=true -Datlassian.mail.popdisabled=true


rem --------------------------------------------------------------------------
rem
rem In general don't make changes below here
rem
rem --------------------------------------------------------------------------

rem --------------------------------------------------------------------------
rem This allows us to actually debug GC related issues by correlating timestamps
rem with other parts of the application logs.  The second option prevents the JVM
rem from suppressing stack traces if a given type of exception occurs frequently,
rem which could make it harder for support to diagnose a problem.
rem --------------------------------------------------------------------------
set JVM_EXTRA_ARGS=-XX:+PrintGCDateStamps -XX:-OmitStackTraceInFastThrow

set _PRG_DIR=%~dp0
type "%_PRG_DIR%\jirabanner.txt"

set JIRA_HOME_MINUSD=
IF "x%JIRA_HOME%x" == "xx" GOTO NOJIRAHOME
     set JIRA_HOME_MINUSD=-Djira.home="%JIRA_HOME%"
:NOJIRAHOME

set JAVA_OPTS=%JAVA_OPTS% -Xms%JVM_MINIMUM_MEMORY% -Xmx%JVM_MAXIMUM_MEMORY% %JVM_REQUIRED_ARGS% %DISABLE_NOTIFICATIONS% %JVM_SUPPORT_RECOMMENDED_ARGS% %JVM_EXTRA_ARGS% %JIRA_HOME_MINUSD%


rem Checks if the JAVA_HOME has a space in it (can cause issues)
SET _marker="x%JAVA_HOME%"
SET _marker=%_marker: =%
IF NOT %_marker% == "x%JAVA_HOME%" ECHO JAVA_HOME "%JAVA_HOME%" contains spaces. Please change to a location without spaces if this causes problems.

rem Perm Gen size needs to be increased if encountering OutOfMemoryError: PermGen problems. Specifying PermGen size is not valid on IBM JDKs
set JIRA_MAX_PERM_SIZE=384m
IF EXIST "%_PRG_DIR%\permgen.bat" goto startPermGenCheck
goto skipPermGenCheck
:startPermGenCheck
call "%_PRG_DIR%\permgen.bat"
if ERRORLEVEL 1 goto endPermGenCheck
set JAVA_OPTS=-XX:MaxPermSize=%JIRA_MAX_PERM_SIZE% %JAVA_OPTS%
:endPermGenCheck
set JIRA_MAX_PERM_SIZE=
rem Clear the errorlevel which may have been set by permgen.bat
cmd /c
:skipPermGenCheck

echo.
echo If you encounter issues starting or stopping JIRA, please see the Troubleshooting guide at http://confluence.atlassian.com/display/JIRA/Installation+Troubleshooting+Guide
echo.
IF "x%JIRA_HOME%x" == "xx" GOTO NOJIRAHOME2
    echo Using JIRA_HOME:       %JIRA_HOME%
:NOJIRAHOME2
