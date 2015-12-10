#! /bin/sh
cd "`dirname "$0"`"
SETTINGSFILE="settings.xml"
LOCALREPO="localrepo"
set -e
export PATH=.:$PATH
mvn2.sh clean install -f jira-project/pom.xml -Dmaven.test.skip -Dmaven.test.skip -s $SETTINGSFILE -Dmaven.repo.local="`pwd`/$LOCALREPO" "$@"
mvn2.sh clean install -f jira-project/jira-components/jira-webapp/pom.xml -Dmaven.test.skip -Pbuild-from-source-dist -Dmaven.test.skip -s $SETTINGSFILE -Dmaven.repo.local="`pwd`/$LOCALREPO" "$@"
mvn2.sh clean package -Dmaven.test.skip -f jira-project/jira-distribution/jira-webapp-dist/pom.xml -Dmaven.test.skip -s $SETTINGSFILE -Dmaven.repo.local="`pwd`/$LOCALREPO" "$@"
