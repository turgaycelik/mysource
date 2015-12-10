package com.atlassian.jira.functest.framework.admin;

import com.atlassian.jira.functest.framework.AbstractFuncTestUtil;
import com.atlassian.jira.functest.framework.Administration;
import com.atlassian.jira.functest.framework.Navigation;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import junit.framework.Assert;
import net.sourceforge.jwebunit.WebTester;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;

/**
 * Implementation of {@link ProjectImport}.
 *
 * @since v4.1
 */
public class ProjectImportImpl extends AbstractFuncTestUtil implements ProjectImport
{
    private final Navigation navigation;
    private final Administration administration;

    public ProjectImportImpl(WebTester tester, JIRAEnvironmentData environmentData, final Navigation navigation, final Administration administration)
    {
        super(tester, environmentData, 2);
        this.navigation = navigation;
        this.administration = administration;
    }

    public File doImportToSummary(final String backupFileName, final String currentSystemXML, final String attachmentPath)
    {
        File tempFile = importAndExportBackupAndSetupCurrentInstance(backupFileName, currentSystemXML);

        File jiraImportDirectory = new File(administration.getJiraHomeDirectory(), "import");
        try
        {
            FileUtils.copyFileToDirectory(tempFile, jiraImportDirectory);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Could not copy file " + tempFile.getAbsolutePath() + " to the import directory in jira home " + jiraImportDirectory, e);
        }

        importToPreImportSummaryPage(attachmentPath, tempFile);

        return tempFile;
    }

    public void importToPreImportSummaryPage(final String attachmentPath, final File tempFile)
    {
        // Lets try our import
        this.navigation.gotoAdminSection("project_import");

        // Get to the project select page
        tester.setWorkingForm("project-import");
        tester.assertTextPresent("Project Import: Select Backup File");
        tester.setFormElement("backupXmlPath", tempFile.getAbsolutePath());
        if (attachmentPath != null)
        {
            tester.setFormElement("backupAttachmentPath", attachmentPath);
        }
        tester.submit();

        advanceThroughWaitingPage();
        tester.assertTextPresent("Project Import: Select Project to Import");

        // Choose the MKY project
        tester.selectOption("projectKey", "monkey");
        tester.submit("Next");
        advanceThroughWaitingPage();
    }

    public File importAndExportBackupAndSetupCurrentInstance(final String backupFileName, final String currentSystemXML)
    {
        // We always need to restore the data and write it out to a tmp file whos path we know
        administration.restoreData(backupFileName);
        // Find a unique temporary filename to export to.
        // We don't need to delete the file, as the export will handle overwrite. It might help us stay unique.
        File tempFile = administration.exportDataToFile(FilenameUtils.removeExtension(backupFileName) + "_out.xml");

        // Now do the test

        // Import the data that has the project data missing and it is ready to be imported
        administration.restoreData(currentSystemXML);
        administration.attachments().enable();
        return tempFile;
    }

    private void advanceThroughWaitingPage()
    {
        int count = 0;
        while (tester.getDialog().getResponseText().indexOf("Project Import: Progress") != -1)
        {
            // We need to click the refresh which should take us to the error page
            tester.submit("Refresh");
            // OK - we are still in progress. Wait a little while before we try again.
            try
            {
                Thread.sleep(100);
            }
            catch (InterruptedException e)
            {
                // Not expected.
                throw new RuntimeException(e);
            }
            // Safety net to make sure that we don't get in an infinite loop.
            count++;
            if (count >= 100)
            {
                Assert.fail("Our project import backup selection has taken too long!");
            }
        }
    }
}
