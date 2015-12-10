#!/bin/sh

# ----- Verify and Set Required Environment Variables -------------------------

if [ "$TERM" = "cygwin" ] ; then
  S=';'
else
  S=':'
fi

# ----- Set Up The Runtime Classpath ------------------------------------------

OLD_M2_HOME="$M2_HOME"
export M2_HOME="$PWD/maven2/apache-maven-2.1.0"

OLD_PATH="$PATH"
OLD_MAVEN_OPTS="$MAVEN_OPTS"
export PATH=$M2_HOME/bin:$JAVA_HOME/bin:$PATH
export MAVEN_OPTS="-Xmx1024m -XX:MaxPermSize=512m"

mvn $*
EXITCODE=$?

export PATH="$OLD_PATH"
export M2_HOME="$OLD_M2_HOME"
export MAVEN_OPTS="$OLD_MAVEN_OPTS"

exit $EXITCODE
