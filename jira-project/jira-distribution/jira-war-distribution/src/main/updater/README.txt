*************************
Note: This update system is not in use at the moment,
pending the fixing of bugs in the .bat wrapper script.
**************************

JIRA Updater
------------

This directory contains JIRA's update system.  With the JIRA updater,
you can browse the list of bug fixes, language packs and other updates
that Atlassian makes available.  Once an update is selected, it can be
applied to the local system.

To start, run 'jira-update' from a console.

Each time an update is applied, a timestamped log file recording what
occurred is written in the logs/ directory.  A record of all applied
patches is kept in
../atlassian-jira/WEB-INF/classes/applied-updates.properties


Firewalls
---------

The Atlassian website needs to be accessible from the host running
'jira-update'. If you are behind a firewall, you should set the
following properties:

export JIRA_OPTS='-Dhttp.proxyHost=<web-proxy> -Dhttp.proxyPort=<port>'

or on Windows

set JIRA_OPTS='-Dhttp.proxyHost=<web-proxy> -Dhttp.proxyPort=<port>'

