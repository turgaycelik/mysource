#!/bin/bash

# hacky script to test JIRA linux installer
# chris@atlassian.com

# set this to the place where you want to run the tests
TEST_HOME='~inception/'
BIN_NAME='atlassian-jira-4.4-SNAPSHOT.bin'
# change this to copy from your dev machine to $TEST_HOME/tmp
cp_bin() {
    cp ./target/$BIN_NAME ./tmp/
}

###############

echo testing jira installer

pushd $TEST_HOME

########################################
#### first uninstall from last run
########################################

echo first cleaning up any leftovers
fail=0
if [[ -x /opt/atlassian/jira/bin/shutdown.sh ]]; then
    echo shutting down
    /opt/atlassian/jira/bin/shutdown.sh
    sleep 6
    echo uninstalling
    yes | /opt/atlassian/jira/uninstall
    sleep 3
    if [[ -f /etc/init.d/jira ]]; then
        echo ********************
        echo service still exists !! FAIL
        echo ********************
        fail=1
        echo removing service
        rm /etc/init.d/jira
        find /etc/ -name '*95jira' -exec rm {} \;
    fi
else
    echo jira not found to be installed
    if [[ $(ps aux |grep '[c]atalina') ]]; then
        echo OOPS process is running, die dammit
        killall java
        sleep 2
        if [[ $(ps aux |grep '[c]atalina') ]]; then
            echo JIRA WILL NOT DIE!
            exit 1
        fi
    fi
fi
echo removing all extra stuff now
rm -rf /opt/atlassian/
rm -rf /var/atlassian/


userexists=0
grep jira /etc/passwd >/dev/null && (echo "jira user exists"; userexists=1)
if [[ -d /home/jira ]]; then
    echo "jira user's home directory is still there!! removing manually but that's a FAIL"
    fail=1
    rm -rf /home/jira
fi
if [ $userexists -eq 1 ]; then
    /usr/sbin/userdel -rf jira
fi
if (( fail )) ; then
    echo failed.......
    exit 1
fi

########################################
#### now installing
########################################

echo copying installer from dev machine
cp_bin
echo installing
mkdir -p tmp
pushd tmp
if [[ -e response.varfile ]]; then
    echo found response.varfile so i am using that
    ./$BIN_NAME -Dinstall4j.keepLog=true -Dinstall4j.debug=true -q -varfile response.varfile
else
    echo no varfile
    ./atlassian-jira-4.4-SNAPSHOT.bin -Dinstall4j.keepLog=true -Dinstall4j.debug=true
fi
echo installed, now waiting for it to be up
sleep 10
rm -f wget.html
wget -q -O wget.html http://localhost:8080/
if [[ ! $(grep 'Database Configuration' wget.html) ]]; then
   echo failed to find jira running at http://localhost:8080/
   exit 1
else
    echo running nicely woot

    echo please check the following output for correctness !!
    echo check this jira install root
    ls -las /opt/atlassian/jira
    echo check this home directory
    ls -las /var/atlassian/application-data/jira
    if [[ ! $(ls /home |grep jira) ]]; then
        echo "###### USER HOME DIR WAS NOT CREATED "
    fi
    echo check the ownership of the binaries
    ls -las /opt/atlassian/jira/bin
    echo check there is a jira running and that it is running as the jira user
    ps aux |grep '[c]atalina'
    echo check the services are right
    find /etc/ -name '*jira*' |sort
fi
popd
popd
