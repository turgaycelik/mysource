#!/bin/sh
# -----------------------------------------------------------------------------
# Run script for the JIRA Configurator
# -----------------------------------------------------------------------------

# Discover the java executable to run - similar to catalina.sh and setclasspath.sh
# If $JRE_HOME is empty, default to $JAVA_HOME
if [ -z "$JRE_HOME" ]; then
  JRE_HOME="$JAVA_HOME"
fi
if [ -z "$JRE_HOME" ]; then
  echo No JRE_HOME or JAVA_HOME environment variable is set - attempting to just run 'java' command
  _RUNJAVA=java
else
  _RUNJAVA="$JRE_HOME"/bin/java
fi

macosx=false;
case "`uname -s`" in
    Darwin) macosx=true;;
esac

# Change to the bin directory
PRG="$0"
PRGDIR="`dirname "$PRG"`"
cd "$PRGDIR" || exit

CP="jira-configurator.jar:../atlassian-jira/WEB-INF/classes:../atlassian-jira/WEB-INF/lib/*:../lib/*"
MAIN="com.atlassian.jira.configurator.Configurator"
NAME="JIRA Configuration Tool"

# Run the Configurator Java class
if $macosx ; then
    "$_RUNJAVA" -Xdock:name="$NAME" -Dapple.laf.useScreenMenuBar=true -classpath "$CP" "$MAIN" "$@"
else
    "$_RUNJAVA" -classpath "$CP" "$MAIN" "$@"
fi

