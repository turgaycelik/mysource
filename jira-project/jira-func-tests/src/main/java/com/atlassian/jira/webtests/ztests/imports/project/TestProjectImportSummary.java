package com.atlassian.jira.webtests.ztests.imports.project;

import java.io.File;

import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import com.meterware.httpunit.HttpUnitOptions;

/**
 * Tests the validation errors that can be displayed on the project import summary page.
 *
 * @since v3.13
 */
@WebTest ({ Category.FUNC_TEST, Category.PROJECT_IMPORT })
public class TestProjectImportSummary extends AbstractProjectImportTestCase
{
    public void testJumpToImportSummary() throws Exception
    {
        this.administration.restoreData("blankWithOldDefault.xml");

        tester.gotoPage("/secure/admin/ProjectImportSummary!default.jspa");
        tester.assertTextPresent("There is no mapping result to display. Perhaps your session has timed out, please restart the project import wizard");
        tester.assertSubmitButtonNotPresent("Import");
        tester.assertSubmitButtonNotPresent("prevButton");
        tester.assertTextPresent("Cancel");
    }

    public void testIssueTypeValidation() throws Exception
    {
        File tempFile = null;
        try
        {
            tempFile = doProjectImport("TestProjectImportSummary.xml", "TestProjectImportSummaryIssueTypes2.xml");

            // Check that the import has been stopped
            tester.assertTextPresent("The data mappings have produced errors, you can not import this project until all errors have been resolved. See below for details.");
            // Check issue type errors
            text.assertTextPresentHtmlEncoded("The issue type 'NotInScheme' exists in the system but is not valid for the projects issue type scheme.");
            text.assertTextPresentHtmlEncoded("The issue type 'Bug' is required for the import but does not exist in the current JIRA instance");
            // Test that there is only one error
            XPathLocator pathLocator = new XPathLocator(tester, "//li[@class='error']");
            assertEquals(1, pathLocator.getNodes().length);
            // Make sure that nothing else on the screen has had any validation done
            pathLocator = new XPathLocator(tester, "//li[@class='unprocessed']");
            assertEquals(20, pathLocator.getNodes().length);

            // Assert we have no Submit Button.
            tester.assertSubmitButtonNotPresent("Import");
        }
        finally
        {
            if (tempFile != null)
            {
                tempFile.delete();
            }
        }
    }

    public void testSubTaskDoesNotExistIssueTypeValidation() throws Exception
    {
        File tempFile = null;
        try
        {
            tempFile = doProjectImport("TestProjectImportSummarySubTask.xml", "TestProjectImportSummarySubTask2.xml");

            // Check that the import has been stopped
            tester.assertTextPresent("The data mappings have produced errors, you can not import this project until all errors have been resolved. See below for details.");
            // Check issue type errors
            text.assertTextPresentHtmlEncoded("Sub-tasks are currently disabled in JIRA, please enable sub-tasks. The sub-task issue type 'NotExistSubTask' is required for the import but does not exist in the current JIRA instance.");
            text.assertTextPresentHtmlEncoded("The issue type 'NonSubTask' is defined as a normal issue type in the backup project, but it is a sub-task issue type in the current JIRA instance.");
            text.assertTextPresentHtmlEncoded("The issue type 'Sub-task' is defined as a sub-task in the backup project, but it is a normal issue type in the current JIRA instance.");
            text.assertTextPresentHtmlEncoded("The issue type 'Bug' is required for the import but does not exist in the current JIRA instance");
            // Test that there is only one error
            XPathLocator pathLocator = new XPathLocator(tester, "//li[@class='error']");
            assertEquals(1, pathLocator.getNodes().length);
            // Make sure that nothing else on the screen has had any validation done
            pathLocator = new XPathLocator(tester, "//li[@class='unprocessed']");
            assertEquals(20, pathLocator.getNodes().length);

            // Assert we have no Submit Button.
            tester.assertSubmitButtonNotPresent("Import");

            // Now enable sub-tasks and make sure we get a different kind of message
            this.administration.activateSubTasks();

            importToPreImportSummaryPage(tempFile);
            // Check that the import has been stopped
            tester.assertTextPresent("The data mappings have produced errors, you can not import this project until all errors have been resolved. See below for details.");
            // Check issue type errors
            text.assertTextPresentHtmlEncoded("The sub-task issue type 'NotExistSubTask' is required for the import but does not exist in the current JIRA instance.");
            text.assertTextPresentHtmlEncoded("The issue type 'NonSubTask' is defined as a normal issue type in the backup project, but it is a sub-task issue type in the current JIRA instance.");
            text.assertTextPresentHtmlEncoded("The issue type 'Sub-task' is defined as a sub-task in the backup project, but it is a normal issue type in the current JIRA instance.");
            text.assertTextPresentHtmlEncoded("The issue type 'Bug' is required for the import but does not exist in the current JIRA instance");
            // Test that there is only one error
            pathLocator = new XPathLocator(tester, "//li[@class='error']");
            assertEquals(1, pathLocator.getNodes().length);
            // Make sure that nothing else on the screen has had any validation done
            pathLocator = new XPathLocator(tester, "//li[@class='unprocessed']");
            assertEquals(20, pathLocator.getNodes().length);

            // Assert we have no Submit Button.
            tester.assertSubmitButtonNotPresent("Import");
        }
        finally
        {
            if (tempFile != null)
            {
                tempFile.delete();
            }
        }
    }

    public void testCustomFieldValidation() throws Exception
    {
        File tempFile = null;
        try
        {
            tempFile = doProjectImport("TestProjectImportSummary.xml", "TestProjectImportSummaryCustomFields2.xml");

            // Make sure that Issue Types validated.
            XPathLocator pathLocator = new XPathLocator(tester, "//li/img[@alt='OK']");
            assertEquals(1, pathLocator.getNodes().length);

            // Check that the import has been stopped
            tester.assertTextPresent("The data mappings have produced errors, you can not import this project until all errors have been resolved. See below for details.");
            // Assert we have no Submit Button.
            tester.assertSubmitButtonNotPresent("Import");

            // See the errors for Custom Fields.
            text.assertTextPresentHtmlEncoded("The custom field 'number cf' in the backup project is of type 'Number Field' but the field with the same name in the current JIRA instance is of a different type.");
            text.assertTextPresentHtmlEncoded("The custom field 'Cascading Select CF' in the backup project is used by issue types 'Bug' but the field with the same name in the current JIRA instance is not available to those issue types in this project.");
            text.assertTextPresentHtmlEncoded("The custom field 'Project CF' of type 'Project Picker (single project)' is required for the import but does not exist in the current JIRA instance.");
            // Test that there is only one error
            pathLocator = new XPathLocator(tester, "//li[@class='error']");
            assertEquals(1, pathLocator.getNodes().length);
            // Test that the other sections are "Not validated"
            pathLocator = new XPathLocator(tester, "//li[@class='unprocessed']");
            assertEquals(19, pathLocator.getNodes().length);
        }
        finally
        {
            if (tempFile != null)
            {
                tempFile.delete();
            }
        }
    }

    public void testStatusValidationDefaultWorkflowNoProject() throws Exception
    {
        File tempFile = null;
        try
        {
            tempFile = doProjectImport("TestProjectImportSummary.xml", "TestProjectImportSummaryStatuses2.xml");


            // Make sure that most sections are validated.
            XPathLocator pathLocator = new XPathLocator(tester, "//li/img[@alt='OK']");
            assertEquals(19, pathLocator.getNodes().length);

            // Check that the import has been stopped
            tester.assertTextPresent("The data mappings have produced errors, you can not import this project until all errors have been resolved. See below for details.");
            // Assert we have no Submit Button.
            tester.assertSubmitButtonNotPresent("Import");

            // See the errors for Custom Fields.
            text.assertTextPresentHtmlEncoded("The status 'Snafu' is required for the import but does not exist in the current JIRA instance.");
            text.assertTextPresentHtmlEncoded("The status 'Fubar' is in use by an issue of type 'NonSubTask' in the backup file. The default workflow 'jira', which is associated with issue type 'NonSubTask', does not use this status. This workflow is not editable. You must create a project with key 'MKY', instead of letting the import create it for you, and associate a workflow with issue type 'NonSubTask' that uses the status. To do this you will need to use a workflow scheme.");
            text.assertTextPresentHtmlEncoded("The status 'Snafu' is in use by an issue of type 'Sub-task' in the backup file. The default workflow 'jira', which is associated with issue type 'Sub-task', does not use this status. This workflow is not editable. You must create a project with key 'MKY', instead of letting the import create it for you, and associate a workflow with issue type 'Sub-task' that uses the status. To do this you will need to use a workflow scheme.");
            // Test that there is only one error
            pathLocator = new XPathLocator(tester, "//li[@class='error']");
            assertEquals(1, pathLocator.getNodes().length);
            // Test that no other sections are "Not validated"
            pathLocator = new XPathLocator(tester, "//li[@class='unprocessed']");
            assertEquals(0, pathLocator.getNodes().length);
        }
        finally
        {
            if (tempFile != null)
            {
                tempFile.delete();
            }
        }
    }

    public void testStatusValidationDefaultWorkflowProjectExists() throws Exception
    {
        File tempFile = null;
        try
        {
            tempFile = doProjectImport("TestProjectImportSummary.xml", "TestProjectImportSummaryStatusesProjectExists2.xml");


            // Make sure that most sections are validated.
            XPathLocator pathLocator = new XPathLocator(tester, "//li/img[@alt='OK']");
            assertEquals(19, pathLocator.getNodes().length);

            // Check that the import has been stopped
            tester.assertTextPresent("The data mappings have produced errors, you can not import this project until all errors have been resolved. See below for details.");
            // Assert we have no Submit Button.
            tester.assertSubmitButtonNotPresent("Import");

            // See the errors for Custom Fields.
            text.assertTextPresentHtmlEncoded("The status 'Snafu' is required for the import but does not exist in the current JIRA instance.");
            text.assertTextPresentHtmlEncoded("The status 'Fubar' is in use by an issue of type 'NonSubTask' in the backup file. The default workflow 'jira', which is associated with issue type 'NonSubTask', does not use this status. This workflow is not editable. You must associate a workflow with issue type 'NonSubTask' that uses the status. To do this you will need to use a workflow scheme.");
            text.assertTextPresentHtmlEncoded("The status 'Snafu' is in use by an issue of type 'Sub-task' in the backup file. The default workflow 'jira', which is associated with issue type 'Sub-task', does not use this status. This workflow is not editable. You must associate a workflow with issue type 'Sub-task' that uses the status. To do this you will need to use a workflow scheme.");
            // Test that there is only one error
            pathLocator = new XPathLocator(tester, "//li[@class='error']");
            assertEquals(1, pathLocator.getNodes().length);
            // Test that no other sections are "Not validated"
            pathLocator = new XPathLocator(tester, "//li[@class='unprocessed']");
            assertEquals(0, pathLocator.getNodes().length);
        }
        finally
        {
            if (tempFile != null)
            {
                tempFile.delete();
            }
        }
    }

    public void testStatusValidationCustomWorkflowProjectExists() throws Exception
    {
        File tempFile = null;
        try
        {
            tempFile = doProjectImport("TestProjectImportSummary.xml", "TestProjectImportSummaryStatusesProjectExistsCustomWorkflow2.xml");


            // Make sure that most sections are validated.
            XPathLocator pathLocator = new XPathLocator(tester, "//li/img[@alt='OK']");
            assertEquals(19, pathLocator.getNodes().length);

            // Check that the import has been stopped
            tester.assertTextPresent("The data mappings have produced errors, you can not import this project until all errors have been resolved. See below for details.");
            // Assert we have no Submit Button.
            tester.assertSubmitButtonNotPresent("Import");

            // See the errors for Custom Fields.
            text.assertTextPresentHtmlEncoded("The status 'Snafu' is required for the import but does not exist in the current JIRA instance.");
            text.assertTextPresentHtmlEncoded("The status 'Fubar' is in use by an issue of type 'NonSubTask' in the backup file. The workflow 'Copy of jira', which is associated with issue type 'NonSubTask', does not use this status. You must either edit the workflow to use the status or associate a workflow with issue type 'NonSubTask' that uses the status.");
            text.assertTextPresentHtmlEncoded("The status 'Snafu' is in use by an issue of type 'Sub-task' in the backup file. The workflow 'Copy of jira', which is associated with issue type 'Sub-task', does not use this status. You must either edit the workflow to use the status or associate a workflow with issue type 'Sub-task' that uses the status.");
            // Test that there is only one error
            pathLocator = new XPathLocator(tester, "//li[@class='error']");
            assertEquals(1, pathLocator.getNodes().length);
            // Test that no other sections are "Not validated"
            pathLocator = new XPathLocator(tester, "//li[@class='unprocessed']");
            assertEquals(0, pathLocator.getNodes().length);
        }
        finally
        {
            if (tempFile != null)
            {
                tempFile.delete();
            }
        }
    }

    public void testPriorityAndResolutionValidation() throws Exception
    {
        File tempFile = null;
        try
        {
            tempFile = doProjectImport("TestProjectImportSummary.xml", "TestProjectImportSummaryResolutionPriority2.xml");

            // Make sure that most sections are validated.
            XPathLocator pathLocator = new XPathLocator(tester, "//li/img[@alt='OK']");
            assertEquals(18, pathLocator.getNodes().length);

            // Check that the import has been stopped
            tester.assertTextPresent("The data mappings have produced errors, you can not import this project until all errors have been resolved. See below for details.");
            // Assert we have no Submit Button.
            tester.assertSubmitButtonNotPresent("Import");

            // See the errors for Priority.
            text.assertTextPresentHtmlEncoded("The priority 'Major' is required for the import but does not exist in the current JIRA instance.");
            // See the error fof Resolution
            text.assertTextPresentHtmlEncoded("The resolution 'Fixed' is required for the import but does not exist in the current JIRA instance.");
            // "Johnson" warning should only be shown when we CAN import.
            tester.assertTextNotPresent("Please note that performing an import will cause JIRA to be unavailable to all users until the import has completed.");

            // Test that there are only two errors
            pathLocator = new XPathLocator(tester, "//li[@class='error']");
            assertEquals(2, pathLocator.getNodes().length);
            // Test that no other sections are "Not validated"
            pathLocator = new XPathLocator(tester, "//li[@class='unprocessed']");
            assertEquals(0, pathLocator.getNodes().length);
        }
        finally
        {
            if (tempFile != null)
            {
                tempFile.delete();
            }
        }
    }

    public void testUserValidation() throws Exception
    {
        // NOTE: this only tests the warning, there is another test TestProjectImportUsersDoNotExistPage which tests
        // the error condition
        File tempFile = null;
        try
        {
            tempFile = doProjectImport("TestProjectImportSummary.xml", "TestProjectImportSummaryUser2.xml");

            // Make sure that most sections are validated.
            XPathLocator pathLocator = new XPathLocator(tester, "//li/img[@alt='OK']");
            assertEquals(19, pathLocator.getNodes().length);

            // Check that the import has NOT been stopped
            tester.assertTextNotPresent("The data mappings have produced errors, you can not import this project until all errors have been resolved. See below for details.");
            // Assert we have a Submit Button.
            tester.assertSubmitButtonPresent("Import");

            // See the warning for Users.
            text.assertTextPresentHtmlEncoded("There are '1' users that will be automatically created if the import continues.");
            // Test that there are no errors
            pathLocator = new XPathLocator(tester, "//li[@class='error']");
            assertEquals(0, pathLocator.getNodes().length);
            // Test that no other sections are "Not validated"
            pathLocator = new XPathLocator(tester, "//li[@class='unprocessed']");
            assertEquals(0, pathLocator.getNodes().length);
        }
        finally
        {
            if (tempFile != null)
            {
                tempFile.delete();
            }
        }
    }

    public void testProjectRoleValidation() throws Exception
    {
        File tempFile = null;
        try
        {
            tempFile = doProjectImport("TestProjectImportSummary.xml", "TestProjectImportSummaryProjectRole2.xml");

            // Make sure that most sections are validated.
            XPathLocator pathLocator = new XPathLocator(tester, "//li/img[@alt='OK']");
            assertEquals(19, pathLocator.getNodes().length);

            // Check that the import has been stopped
            tester.assertTextPresent("The data mappings have produced errors, you can not import this project until all errors have been resolved. See below for details.");
            // Assert we have no Submit Button.
            tester.assertSubmitButtonNotPresent("Import");

            // See the errors for ProjectRole.
            // One of these is from a comment, and one from a worklog
            text.assertTextPresentHtmlEncoded("The project role 'Developers' is required for the import but does not exist in the current JIRA instance.");
            text.assertTextPresentHtmlEncoded("The project role 'dudes' is required for the import but does not exist in the current JIRA instance.");
            // Test that there are only two errors
            pathLocator = new XPathLocator(tester, "//li[@class='error']");
            assertEquals(1, pathLocator.getNodes().length);
            // Test that no other sections are "Not validated"
            pathLocator = new XPathLocator(tester, "//li[@class='unprocessed']");
            assertEquals(0, pathLocator.getNodes().length);
        }
        finally
        {
            if (tempFile != null)
            {
                tempFile.delete();
            }
        }
    }

    public void testGroupValidation() throws Exception
    {
        File tempFile = null;
        try
        {
            tempFile = doProjectImport("TestProjectImportSummary.xml", "TestProjectImportSummaryGroups2.xml");
            // Make sure that most sections are validated.
            XPathLocator pathLocator = new XPathLocator(tester, "//li/img[@alt='OK']");
            assertEquals(19, pathLocator.getNodes().length);

            // Check that the import has been stopped
            tester.assertTextPresent("The data mappings have produced errors, you can not import this project until all errors have been resolved. See below for details.");
            // Assert we have no Submit Button.
            tester.assertSubmitButtonNotPresent("Import");

            // See the errors for Group.
            // One of these is from a comment, and one from a worklog
            text.assertTextPresentHtmlEncoded("The group 'baddies' is required for the import but does not exist in the current JIRA instance.");
            text.assertTextPresentHtmlEncoded("The group 'goodies' is required for the import but does not exist in the current JIRA instance.");
            tester.assertTextNotPresent("The group &#39;The Others&#39; is required for the import but does not exist in the current JIRA instance.");
            tester.assertTextNotPresent("The group &#39;The Dark Brotherhood&#39; is required for the import but does not exist in the current JIRA instance.");
            // Test that there is one error
            pathLocator = new XPathLocator(tester, "//li[@class='error']");
            assertEquals(1, pathLocator.getNodes().length);
            // Test that no other sections are "Not validated"
            pathLocator = new XPathLocator(tester, "//li[@class='unprocessed']");
            assertEquals(0, pathLocator.getNodes().length);
        }
        finally
        {
            if (tempFile != null)
            {
                tempFile.delete();
            }
        }
    }

    public void testIssueLinkTypeValidation() throws Exception
    {
        File tempFile = null;
        try
        {
            tempFile = doProjectImport("TestProjectImportSummary.xml", "TestProjectImportSummaryIssueLinkTypes2.xml");

            // Make sure that most sections are validated.
            XPathLocator pathLocator = new XPathLocator(tester, "//li/img[@alt='OK']");
            assertEquals(19, pathLocator.getNodes().length);

            // Check that the import has been stopped
            tester.assertTextPresent("The data mappings have produced errors, you can not import this project until all errors have been resolved. See below for details.");
            // Assert we have no Submit Button.
            tester.assertSubmitButtonNotPresent("Import");

            // See the errors for Issue Link Type.
            tester.assertTextPresent("The project to import includes subtasks, but subtasks are disabled in the current system.");
            text.assertTextPresentHtmlEncoded("The Issue Link Type 'Duplicate' is required for the import but does not exist in the current JIRA instance.");
            // Test that there is one error
            pathLocator = new XPathLocator(tester, "//li[@class='error']");
            assertEquals(1, pathLocator.getNodes().length);
            // Test that no other sections are "Not validated"
            pathLocator = new XPathLocator(tester, "//li[@class='unprocessed']");
            assertEquals(0, pathLocator.getNodes().length);
        }
        finally
        {
            if (tempFile != null)
            {
                tempFile.delete();
            }
        }
    }

    public void testIssueCustomFieldOptionsValidation() throws Exception
    {
        File tempFile = null;
        try
        {
            tempFile = doProjectImport("TestProjectImportSummary.xml", "TestProjectImportSummaryCustomFieldOptions2.xml");

            // Make sure that most sections are validated.
            XPathLocator pathLocator = new XPathLocator(tester, "//li/img[@alt='OK']");
            assertEquals(19, pathLocator.getNodes().length);

            // Check that the import has been stopped
            tester.assertTextPresent("The data mappings have produced errors, you can not import this project until all errors have been resolved. See below for details.");
            // Assert we have no Submit Button.
            tester.assertSubmitButtonNotPresent("Import");

            // See the errors for Custom Field Options
            text.assertTextPresentHtmlEncoded("The custom field 'Cascading Select CF' requires option with parent option 'A' and child option 'A2' for the import but it does not exist in the current JIRA instance.");
            // Test that there is one error
            pathLocator = new XPathLocator(tester, "//li[@class='error']");
            assertEquals(1, pathLocator.getNodes().length);
            // Test that no other sections are "Not validated"
            pathLocator = new XPathLocator(tester, "//li[@class='unprocessed']");
            assertEquals(0, pathLocator.getNodes().length);
        }
        finally
        {
            if (tempFile != null)
            {
                tempFile.delete();
            }
        }
    }

    public void testIssueCustomFieldValuesValidation() throws Exception
    {
        File tempFile = null;
        try
        {
            tempFile = doProjectImport("TestProjectImportSummaryCustomFieldValues.xml", "TestProjectImportSummaryCustomFieldValues2.xml");

            // Make sure that most sections are validated.
            XPathLocator pathLocator = new XPathLocator(tester, "//li/img[@alt='OK']");
            assertEquals(15, pathLocator.getNodes().length);

            // Check that the import has been stopped
            tester.assertTextPresent("The data mappings have produced errors, you can not import this project until all errors have been resolved. See below for details.");
            // Assert we have no Submit Button.
            tester.assertSubmitButtonNotPresent("Import");

            // See the errors for Custom Field Options
            text.assertTextPresentHtmlEncoded("The custom field 'target_milestone' requires option 'dylan 1' for the import but it does not exist in the current JIRA instance.");
            text.assertTextPresent("The custom field &#39;Multi--select cf&#39; requires option &#39;option 2&#39; for the import but it does not exist in the current JIRA instance. (NOTE: repetitive spaces have been replaced by '-' characters)");
            text.assertTextPresentHtmlEncoded("The group 'goodies' is required for the import but does not exist in the current JIRA instance.");
            // This one is a warning
            text.assertTextPresentHtmlEncoded("The project 'HSP' does not exist in the current JIRA system. This custom field value will not be imported.");

            // Test that there are 4 errors
            pathLocator = new XPathLocator(tester, "//li[@class='error']");
            assertEquals(4, pathLocator.getNodes().length);
            // Test that no other sections are "Not validated"
            pathLocator = new XPathLocator(tester, "//li[@class='unprocessed']");
            assertEquals(0, pathLocator.getNodes().length);


        }
        finally
        {
            if (tempFile != null)
            {
                tempFile.delete();
            }
        }
    }

    public void testFileAttachmentsValidation21FilesDoNotExist() throws Exception
    {
        File jiraImportAttachmentsDirectory = new File(new File(administration.getJiraHomeDirectory(), "import"), "attachments");
        File mky = new File(jiraImportAttachmentsDirectory, "MKY");
        mky.mkdir();

        File tempFile = null;
        try
        {

            // Get to the summary screen
            tempFile = doProjectImport("TestProjectImportSummary.xml", "TestProjectImportSummaryFileAttachments2.xml");

            // Check that there are 20 warning messages and 1 message about there being too many warnings, also check there is not a warning about the 1 file that exists
            tester.assertTextPresent("There are more than twenty attachment entries that do not exist in the attachment directory. See your logs for full details.");

            // Check that the import has NOT been stopped
            tester.assertTextNotPresent("The data mappings have produced errors, you can not import this project until all errors have been resolved. See below for details.");
            // Assert we have a Submit Button.
            tester.assertSubmitButtonPresent("Import");
        }
        finally
        {
            mky.delete();
            if (tempFile != null)
            {
                tempFile.delete();
            }
        }
    }

    public void testFileAttachmentsValidationProjectDirectoryDoesNotExist() throws Exception
    {
        File tempFile = null;

        try
        {
            // Get to the summary screen
            tempFile = doProjectImport("TestProjectImportSummary.xml", "TestProjectImportSummaryFileAttachments2.xml");

            // Check that there are 20 warning messages and 1 message about there being too many warnings, also check there is not a warning about the 1 file that exists
            text.assertTextPresentHtmlEncoded("The provided attachment path does not contain a sub-directory called 'MKY'. If you proceed with the import attachments will not be included.");

            // Check that the import has NOT been stopped
            tester.assertTextNotPresent("The data mappings have produced errors, you can not import this project until all errors have been resolved. See below for details.");
            // Assert we have a Submit Button.
            tester.assertSubmitButtonPresent("Import");

        }
        finally
        {
            if (tempFile != null)
            {
                tempFile.delete();
            }
        }
    }

    public void testFileAttachmentsValidation20FilesDoNotExist() throws Exception
    {
        File tempFileOther = null;
        // Fake up the attachments directory
        File attachmentsBackupDir = null;
        File projectDir = null;
        File issueDir = null;
        File tempFile = null;
        try
        {
            // Create a unique temp folder to use as our "attachments backup directory"
            attachmentsBackupDir = new File(new File(administration.getJiraHomeDirectory(), "import"), "attachments");

            projectDir = new File(attachmentsBackupDir, "MKY");
            projectDir.mkdir();
            issueDir = new File(projectDir, "MKY-1");
            issueDir.mkdir();
            tempFile = new File(issueDir, "10020_test.html");
            tempFile.createNewFile();

            // Get to the summary screen
            tempFileOther = doProjectImport("TestProjectImportSummary.xml", "TestProjectImportSummaryFileAttachments2.xml");

            // Check that there are 20 warning messages and 1 message about there being too many warnings, also check there is not a warning about the 1 file that exists
            tester.assertTextNotPresent("The attachment 'test.html' does not exist at '" + tempFile.getAbsolutePath() + "'. It will not be imported.");
            text.assertTextPresentHtmlEncoded("The attachment 'test1.txt' does not exist at '" + issueDir.getAbsolutePath() + File.separator + "10030'. It will not be imported.");
            text.assertTextPresentHtmlEncoded("The attachment 'test2.txt' does not exist at '" + issueDir.getAbsolutePath() + File.separator + "10040'. It will not be imported.");
            text.assertTextPresentHtmlEncoded("The attachment 'test3.txt' does not exist at '" + issueDir.getAbsolutePath() + File.separator + "10050'. It will not be imported.");
            text.assertTextPresentHtmlEncoded("The attachment 'test4.txt' does not exist at '" + issueDir.getAbsolutePath() + File.separator + "10060'. It will not be imported.");
            text.assertTextPresentHtmlEncoded("The attachment 'test5.txt' does not exist at '" + issueDir.getAbsolutePath() + File.separator + "10070'. It will not be imported.");
            text.assertTextPresentHtmlEncoded("The attachment 'test6.txt' does not exist at '" + issueDir.getAbsolutePath() + File.separator + "10080'. It will not be imported.");
            text.assertTextPresentHtmlEncoded("The attachment 'test7.txt' does not exist at '" + issueDir.getAbsolutePath() + File.separator + "10090'. It will not be imported.");
            text.assertTextPresentHtmlEncoded("The attachment 'test8.txt' does not exist at '" + issueDir.getAbsolutePath() + File.separator + "10100'. It will not be imported.");
            text.assertTextPresentHtmlEncoded("The attachment 'test9.txt' does not exist at '" + issueDir.getAbsolutePath() + File.separator + "10110'. It will not be imported.");
            text.assertTextPresentHtmlEncoded("The attachment 'test10.txt' does not exist at '" + issueDir.getAbsolutePath() + File.separator + "10120'. It will not be imported.");
            text.assertTextPresentHtmlEncoded("The attachment 'test11.txt' does not exist at '" + issueDir.getAbsolutePath() + File.separator + "10130'. It will not be imported.");
            text.assertTextPresentHtmlEncoded("The attachment 'test12.txt' does not exist at '" + issueDir.getAbsolutePath() + File.separator + "10140'. It will not be imported.");
            text.assertTextPresentHtmlEncoded("The attachment 'test13.txt' does not exist at '" + issueDir.getAbsolutePath() + File.separator + "10150'. It will not be imported.");
            text.assertTextPresentHtmlEncoded("The attachment 'test14.txt' does not exist at '" + issueDir.getAbsolutePath() + File.separator + "10160'. It will not be imported.");
            text.assertTextPresentHtmlEncoded("The attachment 'test15.txt' does not exist at '" + issueDir.getAbsolutePath() + File.separator + "10170'. It will not be imported.");
            text.assertTextPresentHtmlEncoded("The attachment 'test16.txt' does not exist at '" + issueDir.getAbsolutePath() + File.separator + "10180'. It will not be imported.");
            text.assertTextPresentHtmlEncoded("The attachment 'test17.txt' does not exist at '" + issueDir.getAbsolutePath() + File.separator + "10190'. It will not be imported.");
            text.assertTextPresentHtmlEncoded("The attachment 'test18.txt' does not exist at '" + issueDir.getAbsolutePath() + File.separator + "10200'. It will not be imported.");
            text.assertTextPresentHtmlEncoded("The attachment 'test19.txt' does not exist at '" + issueDir.getAbsolutePath() + File.separator + "10210'. It will not be imported.");
            text.assertTextPresentHtmlEncoded("The attachment 'test20.txt' does not exist at '" + issueDir.getAbsolutePath() + File.separator + "10220'. It will not be imported.");

            // Check that the import has NOT been stopped
            tester.assertTextNotPresent("The data mappings have produced errors, you can not import this project until all errors have been resolved. See below for details.");
            // Assert we have a Submit Button.
            tester.assertSubmitButtonPresent("Import");

        }
        finally
        {
            if (tempFile != null)
            {
                tempFile.delete();
            }
            if (issueDir != null)
            {
                issueDir.delete();
            }
            if (projectDir != null)
            {
                projectDir.delete();
            }
            if (tempFileOther != null)
            {
                tempFileOther.delete();
            }
        }

    }


    public void testNoCustomFieldsIncludedAndHappyPath() throws Exception
    {
        File tempFile = null;
        try
        {
            tempFile = doProjectImport("TestProjectImportSummaryNoCustomFields.xml", "TestProjectImportSummaryNoCustomFields2.xml");
            // Make sure that most sections are validated.
            XPathLocator pathLocator = new XPathLocator(tester, "//li/img[@alt='OK']");
            assertEquals(11, pathLocator.getNodes().length);

            // Check that the import has NOT been stopped
            tester.assertTextNotPresent("The data mappings have produced errors, you can not import this project until all errors have been resolved. See below for details.");
            // Make sure the no custom fields message is there
            tester.assertTextPresent("No importable custom fields");
            // Test that there is one error
            pathLocator = new XPathLocator(tester, "//li[@class='error']");
            assertEquals(0, pathLocator.getNodes().length);
            // "Johnson" warning should be shown when we can import.
            tester.assertTextPresent("Please note that performing an import will cause JIRA to be unavailable to all users until the import has completed.");
            // Test that only the no custom fields message is "Not validated"
            pathLocator = new XPathLocator(tester, "//li[@class='unprocessed']");
            assertEquals(1, pathLocator.getNodes().length);

            tester.assertSubmitButtonPresent("Import");
        }
        finally
        {
            if (tempFile != null)
            {
                tempFile.delete();
            }
        }
    }


    public void testReMapAndValidate() throws Exception
    {
        File tempFile = null;
        try
        {
            tempFile = doProjectImport("TestProjectImportSummary.xml", "TestProjectImportSummaryGroups2.xml");
            // Make sure that most sections are validated.
            XPathLocator pathLocator = new XPathLocator(tester, "//li/img[@alt='OK']");
            assertEquals(19, pathLocator.getNodes().length);

            // Check that the import has been stopped
            tester.assertTextPresent("The data mappings have produced errors, you can not import this project until all errors have been resolved. See below for details.");
            // Assert we have no Submit Button.
            tester.assertSubmitButtonNotPresent("Import");

            // See the errors for Group.
            // One of these is from a comment, and one from a worklog
            text.assertTextPresentHtmlEncoded("The group 'baddies' is required for the import but does not exist in the current JIRA instance.");
            text.assertTextPresentHtmlEncoded("The group 'goodies' is required for the import but does not exist in the current JIRA instance.");
            // Test that there is one error
            pathLocator = new XPathLocator(tester, "//li[@class='error']");
            assertEquals(1, pathLocator.getNodes().length);
            // Test that no other sections are "Not validated"
            pathLocator = new XPathLocator(tester, "//li[@class='unprocessed']");
            assertEquals(0, pathLocator.getNodes().length);


            // Now fix the group problems
            backdoor.usersAndGroups().addGroup("baddies");
            backdoor.usersAndGroups().addGroup("goodies");

            // Now jump back to the page and verify that it has not been updated yet
            tester.gotoPage("/secure/admin/ProjectImportSummary!default.jspa");
            // Check that the import has been stopped
            tester.assertTextPresent("The data mappings have produced errors, you can not import this project until all errors have been resolved. See below for details.");
            // Assert we have no Submit Button.
            tester.assertSubmitButtonNotPresent("Import");
            // Assert we have the Refresh Validations button
            tester.assertSubmitButtonPresent("refreshValidationButton");

            // See the errors for Group.
            // One of these is from a comment, and one from a worklog
            text.assertTextPresentHtmlEncoded("The group 'baddies' is required for the import but does not exist in the current JIRA instance.");
            text.assertTextPresentHtmlEncoded("The group 'goodies' is required for the import but does not exist in the current JIRA instance.");
            // Test that there is one error
            pathLocator = new XPathLocator(tester, "//li[@class='error']");
            assertEquals(1, pathLocator.getNodes().length);
            // Test that no other sections are "Not validated"
            pathLocator = new XPathLocator(tester, "//li[@class='unprocessed']");
            assertEquals(0, pathLocator.getNodes().length);

            // Now click the remap/validate link and make sure we have picked up the changes
            tester.clickButton("refreshValidationButton");
            advanceThroughWaitingPage();

            // Check that the import has NOT been stopped
            tester.assertTextNotPresent("The data mappings have produced errors, you can not import this project until all errors have been resolved. See below for details.");
            // Assert we have a Submit Button.
            tester.assertSubmitButtonPresent("Import");

            // Now click the remap/validate LINK
            tester.clickLinkWithText("Refresh validations");
            advanceThroughWaitingPage();

            // Check that the import has NOT been stopped
            tester.assertTextNotPresent("The data mappings have produced errors, you can not import this project until all errors have been resolved. See below for details.");
            // Assert we have a Submit Button.
            tester.assertSubmitButtonPresent("Import");

            // See the errors for Group are no longer present.
            text.assertTextNotPresent("The group &#39;baddies&#39; is required for the import but does not exist in the current JIRA instance.");
            text.assertTextNotPresent("The group &#39;goodies&#39; is required for the import but does not exist in the current JIRA instance.");
            // Test that there are no errors
            pathLocator = new XPathLocator(tester, "//li[@class='error']");
            assertEquals(0, pathLocator.getNodes().length);
            // Test that no other sections are "Not validated"
            pathLocator = new XPathLocator(tester, "//li[@class='unprocessed']");
            assertEquals(0, pathLocator.getNodes().length);

        }
        finally
        {
            if (tempFile != null)
            {
                tempFile.delete();
            }
        }
    }


    public void testPreviousButton() throws Exception
    {
        File tempFile = null;
        try
        {
            tempFile = doProjectImport("TestProjectImportSummary.xml", "TestProjectImportSummaryGroups2.xml");
            // Make sure that most sections are validated.
            XPathLocator pathLocator = new XPathLocator(tester, "//li/img[@alt='OK']");
            assertEquals(19, pathLocator.getNodes().length);

            // Check that the import has been stopped
            tester.assertTextPresent("The data mappings have produced errors, you can not import this project until all errors have been resolved. See below for details.");
            // Assert we have no Submit Button.
            tester.assertSubmitButtonNotPresent("Import");

            // See the errors for Group.
            // One of these is from a comment, and one from a worklog
            text.assertTextPresentHtmlEncoded("The group 'baddies' is required for the import but does not exist in the current JIRA instance.");
            text.assertTextPresentHtmlEncoded("The group 'goodies' is required for the import but does not exist in the current JIRA instance.");
            // Test that there is one error
            pathLocator = new XPathLocator(tester, "//li[@class='error']");
            assertEquals(1, pathLocator.getNodes().length);
            // Test that no other sections are "Not validated"
            pathLocator = new XPathLocator(tester, "//li[@class='unprocessed']");
            assertEquals(0, pathLocator.getNodes().length);


            // Now click previous and see that we are on the project select page
            tester.submit("prevButton");

            // Check that we are on the project select page with all the projects present
            tester.assertTextPresent("Project Import: Select Project to Import");

            tester.selectOption("projectKey", "monkey");
            tester.selectOption("projectKey", "homosapien");
        }
        finally
        {
            if (tempFile != null)
            {
                tempFile.delete();
            }
        }
    }

    public void testProjectImportResumeLinkStep2() throws Exception
    {
        File tempFile = null;
        try
        {
            // Get outselves to Step2
            final String backupFileName = "TestProjectImportSummaryNoCustomFields.xml";
            final String currentSystemXML = "TestProjectImportSummaryNoCustomFields2.xml";

            // We always need to restore the data and write it out to a tmp file whos path we know
            this.administration.restoreData(backupFileName);


            // Get the temp dir
            tempFile = this.administration.exportDataToFile("TestProjectImportSummaryNoCustomFields_out.xml");
            tempFile = copyFileToJiraImportDirectory(tempFile);

            // Now do the test

            // Import the data that has the project data missing and it is ready to be imported
            this.administration.restoreData(currentSystemXML);
            administration.attachments().enable();

            // Lets try our import
            this.navigation.gotoAdminSection("project_import");

            // Make sure the resume link is not present
            tester.assertTextNotPresent("You were in the middle of performing an import");

            // Get to the project select page
            tester.setWorkingForm("project-import");
            tester.assertTextPresent("Project Import: Select Backup File");
            tester.setFormElement("backupXmlPath", tempFile.getName());
            tester.submit();

            advanceThroughWaitingPage();
            tester.assertTextPresent("Project Import: Select Project to Import");

            // Now jump to first page and make sure the resume link is there
            this.navigation.gotoAdminSection("project_import");
            // Make sure we have the resume link
            tester.assertTextPresent("You were in the middle of performing an import. Would you");

            HttpUnitOptions.setScriptingEnabled(true);

            tester.clickLinkWithText("resume");
            // Make sure we are on step 2
            tester.assertTextPresent("Select the project you wish to import");
            tester.selectOption("projectKey", "monkey");
            tester.selectOption("projectKey", "homosapien");

            // Test that the cancel button clears my session
            tester.clickLink("cancelButton");
            tester.assertTextPresent("This tool allows you to import a single JIRA project from a backup file.");
            tester.assertTextNotPresent("You were in the middle of performing an import. Would you");
        }
        finally
        {
            if (tempFile != null)
            {
                tempFile.delete();
            }
            HttpUnitOptions.setScriptingEnabled(false);
        }
    }

    public void testProjectImportResumeLinkStep3() throws Exception
    {
        File tempFile = null;
        try
        {
            // Get us to the 3rd Step
            tempFile = doProjectImport("TestProjectImportSummaryNoCustomFields.xml", "TestProjectImportSummaryNoCustomFields2.xml");
            // Make sure that most sections are validated.
            XPathLocator pathLocator = new XPathLocator(tester, "//li/img[@alt='OK']");
            assertEquals(11, pathLocator.getNodes().length);

            // Check that the import has NOT been stopped
            tester.assertTextNotPresent("The data mappings have produced errors, you can not import this project until all errors have been resolved. See below for details.");
            // Make sure the no custom fields message is there
            tester.assertTextPresent("No importable custom fields");
            // Test that there is one error
            pathLocator = new XPathLocator(tester, "//li[@class='error']");
            assertEquals(0, pathLocator.getNodes().length);
            // Test that only the no custom fields message is "Not validated"
            pathLocator = new XPathLocator(tester, "//li[@class='unprocessed']");
            assertEquals(1, pathLocator.getNodes().length);

            tester.assertSubmitButtonPresent("Import");

            // Now go back to the Step 1 screen
            // Now jump to first page and make sure the resume link is there
            this.navigation.gotoAdminSection("project_import");
            // Make sure we have the resume link
            tester.assertTextPresent("You were in the middle of performing an import for project 'monkey'. Would you");

            HttpUnitOptions.setScriptingEnabled(true);

            tester.clickLinkWithText("resume");
            advanceThroughWaitingPage();
            // Make sure we are on step 3
            tester.assertTextNotPresent("The data mappings have produced errors, you can not import this project until all errors have been resolved. See below for details.");
            // Make sure the no custom fields message is there
            tester.assertTextPresent("No importable custom fields");
            // Test that there is one error
            pathLocator = new XPathLocator(tester, "//li[@class='error']");
            assertEquals(0, pathLocator.getNodes().length);
            // Test that only the no custom fields message is "Not validated"
            pathLocator = new XPathLocator(tester, "//li[@class='unprocessed']");
            assertEquals(1, pathLocator.getNodes().length);

            tester.assertSubmitButtonPresent("Import");

            // Test that the cancel button clears my session
            tester.clickLink("cancelButton");
            tester.assertTextPresent("This tool allows you to import a single JIRA project from a backup file.");
            tester.assertTextNotPresent("You were in the middle of performing an import of project 'monkey'. Would you");
        }
        finally
        {
            if (tempFile != null)
            {
                tempFile.delete();
            }
        }

    }


    public void testIssueSecurityLevelValidation() throws Exception
    {
        File tempFile = null;
        try
        {
            tempFile = doProjectImport("TestProjectImportSummaryWithSecurityLevels.xml", "TestProjectImportSummaryNoCustomFields2.xml");
            // Make sure that most sections are validated.
            XPathLocator pathLocator = new XPathLocator(tester, "//li/img[@alt='OK']");
            assertEquals(10, pathLocator.getNodes().length);

            // Check that the import has been stopped
            tester.assertTextPresent("The data mappings have produced errors, you can not import this project until all errors have been resolved. See below for details.");
            // Assert we have no Submit Button.
            tester.assertSubmitButtonNotPresent("Import");

            // See the errors for IssueSecurityLevel.
            text.assertTextPresentHtmlEncoded("The issue security level 'level2' is required for the import. Please create a project with key 'MKY', and configure its issue security scheme.");
            // Test that there is one error
            pathLocator = new XPathLocator(tester, "//li[@class='error']");
            assertEquals(1, pathLocator.getNodes().length);
            // Test that only custom field values are unvalidated
            pathLocator = new XPathLocator(tester, "//li[@class='unprocessed']");
            assertEquals(1, pathLocator.getNodes().length);
        }
        finally
        {
            if (tempFile != null)
            {
                tempFile.delete();
            }
        }
    }

    public void testIssueSecurityLevelValidationProjectExists() throws Exception
    {
        File tempFile = null;
        try
        {
            tempFile = doProjectImport("TestProjectImportSummaryWithSecurityLevels.xml", "TestProjectImportSummaryIssueSecurity2.xml");
            // Make sure that most sections are validated.
            XPathLocator pathLocator = new XPathLocator(tester, "//li/img[@alt='OK']");
            assertEquals(10, pathLocator.getNodes().length);

            // Check that the import has been stopped
            tester.assertTextPresent("The data mappings have produced errors, you can not import this project until all errors have been resolved. See below for details.");
            // Assert we have no Submit Button.
            tester.assertSubmitButtonNotPresent("Import");

            // See the errors for IssueSecurityLevel.
            text.assertTextPresentHtmlEncoded("The issue security level 'level2' is required for the import but does not exist in the configured issue security scheme for this project.");
            // Test that there is one error
            pathLocator = new XPathLocator(tester, "//li[@class='error']");
            assertEquals(1, pathLocator.getNodes().length);
            // Test that only custom field values are unvalidated
            pathLocator = new XPathLocator(tester, "//li[@class='unprocessed']");
            assertEquals(1, pathLocator.getNodes().length);
        }
        finally
        {
            if (tempFile != null)
            {
                tempFile.delete();
            }
        }
    }
}
