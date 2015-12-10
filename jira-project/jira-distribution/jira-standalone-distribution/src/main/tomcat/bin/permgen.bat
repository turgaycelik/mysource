@ECHO OFF
REM Determine which JDK is being used
SET javaPath=%JAVA_HOME%\bin

IF NOT "%JAVA_HOME%" == "" GOTO gotJavaHome

REM Try to find Java on the path
FOR %%i IN (java.exe) DO SET javaPath=%%~dp$PATH:i
if "x%javaPath%"=="x" GOTO finish

REM try to see if it is a JDK or a JRE
SET javaBinDir=%javaPath:~-4%
IF NOT "%javaBinDir%" == "bin\"  GOTO :isJRE

SET JAVA_HOME=%javaPath:~0,-5%
GOTO :gotJavaHome

:isJRE

:gotJavaHome
"%javaPath%\java.exe" -version 2>&1 | FINDSTR IBM
IF ERRORLEVEL 1 GOTO finish

:finishNoPermGen
exit /b 1

:finish
exit /b 0
