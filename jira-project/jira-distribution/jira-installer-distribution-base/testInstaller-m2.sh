#!/bin/bash

# script for testing the JIRA installer
# designed to run on Cygwin inside the guest OS of VMware 
# test result needs to be written to a given location

SETUP_URL="http://localhost:8080/" 
SHARED_FOLDER="//.host/Shared Folders/installer"
JIRA_HOME="C:\Program Files\Atlassian\Application Data\JIRA"

if (( $#  != 3 )); then
    echo "usage: $0 <installer-executable> <output-filename> <edition>"
    exit 1
fi

installer=$1
outfile=$2
edition=$3

cd `dirname $0` 
if [[ -e $outfile ]]; then 
    rm $outfile
fi

BASE="$(pwd)"

echo "copying from $SHARED_FOLDER" >> $outfile
cp "$SHARED_FOLDER"/$installer .
RETCODE=$?
if (( RETCODE != 0 )) ; then 
	echo unable to copy installer files >> $outfile
	exit 1
fi
installer=$(basename $installer)
if [[ ! -e $installer ]]; then
	echo "don't have $installer" >> $outfile
fi
cp "$SHARED_FOLDER"/JIRA_windows.varfile .
RETCODE=$?
if (( RETCODE != 0 )) ; then 
	echo unable to copy varfile >> $outfile
	exit 1
fi
cp "$SHARED_FOLDER"/target/func_tests.tbz2 .
RETCODE=$?
if (( RETCODE != 0 )) ; then 
	echo unable to copy func tests >> $outfile
	exit 1
fi


# execute the installer
echo running $installer >>$outfile
./$installer -c -q -Dinstall4j.debug=true -Dinstall4j.keepLog=true -varfile JIRA_windows.varfile >>$outfile 2>&1
RETCODE=$?
if (( $RETCODE != 0 )); then 
    echo "problem invoking $installer: $RETCODE" >> $outfile
    exit 1
fi

echo snoozing >>$outfile
sleep 40

# validation step, test installation worked
echo "now validating the installation" >> $outfile

# use wget to do a quick check for expected state
wget $SETUP_URL -O wget.response -o wget.log 

grep "Your Company" wget.response
if (( $? == 0 )); then
    echo "got the setup page through wget - pretty happy at this point" >>$outfile
else
    echo "no setup page - bummer, not bothering with func tests then" >>$outfile
	exit 3
fi


echo "unpacking the func tests" >> $outfile
test -e func_tests.tbz2 || ( echo "no func tests found" >> $outfile; exit 1)
tar jxf func_tests.tbz2 || ( echo "couldn't untar func tests" >> $outfile; exit 1)

pushd jira-func-tests

echo "maven test" >>$outfile
/cygdrive/c/Progra~1/Apache~1/apache-maven-2.1.0/bin/mvn.bat verify -Dtest.server.properties=target/classes/localtest.installer.properties >>$outfile 2>&1
if (( $? == 0 )); then
    echo "maven func test success" >> $outfile
else 
    echo "***  maven func test failure. WARNING: stderr is not logged here, and so you will have to run this manually to find the error. :(" >> $outfile
fi

popd

echo "Taring up the results" >>$outfile

output_tar="${BASE}/output.tar"
touch "${output_tar}"

# Start creating an output tar file. This one gets all the results.
pushd "jira-components/jira-func-tests/target"
    tar rvf "${output_tar}" "surefire-reports" >>$outfile 2>&1 || echo "Could not tar the test results." >>$outfile
popd

# Add the logs to the tar file.
pushd "${JIRA_HOME}"
    tar rvf "${output_tar}" "log" >>$outfile 2>&1 || echo "Could not tar the JIRA logs." >>$outfile
popd

# Get the random output files that we like.
for i in results.txt wget.response wget.log error.log; do
    tar rvf "${output_tar}" "$i" || echo "Could not tar $i." >>$outfile
done

echo "Bzip2 up the results" >>$outfile
bzip2 "${output_tar}" >>$outfile 2>&1



# vim: set sw=4 sts=4 ts=4:
