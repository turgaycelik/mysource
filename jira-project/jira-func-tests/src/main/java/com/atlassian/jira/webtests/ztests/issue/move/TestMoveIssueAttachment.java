package com.atlassian.jira.webtests.ztests.issue.move;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.FuncTestHelperFactory;
import com.atlassian.jira.functest.framework.Navigation;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import net.sourceforge.jwebunit.WebTester;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Tests that attachments are moved successfully with an Issue move (single and Bulk).
 *
 * There are separate tests depending on whether we need to do a workflow migration or not, because there was a regression
 * that only occured when we did the migration (JRA-16223).
 */
@WebTest({Category.FUNC_TEST, Category.ATTACHMENTS, Category.MOVE_ISSUE })
public class TestMoveIssueAttachment extends FuncTestCase
{
    private String attachmentPath;

    protected void setUpTest()
    {
        this.administration.restoreData("TestMoveIssueAttachment.xml");
        // Enable attachments as we want to use the default attachments directory.
        administration.attachments().enable();
        attachmentPath = administration.getCurrentAttachmentPath();
    }

    public void testMoveSingleIssueSameWorkflow() throws Exception
    {
        // we need to put a file in the attachments directory where JIRA expects to find it from the DB attachment data.
        installAttachedFile("AAAAAA");
        navigation.issue().viewIssue("RAT-1");
        tester.clickLinkWithText("info.txt");
        tester.assertTextPresent("AAAAAA");

        navigation.issue().viewIssue("RAT-1");
        // Click Link 'Move' (id='move_issue').
        tester.clickLink("move-issue");
        // Select 'Bovine' from select box 'pid'.
        tester.selectOption("pid", "Bovine");
        tester.submit("Next >>");
        tester.submit("Next >>");
        tester.submit("Move");
        tester.clickLinkWithText("info.txt");
        // Now we should see the data in the info.txt file if it was moved correctly.
        tester.assertTextPresent("AAAAAA");

        // Check that the file is where we expect it to be:
        File movedFile = new File(attachmentPath + "/COW/COW-15", "10010_info.txt");
        assertTrue(movedFile.exists());
        // Delete the file, or it could cause the next test to accidentally pass.
        deleteAttachment("COW-15");
        // Check that the file is actually deleted from the file sytem:
        assertFalse(movedFile.exists());
    }

    public void testMoveSingleIssueDifferentWorkflow() throws Exception
    {
        // we need to put a file in the attachments directory where JIRA expects to find it from the DB attachment data.
        installAttachedFile("testMo");

        navigation.issue().viewIssue("RAT-1");
        // Click Link 'Move' (id='move_issue').
        tester.clickLink("move-issue");
        tester.assertTextPresent("Step 1 of 4");
        // Select 'Canine' from select box 'pid'.
        tester.selectOption("pid", "Canine");
        tester.submit("Next >>");
        tester.assertTextPresent("Step 3 of 4");
        tester.submit("Next >>");
        tester.assertTextPresent("Move Issue: Confirm");
        tester.submit("Move");
        tester.assertTextPresent("DOG-1");
        navigation.issue().viewIssue("DOG-1");
        tester.clickLinkWithText("info.txt");
        // Now we should see the data in the info.txt file if it was moved correctly.
        tester.assertTextPresent("testMo");

        // Check that the file is where we expect it to be:
        File movedFile = new File(attachmentPath + "/DOG/DOG-1", "10010_info.txt");
        assertTrue(movedFile.exists());
        // Delete the file, or it could cause the next test to accidentally pass.
        deleteAttachment("DOG-1");
        // Check that the file is actually deleted from the file sytem:
        assertFalse(movedFile.exists());
    }

    public void testBulkMoveIssueSameWorkflow() throws Exception
    {
        // we need to put a file in the attachments directory where JIRA expects to find it from the DB attachment data.
        installAttachedFile("testBu");

        // Go to navigator
        navigation.issueNavigator().bulkEditAllIssues();
        tester.checkCheckbox("bulkedit_10000", "on");
        tester.submit("Next");
        tester.checkCheckbox("operation", "bulk.move.operation.name");
        tester.submit("Next");
        // Select 'Bovine' from select box '10000_1_pid'.
        tester.selectOption("10000_1_pid", "Bovine");
        tester.submit("Next");
        tester.submit("Next");
        tester.submit("Next");
        waitAndReloadBulkOperationProgressPage();

        // Go tot the moved issue - COW-15
        navigation.issue().viewIssue("COW-15");
        tester.clickLinkWithText("info.txt");
        // Now we should see the data in the info.txt file if it was moved correctly.
        tester.assertTextPresent("testBu");

        // Check that the file is where we expect it to be:
        File movedFile = new File(attachmentPath + "/COW/COW-15", "10010_info.txt");
        assertTrue(movedFile.exists());
        // Delete the file, or it could cause the next test to accidentally pass.
        deleteAttachment("COW-15");
        // Check that the file is actually deleted from the file sytem:
        assertFalse(movedFile.exists());
    }

    public void testBulkMoveIssueDifferentWorkflow() throws Exception
    {
        // we need to put a file in the attachments directory where JIRA expects to find it from the DB attachment data.
        installAttachedFile("testBu");

        // Go to navigator
        navigation.issueNavigator().bulkEditAllIssues();
        tester.checkCheckbox("bulkedit_10000", "on");
        tester.submit("Next");
        tester.checkCheckbox("operation", "bulk.move.operation.name");
        tester.submit("Next");
        // Select 'Bovine' from select box '10000_1_pid'.
        tester.selectOption("10000_1_pid", "Canine");
        tester.submit("Next");
        tester.submit("Next");
        tester.submit("Next");
        waitAndReloadBulkOperationProgressPage();

        // Go to the moved issue - Dog-1
        navigation.issue().viewIssue("DOG-1");
        tester.clickLinkWithText("info.txt");
        // Now we should see the data in the info.txt file if it was moved correctly.
        tester.assertTextPresent("testBu");

        // Check that the file is where we expect it to be:
        File movedFile = new File(attachmentPath + "/DOG/DOG-1", "10010_info.txt");
        assertTrue(movedFile.exists());
        // Delete the file, or it could cause the next test to accidentally pass.
        deleteAttachment("DOG-1");
        // Check that the file is actually deleted from the file sytem:
        assertFalse(movedFile.exists());
    }

    public void testMoveSingleIssueTwoUsers() throws Exception
    {
        // we need to put a file in the attachments directory where JIRA expects to find it from the DB attachment data.
        installAttachedFile("WWWWww");

        final FuncTestHelperFactory funcTestHelperFactory2 = new FuncTestHelperFactory(this, getEnvironmentData());
        WebTester tester2 = funcTestHelperFactory2.getTester();
        Navigation navigation2 = funcTestHelperFactory2.getNavigation();

        // Start a move with User 1
        navigation.issue().viewIssue("RAT-1");
        // Click Link 'Move' (id='move_issue').
        tester.clickLink("move-issue");

        // Start a move with User 2
        navigation2.issue().viewIssue("RAT-1");
        tester2.clickLink("move-issue");

        // Let user 1 move the issue to project Bovine
        // Select 'Bovine' from select box 'pid'.
        tester.selectOption("pid", "Bovine");
        tester.submit("Next >>");
        tester.assertTextPresent("Step 3 of 4");
        tester.submit("Next >>");

        // Let User 2 try to move the isse to project "Canine"
        tester2.selectOption("pid", "Canine");
        tester2.submit("Next >>");
        tester2.assertTextPresent("Step 3 of 4");
        tester2.submit("Next >>");
        tester2.assertTextPresent("Move Issue: Confirm");
        tester2.submit("Move");
        tester2.assertTextPresent("DOG-1");
        tester2.clickLinkWithText("info.txt");
        tester2.assertTextPresent("WWWWww");

        // User 1
        tester.assertTextPresent("Move Issue: Confirm");
        tester.submit("Move");
        tester.assertTextPresent("Move Issue: Confirm");
        tester.assertTextPresent("Cannot move Issue RAT-1 because it has already been moved to DOG-1.");

        // Check that the file is where we expect it to be:
        File movedFile = new File(attachmentPath + "/DOG/DOG-1", "10010_info.txt");
        assertTrue(movedFile.exists());
        // Delete the file, or it could cause the next test to accidentally pass.
        deleteAttachment("DOG-1");
        // Check that the file is actually deleted from the file sytem:
        assertFalse(movedFile.exists());
    }

    /**
     * This tests what happens to attachments when two users do a bulk move at the same time to different projects.
     *
     * <p> See http://jira.atlassian.com/browse/JRA-15475
     *
     * @throws Exception If the test is stuffed.
     */
    public void testBulkMoveIssueTwoUsers() throws Exception
    {
        // we need to put a file in the attachments directory where JIRA expects to find it from the DB attachment data.
        installAttachedFile("JJJJJJ");

        // We need to set up a second FuncTestHelperFactory in order to simulate our second user.
        final FuncTestHelperFactory funcTestHelperFactory2 = new FuncTestHelperFactory(this, getEnvironmentData());
        WebTester tester2 = funcTestHelperFactory2.getTester();
        Navigation navigation2 = funcTestHelperFactory2.getNavigation();

        //Start the bulk move for user 1
        navigation.issueNavigator().bulkEditAllIssues();
        tester.assertTextPresent("Step 1 of 4: Choose Issues");
        tester.checkCheckbox("bulkedit_10000", "on");
        tester.submit("Next");
        tester.assertTextPresent("Step 2 of 4: Choose Operation");
        tester.checkCheckbox("operation", "bulk.move.operation.name");
        tester.submit("Next");
        tester.assertTextPresent("Select Projects and Issue Types");
        // Select 'Canine' from select box '10000_1_pid'.
        tester.selectOption("10000_1_pid", "Canine");
        tester.submit("Next");
        tester.assertTextPresent("Update Fields for Target Project 'Canine' - Issue Type 'Bug'");
        tester.submit("Next");
        tester.assertTextPresent("Confirmation");

        //Start the bulk move for user 2
        navigation2.issueNavigator().bulkEditAllIssues();
        tester2.assertTextPresent("Step 1 of 4: Choose Issues");
        tester2.checkCheckbox("bulkedit_10000", "on");
        tester2.submit("Next");
        tester2.assertTextPresent("Step 2 of 4: Choose Operation");
        tester2.checkCheckbox("operation", "bulk.move.operation.name");
        tester2.submit("Next");
        tester2.assertTextPresent("Select Projects and Issue Types");
        // Select 'Bovine' from select box '10000_1_pid'.
        tester2.selectOption("10000_1_pid", "Bovine");
        tester2.submit("Next");
        tester2.assertTextPresent("Update Fields for Target Project 'Bovine' - Issue Type 'Bug'");
        tester2.submit("Next");
        tester2.assertTextPresent("Confirmation");
        tester2.submit("Next");
        waitAndReloadBulkOperationProgressPage(tester2);
        tester2.assertTextPresent("Issue Navigator");

        // User 2 has completed the move - check the attachment
        // Go to the moved issue
        navigation2.issue().viewIssue("COW-15");
        tester2.clickLinkWithText("info.txt");
        // Now we should see the data in the info.txt file if it was moved correctly.
        tester2.assertTextPresent("JJJJJJ");
        // Check that the file is where we expect it to be:
        File movedFile = new File(attachmentPath + "/COW/COW-15", "10010_info.txt");
        assertTrue(movedFile.exists());

        // Now let User 1 finish trying to move:

        tester.submit("Next");
        tester.assertTextPresent("Confirmation");
        tester.assertTextPresent("At least one of the issues you are trying to move has been recently moved by another user (RAT-1). Please cancel and start again.");

        // Check that the file is still in COW:
        movedFile = new File(attachmentPath + "/COW/COW-15", "10010_info.txt");
        assertTrue(movedFile.exists());
        // Delete the file, or it could cause the next test to accidentally pass.
        deleteAttachment("COW-15");
        // Check that the file is actually deleted from the file sytem:
        assertFalse(movedFile.exists());
    }

    private String installAttachedFile(final String contents) throws IOException
    {
        File attachedFile = new File(attachmentPath + "/RAT/RAT-1", "10010_info.txt");
        attachedFile.getParentFile().mkdirs();
        PrintWriter out = new PrintWriter(new FileWriter(attachedFile));
        try
        {
            out.println(contents);
        }
        finally
        {
            out.close();
        }
        return attachmentPath;
    }

    private void deleteAttachment(String issueKey)
    {
        navigation.issue().viewIssue(issueKey);
        tester.clickLink("manage-attachment-link");
        // Click Link 'Delete'
        tester.clickLink("del_10010");
        tester.submit("Delete");
    }
}
