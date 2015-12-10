package com.atlassian.jira.webtests.ztests.issue.move;

import java.util.List;

import javax.mail.internet.MimeMessage;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.EmailFuncTestCase;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@WebTest ({ Category.FUNC_TEST, Category.ISSUES, Category.MOVE_ISSUE })
public class TestMoveIssueNotifications extends EmailFuncTestCase
{
    public static final String ISSUE_KEY = "PONE-1";

    public static final String TEXT_ON_ISSUE_MOVED_MAIL = "moved";
    public static final String TEXT_ON_ISSUE_UPDATED_MAIL = "updated";

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("TestMoveIssueNotifications.xml");
        navigation.userProfile().changeNotifyMyChanges(true);
        configureAndStartSmtpServer();
    }

    public void testMovingIssueWithoutSubtaskSendsEmailWhenWeChangeProject() throws Exception
    {
        moveIssueToDifferentProject(ISSUE_KEY);

        assertEmailContainingText(TEXT_ON_ISSUE_MOVED_MAIL);
    }

    public void testMovingIssuesWithoutSubtaskSendsEmailWhenWeDoNotChangeProject() throws Exception
    {
        changeIssueTypeKeepingItOnTheSameProject(ISSUE_KEY);

        assertEmailContainingText(TEXT_ON_ISSUE_UPDATED_MAIL);
    }

    private void moveIssueToDifferentProject(final String issueKey)
    {
        navigation.issue().gotoIssue(issueKey);
        tester.clickLink("move-issue");
        tester.selectOption("pid", "project2");
        tester.submit("Next >>");
        tester.submit("Next >>");
        tester.submit("Move");
    }

    private void changeIssueTypeKeepingItOnTheSameProject(final String issueKey)
    {
        navigation.issue().gotoIssue(issueKey);
        tester.clickLink("move-issue");
        tester.selectOption("issuetype", "Task");
        tester.submit("Next >>");
        tester.submit("Next >>");
        tester.submit("Move");
    }

    private void assertEmailContainingText(String text) throws Exception
    {
        flushMailQueueAndWait(1);
        List<MimeMessage> messagesForRecipient = getMessagesForRecipient("admin@example.com");
        assertThat(messagesForRecipient.size(), is(1));
        assertEmailBodyContains(messagesForRecipient.get(0), text);
    }
}
