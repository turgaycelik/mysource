package com.atlassian.jira.webtests.ztests.email;

import com.atlassian.jira.functest.framework.email.EmailKit;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.AbstractSubscriptionEmailTest;
import com.atlassian.jira.webtests.EmailFuncTestCase;
import com.icegreen.greenmail.util.GreenMailUtil;

import javax.mail.internet.MimeMessage;

/**
 * Test email subscriptions to shared filters in JIRA.
 * <p/>
 * The test data in 'TestSubscription.xml' contains three projects. 'ProjectOne' (KEY: ONE) contains 210 issues. All of
 * these issues can be returned using the global 'FilterOne' or 'FredFilterOne' saved search. 'ProjectTwo' (KEY: TWO)
 * conatins 2 issues. All of these issues can be returned using the global 'FilterTwo' or 'FredFilterTwo' saved search.
 * Finally, there are global filters called 'ZeroFilter' and 'FredZeroFilter' that returns no issues.
 * <p/>
 * The test data also contains three users. Admin is the administrator and expectes text e-mails. Bob is not an
 * administrator but also expects text e-mails. Fred is not an administrator and expects HTML e-mails.
 *
 * @since v3.13
 */
@SuppressWarnings({ "FeatureEnvy", "ClassTooDeepInInheritanceTree" })  // Meaningless for tests
@WebTest({ Category.FUNC_TEST, Category.BROWSING, Category.EMAIL })
public class TestSharedEmailSubscription extends AbstractSubscriptionEmailTest
{

    public void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("TestSharedSubscription.xml");
        configureAndStartSmtpServer();
        backdoor.userProfile().changeUserNotificationType("bob", "text");
    }

    /**
     * Check e-mail subscription to a shared filter that contains no issues using an unpriveleged user. The e-mail
     * returned should be in HTML for this user.
     *
     * @throws Exception test just throws exception when unexpected error occurs to fail the test.
     */

    public void testHtmlSharedZeroResults() throws Exception
    {
        navigation.login(FRED_USERNAME, FRED_PASSWORD);
        subscribeToFilterAndRun(FilterConfig.ADMIN_FILTER_ZERO_RESULTS, null);

        navigation.logout();
        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);

        flushMailQueueAndWait(1);
        MimeMessage[] messages = mailService.getReceivedMessages();

        assertNotNull(messages[0]);

        //Fred's message should have:
        // 1. No issues.
        // 2. No partial isssues link.
        // 3. Edit link.

        assertHtmlMessageValid(messages[0], FilterConfig.ADMIN_FILTER_ZERO_RESULTS, EmailFuncTestCase.DEFAULT_FROM_ADDRESS, FRED_EMAIL, FRED_USERNAME);
        String body = GreenMailUtil.getBody(messages[0]);
        assertNotPartialLink(body);
        assertEditLinkHtml(body);
    }

    /**
     * Check e-mail subscription to a shared filter that contains all matched issues using an unpriveleged user. The
     * e-mail returned should be in HTML for this user.
     *
     * @throws Exception test just throws exception when unexpected error occurs to fail the test.
     */

    public void testHtmlSharedCompleteResults() throws Exception
    {
        navigation.login(FRED_USERNAME, FRED_PASSWORD);
        subscribeToFilterAndRun(FilterConfig.ADMIN_FILTER_FULL, null);

        navigation.logout();
        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);

        flushMailQueueAndWait(1);
        MimeMessage[] messages = mailService.getReceivedMessages();

        assertNotNull(messages[0]);

        //Fred's message should have:
        // 1. Two issues.
        // 2. No partial isssues link.
        // 3. Edit link.

        assertHtmlMessageValid(messages[0], FilterConfig.ADMIN_FILTER_FULL, EmailFuncTestCase.DEFAULT_FROM_ADDRESS, FRED_EMAIL, FRED_USERNAME);
        String body = EmailKit.getBody(messages[0]);
        assertNotPartialLink(body);
        assertEditLinkHtml(body);
    }

    /**
     * Check e-mail subscription that only contains some of the filter's results using an unprivieleged user. There
     * should be a link to the filter in these e-mails. The e-mail returned should be in HTML for this user.
     *
     * @throws Exception test just throws exception when unexpected error occurs to fail the test.
     */

    public void testHtmlSharedPartialResults() throws Exception
    {
        navigation.login(FRED_USERNAME, FRED_PASSWORD);
        subscribeToFilterAndRun(FilterConfig.ADMIN_FILTER_PARTIAL, null);

        navigation.logout();
        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);

        flushMailQueueAndWait(1);
        MimeMessage[] messages = mailService.getReceivedMessages();

        assertNotNull(messages[0]);

        //Fred's message should have:
        // 1. 200 issues.
        // 2. Partial isssues link.
        // 3. Edit link.

        assertHtmlMessageValid(messages[0], FilterConfig.ADMIN_FILTER_PARTIAL, EmailFuncTestCase.DEFAULT_FROM_ADDRESS, FRED_EMAIL, FRED_USERNAME);
        String body = EmailKit.getBody(messages[0]);
        body = assertPartialLinkHtml(FilterConfig.ADMIN_FILTER_PARTIAL, body);
        assertEditLinkHtml(body);
    }

    /**
     * Check e-mail subscription to a shared filter that contains no issues using an unpriveleged user. The e-mail
     * returned should be in text for this user.
     *
     * @throws Exception test just throws exception when unexpected error occurs to fail the test.
     */

    public void testTextSharedZeroResults() throws Exception
    {
        navigation.login(BOB_USERNAME, BOB_PASSWORD);
        subscribeToFilterAndRun(FilterConfig.ADMIN_FILTER_ZERO_RESULTS, null);

        navigation.logout();
        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);

        flushMailQueueAndWait(1);
        MimeMessage[] messages = mailService.getReceivedMessages();

        assertNotNull(messages[0]);

        //Bob's message should have:
        // 1. No issues.
        // 2. No partial isssues link.
        // 3. Edit link.

        assertTextMessageValid(messages[0], FilterConfig.ADMIN_FILTER_ZERO_RESULTS, EmailFuncTestCase.DEFAULT_FROM_ADDRESS, BOB_EMAIL, BOB_USERNAME);
        String body = GreenMailUtil.getBody(messages[0]);
        assertNotPartialLink(body);
        assertEditLinkText(body);
    }

    /**
     * Check e-mail subscription to a shared filter that contains all matched issues using an unpriveleged user. The
     * e-mail returned should be in text for this user.
     *
     * @throws Exception test just throws exception when unexpected error occurs to fail the test.
     */

    public void testTextSharedCompleteResults() throws Exception
    {
        navigation.login(BOB_USERNAME, BOB_PASSWORD);
        subscribeToFilterAndRun(FilterConfig.ADMIN_FILTER_FULL, null);

        navigation.logout();
        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);

        flushMailQueueAndWait(1);
        MimeMessage[] messages = mailService.getReceivedMessages();

        assertNotNull(messages[0]);

        //BOB's message should have:
        // 1. Two issues.
        // 2. No partial isssues link.
        // 3. Edit link.

        assertTextMessageValid(messages[0], FilterConfig.ADMIN_FILTER_FULL, EmailFuncTestCase.DEFAULT_FROM_ADDRESS, BOB_EMAIL, BOB_USERNAME);
        String body = GreenMailUtil.getBody(messages[0]);
        assertNotPartialLink(body);
        assertEditLinkText(body);
    }

    /**
     * Check e-mail subscription that only contains some of the filter's results using an unprivieleged user. There
     * should be a link to the filter in these e-mails. The e-mail returned should be in text for this user.
     *
     * @throws Exception test just throws exception when unexpected error occurs to fail the test.
     */

    public void testTextSharedPartialResults() throws Exception
    {
        navigation.login(BOB_USERNAME, BOB_PASSWORD);
        subscribeToFilterAndRun(FilterConfig.ADMIN_FILTER_PARTIAL, null);

        navigation.logout();
        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);

        flushMailQueueAndWait(1);
        MimeMessage[] messages = mailService.getReceivedMessages();

        assertNotNull(messages[0]);

        //Fred's message should have:
        // 1. 200 issues.
        // 2. Partial isssues link.
        // 3. Edit link.

        assertTextMessageValid(messages[0], FilterConfig.ADMIN_FILTER_PARTIAL, EmailFuncTestCase.DEFAULT_FROM_ADDRESS, BOB_EMAIL, BOB_USERNAME);
        String body = GreenMailUtil.getBody(messages[0]);
        body = assertPartialLinkText(FilterConfig.ADMIN_FILTER_PARTIAL, body);
        assertEditLinkText(body);
    }
}
