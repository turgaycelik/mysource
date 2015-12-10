package com.atlassian.jira.webtests.ztests.email;

import java.util.Iterator;
import java.util.List;

import javax.mail.internet.MimeMessage;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.AbstractSubscriptionEmailTest;
import com.atlassian.jira.webtests.EmailFuncTestCase;

import com.google.common.collect.Lists;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @since v4.4
 */
@SuppressWarnings ("ClassTooDeepInInheritanceTree")
@WebTest({ Category.FUNC_TEST, Category.BROWSING, Category.EMAIL })
public class TestSubscriptionsWithTimeZones extends AbstractSubscriptionEmailTest
{

    String BERLIN_EMAIL = "berlin@stuff.com.com";
    String SYDNEY_EMAIL = "sydney@stuff.com.com";

    public void setUpTest()
    {
        super.setUpTest();
        backdoor.restoreDataFromResource("TestSubscriptionsWithTimeZones.xml");
        configureAndStartSmtpServer();
        administration.generalConfiguration().setDefaultUserTimeZone("Australia/Sydney");
    }

    public void testUsersInSameGroupButDifferentTimeZones() throws Exception
    {

        final AbstractSubscriptionEmailTest.FilterConfig filterResultsForAdminUser = new AbstractSubscriptionEmailTest.FilterConfig("Filter with DateTimeCF", null, "ProjectTwo", "TWO", 1, 1)
        {
            public Iterator<Integer> getIssueIterator()
            {
                return Lists.newArrayList(1).iterator();
            }
        };
        final AbstractSubscriptionEmailTest.FilterConfig filterResultsForBerlinUser = new AbstractSubscriptionEmailTest.FilterConfig("Filter with DateTimeCF", null, "ProjectTwo", "TWO", 1, 1)
        {
            public Iterator<Integer> getIssueIterator()
            {
                return Lists.newArrayList(1).iterator();
            }

        };
        final AbstractSubscriptionEmailTest.FilterConfig filterResultsForSydneyUser = new AbstractSubscriptionEmailTest.FilterConfig("Filter with DateTimeCF", null, "ProjectTwo", "TWO", 2, 2)
        {
            public Iterator<Integer> getIssueIterator()
            {
                return Lists.newArrayList(2, 1).iterator();
            }
        };
        subscribeToFilterAndRun(filterResultsForAdminUser, "jira-administrators");

        navigation.logout();
        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);


        flushMailQueueAndWait(3);

        //Bob's message should have:
        // 1. No issues.
        // 2. No partial isssues link.
        // 3. Edit link.


        List<MimeMessage> adminEmail = getMessagesForRecipient(ADMIN_EMAIL);
        assertThat(adminEmail.size(), equalTo(1));
        assertTextMessageValid(adminEmail.get(0), filterResultsForAdminUser, EmailFuncTestCase.DEFAULT_FROM_ADDRESS, ADMIN_EMAIL, ADMIN_USERNAME);

        List<MimeMessage> berlinEmail = getMessagesForRecipient(BERLIN_EMAIL);
        assertThat(berlinEmail.size(), equalTo(1));
        assertTextMessageValid(berlinEmail.get(0), filterResultsForBerlinUser, EmailFuncTestCase.DEFAULT_FROM_ADDRESS, BERLIN_EMAIL, ADMIN_USERNAME);

        List<MimeMessage> sydneyEmail = getMessagesForRecipient(SYDNEY_EMAIL);
        assertThat(sydneyEmail.size(), equalTo(1));
        assertTextMessageValid(sydneyEmail.get(0), filterResultsForSydneyUser, EmailFuncTestCase.DEFAULT_FROM_ADDRESS, SYDNEY_EMAIL, ADMIN_USERNAME);
    }
}
