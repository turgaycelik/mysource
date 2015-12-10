#!/bin/sh

chmod u+x ./tools/ant/bin/antRun
chmod u+x ./tools/ant/bin/ant

# ----- Verify and Set Required Environment Variables -------------------------

if [ "$TERM" = "cygwin" ] ; then
  S=';'
else
  S=':'
fi

# ----- Set Up The Runtime Classpath ------------------------------------------

OLD_ANT_HOME="$ANT_HOME"
unset ANT_HOME

CP=$CLASSPATH
export CP
unset CLASSPATH

"$PWD/./tools/ant/bin/ant" -emacs  $@

unset CLASSPATH

CLASSPATH=$CP
export CLASSPATH
ANT_HOME=OLD_ANT_HOME
export ANT_HOME
