package com.atlassian.jira.webtests.ztests.bulk;

import java.util.List;

import javax.mail.internet.MimeMessage;

import com.atlassian.jira.functest.framework.navigation.BulkChangeWizard;
import com.atlassian.jira.functest.framework.navigation.IssueNavigatorNavigation;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.EmailFuncTestCase;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@WebTest ({ Category.FUNC_TEST, Category.BULK_OPERATIONS, Category.ISSUES })
public class TestBulkMoveIssuesNotifications extends EmailFuncTestCase
{
    public static final String TEXT_ON_ISSUE_MOVED_MAIL = "moved";
    public static final String TEXT_ON_ISSUE_UPDATED_MAIL = "updated";

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("TestBulkMoveIssuesNotifications.xml");
        configureAndStartSmtpServer();
    }

    public void testMovingIssuesSendsEmailWhenWeChangeProject() throws Exception
    {
        loginAs("test");
        moveIssueToADifferentProject();

        expectEmailWitText(TEXT_ON_ISSUE_MOVED_MAIL);
    }

    public void testMovingIssuesSendsEmailWhenWeDoNotChangeProject() throws Exception
    {
        loginAs("test");
        moveTwoIssuesToTheSameProjectChangingTheIssueTypes();

        expectEmailWitText(TEXT_ON_ISSUE_UPDATED_MAIL);
    }

    private void loginAs(String user)
    {
        navigation.login(user);
    }

    private void moveIssueToADifferentProject()
    {
        navigation.issueNavigator().displayAllIssues();
        navigation.issueNavigator()
                .bulkChange(IssueNavigatorNavigation.BulkChangeOption.ALL_PAGES)
                .selectAllIssues()
                .chooseOperation(BulkChangeWizard.BulkOperationsImpl.MOVE)
                .chooseTargetContextForAll("project2")
                .finaliseFields()
                .complete();
        waitAndReloadBulkOperationProgressPage();
    }

    private void moveTwoIssuesToTheSameProjectChangingTheIssueTypes()
    {
        navigation.issueNavigator().displayAllIssues();
        navigation.issueNavigator()
                .bulkChange(IssueNavigatorNavigation.BulkChangeOption.ALL_PAGES)
                .selectAllIssues()
                .chooseOperation(BulkChangeWizard.BulkOperationsImpl.MOVE)
                .chooseTargetContextForAll("project1", "Task")
                .finaliseFields()
                .complete();
        waitAndReloadBulkOperationProgressPage();
    }

    private void expectEmailWitText(String text) throws Exception
    {
        flushMailQueueAndWait(1);
        List<MimeMessage> messagesForRecipient = getMessagesForRecipient("admin@example.com");
        assertThat(messagesForRecipient.size(), is(1));
        assertEmailBodyContains(messagesForRecipient.get(0), text);
    }
}
