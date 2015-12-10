#!/bin/bash

# wrapper script for the building of the installer for testing
# install4j_home on bamboo: /opt/java/tools/install4j-4.0.5


function usage() {
    echo
    echo "You must supply these flags with values: edition (-e), install4j home (-h), virtual machine (-v) and build extras (-b)"
    echo $0 -e "<enterprise|professional|standard> -h </path/to/install4j> -v </full/path/to/vmfile.vmx> -b </full/path/to/build-extras>"
    echo
}

while getopts "e:h:v:b:" OPTION; do
    case $OPTION in
        e) edition="$OPTARG" ;;
        h) install4j_home="$OPTARG" ;;
        v) vm="$OPTARG" ;;
        b) build_extras="$OPTARG" ;;
    esac
done

if [[ -z $edition || -z $install4j_home || -z $vm  || -z $build_extras ]]; then
    usage
    exit 2
fi

if [[ ! -e $vm ]]; then
    echo
    echo vm $vm does not exist
    echo
    exit 2
fi

if [[ ! -d $install4j_home ]]; then
    echo
    echo install4j home $install4j_home directory not found
    echo
    exit 2
fi

if [[ ! -d $build_extras ]]; then
    echo
    echo build extras $build_extras directory not found
    echo this directory stores Tomcat which is bundled with JIRA Standalone
    echo
    exit 2
fi

# Setup some variables
mvn="${M2_HOME}/bin/mvn"
if [[ ! -x "$mvn" ]]; then
       echo
       echo mvn $mvn not an executable
       if [[ -z $M2_HOME ]]; then
       echo "why don't you set M2_HOME"
       fi
       echo
       exit 2
fi

function build_standalone() {

	pushd ../../
    groovy="${GROOVY_HOME}/bin/groovy"
    $groovy maven2release.groovy
  	$mvn clean install dependency:tree -DskipTests=true -Dmaven.repo.local=/tmp/testInstallerRepo

	popd
}

function build_installer() {
    echo building the installer
      
    local INSTALLER_PATH="target/"
    # make it so code signing failure (due to no keystore) does not cause whole installer build to fail
    perl -pi -e 's/failOnPostProcessorError="true"/failOnPostProcessorError="false"/' jira-template.install4j

    pushd ../
    
    $mvn clean install dependency:tree -DskipTests=true -pl jira-installer-distribution -am -Dmaven.repo.local=/tmp/testInstallerRepo -Dinstall4j.home=$install4j_home -Dinstaller.standalone.permgen=512m -Dinstaller.extra.java.properties="-Djira.jelly.on=true -Djira.plugins.bundled.disable=false -Djira.paths.set.allowed=true"

    popd

    installer=$(ls ${INSTALLER_PATH}atlassian-jira*.exe)
    if [[ -z $installer ]]; then 
        echo unable to find an installer in ${INSTALLER_PATH}
        exit 1
    fi
}


# MAIN

set -e
build_standalone
build_installer
./vmTestInstaller-m2.sh -e "$edition" -i "$installer" -v "$vm"
exit $?

# vim: set sw=4 sts=4 ts=4:
