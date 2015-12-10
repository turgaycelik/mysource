package com.atlassian.jira.webtests.ztests.filter;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * 
 * @since v4.0
 */
@WebTest ({ Category.FUNC_TEST, Category.FILTERS })
public class TestFilterWarnings extends FuncTestCase
{
    private static final String NO_MAIL_SERVER_IS_CURRENTLY_CONFIGURED =
            "No mail server is currently configured. Notifications will not be sent.";

    /**
     * Set up a simple instance with a filter and a subscription to it.
     */
    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreBlankInstance();
        String filter = backdoor.filters().createFilter("", "Test Filter");
        navigation.manageFilters().addSubscription(Integer.parseInt(filter, 10));
    }

    private void addSubscription()
    {

        tester.checkCheckbox("filter.subscription.prefix.dailyWeeklyMonthly", "daysOfMonth");
        tester.submit("Subscribe");
    }

    public void testMailNotConfiguredWarning()
    {
        addSubscription();

        navigation.manageFilters().goToDefault();
        navigation.manageFilters().manageSubscriptions(10000);
        text.assertTextPresent(locator.page(), NO_MAIL_SERVER_IS_CURRENTLY_CONFIGURED);
        text.assertTextNotPresent(locator.page(), "Run now");
    }

    public void testMailConfiguredNoWarning()
    {
        addSubscription();

        navigation.gotoDashboard();
        administration.mailServers().Smtp().goTo().add("testserver", "admin@example.com", "JIRA", "mail.example.com");

        navigation.manageFilters().goToDefault();
        navigation.manageFilters().manageSubscriptions(10000);
        text.assertTextNotPresent(locator.page(), NO_MAIL_SERVER_IS_CURRENTLY_CONFIGURED);
        text.assertTextPresent(locator.page(), "Run now");
    }

    public void testNoMailServerNoSubscriptions()
    {
        navigation.manageFilters().goToDefault();
        text.assertTextNotPresent(locator.page(), NO_MAIL_SERVER_IS_CURRENTLY_CONFIGURED);
    }
}
