package com.atlassian.jira.webtests.ztests.imports.project;

import com.atlassian.jira.functest.framework.FuncTestCase;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * Has some base methods for testing ProjectImport.
 *
 * @since v3.13
 */
public abstract class AbstractProjectImportTestCase extends FuncTestCase
{
    public void advanceThroughWaitingPage()
    {
        int count = 0;
        while (tester.getDialog().getResponseText().indexOf("Project Import: Progress") != -1)
        {
            // We need to click the refresh which should take us to the next page
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
                fail("Our project import backup selection has taken too long!");
            }
        }
    }

    /**
     * Takes two XML paths and runs the Project Import up to the summary screen.
     * The first backup file is imported and then exported to the temp directory.
     * We need to do this to migrate the file to the latest version of JIRA.
     * Then we import the second file. (This will usually have some config changes that will show errors and/or warnings
     * on the Summary Screen.)
     * <p>
     * This method then kicks off a project import on the MKY project and takes it up to the Summary page.
     * </p>
     *
     * @param backupFileName   The File that we project import.
     * @param currentSystemXML The "current system" state.
     * @return The temp file, which should be deleted by the calling test.
     */
    public File doProjectImport(String backupFileName, String currentSystemXML)
    {
        return doProjectImport(backupFileName, currentSystemXML, false);
    }

    /**
     * Takes two XML paths and runs the Project Import up to the summary screen.
     * The first backup file is imported and then exported to the temp directory.
     * We need to do this to migrate the file to the latest version of JIRA.
     * Then we import the second file. (This will usually have some config changes that will show errors and/or warnings
     * on the Summary Screen.)
     * <p>
     * This method then kicks off a project import on the MKY project and takes it up to the Summary page.
     * </p>
     *
     * @param backupFileName   The File that we project import.
     * @param currentSystemXML The "current system" state.
     * @param doPluginsRefresh specify <code>true</code> if you are testing plugins and need JIRA to do a full refresh
     * on data import; if testing core JIRA then a quick import should suffice.
     * @return The temp file, which should be deleted by the calling test.
     */
    public File doProjectImport(String backupFileName, String currentSystemXML, final boolean doPluginsRefresh)
    {
        File tempFile = importAndExportBackupAndSetupCurrentInstance(backupFileName, currentSystemXML, doPluginsRefresh);

        importToPreImportSummaryPage(tempFile);

        return tempFile;
    }

    protected File copyFileToJiraImportDirectory(final File file)
    {
        File jiraImportDirectory = new File(administration.getJiraHomeDirectory(), "import");
        if(file.getParentFile().equals(jiraImportDirectory))
        {
            //File already in the import directory, no need to copy
            return file;
        }

        try
        {
            FileUtils.copyFileToDirectory(file, jiraImportDirectory);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Could not copy file " + file.getAbsolutePath() + " to the import directory in jira home " + jiraImportDirectory, e);
        }
        return new File(jiraImportDirectory, file.getName());
    }

    public void importToPreImportSummaryPage(final File tempFile)
    {
        copyFileToJiraImportDirectory(tempFile);

        // Lets try our import
        this.navigation.gotoAdminSection("project_import");

        // Get to the project select page
        tester.setWorkingForm("project-import");
        tester.assertTextPresent("Project Import: Select Backup File");
        tester.setFormElement("backupXmlPath", tempFile.getName());
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
        return importAndExportBackupAndSetupCurrentInstance(backupFileName, currentSystemXML, false);
    }

    public File importAndExportBackupAndSetupCurrentInstance(final String backupFileName, final String currentSystemXML, final boolean doPluginsRefresh)
    {
        // We always need to restore the data and write it out to a tmp file whos path we know
        if (doPluginsRefresh)
        {
            this.administration.restoreDataSlowOldWay(backupFileName);
        }
        else
        {
            this.administration.restoreData(backupFileName, false);
        }

        // We don't need to delete the file, as the export will handle overwrite. It might help us stay unique.
        File backupFile = this.administration.exportDataToFile(FilenameUtils.removeExtension(backupFileName) + "_out.xml");

        // Now do the test
        backupFile = copyFileToJiraImportDirectory(backupFile);

        // Import the data that has the project data missing and it is ready to be imported
        this.administration.restoreData(currentSystemXML, false);
        this.administration.attachments().enable();
        return backupFile;
    }
}
