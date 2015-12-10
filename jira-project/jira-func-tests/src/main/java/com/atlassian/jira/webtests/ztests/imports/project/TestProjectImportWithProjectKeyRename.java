package com.atlassian.jira.webtests.ztests.imports.project;

import java.io.File;
import java.io.IOException;

import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.functest.rules.RemoveAttachmentsRule;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Attachment;

import com.google.common.collect.Iterables;

import org.apache.commons.io.FileUtils;
import org.hamcrest.collection.IsIterableWithSize;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

/**
 * Func tests the project import results screen and the errors leading up to that screen.
 *
 * @since v3.13
 */
@WebTest ({ Category.FUNC_TEST, Category.PROJECT_IMPORT, Category.DATABASE })
public class TestProjectImportWithProjectKeyRename extends AbstractProjectImportTestCase
{
    protected RemoveAttachmentsRule removeAttachmentsRule;

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        backdoor.attachments().enable();
        removeAttachmentsRule = new RemoveAttachmentsRule(this);
        removeAttachmentsRule.before(); // Clean up any left-overs from previous tests
    }

    @Override
    protected void tearDownTest()
    {
        removeAttachmentsRule.after();
        super.tearDownTest();
    }

    private void copyAttachmentFilesToJiraHome()
    {
        final File jiraAttachmentsPath = new File(administration.getJiraHomeDirectory(), "import/attachments");
        final File testAttachmentsPath = new File(environmentData.getXMLDataLocation(), "TestProjectImportWithProjectKeyRename/attachments");

        try
        {
            FileUtils.copyDirectory(testAttachmentsPath, jiraAttachmentsPath);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void testImportAttachmentsFromOldPlace() throws Exception
    {
        copyAttachmentFilesToJiraHome();

        File tempFile = null;

        try
        {
            tempFile = doProjectImport("TestProjectImportWithProjectKeyRename/TestProjectImportWithProjectKeyRename.xml", "TestProjectImportStandardSimpleDataNoProject.xml");

            // Make sure we are ready to import
            tester.assertTextPresent("The results of automatic mapping are displayed below. You will not be able to continue if any validation errors were raised");
            tester.assertSubmitButtonPresent("Import");

            tester.submit("Import");
            advanceThroughWaitingPage();

            // We should end up on the results page
            XPathLocator xPathLocator = new XPathLocator(tester, "//div[@id='systemfields']/ul");
            String projectDetailsResults = xPathLocator.getText();
            assertThat(projectDetailsResults, containsString("Key: MYK"));
            assertThat(projectDetailsResults, containsString("Description: This is a description for a monkey project."));


            xPathLocator = new XPathLocator(tester, "//div[@id='customfields']/ul");
            String customDetailsResults = xPathLocator.getText();
            assertThat(customDetailsResults, containsString("Attachments: 2 out of 2"));

            // We want to test the link on the project Key works fine. It should take us to view project for Monkey
            tester.clickLinkWithText("MYK");
            assertions.assertNodeByIdHasText("project-config-header-name", "monkey");

            final JiraRestClient restClient = createRestClient();

            // Assert that the Project was created with all details
            final com.atlassian.jira.rest.client.api.domain.Project project = restClient.getProjectClient().getProject("MYK").claim();
            assertEquals("monkey", project.getName());
            assertEquals("This is a description for a monkey project.", project.getDescription());
            assertNotNull(project.getUri());
            assertEquals("http://monkeyproject.example.com", project.getUri().toString());
            assertEquals(FRED_FULLNAME, project.getLead().getDisplayName());

            com.atlassian.jira.rest.client.api.domain.Issue issue = restClient.getIssueClient().getIssue("MYK-1").claim();
            assertThat(issue.getAttachments(), IsIterableWithSize.<Attachment>iterableWithSize(2));

            // It's a bit tricky here - because Project Import creates the real attachments asynchronously the order
            // of attachments can change, we need to handle that
            final String detailsAttachmentId, errorsAttachmentId;
            if (Iterables.getFirst(issue.getAttachments(), null).getFilename().equals("details.png"))
            {
                detailsAttachmentId = getAttachmentId(issue, 0);
                errorsAttachmentId = getAttachmentId(issue, 1);
            }
            else
            {
                detailsAttachmentId = getAttachmentId(issue, 1);
                errorsAttachmentId = getAttachmentId(issue, 0);
            }

            assertFilesEqual(new File(administration.getJiraHomeDirectory(), "import/attachments/MKY/MKY-1/10010"),
                    new File(administration.getJiraHomeDirectory(), "data/attachments/MYK/MYK-1/" + detailsAttachmentId));

            assertFilesEqual(new File(administration.getJiraHomeDirectory(), "import/attachments/MKY/MKY-1/10011"),
                    new File(administration.getJiraHomeDirectory(), "data/attachments/MYK/MYK-1/" + errorsAttachmentId));
        }
        finally
        {
            if (tempFile != null)
            {
                tempFile.delete();
            }
        }
    }

    protected String getAttachmentId(final com.atlassian.jira.rest.client.api.domain.Issue issue, final int i)
    {
        final String attachmentUrl = Iterables.get(issue.getAttachments(), i).getSelf().toString();
        return attachmentUrl.substring(attachmentUrl.lastIndexOf('/'));
    }

    public static void assertFilesEqual(final File sourceFile, final File destinationFile) throws IOException
    {
        assertThat(destinationFile.length(), equalTo(sourceFile.length()));
        assertTrue("both files should be the same", FileUtils.contentEquals(sourceFile, destinationFile));
    }

    public void testImportWhenExistingProjectUsedKeyInThePast()
    {
        File tempFile = doProjectImport("TestProjectImportWithProjectKeyRename/TestProjectImportWithProjectKeyRename.xml", "TestProjectImportStandardSimpleDataNoProject.xml");
        try
        {
            long projectId = backdoor.project().addProject("Test", "TES", "admin");
            backdoor.project().addProjectKey(projectId, "MYK");
            backdoor.attachments().enable();

            this.navigation.gotoAdminSection("project_import");
            // Get to the project select page
            tester.setWorkingForm("project-import");
            tester.assertTextPresent("Project Import: Select Backup File");
            tester.setFormElement("backupXmlPath", tempFile.getAbsolutePath());
            tester.submit();

            advanceThroughWaitingPage();
            tester.assertTextPresent("Project Import: Select Project to Import");
            tester.selectOption("projectKey", "monkey");
            //this message is actually wrong raised and issue JRADEV-23548
            text.assertTextPresentHtmlEncoded("The existing project 'Test' used key 'MYK' in the past. You can not re-use the key for a backup project.");
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
