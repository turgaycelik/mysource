#!/bin/bash

# script to run a vmware image and test the JIRA windows installer in it
#
# USAGE:

# vmTestInstaller -e <edition: enterprise|professional|standard> -i <Installer.exe> -v </full/path/to/vmfile.vmx>



# process args

function usage() {
    echo
    echo "You must supply these flags with values: edition (-e), installer (-i) and virtual machine (-v)"
    echo $0 -e "<enterprise|professional|standard> -i </path/to/JIRAInstaller.exe> -v </full/path/to/vmfile.vmx>"
    echo
}

function check_options() {
	if [[ -z $edition || -z $installer || -z "$vm" ]]; then
		usage
		exit 2
	fi
	basename_installer=$(basename $installer)
}


# constants

GUEST_USERNAME="atlassian"
GUEST_PASSWORD="atlassian"
SNAPSHOT_NAME="installer-test-baseline-m2-newcreds"
TEST_DIR="jira-installer-test"
INSTALLER_CONFIG="JIRA_windows.varfile"
TEST_SCRIPT="testInstaller-m2.sh"
BASE=$(pwd)

# global variables
vncdisplay="" # dynamically determined


# prepares the vm by copying the installer and test scripts to it
function prep_vm() {
    
    echo "prepping vm"
    set -e # turn on error hypersensitivity

    echo "packaging func tests"
    if [[ ! -e ../../jira-func-tests ]]; then
        echo "expected to find func tests in sister directory"
        exit 1
    fi
    
#    mv test-pom.xml ../../jira-func-tests/pom.xml
    mv src/test/resources/localtest.installer.properties ../../jira-func-tests/src/main/resources/localtest.installer.properties    
    tar --exclude .svn -jcf target/func_tests.tbz2 ../../jira-components/jira-func-tests

    echo "creating C:\\$TEST_DIR directory on Windows VM"
    vmrun -gu $GUEST_USERNAME -gp $GUEST_PASSWORD "createDirectoryInGuest"  "$vm" "c:\\$TEST_DIR"

    echo "copying test script from host to guest"
    vmrun -gu $GUEST_USERNAME -gp $GUEST_PASSWORD "copyFileFromHostToGuest"  "$vm" $BASE/$TEST_SCRIPT "c:\\$TEST_DIR\\$TEST_SCRIPT"
    
	echo "adding installer dir as a shared folder"
    vmrun -gu $GUEST_USERNAME -gp $GUEST_PASSWORD "addSharedFolder"  "$vm" installer $BASE
    set +e # turn off error hypersensitivity
}

function start_vm_at_snapshot() {
    set -e
    echo "resetting vm state"
    vmrun -gu $GUEST_USERNAME -gp $GUEST_PASSWORD "revertToSnapshot"  "$vm" $SNAPSHOT_NAME
    echo "starting vm"
    vmrun -gu $GUEST_USERNAME -gp $GUEST_PASSWORD "start"  "$vm" 
    set +e
}

# shutdown
function stop_vm() {

    echo "stopping vm"
    vmrun -gu $GUEST_USERNAME -gp $GUEST_PASSWORD "stop"  "$vm" hard
}

function vnc_server_startup() {
	echo starting vncserver
    vncdisplay=$(vncserver 2>&1 | perl -ne '/^New .* desktop is (.*)$/ && print"$1\n"')
    if [[ -z "$vncdisplay" ]]; then
        echo "failed to create a vncserver or get its display identifier"   
        exit 2
    fi
    export DISPLAY=$vncdisplay
    echo vncserver started on $DISPLAY
}

function vnc_server_shutdown() {
    if ! test -z $vncdisplay; then
        echo stopping vncserver on $DISPLAY
        vncserver -kill $vncdisplay >/dev/null 2>&1
    fi
}

function run_guest_script() {

    echo "running guest script to copy installer, run installer and run func tests on installer... "
    vmrun -gu $GUEST_USERNAME -gp $GUEST_PASSWORD "runProgramInGuest"  "$vm" \
        'C:\windows\system32\cmd.exe' \
        "/c c:\\cygwin\\bin\\bash.exe --login c:\\$TEST_DIR\\testInstaller-m2.sh $installer /cygdrive/c/$TEST_DIR/results.txt $edition"
}

function get_file() {
    echo "Trying to copy from '$1' to '$2'."
	vmrun -gu $GUEST_USERNAME -gp $GUEST_PASSWORD "copyFileFromGuestToHost"  "$vm" "$1" "$2"
}

# get the results of the tests out of the guest 
function get_results() {
    echo "getting the results"

    local TARGET="${BASE}/target"
    local OUTPUT="output.tar.bz2"

    pushd "${TARGET}"
    get_file "c:\\$TEST_DIR\\${OUTPUT}" "${OUTPUT}" && tar xfj "${OUTPUT}"
    popd
}

cleanup() {

    # These commands may fail so let them.
    set +o errexit

	stop_vm

    vnc_server_shutdown
}


function test_installer() {
    start_time=`date +%s`

	check_options

    echo testing installer in vmware

    #Clean up the server and vnc server always no matter how we exit. This will stop VNC servers from showing up
    #on the box that nobody uses.
    trap cleanup INT TERM EXIT

    vnc_server_startup

    start_vm_at_snapshot

    prep_vm

	run_guest_script

	get_results

    # analyse the results
    echo ==== Logs from Windows script: ====
    if [[ -e $BASE/target/results.txt ]]; then
		dos2unix $BASE/target/results.txt
        cat $BASE/target/results.txt
        grep "maven func test success" $BASE/target/results.txt >/dev/null
        RETURN_CODE=$?
    else
        echo ERROR: cannot find the results.txt file
        RETURN_CODE=1
    fi


    now=`date +%s`
    duration=`expr \( $now - $start_time \) / 60`
    echo ==== End of Windows Log. ====
    echo .
    echo "Completed installer test in $duration minutes"
    return $RETURN_CODE
}

##########
# MAIN
##########

while getopts "e:i:v:" OPTION; do
	case $OPTION in
		e) edition="$OPTARG" ;;
		i) installer="$OPTARG" ;;
		v) vm="$OPTARG" ;;
	esac
done

echo "will test installer using:"
echo "edition=$edition"
echo "installer=$installer"
echo "vm=$vm"

test_installer
exit $?

# vim: set sw=4 sts=4 ts=4:
