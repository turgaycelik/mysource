package com.atlassian.jira.webtests.ztests.subtask;

import java.util.List;

import javax.mail.internet.MimeMessage;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.EmailFuncTestCase;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@WebTest ({ Category.FUNC_TEST, Category.ISSUES, Category.SUB_TASKS })
public class TestIssueToSubtaskConversionNotifications extends EmailFuncTestCase
{
    private static final String ISSUE_TO_CONVERT_ID = "10001";
    private static final String PARENT_ISSUE = "TEST-1";
    private static final String SUBTASK_TYPE_ID = "5";

    public static final String EMAIL_ADDRESS = "admin@example.com";

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("TestIssueToSubtaskConversionNotifications.xml");
        configureAndStartSmtpServer();
    }

    public void testEmailIsSentWhenIssueIsConvertedToSubtask() throws Exception
    {
        convertIssueToSubtask(ISSUE_TO_CONVERT_ID);

        assertOneEmailWasSent(EMAIL_ADDRESS, "updated", "an issue", "Change By:", "Parent:", "TEST-1", "TEST-2", "Bug",
                "Sub-task", "Add Comment", "ViewProfile.jspa?name=admin", "This message was sent by Atlassian JIRA");
    }

    public void convertIssueToSubtask(String issueId)
    {
        tester.gotoPage("/secure/ConvertIssueSetIssueType.jspa?id=" + issueId + "&parentIssueKey=" + PARENT_ISSUE +
                "&issuetype=" + SUBTASK_TYPE_ID);
        tester.submit("Next >>");
        tester.submit("Finish");
    }

    private void assertOneEmailWasSent(String emailAddress, String... expectedTexts) throws Exception
    {
        flushMailQueueAndWait(1);
        List<MimeMessage> messagesForRecipient = getMessagesForRecipient(emailAddress);
        assertThat(messagesForRecipient.size(), is(1));
        for (String expectedText : expectedTexts)
        {
            assertEmailBodyContains(messagesForRecipient.get(0), expectedText);
        }
    }
}
