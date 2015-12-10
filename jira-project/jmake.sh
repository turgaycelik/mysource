#!/bin/bash

BASE=$(dirname $0)
files=($BASE/bin/cows/*)
COWSAY="$BASE/bin/cowsay -f `printf "%s\n" "${files[RANDOM % ${#files[@]}]}"`"
LOLCAT="$BASE/bin/gems/bin/lolcat"
export GEM_HOME=$BASE/bin/gems
#
# SECTION 1: defining commands
#
# This is the section that declares and implements all the commands to run
#
# 1) Add a KEY for your command to jmake_keys
# 2) Add jmake_cmd_KEY, jmake_shortdesc_KEY, jmake_options_KEY variables
# 3) Add a jmake_fn_KEY function that implements the functionality
# 4) Optionally add jmake_longdesc_KEY which should be a function printing out detailed description/help
#


# keep track of time
. "`dirname $0`/bin/timer.sh"
time=$(timer)

jmake_keys=(help fart unit findbugs run debug standalone source up pup idea vdep RP CI BP WAR ondemand)

jmake_cmd_help="help"
jmake_aka_help="help"
jmake_shortdesc_help="Full help"
jmake_options_help=""

jmake_cmd_fart="fix-idea-artifact"
jmake_aka_fart="fart"
jmake_shortdesc_fart="Do the needfull to get a working IDEA artifact (bundled plugins, war initialisation) (alias fart)"
jmake_options_fart="clean noclean cleanall offline update"

jmake_cmd_unit="unit-tests"
jmake_aka_unit="unit-tests"
jmake_shortdesc_unit="Run all the unit tests"
jmake_options_unit="clean noclean cleanall offline update skipbundled findbugs mvn3"

jmake_cmd_findbugs="findbugs"
jmake_aka_findbugs="fb"
jmake_shortdesc_findbugs="Run Finbugs checks over the codebase. No clean by default. Needs mvn3"
jmake_options_findbugs="clean noclean cleanall skipbundled"

jmake_cmd_run="run"
jmake_aka_run="run"
jmake_shortdesc_run="Run JIRA"
jmake_options_run="clean noclean cleanall multitenant offline update"

jmake_cmd_debug="debug"
jmake_aka_debug="debug"
jmake_shortdesc_debug="Run and Debug JIRA"
jmake_options_debug="clean noclean cleanall suspend multitenant offline update"

jmake_cmd_standalone="standalone"
jmake_aka_standalone="sa"
jmake_shortdesc_standalone="Build standalone JIRA (alias sa)"
jmake_options_standalone="clean noclean cleanall run offline update"

jmake_cmd_source="source"
jmake_aka_source="src"
jmake_shortdesc_source="Build JIRA Source Distribution (alias src)"
jmake_options_source="clean noclean cleanall offline update"

jmake_cmd_up="up"
jmake_aka_up="svnUp"
jmake_aka_up="gitUp"
jmake_shortdesc_up="svn update / git pull with smarts (alias svnUp / gitUp)"
jmake_options_up=""

jmake_cmd_pup="pup"
jmake_aka_pup="pup"
jmake_shortdesc_pup="Post update rebuild"
jmake_options_pup=""

jmake_cmd_idea="idea"
jmake_aka_idea="idea"
jmake_shortdesc_idea="Refresh IDEA project (close project first)"
jmake_options_idea="offline clean update"

jmake_cmd_vdep="verify-dependencies"
jmake_aka_vdep="vdep"
jmake_shortdesc_vdep="Run the maven dependency tracker plugin"
jmake_options_vdep=""

jmake_cmd_RP="ref-plugins"
jmake_aka_RP="rp"
jmake_shortdesc_RP="Build the reference plugins (alias rp)"
jmake_options_RP="clean noclean cleanall offline nodeps install update"

jmake_cmd_CI="CI"
jmake_aka_CI="ci"
jmake_shortdesc_CI="Runs the unit/func tests as in CI (ala https://jira.bamboo.atlassian.com/browse/JIRAHEAD-CI/)"
jmake_options_CI="noclean cleanall jobs func single"

jmake_cmd_BP="bundled-plugins"
jmake_aka_BP="bp"
jmake_shortdesc_BP="Build the bundled plugins (alias bp)"
jmake_options_BP="clean noclean cleanall install offline update"

jmake_cmd_WAR="initialise-war"
jmake_aka_WAR="war"
jmake_shortdesc_WAR="Build the web application (alias war)"
jmake_options_WAR="clean noclean cleanall install offline update"

jmake_cmd_ondemand="ondemand"
jmake_aka_ondemand="od"
jmake_shortdesc_ondemand="Build & Run JIRA OnDemand"
jmake_options_ondemand="run jira-only alacarte gapps install quick clean nodeps offline update tests deploy"

function say() {
    local iCanHazLolcat=true
    if [ -x "~/.nocow" ]; then
        iCanHazLolcat=false
    else
        # pre-flight checks
        (echo "test" | $COWSAY) >/dev/null 2>&1 || iCanHazLolcat=false
        (echo "test" | $LOLCAT) >/dev/null 2>&1 || iCanHazLolcat=false
    fi
    
    $iCanHazLolcat && (echo "$*" | $COWSAY | $LOLCAT)
    $iCanHazLolcat || echo "$*"
}

jmake_fn_help() {
    say $(printf -- "Use '%s CMD --help' for more help\n\n" `basename $0`)
    for x in ${jmake_keys[@]} ; do
        printf "%s %-20s" `basename $0` `cmdprop cmd $x`
        echo `cmdprop shortdesc $x`
    done
}

jmake_longdesc_help() {
    echo "Help on help on help on help... HEEELP, it's a recursive trap!"
}

jmake_fn_fart() {
    if [ -z $jmake_arg_clean ] ; then jmake_arg_clean="" ; fi
    local clean=""
    case $jmake_arg_clean in
    "clean" )
        rm -fr jira-components/jira-webapp/target # so that the needswebapp is activated below
        clean="clean" ;;
    "cleanall" )
        rm -fr jira-components/jira-webapp/target # so that the needswebapp is activated below
        clean="clean" ;;
    esac

    local targetmodule="jira-components/jira-plugins/jira-bundled-plugins"

    local needswebapp=""
    if [ ! -d jira-components/jira-webapp/target ] ; then
        needswebapp="true";
        targetmodule="jira-components/jira-webapp"
     fi

    callmvn $clean $jmake_arg_offline $jmake_arg_update install -Ppseudo-loc -Pide-setup -am -pl $targetmodule \
        -Dmaven.test.unit.skip=true -Dskip.smartass.zip.update=true -DskipTests
    [[ ($? -eq 0) && (-z $needswebapp) ]] && callmvn -o initialize -Ppseudo-loc -Pide-setup -pl jira-components/jira-webapp \
        -Dmaven.test.unit.skip=true -Dskip.smartass.zip.update=true
}

jmake_fn_unit() {
    test_modues="jira-components/jira-tests-parent/,jira-components/jira-tests-parent/jira-tests,jira-components/jira-tests-parent/jira-tests-unit/,jira-components/jira-tests-parent/jira-tests-legacy/"
    if [ -z $jmake_arg_clean ] ; then jmake_arg_clean="noclean" ; fi
    local clean=""
    case $jmake_arg_clean in
    "clean" )
        callmvn clean -pl $test_modues ;;
    "cleanall" )
        clean="clean"
    esac

    local bundled_plugins_module=""
    if [ -z $jmake_arg_skipbundled ]; then
        bundled_plugins_module=",jira-components/jira-plugins/jira-bundled-plugins"
    fi
    mvn_run_args="$clean $jmake_arg_offline verify -pl "$test_modues$bundled_plugins_module" -Djira.minify.skip=true -am -DperformApiCheck -DajEnforcer"
    if [ -n "$jmake_arg_findbugs" ]; then
        callmvn3  $mvn_run_args -Pfindbugs
    elif [ -n "$jmake_arg_mvn3" ]; then
        callmvn3 $mvn_run_args  -T1.5C
    else
        callmvn $mvn_run_args
    fi
}

jmake_fn_findbugs() {
    if [ -z $jmake_arg_clean ] ; then jmake_arg_clean="noclean" ; fi
    local clean=""
    case $jmake_arg_clean in
    "clean" )
        clean="clean" ;;
    "cleanall" )
        clean="clean"
    esac

    local bundled_plugins_module=""
    if [ -z $jmake_arg_skipbundled ]; then
        bundled_plugins_module=",jira-components/jira-plugins/jira-bundled-plugins"
    fi

    callmvn3 $clean test -pl "jira-components/jira-core$bundled_plugins_module" -am -DskipTests -Pfindbugs
}

jmake_fn_run() {
    if [ -z $jmake_arg_clean ] ; then jmake_arg_clean="noclean" ; fi
    local clean=""
    case $jmake_arg_clean in
    "clean" )
        clean="clean" ;;
    "cleanall" )
        clean="clean"
    esac

    callmvn $clean $jmake_arg_offline $jmake_arg_update verify $jmake_arg_multitenant -Dmaven.test.skip=true -Pdistribution -pl jira-distribution/jira-webapp-dist-runner -am -Drun-webapp-dist
}

jmake_fn_debug() {
    if [ -z $jmake_arg_clean ] ; then jmake_arg_clean="noclean" ; fi
    local clean=""
    case $jmake_arg_clean in
    "clean" )
        clean="clean" ;;
    "cleanall" )
        clean="clean"
    esac

    local debugsuspend="debug.suspend=n"
    case $jmake_arg_suspend in
    "suspend" )
        debugsuspend="debug.suspend=y" ;;
    esac

    callmvn $clean $jmake_arg_offline $jmake_arg_update install -Ddebug -D$debugsuspend $jmake_arg_multitenant -Dmaven.test.skip=true -Pdistribution -pl jira-distribution/jira-webapp-dist-runner -am -Drun-webapp-dist
}

jmake_fn_standalone() {
    if [ -z $jmake_arg_clean ] ; then jmake_arg_clean="noclean" ; fi
    local clean=""
    case $jmake_arg_clean in
    "clean" )
        clean="clean" ;;
    "cleanall" )
        clean="clean"
    esac

    callmvn $clean $jmake_arg_offline $jmake_arg_update install -pl jira-distribution/jira-standalone-distribution -am -Pdistribution -Dmaven.test.unit.skip=true -Dmaven.test.func.skip=true -Dmaven.test.selenium.skip=true
    bin/unpackStandalone  $jmake_arg_run
}

jmake_fn_source() {
    if [ -z $jmake_arg_clean ] ; then jmake_arg_clean="noclean" ; fi
    local clean=""
    case $jmake_arg_clean in
    "clean" )
        clean="clean" ;;
    "cleanall" )
        clean="clean"
    esac

    callmvn $clean $jmake_arg_offline $jmake_arg_update install -pl jira-distribution/jira-source-distribution -am -Pdistribution -Dmaven.test.unit.skip=true -Dmaven.test.func.skip=true -Dmaven.test.selenium.skip=true

}

jmake_fn_up() {
    bin/devVcsUp
    bin/devPup
}

jmake_fn_pup() {
    bin/devPup
}

jmake_fn_idea() {

    local download_tomcat=""
    if [ "$jmake_arg_clean" != "" ]; then
        download_tomcat="download-tomcat"
    fi

    local working=false
    local i=`jps -v | grep idea`
    if [[ -n "${i//\s//}" ]]; then 
        echo ""
        echo "You have the following idea processes running"
        echo "--------------------------------------------------------------------------------"
        echo $i 
        echo "--------------------------------------------------------------------------------"
        working=true
        while $working; do
            echo ""
            read -p "Have you exited IDEA? [y/n]" yn
            case $yn in
                [Yy]* ) working=false;;
                [Nn]* ) working=true;;
                * ) echo "Answer me goddamn it! Have you exited IDEA? Yes or No?";;
            esac
        done
    fi

    echo "Refreshing your IDEA settings...." ;
    rm -Rf tomcatBase

    local OLD=".idea/modules.xml.old"
    local NEW=".idea/modules.xml"
    local flag=0

    if [ -f "$NEW" ]; then
        mv -f "$NEW" "$OLD"
        flag=1
    fi

    # people who have previously built JIRA in Maven 1 may have invalid antlr
    # jars in the repo. delete them if they are older than 30 days so Maven 2
    # can download the correct ones.
    local ANTLR_JARS=`find ~/.m2/repository/org/antlr -type f -ctime +30d`
    if [ "$ANTLR_JARS" != "" ]; then
       echo -n "Deleting old ANTLR jars from ~/.m2/repository/org/antlr... "
       rm -rf ~/.m2/repository/org/antlr
       echo "done."
    fi

    # first build the jira-ide-support module. this copies the jira.idea.properties file into place.
    callmvn $clean install -Pide-setup,$download_tomcat -pl jira-ide-support -am $jmake_arg_offline

    # then build the webapp (this makes the bundled plugins)
    callmvn $clean $jmake_arg_offline $jmake_arg_update install -Pide-setup,pseudo-loc,dev-mode-plugins \
        -am -pl jira-components/jira-webapp \
        -Dmaven.test.unit.skip=true -DskipTests
    
    if [ $flag -ne 0 ]; then
        mv -f "$OLD" "$NEW"
    fi
}

jmake_fn_vdep() {
    # unfortunately we need to reach the compile phase for the reactor to kick in...
    callmvn -pl jira-components/jira-core -DverifyDependencies -am compile
}

jmake_fn_RP() {
    if [ -z $jmake_arg_clean ] ; then jmake_arg_clean="clean" ; fi
    local clean=""
    case $jmake_arg_clean in
    "clean" )
        clean="clean" ;;
    "cleanall" )
        clean="clean"
    esac
    local ref_plugins_target=""
    if [ -z $REF_PLUGINS_LOCATION ] ; then
        ref_plugins_target="$HOME/jira-reference-plugins"
    else
        ref_plugins_target=$REF_PLUGINS_LOCATION
    fi
    if [ ! -d $ref_plugins_target ] ; then
        mkdir $ref_plugins_target
        result=$?
        if [ $result -ne 0 ] ; then
            echo "Unable to create target reference-plugins directory $ref_plugins_target. Plugin artifacts will not be copied over"
            ref_plugins_target=""
        fi
    fi
    local target=""
    if [ -z $jmake_arg_install ] ; then
        target="package"
    else
        target="install"
    fi
    local ref_modules="jira-components/jira-plugins/jira-reference-upgraded-plugin/,jira-components/jira-plugins/jira-reference-dependent-plugin/,jira-components/jira-plugins/jira-reference-upgraded-language-pack/"
    callmvn $clean $jmake_arg_offline $jmake_arg_update $target -pl $ref_modules $jmake_arg_nodeps -Dmaven.test.unit.skip=true
    local mvn_result=$?
    local jira_plugins_loc="jira-components/jira-plugins"
    local ref_plugin_locs=("$jira_plugins_loc/jira-reference-plugin" "$jira_plugins_loc/jira-reference-upgraded-plugin" "$jira_plugins_loc/jira-reference-dependent-plugin" "$jira_plugins_loc/jira-reference-language-pack" "$jira_plugins_loc/jira-reference-upgraded-language-pack")
    if [[ ( mvn_result -eq 0 ) && ( -d $ref_plugins_target ) ]] ; then
        for loc in ${ref_plugin_locs[@]} ; do
            cp -v $loc/target/jira-reference-*-SNAPSHOT.jar $ref_plugins_target
        done
    fi
}


jmake_longdesc_RP() {
    cat<<EndOfHelpMsg

jmake ref-plugins
------------------

Builds JIRA reference plugins and copies target artifacts to a specified directory.
The target directory is $HOME/jira-reference-plugins by default, unles REF_PLUGINS_LOCATION environment variable is specified.
If the target directory cannot be created, no files will be copied over.

The main maven goal is 'package' by default, unless 'install' option is provided.

The 'clean' maven goal is activated by default, unless 'noclean' option is provided.
EndOfHelpMsg
}

jmake_fn_BP() {
    build_module "jira-components/jira-plugins/jira-bundled-plugins"
}


jmake_longdesc_BP() {
    cat<<EndOfHelpMsg

jmake bundled-plugins
---------------------

Builds JIRA bundled plugins.

The main maven goal is 'package' by default, unless  the 'install' option is provided.

The 'clean' maven goal is activated by default, unless the 'noclean' option is provided.
EndOfHelpMsg
}

jmake_fn_WAR() {
    build_module "jira-components/jira-webapp"
}

jmake_longdesc_WAR() {
    cat<<EndOfHelpMsg

jmake initialise-war
--------------------

Builds JIRA web application.

The main maven goal is 'package' by default, unless  the 'install' option is provided.

The 'clean' maven goal is activated by default, unless the 'noclean' option is provided.
EndOfHelpMsg
}

build_module() {
    if [ -z $jmake_arg_clean ] ; then jmake_arg_clean="clean" ; fi
    local clean=""
    case $jmake_arg_clean in
    "noclean" )
        clean="" ;;
    "clean" )
        clean="clean" ;;
    "cleanall" )
        clean="clean" ;;
    esac

    local target=""
    if [ -z $jmake_arg_install ] ; then
        target="package"
    else
        target="install"
    fi

    local targetmodule=$1

    callmvn $clean $jmake_arg_offline $jmake_arg_update $target -am -pl $targetmodule \
        -Dmaven.test.unit.skip=true -DskipTests

}

CI_runjob() {
    MVN_PIPE_PREFIX=$1
    shift

    local skip="yes"
    for a in $jmake_arg_jobs ; do
        if [ $MVN_PIPE_PREFIX == $a ] ; then skip="no" ; break ; fi
    done
    
    local prefix=""
    if [[ "$jmake_arg_single" == com.atlassian.jira.webtest.webdriver* && $MVN_PIPE_PREFIX != WEBDRIVER* ]] || \
       [[ "$jmake_arg_single" == com.atlassian.jira.webtests.ztests* && ($MVN_PIPE_PREFIX != FUNC* || $MVN_PIPE_PREFIX == "FUNCUNIT") ]]; then
       if [ $MVN_PIPE_PREFIX != "COMPILE" ] ; then skip="yes" ; fi
    fi         
    if [ "yes" = $skip  ] ; then
        printf "(Skipped %s)\n" $MVN_PIPE_PREFIX
        return 0;
    fi

    callmvn "$@"
    local ret=$?
    [ $ret -eq 0 ] && MVN_PIPESTATUS_GREEN="$MVN_PIPESTATUS_GREEN $MVN_PIPE_PREFIX" || MVN_PIPESTATUS_RED="$MVN_PIPESTATUS_RED $MVN_PIPE_PREFIX"
}

jmake_fn_CI() {
    if [ -z $jmake_arg_clean ] ; then jmake_arg_clean="cleanall" ; fi
    local clean=""
    case $jmake_arg_clean in
    "noclean" )
        clean="" ;;
    "cleanall" )
        clean="clean"
    esac

    rm -f jmake-CI.log

    MVN_PIPESTATUS_GREEN=""
    MVN_PIPESTATUS_RED=""

    local nFBATCHES=20
    local nSBATCHES=15
    local nWBATCHES=7

    local showjobs="";
    if [[ -z "$jmake_arg_func" && -z "$jmake_arg_jobs" && -z "$jmake_arg_single" ]] ; then showjobs="true"; fi

    if [ -z "$jmake_arg_jobs" ] ; then
        # default jobs
        jmake_arg_jobs="COMPILE"
        if [[ -n "$jmake_arg_func" || -n $showjobs || -n "$jmake_arg_single" ]] ; then
            jmake_arg_jobs="$jmake_arg_jobs FUNCUNIT"
            for (( i=1 ; i <= $nFBATCHES ; i++ )) ; do printf -v jmake_arg_jobs "%s FUNC%02d" "$jmake_arg_jobs" "$i" ; done
        fi
        if [[ -n $showjobs || -n "$jmake_arg_single" ]] ; then
            jmake_arg_jobs="$jmake_arg_jobs"
            for (( i=1 ; i <= $nWBATCHES ; i++ )) ; do printf -v jmake_arg_jobs "%s WEBDRIVERBATCH%02d" "$jmake_arg_jobs" "$i" ; done
            jmake_arg_jobs="$jmake_arg_jobs UALINT"
        fi
    fi

    if [ -n "$showjobs" ] ; then
        printf "use 'CI func' to run CI tests, or use 'CI jobs' to pick from the available jobs:\n"
        printf " %s\n" "$jmake_arg_jobs";
        return 1;
    fi

    CI_runjob COMPILE $clean verify \
        -Dmaven.test.unit.skip=true -Dmaven.test.func.skip=true -Dmaven.test.selenium.skip=true
    [ $? -ne 0 ] && return 1

    local single_job=""    
    if [ -n "$jmake_arg_single" ] ; then
        single_job="-Djira.functest.single.testclass=$jmake_arg_single"
        nFBATCHES=1
        nSBATCHES=1
        nWBATCHES=1
    fi

    ## FUNC JOBS
    for (( BATCH=1 ; BATCH <= $nFBATCHES ; BATCH++ )) ; do
        rm -fr jira-distribution/jira-func-tests-runner/target # this seems necessary so that the first test doesn't see old data
        CI_runjob `printf "FUNC%02d" $BATCH` verify -pl jira-distribution/jira-func-tests-runner -am -Pdistribution \
            -Dmaven.test.unit.skip=true -Djira.security.disabled=true \
            -Datlassian.test.suite.numbatches=$nFBATCHES -Datlassian.test.suite.batch=$BATCH \
            -Djira.minify.skip=true -Dfunc.mode.plugins -Dreference.plugins $single_job
    done

    CI_runjob FUNCUNIT verify \
        -pl jira-distribution/jira-func-tests-runner -am -Pdistribution \
        -Dmaven.test.func.skip=true -Djira.security.disabled=true \
        -Djira.minify.skip=true

    for (( BATCH=1 ; BATCH <= $nWBATCHES ; BATCH++ )) ; do
        rm -fr jira-distribution/jira-webdriver-tests-runner/target # this seems necessary so that the first test doesn't see old data
        CI_runjob `printf "WEBDRIVERBATCH%02d" $BATCH` verify \
            -pl jira-distribution/jira-webdriver-tests-runner -am -Pdistribution \
            -Dmaven.test.func.skip=true -Dmaven.test.unit.skip=true \
            -Djira.security.disabled=true -Djava.awt.headless=true \
            -Datlassian.test.suite.numbatches=$nWBATCHES -Datlassian.test.suite.batch=$BATCH \
            -Djira.minify.skip=true \
            -Datlassian.test.suite.package=com.atlassian.jira.webtest.webdriver.tests \
            -Datlassian.test.suite.includes=WEBDRIVER_TEST -Datlassian.test.suite.excludes=TPM,RELOADABLE_PLUGINS $single_job
    done

    CI_runjob UALINT verify \
        -pl jira-distribution/jira-webapp-dist,jira-distribution/jira-integration-tests -am -Pdistribution \
        -Dmaven.test.unit.skip=true -Djira.security.disabled=true \
        -Djava.awt.headless=true -Djira.minify.skip=true -Dfunc.mode.plugins=true

    printf "\n\e[J\nPassed: \e[32;1m$MVN_PIPESTATUS_GREEN\e[00m\nFAILED: \e[31;1m$MVN_PIPESTATUS_RED\e[00m\n\n"

    printf "(Also see jmake-CI.log for a copy of the console output.)\n"
}

jmake_longdesc_CI() {
    cat<<EndOfHelpMsg

jmake CI
--------

Runs the unit/func tests as in CI (ala https://jira.bamboo.atlassian.com/browse/JIRAHEAD-CI/)

Print out the jobs that could be run
$ jmake CI

Run the "func" plans (the CI plan)
$ jmake CI func

Run all the plans
$ jmake CI func

Don't clean before running the first COMPILE
$ jmake CI noclean func

Run just one job
$ jmake CI jobs FUNC10

Run a couple of jobs
$ jmake CI jobs "FUNC10 FUNC11 FUNC12"

Run a single func/webdriver test
$ jmake CI single com.atlassian.jira.functest.web.TestAddProject

Do nothing, but print out the maven commands that would have run
$ jmake CI --dry-run jobs "FUNC10 FUNC11 FUNC12"

EndOfHelpMsg
}


jmake_fn_ondemand() {
    if [ -n "$jmake_arg_install" ] ; then
        if [ -n "$jmake_arg_run" ] || [ -n "$jmake_arg_deploy" ] ; then
            echo "run=$jmake_arg_run "
            echo "OnDemand: use either install, run or deploy, cannot use more than one"
            exit
        fi
        # build only
        if [ -z $jmake_arg_quick ] ; then
            echo "Executing full build of JIRA OnDemand"
            callmvn $jmake_arg_clean install $jmake_arg_offline $jmake_arg_update -pl jira-ondemand-project/jira-ondemand-webapp $jmake_arg_nodeps -DskipTests=true -Pondemand
        else
            echo "Executing short build of JIRA OnDemand. JIRA core components will be picked up from some location that only Maven knows (or more likely doesn't)"
            cd jira-ondemand-project
            callmvn $jmake_arg_clean install $jmake_arg_offline $jmake_arg_update -DskipTests=true
            cd ..
        fi
    elif [ -n "$jmake_arg_run" ]; then
        if [ -n "$jmake_arg_deploy" ]; then
            echo "OnDemand: you must specify either install (to build OnDemand) or run (to build & run it!), or deploy"
            exit
        fi
        # run CLI
        cd jira-ondemand-project
        local run_profile=""
        if [ -n "$jmake_arg_ondemand_profile" ] ; then
            run_profile="-P$jmake_arg_ondemand_profile"
        fi
        if [ -n "$jmake_arg_ondemand_gapps" ] ; then
            run_profile="$run_profile -Pgapps"
        fi
        if [ -n "$run_profile" ] ; then
            echo "Running JIRA OnDemand with profile: $run_profile"
            mvn $run_profile $jmake_arg_offline
        else
            echo "Running default JIRA OnDemand configuration (Crowd+JIRA+Confluence)"
            mvn $jmake_arg_offline
        fi
    else 
        if [ -z "$jmake_arg_deploy" ] ; then
            echo "OnDemand: you must specify either install (to build OnDemand) or run (to build & run it!), or deploy"
            exit
        fi

        if [ -z "$jmake_arg_deploy_host" ] ; then
            echo "OnDemand: if deploying, you must specify the hostname after deploy"
            exit
        fi

        local deploy_script="`pwd`/../ondemand-fireball/scripts/unicorn-deploy/dev-deploy.py"
        if [ -f "$deploy_script" ] ; then
            callmvn $jmake_arg_clean $jmake_arg_offline install -pl jira-components/jira-webapp -am -Dmaven.test.skip
            cd jira-ondemand-project
            $deploy_script --instance "$jmake_arg_deploy_host" --path jira-ondemand-webapp
        else
            echo "OnDemand: Could not find the dev-deploy script. Ensure that it is located at $deploy_script"
            exit
        fi
    fi
}


jmake_longdesc_ondemand() {
    cat<<EndOfHelpMsg

jmake ondemand
------------------

Build, run or deploy OnDemand with current JIRA source

Full install (incl. re-building JIRA core)
$ jmake ondemand install (clean nodeps update offline)

Quick install (OnDemand components only)
$ jmake ondemand install quick (clean nodeps update offline)

Run in default mode (Crowd+JIRA+Confluence)
$ jmake ondemand run

Run in test mode (Crowd+JIRA+Confluence)
$ jmake ondemand run tests

Run in full alacarte mode (all apps: Crowd+JIRA+Confluence+Fecru+Bamboo)
$ jmake ondemand run alacarte

Run in JIRA-only mode (AKA 'Standalone')
$ jmake ondemand run jira-only

Run in GAPPS mode (AKA 'Google Apps integration')
$ jmake ondemand run gapps

Run in alacarte mode with GAPPS
$ jmake ondemand run alacarte gapps

Deploy jira-ondemand-webapp to myhost.jira-dev.com (Needs some initial setup, see https://extranet.atlassian.com/x/kpOddQ)
$ jmake ondemand deploy myhost.jira-dev.com


The run goals will bring up a CLI environment. For more details see TODO

EndOfHelpMsg
}


#
# Section 2: some utils
#
# some utils you might want to use from your commands above
#


jmake_cmd_log=""
jmake_mvn_inprog=""
callmvn() {
    if [ -z "$MAVEN_OPTS" ] ; then
        export MAVEN_OPTS=-Xmx512m
    fi
    set -- "$@" $JMAKE_MAVEN_OPTS

    jmake_mvn_inprog="yes"
    local msg;
    printf -v msg "\e[1mmvn %s\e[00m\n" "$*"
    jmake_cmd_log+="$msg"

    local ret="noreturnvalue"
    printf "mvn %s\n" "$*"
    if [ -z "$MVN_PIPE_PREFIX" ] ; then
        $jmake_mvn_cmd "$@"
        ret=$?
    else
        #(echo foo ; sleep 0.1 ; echo bar ) | mvnpiper
        $jmake_mvn_cmd "$@" 2>&1 | mvnpiper
        ret=${PIPESTATUS[0]}
    fi

    jmake_mvn_inprog=""

    if [ $ret -ne 0 ] ; then
        printf -v msg "\e[31;1m  Command exited with status %s\e[00m\n" "$ret"
        jmake_cmd_log+="$msg"
    fi

    return $ret
}

callmvn3() {
    if [ -z "$M3_HOME" ]; then
        echo "You need to install Maven 3 for this command to work. Please install Maven 3 and set the environment variable M3_HOME to its home directory"
        exit 1
    else
        jmake_mvn3_cmd="$M3_HOME/bin/mvn"
    fi
    if [ -z "$MAVEN_OPTS" ] ; then
        export MAVEN_OPTS=-Xmx512m
    fi

    # reset M2_HOME - it does no good to mvn3
    local m2home="$M2_HOME"
    if [ -n "$M2_HOME" ]; then
        export M2_HOME=""
    fi

    set -- "$@" $JMAKE_MAVEN_OPTS

    jmake_mvn_inprog="yes"
    local msg;
    printf -v msg "\e[1mmvn %s\e[00m\n" "$*"
    jmake_cmd_log+="$msg"

    local ret="noreturnvalue"
    printf "mvn %s\n" "$*"
    if [ -z "$MVN_PIPE_PREFIX" ] ; then
        $jmake_mvn3_cmd "$@"
        ret=$?
    else
        #(echo foo ; sleep 0.1 ; echo bar ) | mvnpiper
        $jmake_mvn3_cmd "$@" 2>&1 | mvnpiper
        ret=${PIPESTATUS[0]}
    fi

    # restore M2_HOME
    if [ -n "$m2home" ]; then
        export M2_HOME="$m2home"
    fi
    jmake_mvn_inprog=""

    if [ $ret -ne 0 ] ; then
        printf -v msg "\e[31;1m  Command exited with status %s\e[00m\n" "$ret"
        jmake_cmd_log+="$msg"
    fi

    return $ret
}

mvnpiper() {
    SECONDS=0
    while read i ; do
        # clear to end of buffer, print the line of output
        local stamp=`printf "[%s for %2dm%02ds]" "$MVN_PIPE_PREFIX" $(($SECONDS / 60)) $(($SECONDS % 60))`
        printf "\e[J\e[7m%s\e[00m %s\n" "$stamp" "$i"
        printf "%s %s\n" "$stamp" "$i" >> jmake-CI.log

        # print N lines of status, then move cursor to start of that status
        local s="Passed: $MVN_PIPESTATUS_GREEN FAILED: $MVN_PIPESTATUS_RED"
        local nwidth=`tput cols`
        local nlines=$(( ${#s} / $nwidth + 1 ))
        printf "Passed: \e[32;1m$MVN_PIPESTATUS_GREEN\e[00m FAILED: \e[31;1m$MVN_PIPESTATUS_RED\e[00m\n\e[%dF" $nlines
    done
}

callcmd() {
    local msg;
    printf -v msg "\e[1m%s\e[00m\n" "$*"
    jmake_cmd_log+="$msg"
    printf "%s\n" "$*"
    "$@"
}

jmakerecordtime() {

    if [ "$cmd" != "" ]; then
        local dt=`date '+%Y-%m-%d %H:%M:%S'`
        local task="jmake.$cmd"
        local who=`whoami`
        local timetaken=$1

        if [ ! -d ~/.jiradev ]; then
            mkdir ~/.jiradev
        fi
        echo "$dt,$who,$task,$timetaken" >> ~/.jiradev/jiratimers.csv
    fi
}

jmakegrowl() {
    local jiradir="`dirname $0`"
    local icon="$jiradir/jira-components/jira-webapp/src/main/webapp/images/16jira.png"
    which notify-send >/dev/null 2>&1
    if [ $? -eq 0 ]; then
        notify-send -t 60000 -i "$icon" JMAKE "$@" &
    else
        which zenity >/dev/null 2>&1
        if [ $? -eq 0 ]; then
            zenity --info --title=JMAKE --timeout=60 --window-icon="$icon"  --text="$@" &
        else
            which growlnotify >/dev/null 2>&1
            if [ $? -eq 0 ]; then
                local jiradir="`dirname $0`"
                growlnotify JMAKE --image "$icon" -m "$@" &
            fi
        fi
    fi
}

#
# Section The Rest: implementation details
#
# You probably don't need to edit the following unless you are batshit crazy.
#

cleanup() {
    trap - INT TERM EXIT
    elapsed=$(timer $time)

    jmakerecordtime $elapsed

    elapsedtime=`to_time_str $elapsed`

    # get devs back to work!
    printf -v banner "\njmake finished in %s\n\nGet back into IDEA and get JIRA out the door!" $elapsedtime
    [ "$key" != "" -a "$key" != "help" ] && [ -z "$jmake_mvn_inprog" ] && jmakegrowl "$banner"

    if [ -n "$jmake_cmd_log" ] ; then
        printf "\nCommands executed:\n%s\n" "$jmake_cmd_log"
        if [ -n "$jmake_mvn_inprog" ] ; then
            say $(printf -- "Command was interrupted")
        fi
        say $(printf -- "Completed in %s\n" $elapsedtime)
    fi
    exit
}
trap cleanup INT TERM EXIT

cmdprop () {
    local name=jmake_${1}_${2}
    echo ${!name}
}


quickhelp() {
    printf " Commands: `basename $0` ["
    sep=""
    for x in ${jmake_keys[@]} ; do
        printf "$sep"
        sep="|"
        printf " %s " `cmdprop cmd $x`
    done
    printf " ]\n"

    echo " Try: `basename $0` CMD --help"
}

default_cmd_longdesc() {
     printf "%s %-20s" `basename $0` `cmdprop cmd $1`
     echo `cmdprop shortdesc $1`
}

if [ $# -eq 0 ] ; then
    echo "`basename $0` -- the maker's maker."
    echo ""
    quickhelp
    exit
fi

jmake_mvn_cmd="mvn"
jmake_arg_clean=""
jmake_arg_suspend=""
jmake_arg_multitenant=""
jmake_arg_offline=""
jmake_arg_nodeps=-am
jmake_arg_install=""
jmake_arg_update=""
jmake_arg_jobs=""
jmake_arg_func=""
jmake_arg_ondemand_profile=""
jmake_arg_ondemand_gapps=""
jmake_arg_quick=""
jmake_arg_deploy=""
jmake_arg_deploy_host=""
jmake_arg_findbugs=""

if [ "clean" = "$1" ]
then
   jmake_arg_clean="clean"
   shift
fi

if [ "cleanall" = "$1" ]
then
   jmake_arg_clean="cleanall"
   shift
fi

cmd=$1
shift
key=""
for x in ${jmake_keys[@]} ; do
    if [ `cmdprop cmd $x` = $cmd ] ; then
        key=$x
        break
    fi
    if [ `cmdprop aka $x` = $cmd ] ; then
        key=$x
        break
    fi
done


if [ -z $key ] ; then
    printf "Don't understand: %s\n\n" $cmd
    quickhelp
    exit
fi

allowedopts=`cmdprop options $key`

# print command help if necessary
if [[ ( $# -ne 0 ) && ( $1 = "--help" ) ]] ; then
    help_fn=jmake_longdesc_$key
    if type $help_fn >/dev/null 2>&1 ; then
        $help_fn
    else
        default_cmd_longdesc $key
    fi
    key=help
    exit
fi

if [ "--dry-run" = "$1" ]
then
   jmake_mvn_cmd="echo mvn"
   shift
fi

while [ $# -ne 0 ] ; do
    arg=$1
    shift

    # is it an allowed option?
    allowed=""
    for a in $allowedopts ; do
        if [ $arg == $a ] ; then allowed=$a ; break ; fi
    done
    if [ -z $allowed ] ; then
        printf "Unknown option: %s\n" $arg
        echo "Valid options: $allowedopts"
        exit
    fi

    # handle known options
    case $arg in
    "clean" | "noclean" | "cleanall" )
        jmake_arg_clean=$arg ;;
    esac
    case $arg in
    "offline" )
        jmake_arg_offline=-o ;;
    "nodeps" )
        jmake_arg_nodeps=""
    esac
    case $arg in
    "suspend" )
        jmake_arg_suspend=$arg ;;
    "run" )
        jmake_arg_run=$arg ;;
    "multitenant")
        jmake_arg_multitenant=-Dmultitenant ;;
    esac
    case $arg in
    "install" )
        jmake_arg_install=$arg ;;
    esac
    case $arg in
    "update" )
        jmake_arg_update=-U ;;
    esac
    case $arg in
    "noverifydeps" )
        jmake_arg_noverifydeps=true ;;
    esac
    case $arg in
    "skipbundled" )
        jmake_arg_skipbundled=true ;;
    esac
    case $arg in
    "quick" )
        jmake_arg_quick="true" ;;
    esac

    case $arg in
    "jobs" )
        jmake_arg_jobs=$1 ; shift ;;
    "func" )
        jmake_arg_func="true" ;;
    "single" )
         jmake_arg_single=$1 ; shift;;
    esac

    case $arg in
    "jira-only" )
        jmake_arg_ondemand_profile="jira-only" ;;
    "alacarte" )
        jmake_arg_ondemand_profile="alacarte" ;;
    "tests" )
        jmake_arg_ondemand_profile="tests" ;;
    "gapps" )
        jmake_arg_ondemand_gapps="true" ;;
    "deploy" )
        jmake_arg_deploy="true" ; jmake_arg_deploy_host=$1 ; shift;;
    esac

    case $arg in
    "findbugs" )
        jmake_arg_findbugs="true" ;;
    "mvn3" )
        jmake_arg_mvn3="true" ;;
    esac
done

# run the actual command
fn=jmake_fn_${key}
$fn
