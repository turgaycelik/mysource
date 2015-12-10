@echo off

set OLD_M2_HOME=%M2_HOME%
set M2_HOME=maven

set OLD_PATH=%PATH%
set PATH=%M2_HOME%\bin;%JAVA_HOME%\bin;%PATH%
set OLD_MAVEN_OPTS=%MAVEN_OPTS%
set MAVEN_OPTS=-Xmx1024m -XX:MaxPermSize=384m

call mvn clean package -Dmaven.test.skip=true -pl jira-project/jira-distribution/jira-webapp-dist -am -P-dependency-tracking -Pdistribution -P!rest-apidocs  %*

set M2_HOME=%OLD_M2_HOME%
set PATH=%OLD_PATH%
set MAVEN_OPTS=%OLD_MAVEN_OPTS%
