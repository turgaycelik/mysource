package com.atlassian.jira.webtests.ztests.imports.project;

import com.atlassian.jira.functest.framework.HtmlPage;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.meterware.httpunit.WebLink;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Tests the 2nd screen of the project import wizard.
 *
 * @since v3.13
 */
@WebTest ({ Category.FUNC_TEST, Category.PROJECT_IMPORT })
public class TestProjectImportSelectProject extends AbstractProjectImportTestCase
{
    private File tempFile;

    protected void setUpTest()
    {
        // We always need to restore the data and write it out to a tmp file whos path we know
        this.administration.restoreData("TestProjectImportSelectProject.xml");
        final String fileName = "TestProjectImportSelectProject_out.xml";
        log(String.format("Using temporary file '%s'.", fileName));
        final File zipFile = this.administration.exportDataToFile(fileName);
        final InputStream inputStream;
        try
        {
            inputStream = new ZipFile(zipFile).getInputStream(new ZipEntry("entities.xml"));
            final File extractedXml = new File(zipFile.getParentFile(), fileName);
            extractedXml.delete();
            final FileWriter fileWriter = new FileWriter(extractedXml);
            IOUtils.copy(inputStream, fileWriter);
            fileWriter.close();
            tempFile = copyFileToJiraImportDirectory(extractedXml);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    protected void tearDownTest()
    {
        if (!tempFile.delete())
        {
            log(String.format("Unable to delete file '%s'.", tempFile.getAbsoluteFile().getPath()));
        }
    }

    public void testJumpToSelectProjectScreen()
    {
        tester.gotoPage("/secure/admin/ProjectImportSelectProject!default.jspa");
        tester.assertTextPresent("There are no projects to display. Perhaps your session has timed out, please restart the project import wizard.");
    }

    public void testValidationErrors()
    {
        Long projectId = backdoor.project().getProjectId(PROJECT_MONKEY_KEY);
        tester.gotoPage("/secure/project/EditProjectLeadAndDefaultAssignee!default.jspa?pid=" + projectId);

        tester.selectOption("assigneeType", "Project Lead");
        tester.submit("Update");

        // Now toggle the allow unassigned issues setting
        this.administration.generalConfiguration().setAllowUnassignedIssues(false);

        // Lets try our import
        this.navigation.gotoAdminSection("project_import");

        // Get to the project select page
        tester.setWorkingForm("project-import");
        tester.assertTextPresent("Project Import: Select Backup File");
        tester.setFormElement("backupXmlPath", tempFile.getName());
        tester.submit();

        advanceThroughWaitingPage();
        tester.assertTextPresent("Project Import: Select Project to Import");

        tester.selectOption("projectKey", "homosapien");
        tester.submit("Next");

        // There should be errors on this page
        text.assertTextPresentHtmlEncoded("The existing project with key 'HSP' contains '26' issues. You can not import a backup project into a project that contains existing issues.");
        text.assertTextPresentHtmlEncoded("The existing project with key 'HSP' contains '3' versions. You can not import a backup project into a project that contains existing versions.");
        text.assertTextPresentHtmlEncoded("The existing project with key 'HSP' contains '3' components. You can not import a backup project into a project that contains existing components.");

        // try to import the monkey project and get the unassgined error
        tester.selectOption("projectKey", "monkey");
        tester.submit("Next");

        text.assertTextPresentHtmlEncoded("The backup project 'monkey' has 'unassigned' default assignee, but this JIRA instance does not allow unassigned issues.");
    }

    public void testCustomFieldsWrongVersion() throws IOException
    {
        // modify the XML file on disk so that the custom field versions are different
        modifyCustomFieldPluginVersion();

        // Lets try our import
        this.navigation.gotoAdminSection("project_import");

        // Get to the project select page
        tester.setWorkingForm("project-import");
        tester.assertTextPresent("Project Import: Select Backup File");
        tester.setFormElement("backupXmlPath", tempFile.getName());
        tester.submit();

        advanceThroughWaitingPage();
        tester.assertTextPresent("Project Import: Select Project to Import");

        tester.selectOption("projectKey", "monkey");
        tester.submit("Next");

        // There should be errors on this page
        text.assertTextPresentHtmlEncoded("The backup project 'monkey' requires custom field named 'text cf' with full key 'com.atlassian.jira.plugin.system.customfieldtypes:textarea'. In the current instance of JIRA the plugin is at version '1.0', but in the backup it is at version '10.0'.");
        text.assertTextPresentHtmlEncoded("The backup project 'monkey' requires custom field named 'target_milestone' with full key 'com.atlassian.jira.plugin.system.customfieldtypes:select'. In the current instance of JIRA the plugin is at version '1.0', but in the backup it is at version '10.0'.");
        text.assertTextPresentHtmlEncoded("The backup project 'monkey' requires custom field named 'fgsdfgsd' with full key 'com.atlassian.jira.plugin.system.customfieldtypes:textfield'. In the current instance of JIRA the plugin is at version '1.0', but in the backup it is at version '10.0'.");
        text.assertTextPresentHtmlEncoded("The backup project 'monkey' requires custom field named 'number cf' with full key 'com.atlassian.jira.plugin.system.customfieldtypes:float'. In the current instance of JIRA the plugin is at version '1.0', but in the backup it is at version '10.0'.");
    }

    public void testCustomFieldsPluginDoesNotExist() throws IOException
    {
        // modify the XML file on disk so that the custom field versions are different
        deleteCustomFieldPluginVersion();

        // Lets try our import
        this.navigation.gotoAdminSection("project_import");

        // Get to the project select page
        tester.setWorkingForm("project-import");
        tester.assertTextPresent("Project Import: Select Backup File");
        tester.setFormElement("backupXmlPath", tempFile.getName());
        tester.submit();

        advanceThroughWaitingPage();
        tester.assertTextPresent("Project Import: Select Project to Import");

        tester.selectOption("projectKey", "monkey");
        tester.submit("Next");

        // There should be errors on this page
        text.assertTextPresentHtmlEncoded("The backup project 'monkey' requires custom field named 'fgsdfgsd' with full key 'com.atlassian.jira.plugin.system.customfieldtypes:textfield'. In the current instance of JIRA the plugin is at version '1.0', but this custom field was not installed in the backup data. You may want to create an XML backup with this version of the plugin installed.");
        text.assertTextPresentHtmlEncoded("The backup project 'monkey' requires custom field named 'number cf' with full key 'com.atlassian.jira.plugin.system.customfieldtypes:float'. In the current instance of JIRA the plugin is at version '1.0', but this custom field was not installed in the backup data. You may want to create an XML backup with this version of the plugin installed.");
        text.assertTextPresentHtmlEncoded("The backup project 'monkey' requires custom field named 'target_milestone' with full key 'com.atlassian.jira.plugin.system.customfieldtypes:select'. In the current instance of JIRA the plugin is at version '1.0', but this custom field was not installed in the backup data. You may want to create an XML backup with this version of the plugin installed.");
        text.assertTextPresentHtmlEncoded("The backup project 'monkey' requires custom field named 'text cf' with full key 'com.atlassian.jira.plugin.system.customfieldtypes:textarea'. In the current instance of JIRA the plugin is at version '1.0', but this custom field was not installed in the backup data. You may want to create an XML backup with this version of the plugin installed.");
    }

    public void testParseExceptionInJIRAData() throws Exception
    {
        File file = importAndExportBackupAndSetupCurrentInstance("TestProjectImportParseExceptionScreen2.xml", "TestProjectImportSummaryNoCustomFields2.xml");
        try
        {
            // Lets try our import
            this.navigation.gotoAdminSection("project_import");

            // Get to the project select page
            tester.setWorkingForm("project-import");
            tester.assertTextPresent("Project Import: Select Backup File");
            tester.setFormElement("backupXmlPath", file.getAbsolutePath());
            tester.submit();

            advanceThroughWaitingPage();
            tester.assertTextPresent("Project Import: Select Project to Import");

            tester.selectOption("projectKey", "monkey");
            tester.submit("Next");

            advanceThroughWaitingPage();

            // Make sure we see the parse error on the screen
            tester.assertTextPresent("Project Import: Select Project to Import");
            tester.assertTextPresent("There was a problem parsing the backup XML file at");
            // We don't assert the actual pathname because absolute pathing has platform dependent weirdness.
            tester.assertTextPresent("A comment must have an issue id specified.");
        }
        finally
        {
            file.delete();
        }

    }

    private void modifyCustomFieldPluginVersion() throws IOException
    {
        final StringWriter stringWriter = new StringWriter();
        Reader in = new InputStreamReader(new FileInputStream(tempFile));
        copy(in, stringWriter, 1024 * 4);
        in.close();
        String s = stringWriter.toString();
        stringWriter.close();

        s = StringUtils.replace(s, "<PluginVersion id=\"10002\" name=\"Custom Field Types &amp; Searchers\" key=\"com.atlassian.jira.plugin.system.customfieldtypes\" version=\"1.0\"/>", "<PluginVersion id=\"10002\" name=\"Custom Field Types &amp; Searchers\" key=\"com.atlassian.jira.plugin.system.customfieldtypes\" version=\"10.0\"/>");

        FileWriter fw = new FileWriter(tempFile);
        fw.write(s);
        fw.close();
    }

    private void deleteCustomFieldPluginVersion() throws IOException
    {
        final StringWriter stringWriter = new StringWriter();
        Reader in = new InputStreamReader(new FileInputStream(tempFile));
        copy(in, stringWriter, 1024 * 4);
        in.close();
        String s = stringWriter.toString();
        stringWriter.close();

        s = StringUtils.replace(s, "<PluginVersion id=\"10002\" name=\"Custom Field Types &amp; Searchers\" key=\"com.atlassian.jira.plugin.system.customfieldtypes\" version=\"1.0\"/>", "");

        FileWriter fw = new FileWriter(tempFile);
        fw.write(s);
        fw.close();
    }

    private void copy( final Reader input, final Writer output, final int bufferSize )
        throws IOException
    {
        final char[] buffer = new char[ bufferSize ];
        int n = 0;
        while( -1 != ( n = input.read( buffer ) ) )
        {
            output.write( buffer, 0, n );
        }
    }

}
