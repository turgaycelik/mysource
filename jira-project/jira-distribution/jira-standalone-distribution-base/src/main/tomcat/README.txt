----------------------------------------------
JIRA @VERSION@-#@BUILD_NUMBER@ README
----------------------------------------------

Thank you for downloading JIRA @VERSION@. This distribution comes with a
built-in Tomcat @TOMCAT_VERSION@ application server, so it runs (almost)
out of the box.

BRIEF INSTALL GUIDE
-------------------

1. Install Oracle's (formerly Sun's) Java Development Kit (JDK) or
   Java Runtime Environment (JRE) version 1.6 or above:

   http://www.oracle.com/technetwork/java/javase/downloads/index.html

2. Set the JAVA_HOME variable to where you installed Java. The Windows
   and Linux installers will do this for you. See the following instructions
   for details:

   http://docs.atlassian.com/jira/docs-@DOCS_VERSION@/Installing+Java

3. Set your JIRA Home Directory.
   Instructions on how to set your JIRA Home Directory can be found here:

   http://docs.atlassian.com/jira/docs-@DOCS_VERSION@/Setting+your+JIRA+Home+Directory

4. Run 'bin\start-jira.bat' (for Windows) or 'bin/start-jira.sh' (for Linux/Solaris)
   to start JIRA. Check that there are no errors on the console. See below for
   troubleshooting advice.

5. Point your browser at http://localhost:8080/
   You should see JIRA's Setup Wizard.

Full documentation is available online at:

http://docs.atlassian.com/jira/docs-@DOCS_VERSION@/Installing+JIRA


PROBLEMS?
---------

A common startup problem is when another program has claimed port 8080, which
JIRA is configured to run on by default. To avoid this port conflict, JIRA's
port can be changed in conf/server.xml.

If you encounter any problems, please create a support request at:
http://support.atlassian.com


QUESTIONS?
----------

Questions? Try the docs at:

http://docs.atlassian.com/jira/docs-@DOCS_VERSION@/

or ask at Atlassian Answers for JIRA:

https://answers.atlassian.com/tags/jira


-----------------------------------------------------------
Happy issue tracking and thank you for using JIRA!
- The Atlassian Team
-----------------------------------------------------------
