---------------------------------------------------------------------
JIRA @VERSION@-#@BUILD_NUMBER@ WAR DISTRIBUTION README
---------------------------------------------------------------------

Thank you for downloading the JIRA @VERSION@ - WAR (Webapp ARchive) distribution.

This distribution is intended to be used as a WAR deployed in a J2EE
application server (such as Apache Tomcat).

You can also download a distribution which is bundled with
Apache Tomcat and will run out of the box. That distribution
is a lot easier to set up and you can always migrate your
data between JIRA installations very easily using the XML
export/import.


REQUIREMENTS
------------

You will need Oracle (formerly Sun) Java Development Kit (JDK) or
Java Runtime Environment (JRE) version 1.6 or above.

See our Supported Platforms page for details on what Java (and other)
Platforms are supported by JIRA:

http://docs.atlassian.com/jira/docs-@DOCS_VERSION@/Supported+Platforms


BRIEF INSTALL GUIDE
-------------------

For the impatient, here's a brief installation guide:

1. (Optional) Edit 'edit-webapp/WEB-INF/classes/entityengine.xml' to ensure
   the transaction factory is configured correctly for the app server (see below).

2. Specify the location of your JIRA Home Directory:
   edit 'edit-webapp/WEB-INF/classes/jira-application.properties' and set the jira.home property to your JIRA home directory.

   Alternatively you can add a web context property called 'jira.home'. This property is set in different files depending on your servlet application server.
   For example, you may need to configure the server.xml file (for Tomcat), configure the web.xml file, or set 'Context parameter' options on the deployment UI of the application server.
   See http://docs.atlassian.com/jira/docs-@DOCS_VERSION@/Setting+your+JIRA+Home+Directory for more information how to configure this parameter.

3. (Optional) Place any custom source files that should be compiled into the
    src/ directory. (Remember to create the appropriate package directories)

4. Run 'build.bat' (for Windows) or 'build.sh' (for Linux/Solaris) to
   build the WAR file.

5. Configure your servlet container as described in the appropriate instructions
   for your application server within the following section:

   http://docs.atlassian.com/jira/docs-@DOCS_VERSION@/Installing+JIRA+WAR

   JIRA relies on the servlet container to provide a Transaction Manager
   (javax.transaction.UserTransaction). See the contents of the 'etc'
   subdirectory for sample configurations.

6. Deploy the resulting WAR in your application server.

For details on editing entityengine.xml, see

http://docs.atlassian.com/jira/docs-@DOCS_VERSION@/Configuring+the+Entity+Engine+for+JIRA


EDITING CONFIGURATION FILES
---------------------------

The build process works by copying everything in webapp/ to a temporary
directory, then overwriting those files with everything from edit-webapp/.

This enables you to store edited files (eg. configuration files) in
edit-webapp/, and preserve these changes when you upgrade JIRA.

To edit a particular configuration file:

 - locate the file you want to edit in webapp/
 - copy it to the same location under edit-webapp/
   (i.e. if it was webapp/WEB-INF/classes/entityengine.xml, copy it to
   edit-webapp/WEB-INF/classes/entityengine.xml)
 - edit the file
 - rebuild the WAR using 'build.bat' or 'build.sh'

Most files you might want to edit are in webapp/WEB-INF/classes.

Please ask on the mailing list before editing a file if you are unsure
what the file does.

Some commonly edited files (in WEB-INF/classes):

 - jira-application.properties -- Edit to configure JIRAs general configuration.
   For example the JIRA home directory (jira.home property).
 - entityengine.xml -- Edit for your server to change the transaction
   factory.
 - templates/*.vm -- Edit to configure JIRA's outgoing email. These are
   standard Velocity templates.
 - log4j.properties -- Edit to adjust the JIRA logging levels.  In JIRA
   3 and above, you should rather edit log levels through the web
   interface.


CUSTOM SOURCE FILES
-------------------

If you would like to extend JIRA's functionality, you can get your source
files compiled as part of the build process. Just place your source files
in the src/ directory (the directory is empty by default). Remember to create
the required sub-directories under the src directory to match the package
names of your source files, as otherwise the build process will fail. The
files are compiled into the WEB-INF/classes directory of the web application.


UPGRADING
---------

To upgrade JIRA from version 4.0.0, you should proceed as if making a clean
installation and then 'point' JIRA to your original database. The process is
described at:

http://docs.atlassian.com/jira/docs-@DOCS_VERSION@/Upgrading+JIRA+Manually

To upgrade JIRA from a version prior to 4.0.0, you should proceed as if making
a clean installation and then import an XML backup of your old data. The
process is described at:

http://docs.atlassian.com/jira/docs-@DOCS_VERSION@/Migrating+JIRA+to+Another+Server


ERRORS
------

If you encounter any problems, please create a support request at:
http://support.atlassian.com


QUESTIONS?
----------

Questions? Try the docs at:

http://docs.atlassian.com/jira/docs-@DOCS_VERSION@

or ask at Atlassian Answers for JIRA:

https://answers.atlassian.com/tags/jira/


-----------------------------------------------------------
Happy issue tracking and thank you for using JIRA!
- The Atlassian Team
-----------------------------------------------------------

