#!/bin/sh

# Usage: run from within the subprojects/func_tests/xml directory
# Can also be used for selenium-tests module: just copy the script to the xml/ directory of the selenium-tests

# Note: Just because a file is listed here, doesn't mean it isn't used by the func tests in some way.
# Be sure to check that the filename isn't build programmatically.

for f in `ls *.xml`
do
	egrep -qRL --include=*.java "$f" ../java || echo $f
done
