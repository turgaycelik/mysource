#!/bin/sh
# Tar up Heap files
mkdir dumps
find -name '*.hprof' -exec mv {} dumps \;
count=`find dumps -type f | wc -l`
if [ $count -gt 0 ]; then
   tar -vczf target/heapdump.tgz dumps
fi
rm -rf dumps

# look for memory inspection result in logs and print them to the error output
grep "atlassian.jira.memoryinspector.MemoryInspector" "target/jirahome/log/atlassian-jira.log" 1>&2
if [ $? -eq 0 ]; then
    # if grep matched then $? = 0, so we've found MemoryInspector - fail build!
    exit 62
else
    # grep didn't found anything - pass the build
    exit 0
fi