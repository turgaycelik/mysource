package com.atlassian.jira.webtests.ztests.issue.clone;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.locator.Locator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Tests the Issue Linking that happens in a Clone operation.
 * See http://jira.atlassian.com/browse/JRA-17222
 */
@WebTest({Category.FUNC_TEST, Category.ATTACHMENTS, Category.CLONE_ISSUE })
public class TestCloneIssueAttachments extends FuncTestCase
{
    private static final String ATTACHMENT_FILE_NAME_1 = "info.txt";
    private static final String ATTACHMENT_FILE_NAME_2 = "anotherfile.txt";
    private static final String ATTACHMENT_ID_1 = "10010";
    private static final String ATTACHMENT_ID_2 = "10020";
    private static final String ATTACHMENT_CONTENTS_1 = "AAAAA";
    private static final String ATTACHMENT_CONTENTS_2 = "BBBBB";
    private static final String CLONE_ATTACHMENTS_CHECKBOX_NAME = "cloneAttachments";
    private static final String ORIGINAL_ISSUE_KEY = "RAT-1";
    private static final String ORIGINAL_SUBTASK_KEY_1 = "RAT-2";
    private static final String ORIGINAL_SUBTASK_KEY_3 = "RAT-4";
    private static final String ORIGINAL_COW_ISSUE_KEY = "COW-16";

    private static final String ATTACH_FILE_ISSUE_OPERATION_LINK_ID = "attach-file";

    private static final String DESIGN_ATTACHMENT_FILE_NAME_1 = "design_attachment_1.txt";
    private static final String DESIGN_ATTACHMENT_FILE_NAME_2 = "design_attachment_2.txt";
    private static final String DESIGN_ATTACHMENT_CONTENTS_1 = "Design Attachments Stuff";
    private static final String DESIGN_ATTACHMENT_CONTENTS_2 = "Some other Design Attachments Stuff";
    private static final String DESIGN_ATTACHMENT_ID_1 = "10030";
    private static final String DESIGN_ATTACHMENT_ID_2 = "10031";

    private static final String IMPLEMENT_ATTACHMENT_FILE_NAME_1 = "implment_attachment_1.txt";
    private static final String IMPLEMENT_ATTACHMENT_CONTENTS_1 = "Implement Attachments Stuff";
    private static final String IMPLEMENT_ATTACHMENT_ID_1 = "10032";

    private static final String COW_ATTACHMENT_FILE_NAME_1 = "cowfile.txt";

    private static final String CLONE_ATTACHMENTS_CHECKBOX_LABEL = "Clone Attachments";
    private static final String CLONE_ISSUE_OPERATION_NAME = "clone-issue";
    private static final String SUMMARY_FIELD_ID = "summary";
    private static final String CLONE_SUBTASKS_CHECK_BOX_ID = "cloneSubTasks";
    private static final String CREATE_BUTTON_NAME = "Create";

    private String attachmentPath;
    
    protected void setUpTest()
    {
        administration.restoreData("TestCloneIssueAttachments.xml");
        administration.attachments().enable();
        backdoor.darkFeatures().enableForSite("ka.NO_GLOBAL_SHORTCUT_LINKS");

        attachmentPath = administration.getCurrentAttachmentPath();

        installAttachedFile(ORIGINAL_ISSUE_KEY, ATTACHMENT_CONTENTS_1, ATTACHMENT_FILE_NAME_1, ATTACHMENT_ID_1);
        installAttachedFile(ORIGINAL_ISSUE_KEY, ATTACHMENT_CONTENTS_2, ATTACHMENT_FILE_NAME_2, ATTACHMENT_ID_2);
        installAttachedFile(ORIGINAL_SUBTASK_KEY_1, DESIGN_ATTACHMENT_CONTENTS_1, DESIGN_ATTACHMENT_FILE_NAME_1, DESIGN_ATTACHMENT_ID_1);
        installAttachedFile(ORIGINAL_SUBTASK_KEY_1, DESIGN_ATTACHMENT_CONTENTS_2, DESIGN_ATTACHMENT_FILE_NAME_2, DESIGN_ATTACHMENT_ID_2);
        installAttachedFile(ORIGINAL_SUBTASK_KEY_3, IMPLEMENT_ATTACHMENT_CONTENTS_1, IMPLEMENT_ATTACHMENT_FILE_NAME_1, IMPLEMENT_ATTACHMENT_ID_1);
    }

    private void installAttachedFile(final String issueKey, final String contents, String attachmentFileName, String attachmentId) 
    {
        File attachedFile = new File(attachmentPath + "/RAT/" + issueKey, attachmentId + "_" + attachmentFileName);
        attachedFile.getParentFile().mkdirs();

        PrintWriter out = null;
        try
        {
            out = new PrintWriter(new FileWriter(attachedFile));
            out.print(contents);
        }
        catch (IOException ioe)
        {
            throw new RuntimeException(ioe);
        }
        finally
        {
            out.close();
        }
    }

    public void testCloneAttachmentsDisabled() throws Exception
    {
        navigation.issue().viewIssue(ORIGINAL_ISSUE_KEY);

        // Ensure attachments are enabled
        tester.assertLinkPresent(ATTACH_FILE_ISSUE_OPERATION_LINK_ID);

        administration.attachments().disable();

        navigation.issue().viewIssue(ORIGINAL_ISSUE_KEY);
        
        // Ensure attachments are now disabled
        tester.assertLinkNotPresent(ATTACH_FILE_ISSUE_OPERATION_LINK_ID);

        // Ensure the attachments are there
        tester.assertLinkPresentWithText(ATTACHMENT_FILE_NAME_1);
        tester.assertLinkPresentWithText(ATTACHMENT_FILE_NAME_2);

        // Clone the issue
        tester.clickLink(CLONE_ISSUE_OPERATION_NAME);

        // Ensure the option to clone attachments does not exist as attachments are disabled
        tester.assertTextNotPresent(CLONE_ATTACHMENTS_CHECKBOX_LABEL);
    }

    public void testCloneAttachmentsAvailable() throws Exception
    {
        navigation.issue().viewIssue(ORIGINAL_ISSUE_KEY);

        // Ensure the attachments are there
        tester.assertLinkPresentWithText(ATTACHMENT_FILE_NAME_1);
        tester.assertLinkPresentWithText(ATTACHMENT_FILE_NAME_2);

        // Clone the issue
        tester.clickLink(CLONE_ISSUE_OPERATION_NAME);
        // Ensure we offer the option to clone attachments
        tester.assertTextPresent(CLONE_ATTACHMENTS_CHECKBOX_LABEL);

        navigation.issue().attachments(ORIGINAL_ISSUE_KEY).manage().delete();
        navigation.issue().viewIssue(ORIGINAL_ISSUE_KEY);

        // Clone the issue
        tester.clickLink(CLONE_ISSUE_OPERATION_NAME);
        // Ensure we offer the option to clone attachments
        tester.assertTextPresent(CLONE_ATTACHMENTS_CHECKBOX_LABEL);

        // Delete attachments on the first sub-task
        navigation.issue().attachments(ORIGINAL_SUBTASK_KEY_1).manage().delete();
        navigation.issue().viewIssue(ORIGINAL_ISSUE_KEY);

        // Clone the issue
        tester.clickLink(CLONE_ISSUE_OPERATION_NAME);
        // Ensure we still offer the option to clone attachments
        tester.assertTextPresent(CLONE_ATTACHMENTS_CHECKBOX_LABEL);

        // Delete attachments on the first sub-task
        navigation.issue().attachments(ORIGINAL_SUBTASK_KEY_3).manage().delete();
        navigation.issue().viewIssue(ORIGINAL_ISSUE_KEY);

        // Clone the issue
        tester.clickLink(CLONE_ISSUE_OPERATION_NAME);
        // Now we should not offer to clone attachments as the issue has no attachments and neither do the sub-tasks
        tester.assertTextNotPresent(CLONE_ATTACHMENTS_CHECKBOX_LABEL);

        // Ensure it works when no sub-tasks are involved
        navigation.issue().viewIssue(ORIGINAL_COW_ISSUE_KEY);

        // Ensure the attachments are there
        tester.assertLinkPresentWithText(COW_ATTACHMENT_FILE_NAME_1);

        // Clone the issue
        tester.clickLink(CLONE_ISSUE_OPERATION_NAME);
        // Ensure we offer the option to clone attachments
        tester.assertTextPresent(CLONE_ATTACHMENTS_CHECKBOX_LABEL);

        navigation.issue().attachments(ORIGINAL_COW_ISSUE_KEY).manage().delete();
        navigation.issue().viewIssue(ORIGINAL_COW_ISSUE_KEY);

        // Clone the issue
        tester.clickLink(CLONE_ISSUE_OPERATION_NAME);
        // Ensure theoption to clone attachments is not there as the issue has no attachments
        tester.assertTextNotPresent(CLONE_ATTACHMENTS_CHECKBOX_LABEL);
    }

    public void testCloneAttachmentsAvailableForSubTasks() throws Exception
    {
        // Check if cloning attachments option is presented when cloning a sub-task
        navigation.issue().viewIssue(ORIGINAL_SUBTASK_KEY_1);

        // Ensure the attachments are there
        tester.assertLinkPresentWithText(DESIGN_ATTACHMENT_FILE_NAME_1);
        tester.assertLinkPresentWithText(DESIGN_ATTACHMENT_FILE_NAME_2);

        // Clone the issue
        tester.clickLink(CLONE_ISSUE_OPERATION_NAME);
        // Ensure we offer the option to clone attachments
        tester.assertTextPresent(CLONE_ATTACHMENTS_CHECKBOX_LABEL);

        navigation.issue().attachments(ORIGINAL_SUBTASK_KEY_1).manage().delete();
        navigation.issue().viewIssue(ORIGINAL_SUBTASK_KEY_1);

        // Clone the issue
        tester.clickLink(CLONE_ISSUE_OPERATION_NAME);
        // Ensure we do not offer the option to clone attachments as the issue has no attachments
        tester.assertTextNotPresent(CLONE_ATTACHMENTS_CHECKBOX_LABEL);
    }

    public void testCloneNoLinksNoSubTasksNoAttachments() throws Exception
    {
        navigation.issue().viewIssue(ORIGINAL_ISSUE_KEY);

        // Ensure the attachments are there
        tester.assertLinkPresentWithText(ATTACHMENT_FILE_NAME_1);
        tester.assertLinkPresentWithText(ATTACHMENT_FILE_NAME_2);

        // Clone the issue
        tester.clickLink(CLONE_ISSUE_OPERATION_NAME);

        // Ensure the option to enable attachments exists
        tester.assertTextPresent(CLONE_ATTACHMENTS_CHECKBOX_LABEL);
        tester.assertCheckboxNotSelected(CLONE_ATTACHMENTS_CHECKBOX_NAME);

        // Init the form with values
        final String cloneSummary = "CLONE - Library attempts HTTP communications with URLs that are in the Trackback Filter.";
        tester.setFormElement(SUMMARY_FIELD_ID, cloneSummary);
        tester.uncheckCheckbox(CLONE_SUBTASKS_CHECK_BOX_ID);
        tester.submit(CREATE_BUTTON_NAME);

        // Ensure we are on the view issue page and that the attachment was not created.
        tester.assertTextPresent(cloneSummary);
        // Ensure that attachments have not been copied as we did not select for them to be copied
        tester.assertLinkNotPresentWithText(ATTACHMENT_FILE_NAME_1);
        tester.assertLinkNotPresentWithText(ATTACHMENT_FILE_NAME_2);

        // Ensure the attachments are still on the original issue
        navigation.issue().viewIssue(ORIGINAL_ISSUE_KEY);
        tester.assertLinkPresentWithText(ATTACHMENT_FILE_NAME_1);
        tester.assertLinkPresentWithText(ATTACHMENT_FILE_NAME_2);

        navigation.issue().attachments(ORIGINAL_ISSUE_KEY).manage().delete();
    }

    public void testCloneNoLinksNoSubTasksCopyAttachments() throws Exception
    {
        navigation.issue().viewIssue(ORIGINAL_ISSUE_KEY);

        // Ensure the attachments are there
        tester.assertLinkPresentWithText(ATTACHMENT_FILE_NAME_1);
        tester.assertLinkPresentWithText(ATTACHMENT_FILE_NAME_2);

        // Clone the issue
        tester.clickLink(CLONE_ISSUE_OPERATION_NAME);

        tester.assertTextPresent(CLONE_ATTACHMENTS_CHECKBOX_LABEL);
        tester.assertCheckboxNotSelected(CLONE_ATTACHMENTS_CHECKBOX_NAME);

        final String cloneSummary = "CLONE - Library attempts HTTP communications with URLs that are in the Trackback Filter.";
        tester.setFormElement(SUMMARY_FIELD_ID, cloneSummary);
        tester.uncheckCheckbox(CLONE_SUBTASKS_CHECK_BOX_ID);
        tester.checkCheckbox(CLONE_ATTACHMENTS_CHECKBOX_NAME, "true");
        tester.submit(CREATE_BUTTON_NAME);

        // Assert that the attachments have been cloned
        tester.assertTextPresent(cloneSummary);
        tester.assertLinkPresentWithText(ATTACHMENT_FILE_NAME_1);
        tester.assertLinkPresentWithText(ATTACHMENT_FILE_NAME_2);

        // Extract the key of the new issue
        String cloneIssueKey = extractIssueKey();

        tester.clickLinkWithText(ATTACHMENT_FILE_NAME_1);
        // Now we should see the data in the info.txt is correct.
        tester.assertTextPresent(ATTACHMENT_CONTENTS_1);

        navigation.issue().viewIssue(cloneIssueKey);
        tester.clickLinkWithText(ATTACHMENT_FILE_NAME_2);
        // Now we should see the data in the anotherfile.txt is correct.
        tester.assertTextPresent(ATTACHMENT_CONTENTS_2);

        // Go to the clone issue
        navigation.issue().viewIssue(cloneIssueKey);

        // Delete attachment
        navigation.issue().attachments(cloneIssueKey).manage().delete();

        // Go to the clone issue
        navigation.issue().viewIssue(cloneIssueKey);
        tester.assertLinkNotPresentWithText(ATTACHMENT_FILE_NAME_1);
        tester.assertLinkNotPresentWithText(ATTACHMENT_FILE_NAME_2);

        // Go to the original issue
        navigation.issue().viewIssue(ORIGINAL_ISSUE_KEY);
        // Ensure the attachments are still there
        tester.assertLinkPresentWithText(ATTACHMENT_FILE_NAME_1);
        tester.assertLinkPresentWithText(ATTACHMENT_FILE_NAME_2);

        tester.clickLinkWithText(ATTACHMENT_FILE_NAME_1);
        // Now we should see the data in the info.txt file is correct.
        tester.assertTextPresent(ATTACHMENT_CONTENTS_1);

        navigation.issue().viewIssue(ORIGINAL_ISSUE_KEY);
        tester.clickLinkWithText(ATTACHMENT_FILE_NAME_2);
        // Now we should see the data in the anotherfile.txt file is correct.
        tester.assertTextPresent(ATTACHMENT_CONTENTS_2);

        navigation.issue().attachments(ORIGINAL_ISSUE_KEY).manage().delete();
    }

    public void testCloneNoLinksWithSubTasksCopyAttachments() throws Exception
    {
        navigation.issue().viewIssue(ORIGINAL_ISSUE_KEY);

        // Ensure the attachments are there
        tester.assertLinkPresentWithText(ATTACHMENT_FILE_NAME_1);
        tester.assertLinkPresentWithText(ATTACHMENT_FILE_NAME_2);

        // Clone the issue
        tester.clickLink(CLONE_ISSUE_OPERATION_NAME);

        tester.assertTextPresent(CLONE_ATTACHMENTS_CHECKBOX_LABEL);
        tester.assertCheckboxNotSelected(CLONE_ATTACHMENTS_CHECKBOX_NAME);

        final String cloneSummary = "CLONE - Library attempts HTTP communications with URLs that are in the Trackback Filter.";
        tester.setFormElement(SUMMARY_FIELD_ID, cloneSummary);
        tester.assertFormElementEquals(CLONE_SUBTASKS_CHECK_BOX_ID, "true");
        tester.checkCheckbox(CLONE_ATTACHMENTS_CHECKBOX_NAME, "true");
        tester.submit(CREATE_BUTTON_NAME);

        // Assert that the attachments have been cloned
        tester.assertTextPresent(cloneSummary);
        tester.assertLinkPresentWithText(ATTACHMENT_FILE_NAME_1);
        tester.assertLinkPresentWithText(ATTACHMENT_FILE_NAME_2);

        // Extract the key of the new issue
        String cloneIssueKey = extractIssueKey();

        tester.clickLinkWithText(ATTACHMENT_FILE_NAME_1);
        // Now we should see the data in the info.txt is correct.
        tester.assertTextPresent(ATTACHMENT_CONTENTS_1);

        navigation.issue().viewIssue(cloneIssueKey);
        tester.clickLinkWithText(ATTACHMENT_FILE_NAME_2);
        // Now we should see the data in the anotherfile.txt is correct.
        tester.assertTextPresent(ATTACHMENT_CONTENTS_2);

        // Go to the clone issue
        navigation.issue().viewIssue(cloneIssueKey);

        // Look through all the sub-tasks and verify the attachments on each one
        navigation.clickLinkWithExactText("CLONE - Design Solution");

        // Extract the key of the new issue
        String cloneSubtaskIssueKey1 = extractIssueKey();

        tester.clickLinkWithText(DESIGN_ATTACHMENT_FILE_NAME_1);
        // Now we should see the data in the info.txt is correct.
        tester.assertTextPresent(DESIGN_ATTACHMENT_CONTENTS_1);

        navigation.issue().viewIssue(cloneSubtaskIssueKey1);
        tester.clickLinkWithText(DESIGN_ATTACHMENT_FILE_NAME_2);
        // Now we should see the data in the anotherfile.txt is correct.
        tester.assertTextPresent(DESIGN_ATTACHMENT_CONTENTS_2);

        // Go to the clone issue
        navigation.issue().viewIssue(cloneIssueKey);

        navigation.clickLinkWithExactText("CLONE - Implement Solution");

        // Extract the key of the new issue
        String cloneSubtaskIssueKey2 = extractIssueKey();

        tester.clickLinkWithText(IMPLEMENT_ATTACHMENT_FILE_NAME_1);
        // Now we should see the data in the info.txt is correct.
        tester.assertTextPresent(IMPLEMENT_ATTACHMENT_CONTENTS_1);

        // Delete attachment
        navigation.issue().attachments(cloneIssueKey).manage().delete();

        // Go to the clone issue
        navigation.issue().viewIssue(cloneIssueKey);
        tester.assertLinkNotPresentWithText(ATTACHMENT_FILE_NAME_1);
        tester.assertLinkNotPresentWithText(ATTACHMENT_FILE_NAME_2);

        // Go to the original issue
        navigation.issue().viewIssue(ORIGINAL_ISSUE_KEY);
        // Ensure the attachments are still there
        tester.assertLinkPresentWithText(ATTACHMENT_FILE_NAME_1);
        tester.assertLinkPresentWithText(ATTACHMENT_FILE_NAME_2);

        tester.clickLinkWithText(ATTACHMENT_FILE_NAME_1);
        // Now we should see the data in the info.txt file is correct.
        tester.assertTextPresent(ATTACHMENT_CONTENTS_1);

        navigation.issue().viewIssue(ORIGINAL_ISSUE_KEY);
        tester.clickLinkWithText(ATTACHMENT_FILE_NAME_2);
        // Now we should see the data in the anotherfile.txt file is correct.
        tester.assertTextPresent(ATTACHMENT_CONTENTS_2);

        navigation.issue().attachments(ORIGINAL_ISSUE_KEY).manage().delete();

        // Delete attachments from the original sub-tasks and ensure they still exists on the clones sub-tasks
        navigation.issue().attachments(cloneSubtaskIssueKey1).manage().delete();

        // Go to the clone issue
        navigation.issue().viewIssue(cloneSubtaskIssueKey1);
        tester.assertLinkNotPresentWithText(DESIGN_ATTACHMENT_FILE_NAME_1);
        tester.assertLinkNotPresentWithText(DESIGN_ATTACHMENT_FILE_NAME_2);

        // Go to the original issue
        navigation.issue().viewIssue(ORIGINAL_SUBTASK_KEY_1);
        // Ensure the attachments are still there
        tester.assertLinkPresentWithText(DESIGN_ATTACHMENT_FILE_NAME_1);
        tester.assertLinkPresentWithText(DESIGN_ATTACHMENT_FILE_NAME_2);

        tester.clickLinkWithText(DESIGN_ATTACHMENT_FILE_NAME_1);
        // Now we should see the data in the info.txt file is correct.
        tester.assertTextPresent(DESIGN_ATTACHMENT_CONTENTS_1);

        navigation.issue().viewIssue(ORIGINAL_SUBTASK_KEY_1);
        tester.clickLinkWithText(DESIGN_ATTACHMENT_FILE_NAME_2);
        // Now we should see the data in the anotherfile.txt file is correct.
        tester.assertTextPresent(DESIGN_ATTACHMENT_CONTENTS_2);

        navigation.issue().attachments(ORIGINAL_SUBTASK_KEY_1).manage().delete();

        navigation.issue().attachments(cloneSubtaskIssueKey2).manage().delete();

        // Go to the clone issue
        navigation.issue().viewIssue(cloneSubtaskIssueKey2);
        tester.assertLinkNotPresentWithText(IMPLEMENT_ATTACHMENT_FILE_NAME_1);

        // Go to the original issue
        navigation.issue().viewIssue(ORIGINAL_SUBTASK_KEY_3);
        // Ensure the attachments are still there
        tester.assertLinkPresentWithText(IMPLEMENT_ATTACHMENT_FILE_NAME_1);

        tester.clickLinkWithText(IMPLEMENT_ATTACHMENT_FILE_NAME_1);
        // Now we should see the data in the info.txt file is correct.
        tester.assertTextPresent(IMPLEMENT_ATTACHMENT_CONTENTS_1);

        navigation.issue().attachments(ORIGINAL_SUBTASK_KEY_3).manage().delete();
    }

    public void testCloneSubTaskNoLinksCopyAttachments() throws Exception
    {
        // Clone a sub-task issue
        navigation.issue().viewIssue(ORIGINAL_SUBTASK_KEY_1);

        // Ensure the attachments are there
        tester.assertLinkPresentWithText(DESIGN_ATTACHMENT_FILE_NAME_1);
        tester.assertLinkPresentWithText(DESIGN_ATTACHMENT_FILE_NAME_2);

        // Clone the issue
        tester.clickLink(CLONE_ISSUE_OPERATION_NAME);

        tester.assertTextPresent(CLONE_ATTACHMENTS_CHECKBOX_LABEL);
        tester.assertCheckboxNotSelected(CLONE_ATTACHMENTS_CHECKBOX_NAME);

        final String cloneSummary = "CLONE - Sub-Task";
        tester.setFormElement(SUMMARY_FIELD_ID, cloneSummary);
        tester.checkCheckbox(CLONE_ATTACHMENTS_CHECKBOX_NAME, "true");
        tester.submit(CREATE_BUTTON_NAME);

        // Assert that the attachments have been cloned
        tester.assertTextPresent(cloneSummary);
        tester.assertLinkPresentWithText(DESIGN_ATTACHMENT_FILE_NAME_1);
        tester.assertLinkPresentWithText(DESIGN_ATTACHMENT_FILE_NAME_2);

        // Extract the key of the new issue
        String cloneIssueKey = extractIssueKey();

        tester.clickLinkWithText(DESIGN_ATTACHMENT_FILE_NAME_1);
        // Now we should see the data in the info.txt is correct.
        tester.assertTextPresent(DESIGN_ATTACHMENT_CONTENTS_1);

        navigation.issue().viewIssue(cloneIssueKey);
        tester.clickLinkWithText(DESIGN_ATTACHMENT_FILE_NAME_2);
        // Now we should see the data in the anotherfile.txt is correct.
        tester.assertTextPresent(DESIGN_ATTACHMENT_CONTENTS_2);

        // Go to the clone issue
        navigation.issue().viewIssue(cloneIssueKey);

        // Delete attachment
        navigation.issue().attachments(cloneIssueKey).manage().delete();

        // Go to the clone issue
        navigation.issue().viewIssue(cloneIssueKey);
        tester.assertLinkNotPresentWithText(DESIGN_ATTACHMENT_FILE_NAME_1);
        tester.assertLinkNotPresentWithText(DESIGN_ATTACHMENT_FILE_NAME_2);

        // Go to the original issue
        navigation.issue().viewIssue(ORIGINAL_SUBTASK_KEY_1);
        // Ensure the attachments are still there
        tester.assertLinkPresentWithText(DESIGN_ATTACHMENT_FILE_NAME_1);
        tester.assertLinkPresentWithText(DESIGN_ATTACHMENT_FILE_NAME_2);

        tester.clickLinkWithText(DESIGN_ATTACHMENT_FILE_NAME_1);
        // Now we should see the data in the info.txt file is correct.
        tester.assertTextPresent(DESIGN_ATTACHMENT_CONTENTS_1);

        navigation.issue().viewIssue(ORIGINAL_SUBTASK_KEY_1);
        tester.clickLinkWithText(DESIGN_ATTACHMENT_FILE_NAME_2);
        // Now we should see the data in the anotherfile.txt file is correct.
        tester.assertTextPresent(DESIGN_ATTACHMENT_CONTENTS_2);

        navigation.issue().attachments(ORIGINAL_SUBTASK_KEY_1).manage().delete();
    }

    private String extractIssueKey()
    {
        Locator locator = new IdLocator(tester, "key-val");
        return locator.getRawText();
    }
}
