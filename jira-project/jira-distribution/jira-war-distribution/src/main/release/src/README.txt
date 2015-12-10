If you have custom Java files to compile, place them
here (within the appropriate package directory tree, eg. com/company/jira/YourCode.java). Please note
that if your class imports javax.servlet classes, you will need to edit the &lt;javac> section in
../build.xml to include a &lt;pathelement to a servlet.jar class (eg. common/lib/servlet-api.jar in
Tomcat 5.x)