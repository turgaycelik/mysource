package com.atlassian.jira.webtests.ztests.email;

import com.atlassian.jira.functest.framework.locator.WebPageLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.EmailFuncTestCase;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;

import com.icegreen.greenmail.util.GreenMailUtil;

/**
 * @since v4.0
 */
@WebTest ({ Category.FUNC_TEST, Category.EMAIL })
// Currently passing
public class TestUserSignup extends EmailFuncTestCase
{
    private static final String FULL_NAME = "full name";
    
    private static final String SECRET_PASSWORD = "secretpassword";
    private static final String USER1 = "user1";

    private static final String USER1_EMAIL = "user1@example.com";
    private static final String USER2 = "user2";

    private static final String USER2_EMAIL = "user2@example.org";

    public void testUserSignup() throws InterruptedException, MessagingException, IOException
    {
        backdoor.restoreBlankInstance();
        configureAndStartSmtpServer();

        signupAs(USER1, USER1_EMAIL);
        assertSignupEmail(flushAndGetLastMessage(1), USER1, USER1_EMAIL);

        signupAs(USER2, USER2_EMAIL);
        assertSignupEmail(flushAndGetLastMessage(2), USER2, USER2_EMAIL);
    }

    private MimeMessage flushAndGetLastMessage(final int emailCount) throws InterruptedException
    {
        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);
        flushMailQueueAndWait(emailCount);
        MimeMessage[] messages = mailService.getReceivedMessages();
        assertTrue("We expect 1 email in the queue", messages.length == emailCount);
        return messages[emailCount-1];
    }

    private void assertSignupEmail(final MimeMessage message, final String userName, final String emailAddress) throws MessagingException, IOException
    {
        String toAddr = message.getHeader("To","XXX");
        String body = GreenMailUtil.getBody(message);

        text.assertTextSequence(toAddr, emailAddress);
        text.assertTextSequence(body, "Username:", userName);
        text.assertTextSequence(body, "Email:", emailAddress);
        text.assertTextSequence(body, "Full Name:", userName +  FULL_NAME);

        // assert that we dont expose the password eg JRA-6175
        text.assertTextNotPresent(body, SECRET_PASSWORD);
    }

    private void signupAs(final String userName, final String emailAddress)
    {
        navigation.logout();
        tester.gotoPage("secure/Signup!default.jspa");
        tester.setFormElement("username", userName);
        tester.setFormElement("password", SECRET_PASSWORD);
        tester.setFormElement("confirm", SECRET_PASSWORD);
        tester.setFormElement("fullname", userName + FULL_NAME);
        tester.setFormElement("email", emailAddress);
        tester.submit();

        text.assertTextPresent(new WebPageLocator(tester), "You have successfully signed up. If you forget your password, you can have it emailed to you.");
        navigation.gotoPage("/");
    }
}