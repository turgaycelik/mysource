#!/bin/bash

# resolve links - $0 may be a softlink - stolen from catalina.sh
PRG="$0"
while [ -h "$PRG" ]; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done
PRGDIR=`dirname "$PRG"`

. `dirname $0`/user.sh #readin the username

if [ -z "$JIRA_USER" ] || [ $(id -un) == "$JIRA_USER" ]; then
    echo executing as current user

    exec $PRGDIR/shutdown.sh 20 -force $@

elif [ $UID -ne 0 ]; then

    echo JIRA has been installed to run as $JIRA_USER so please sudo run this to enable switching to that user
    exit 1

else

    echo executing using dedicated user
    if [ -x "/sbin/runuser" ]; then
        sucmd="/sbin/runuser"
    else
        sucmd="su"
    fi
    $sucmd -m $JIRA_USER -c "$PRGDIR/shutdown.sh 20 -force $@"

fi
