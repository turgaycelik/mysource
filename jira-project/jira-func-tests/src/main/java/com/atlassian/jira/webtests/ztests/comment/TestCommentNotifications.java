package com.atlassian.jira.webtests.ztests.comment;

import java.util.List;

import javax.mail.internet.MimeMessage;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.EmailFuncTestCase;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@WebTest ({ Category.FUNC_TEST, Category.COMMENTS })
public class TestCommentNotifications extends EmailFuncTestCase
{
    private static final String ISSUE_KEY = "TEST-1";

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("TestCommentNotifications.xml");
        configureAndStartSmtpServer();
    }

    public void testDeletingACommentSendsEmail() throws Exception
    {
        deleteCommentOn(ISSUE_KEY);

        assertEmailWasSentTo("admin@localhost", "updated", "an issue", "TEST-1", "some&nbsp;comment", "Change By:",
                "Add Comment", "Bug", "This message was sent by Atlassian JIRA");
    }

    private void deleteCommentOn(String issueKey)
    {
        tester.beginAt(
                "/browse/" + issueKey + "?page=com.atlassian.jira.plugin.system.issuetabpanels:comment-tabpanel");
        tester.clickLink("delete_comment_10000");
        tester.submit("Delete");
    }

    private void assertEmailWasSentTo(String emailAddress, String... expectedTexts) throws Exception
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
