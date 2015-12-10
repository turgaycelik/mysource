package com.atlassian.jira.webtests.ztests.bulk;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.navigation.BulkChangeWizard;
import com.atlassian.jira.functest.framework.navigation.IssueNavigatorNavigation;
import com.atlassian.jira.functest.framework.navigation.issue.FileAttachmentsList;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.functest.rules.CopyAttachmentsRule;

/**
 * @since v4.4
 */
@WebTest ({ Category.FUNC_TEST, Category.ATTACHMENTS, Category.BULK_OPERATIONS })
public class TestBulkMoveAttachments extends FuncTestCase
{
    public CopyAttachmentsRule copyAttachmentsRule;

    @Override
    public void setUpTest()
    {
        super.setUpTest();
        // This is the XML used to test our various attachment naming schemes so using it for the bulk
        // move test is perfect
        administration.restoreData("TestAttachmentEncoding.xml");

        copyAttachmentsRule = new CopyAttachmentsRule(this);
        copyAttachmentsRule.before();
        copyAttachmentsRule.copyAttachmentsFrom("TestAttachmentEncoding/attachments");
    }

    @Override
    protected void tearDownTest()
    {
        copyAttachmentsRule.after();
        super.tearDownTest();
    }

    // JRA-23830. Make sure that the attachments actually move to the new issue when we do a Bulk Move.
    public void testBulkMove() throws Exception
    {
        // move HSP-1 to MKY project and then verify that all the attachments are still there
        navigation.issueNavigator().displayAllIssues();
        final BulkChangeWizard wizard = navigation.issueNavigator().bulkChange(IssueNavigatorNavigation.BulkChangeOption.ALL_PAGES);
        wizard.selectAllIssues()
            .chooseOperation(BulkChangeWizard.BulkOperationsImpl.MOVE)
            .chooseTargetContextForAll("monkey")
            .finaliseFields()
            .complete();

        waitAndReloadBulkOperationProgressPage();

        // assert that all of the attachments are still reachable
        for (FileAttachmentsList.FileAttachmentItem fileAttachmentItem : navigation.issue().attachments("MKY-1").list().get())
        {
            tester.gotoPage("/secure/attachment/" + fileAttachmentItem.getId() + "/" + fileAttachmentItem.getName());
        }
    }

    // JDEV-24882. Make sure that the attachments actually move to the new issue when we do a Bulk Move.
    public void testBulkMoveFromRenamedProject() throws Exception
    {
        backdoor.project().editProjectKey(backdoor.project().getProjectId("HSP"), "OTHER");

        // move HSP-1 to MKY project and then verify that all the attachments are still there
        navigation.issueNavigator().displayAllIssues();
        final BulkChangeWizard wizard = navigation.issueNavigator().bulkChange(IssueNavigatorNavigation.BulkChangeOption.ALL_PAGES);
        wizard.selectAllIssues()
                .chooseOperation(BulkChangeWizard.BulkOperationsImpl.MOVE)
                .chooseTargetContextForAll("monkey")
                .finaliseFields()
                .complete();

        waitAndReloadBulkOperationProgressPage();

        // assert that all of the attachments are still reachable
        for (FileAttachmentsList.FileAttachmentItem fileAttachmentItem : navigation.issue().attachments("MKY-1").list().get())
        {
            tester.gotoPage("/secure/attachment/" + fileAttachmentItem.getId() + "/" + fileAttachmentItem.getName());
        }
    }

    // JDEV-24882. Make sure that the attachments actually move to the new issue when we do a Bulk Move.
    public void testBulkMoveToRenamedProject() throws Exception
    {
        backdoor.project().editProjectKey(backdoor.project().getProjectId("MKY"), "OTHER");

        // move HSP-1 to MKY project and then verify that all the attachments are still there
        navigation.issueNavigator().displayAllIssues();
        final BulkChangeWizard wizard = navigation.issueNavigator().bulkChange(IssueNavigatorNavigation.BulkChangeOption.ALL_PAGES);
        wizard.selectAllIssues()
                .chooseOperation(BulkChangeWizard.BulkOperationsImpl.MOVE)
                .chooseTargetContextForAll("monkey")
                .finaliseFields()
                .complete();

        waitAndReloadBulkOperationProgressPage();

        // assert that all of the attachments are still reachable
        for (FileAttachmentsList.FileAttachmentItem fileAttachmentItem : navigation.issue().attachments("OTHER-1").list().get())
        {
            tester.gotoPage("/secure/attachment/" + fileAttachmentItem.getId() + "/" + fileAttachmentItem.getName());
        }
    }

    // JDEV-24882. Make sure that the attachments actually move to the new issue when we do a Bulk Move.
    public void testBulkMoveFromRenamedToRenamedProject() throws Exception
    {
        backdoor.project().editProjectKey(backdoor.project().getProjectId("HSP"), "HASP");
        backdoor.project().editProjectKey(backdoor.project().getProjectId("MKY"), "MONKEY");

        // move HSP-1 to MKY project and then verify that all the attachments are still there
        navigation.issueNavigator().displayAllIssues();
        final BulkChangeWizard wizard = navigation.issueNavigator().bulkChange(IssueNavigatorNavigation.BulkChangeOption.ALL_PAGES);
        wizard.selectAllIssues()
                .chooseOperation(BulkChangeWizard.BulkOperationsImpl.MOVE)
                .chooseTargetContextForAll("monkey")
                .finaliseFields()
                .complete();

        waitAndReloadBulkOperationProgressPage();

        // assert that all of the attachments are still reachable
        for (FileAttachmentsList.FileAttachmentItem fileAttachmentItem : navigation.issue().attachments("MONKEY-1").list().get())
        {
            tester.gotoPage("/secure/attachment/" + fileAttachmentItem.getId() + "/" + fileAttachmentItem.getName());
        }
    }
}
