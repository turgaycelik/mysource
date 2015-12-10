${charAt}ECHO OFF
REM -----------------------------------------------------------------------------
REM Run script for the JIRA Configurator
REM -----------------------------------------------------------------------------

REM try to find a JAVA_HOME or JRE_HOME
if not "%JRE_HOME%" == "" goto gotJreHome
set JRE_HOME=%JAVA_HOME%
if not "%JRE_HOME%" == "" goto gotJreHome
echo No JRE_HOME or JAVA_HOME environment variable is set - attempting to just run 'java' command
set _RUNJAVA=java
goto okJavaHome

:gotJreHome
REM Use the java from (1) JRE_HOME or (2) JAVA_HOME; supports spaces in these paths
set _RUNJAVA=%JRE_HOME%\bin\java

:okJavaHome

REM Change to the bin directory
set ORIGINAL_DIR=%cd%
cd %~dp0

REM Run the Configurator Java class
REM Note we only quote this path once, see JRA-31543
set NAME=${product.name} Configuration Tool
set NAME_SWITCH=-Djira.configurator.name=%NAME%
"%_RUNJAVA%" "%NAME_SWITCH%" -classpath jira-configurator.jar;../atlassian-jira/WEB-INF/classes;../atlassian-jira/WEB-INF/lib/*;../lib/* com.atlassian.jira.configurator.Configurator %*

REM batch files would leave me in the bin directory - change back to the original
cd %ORIGINAL_DIR%

