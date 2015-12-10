package com.atlassian.jira.webtests.ztests.email;

import com.atlassian.jira.functest.framework.email.EmailKit;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.AbstractSubscriptionEmailTest;
import com.atlassian.jira.webtests.EmailFuncTestCase;
import com.atlassian.jira.webtests.Groups;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Iterator;

/**
 * Test email subscriptions in JIRA.
 * <p/>
 * The test data in 'TestSubscription.xml' contains three projects. 'ProjectOne' (KEY: ONE) contains 210 issues. All of
 * these issues can be returned using the global 'FilterOne' or 'FredFilterOne' saved search. 'ProjectTwo' (KEY: TWO)
 * contains 2 issues. All of these issues can be returned using the global 'FilterTwo' or 'FredFilterTwo' saved search.
 * Finally, there are global filters called 'ZeroFilter' and 'FredZeroFilter' that returns no issues.
 * <p/>
 * The test data also contains three users. Admin is the administrator and expects text e-mails. Bob is not an
 * administrator but also expects text e-mails. Fred is not an administrator and expects HTML e-mails.
 *
 * @since v3.13
 */
@SuppressWarnings({ "FeatureEnvy", "ClassTooDeepInInheritanceTree" })
@WebTest({ Category.FUNC_TEST, Category.BROWSING, Category.EMAIL })
public class TestEmailSubscription extends AbstractSubscriptionEmailTest
{
    public void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("TestSubscription.xml");
        configureAndStartSmtpServer();
        backdoor.userProfile().changeUserNotificationType("admin", "text");
        backdoor.userProfile().changeUserNotificationType("bob", "text");
    }

    /**
     * Check e-mail subscription with no issues.
     *
     * @throws Exception test just throws exception when unexpected error occurs to fail the test.
     */

    public void testGroupZeroResults() throws Exception
    {
        subscribeToFilterAndRun(FilterConfig.ADMIN_FILTER_ZERO_RESULTS, "jira-users");

        //we should get three messages back.
        flushMailQueueAndWait(3);

        MimeMessage[] messages = mailService.getReceivedMessages();

        //Get the admin's message. It should have:
        // 1. No issues.
        // 2. No partial issues link.
        // 3. An edit link.
        MimeMessage currentMessage = EmailKit.findMessageAddressedTo(ADMIN_EMAIL, messages);
        assertNotNull(currentMessage);

        assertTextMessageValid(currentMessage, FilterConfig.ADMIN_FILTER_ZERO_RESULTS, EmailFuncTestCase.DEFAULT_FROM_ADDRESS, ADMIN_EMAIL, ADMIN_USERNAME);
        String body = EmailKit.getBody(currentMessage);
        assertNotPartialLink(body);
        assertEditLinkText(body);

        //Get bob's message. It should have:
        // 1. No issues.
        // 2. No partial isssues link.
        // 3. No edit link.
        currentMessage = EmailKit.findMessageAddressedTo(BOB_EMAIL, messages);
        assertNotNull(currentMessage);

        assertTextMessageValid(currentMessage, FilterConfig.ADMIN_FILTER_ZERO_RESULTS, EmailFuncTestCase.DEFAULT_FROM_ADDRESS, BOB_EMAIL, ADMIN_USERNAME);
        body = EmailKit.getBody(currentMessage);
        assertNotPartialLink(body);
        assertNotEditLink(body);

        //Get fred's message. It should have:
        // 1. No Issues.
        // 2. No partial issues link.
        // 3. No edit link.
        currentMessage = EmailKit.findMessageAddressedTo(FRED_EMAIL, messages);
        assertNotNull(currentMessage);

        assertHtmlMessageValid(currentMessage, FilterConfig.ADMIN_FILTER_ZERO_RESULTS, EmailFuncTestCase.DEFAULT_FROM_ADDRESS, FRED_EMAIL, ADMIN_USERNAME);
        body = EmailKit.getBody(currentMessage);
        assertNotPartialLink(body);
        assertNotEditLink(body);
    }

    /**
     * Check for subscription e-mail that contains all issue matches.
     *
     * @throws Exception test just throws exception when unexpected error occurs to fail the test.
     */

    public void testGroupCompleteResults() throws Exception
    {
        subscribeToFilterAndRun(FilterConfig.ADMIN_FILTER_FULL, Groups.USERS);

        //we should get three messages back.
        flushMailQueueAndWait(3);
        MimeMessage[] messages = mailService.getReceivedMessages();

        //Get the admin's message. It should have:
        // 1. Two issues.
        // 2. No partial issues link.
        // 3. An edit link.
        MimeMessage currentMessage = EmailKit.findMessageAddressedTo(ADMIN_EMAIL, messages);
        assertNotNull(currentMessage);

        assertTextMessageValid(currentMessage, FilterConfig.ADMIN_FILTER_FULL, EmailFuncTestCase.DEFAULT_FROM_ADDRESS, ADMIN_EMAIL, ADMIN_USERNAME);
        String body = EmailKit.getBody(currentMessage);
        assertNotPartialLink(body);
        assertEditLinkText(body);

        //Get bob's message. It should have:
        // 1. Two issues.
        // 2. No partial isssues link.
        // 3. No edit link.
        currentMessage = EmailKit.findMessageAddressedTo(BOB_EMAIL, messages);
        assertNotNull(currentMessage);

        assertTextMessageValid(currentMessage, FilterConfig.ADMIN_FILTER_FULL, EmailFuncTestCase.DEFAULT_FROM_ADDRESS, BOB_EMAIL, ADMIN_USERNAME);
        body = EmailKit.getBody(currentMessage);
        assertNotPartialLink(body);
        assertNotEditLink(body);

        //Get the fred's message. It should have:
        // 1. Two issues.
        // 2. No partial isssues link.
        // 3. No edit link.

        currentMessage = EmailKit.findMessageAddressedTo(FRED_EMAIL, messages);
        assertNotNull(currentMessage);

        assertHtmlMessageValid(currentMessage, FilterConfig.ADMIN_FILTER_FULL, EmailFuncTestCase.DEFAULT_FROM_ADDRESS, FRED_EMAIL, ADMIN_USERNAME);
        body = EmailKit.getBody(currentMessage);
        assertNotPartialLink(body);
        assertNotEditLink(body);
    }

    /**
     * Check the subscription of an e-mail that only contains some of the results. There should be a link to the filter
     * in these e-mails.
     *
     * @throws Exception test just throws exception when unexpected error occurs to fail the test.
     */

    public void testGroupPartialResults() throws Exception
    {
        subscribeToFilterAndRun(FilterConfig.ADMIN_FILTER_PARTIAL, Groups.USERS);

        //we should get three messages back.
        flushMailQueueAndWait(3);
        MimeMessage[] messages = mailService.getReceivedMessages();

        //Get the admin's message. It should have:
        // 1. 200 issues.
        // 2. A partial issues link.
        // 3. An edit link.

        MimeMessage currentMessage = EmailKit.findMessageAddressedTo(ADMIN_EMAIL, messages);
        assertNotNull(currentMessage);

        assertTextMessageValid(currentMessage, FilterConfig.ADMIN_FILTER_PARTIAL, EmailFuncTestCase.DEFAULT_FROM_ADDRESS, ADMIN_EMAIL, ADMIN_USERNAME);
        String body = EmailKit.getBody(currentMessage);
        body = assertPartialLinkText(FilterConfig.ADMIN_FILTER_PARTIAL, body);
        assertEditLinkText(body);

        //Get bob's message. It should have:
        // 1. 200 issues.
        // 2. A partial isssues link.
        // 3. No edit link.

        currentMessage = EmailKit.findMessageAddressedTo(BOB_EMAIL, messages);
        assertNotNull(currentMessage);

        assertTextMessageValid(currentMessage, FilterConfig.ADMIN_FILTER_PARTIAL, EmailFuncTestCase.DEFAULT_FROM_ADDRESS, BOB_EMAIL, ADMIN_USERNAME);
        body = EmailKit.getBody(currentMessage);
        body = assertPartialLinkText(FilterConfig.ADMIN_FILTER_PARTIAL, body);
        assertNotEditLink(body);

        //Get the fred's message. It should have:
        // 1. 200 issues.
        // 2. A partial isssues link.
        // 3. No edit link.

        currentMessage = EmailKit.findMessageAddressedTo(FRED_EMAIL, messages);
        assertNotNull(currentMessage);

        assertHtmlMessageValid(currentMessage, FilterConfig.ADMIN_FILTER_PARTIAL, EmailFuncTestCase.DEFAULT_FROM_ADDRESS, FRED_EMAIL, ADMIN_USERNAME);
        body = EmailKit.getBody(currentMessage);
        body = assertPartialLinkHtml(FilterConfig.ADMIN_FILTER_PARTIAL, body);
        assertNotEditLink(body);
    }

    /**
     * Check e-mail subscription with no issues for an unprivileged user.
     *
     * @throws Exception test just throws exception when unexpected error occurs to fail the test.
     */

    public void testPersonalZeroResults() throws Exception
    {
        navigation.login(FRED_USERNAME, FRED_PASSWORD);
        subscribeToFilterAndRun(FilterConfig.FRED_FILTER_ZERO_RESULTS, null);

        navigation.logout();
        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);

        flushMailQueueAndWait(1);
        MimeMessage[] messages = mailService.getReceivedMessages();

        assertNotNull(messages[0]);

        //Fred's message should have:
        // 1. No issues.
        // 2. No partial isssues link.
        // 3. Edit link.

        assertHtmlMessageValid(messages[0], FilterConfig.FRED_FILTER_ZERO_RESULTS, EmailFuncTestCase.DEFAULT_FROM_ADDRESS, FRED_EMAIL, FRED_USERNAME);
        String body = EmailKit.getBody(messages[0]);
        assertNotPartialLink(body);
        assertEditLinkHtml(body);
    }

    /**
     * Check for subscription e-mail that contains all issue matches for an unprivileged user.
     *
     * @throws Exception test just throws exception when unexpected error occurs to fail the test.
     */

    public void testPersonalCompleteResults() throws Exception
    {
        navigation.login(FRED_USERNAME, FRED_PASSWORD);
        subscribeToFilterAndRun(FilterConfig.FRED_FILTER_FULL, null);

        navigation.logout();
        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);

        flushMailQueueAndWait(1);
        MimeMessage[] messages = mailService.getReceivedMessages();

        assertNotNull(messages[0]);

        //Fred's message should have:
        // 1. Two issues.
        // 2. No partial isssues link.
        // 3. Edit link.

        assertHtmlMessageValid(messages[0], FilterConfig.FRED_FILTER_FULL, EmailFuncTestCase.DEFAULT_FROM_ADDRESS, FRED_EMAIL, FRED_USERNAME);
        String body = EmailKit.getBody(messages[0]);
        assertNotPartialLink(body);
        assertEditLinkHtml(body);
    }

    /**
     * Check the subscription of an e-mail that only contains some of the results for an unprivieleged user. There
     * should be a link to the filter in these e-mails.
     *
     * @throws Exception test just throws exception when unexpected error occurs to fail the test.
     */
    public void testPersonalPartialResults() throws Exception
    {
        navigation.login(FRED_USERNAME, FRED_PASSWORD);
        subscribeToFilterAndRun(FilterConfig.FRED_FILTER_PARTIAL, null);

        navigation.logout();
        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);

        flushMailQueueAndWait(1);
        MimeMessage[] messages = mailService.getReceivedMessages();

        assertNotNull(messages[0]);

        //Fred's message should have:
        // 1. 200 issues.
        // 2. Partial isssues link.
        // 3. Edit link.

        assertHtmlMessageValid(messages[0], FilterConfig.FRED_FILTER_PARTIAL, EmailFuncTestCase.DEFAULT_FROM_ADDRESS, FRED_EMAIL, FRED_USERNAME);
        String body = EmailKit.getBody(messages[0]);
        body = assertPartialLinkHtml(FilterConfig.FRED_FILTER_PARTIAL, body);
        assertEditLinkHtml(body);
    }

    /**
     * For JRA-17595
     */
    public void testSubscriptionIsHtmlEncoded() throws Exception
    {
        administration.restoreData("Test_JRA_17595.xml");
        configureAndStartSmtpServer();

        final AbstractSubscriptionEmailTest.FilterConfig ADMIN_FILTER_XSS = new AbstractSubscriptionEmailTest.FilterConfig("FilterTwo", "<b>Description</b>", "ProjectTwo", "TWO", 2, 2)
        {
            public Iterator<Integer> getIssueIterator()
            {
                return new AbstractSubscriptionEmailTest.CountingIterator(1, getReturnedIssues());
            }
        };

        subscribeToFilterAndRun(ADMIN_FILTER_XSS, Groups.USERS);

        //we should get four messages back.
        flushMailQueueAndWait(4);
        MimeMessage[] messages = mailService.getReceivedMessages();


        MimeMessage currentMessage = EmailKit.findMessageAddressedTo(ADMIN_EMAIL, messages);
        assertNotNull(currentMessage);

        assertHtmlMessageValid(currentMessage, ADMIN_FILTER_XSS, EmailFuncTestCase.DEFAULT_FROM_ADDRESS, ADMIN_EMAIL, ADMIN_USERNAME);
        String body = EmailKit.getBody(currentMessage);

        // is XSS protection in place??
        text.assertTextPresent(body, "&lt;b&gt;Description&lt;/b&gt;");
        text.assertTextPresent(body, ADMIN_FULLNAME + " &lt;b&gt;bold&lt;/b&gt;");


        currentMessage = EmailKit.findMessageAddressedTo("xss@example.com", messages);
        assertNotNull(currentMessage);
        body = EmailKit.getBody(currentMessage);
        text.assertTextPresent(body, "&lt;b&gt;Description&lt;/b&gt;");
    }

    /**
     * Asserts that the user name is also XSS encoded.  But seriously why call any one <b>xss</b>??
     * <p/>
     * For JRA-17595
     *
     * @throws InterruptedException if stuff goes wrong
     * @throws MessagingException   if the shit hits the fan
     */
    public void testUserNameIsEncodedInHtml() throws InterruptedException, MessagingException
    {
        administration.restoreData("Test_JRA_17595.xml");
        configureAndStartSmtpServer();

        final AbstractSubscriptionEmailTest.FilterConfig XSS_FILTER_XSS = new AbstractSubscriptionEmailTest.FilterConfig("XssFilter", "<b>Description</b>", "ProjectTwo", "TWO", 2, 2)
        {
            public Iterator<Integer> getIssueIterator()
            {
                return new AbstractSubscriptionEmailTest.CountingIterator(1, getReturnedIssues());
            }
        };

        navigation.login("<b>xss</b>", "<b>xss</b>");

        subscribeToFilterAndRun(XSS_FILTER_XSS, null);


        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);
        flushMailQueueAndWait(1);
        final MimeMessage[] messages = mailService.getReceivedMessages();

        // is XSS protection in place??
        final MimeMessage currentMessage = EmailKit.findMessageAddressedTo("xss@example.com", messages);
        assertNotNull(currentMessage);
        final String body = EmailKit.getBody(currentMessage);
        text.assertTextPresent(body, "&lt;b&gt;Description&lt;/b&gt;");
        text.assertTextSequence(body, new String[] { "Subscriber:", "&lt;b&gt;xss&lt;/b&gt;" });
    }
}
