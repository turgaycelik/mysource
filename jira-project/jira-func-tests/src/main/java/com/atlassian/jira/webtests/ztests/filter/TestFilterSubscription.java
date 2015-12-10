package com.atlassian.jira.webtests.ztests.filter;

import com.atlassian.jira.functest.framework.locator.CssLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;

/**
 * Func tests for the new cron editor
 */
@WebTest ({ Category.FUNC_TEST, Category.FILTERS })
public class TestFilterSubscription extends JIRAWebTest
{
    public TestFilterSubscription(String name)
    {
        super(name);
    }

    // Restore a blank instance and create a sample
    //filter for which we can test adding subscriptions.
    public void setUp()
    {
        super.setUp();
        administration.restoreBlankInstance();
        String filter = backdoor.filters().createFilter("", "Test Filter");
        navigation.manageFilters().manageSubscriptions(Integer.parseInt(filter, 10));
    }

    /**
     * Test creating the simplest trigger.  Daily with all the default
     * options.
     */
    public void testDefaultDailyTrigger()
    {
        tester.clickLinkWithText("Add subscription");
        tester.submit("Subscribe");
        assertTextPresent("0 0 1 ? * *");
        assertTextPresent("Daily at 1:00 am");
    }

    /**
     * Daily trigger every 2 hours from 1am - 5pm
     */
    public void testNonDefaultDailyTriggerEvery2Hours()
    {
        tester.clickLinkWithText("Add subscription");
        tester.selectOption("filter.subscription.prefix.interval", "every 2 hours");
        tester.selectOption("filter.subscription.prefix.runToHours", "5");
        tester.selectOption("filter.subscription.prefix.runToMeridian", "pm");
        tester.submit("Subscribe");

        // since the "to" hours is exclusive in the UI but inclusive in the cron expression, check that the cron expression
        // has the "to" hours decremented by 1, but the pretty description still says "8pm"
        assertTextPresent("0 0 1-16/2 ? * *");
        assertTextPresent("Daily every 2 hours from 1:00 am to 5:00 pm");

        // check to make sure that all the edit form elements are set correctly
        tester.clickLink("edit_subscription");
        tester.setWorkingForm("filter-subscription");
        assertOptionSelectedById("filter.subscription.prefix.dailyWeeklyMonthly", "daily");
        assertOptionSelected("filter.subscription.prefix.interval", "every 2 hours");
        assertOptionSelected("filter.subscription.prefix.runFromHours", "1");
        assertOptionSelected("filter.subscription.prefix.runFromMeridian", "am");
        assertOptionSelected("filter.subscription.prefix.runToHours", "5");
        assertOptionSelected("filter.subscription.prefix.runToMeridian", "pm");
    }

    /**
     * Daily trigger every 2 hours all day
     */
    public void testNonDefaultDailyTriggerEvery2HoursAllDay()
    {
        tester.clickLinkWithText("Add subscription");
        tester.selectOption("filter.subscription.prefix.interval", "every 2 hours");
        tester.selectOption("filter.subscription.prefix.runFromHours", "5");
        tester.selectOption("filter.subscription.prefix.runFromMeridian", "pm");
        tester.selectOption("filter.subscription.prefix.runToHours", "5");
        tester.selectOption("filter.subscription.prefix.runToMeridian", "pm");
        tester.submit("Subscribe");
        assertTextPresent("0 0 */2 ? * *");
        assertTextPresent("Daily every 2 hours");
        assertTextNotPresent("from 5:00 pm to 5:00 pm");

        // check to make sure that all the edit form elements are set correctly
        tester.clickLink("edit_subscription");
        tester.setWorkingForm("filter-subscription");
        assertOptionSelectedById("filter.subscription.prefix.dailyWeeklyMonthly", "daily");
        assertOptionSelected("filter.subscription.prefix.interval", "every 2 hours");
        assertOptionSelected("filter.subscription.prefix.runFromHours", "12");
        assertOptionSelected("filter.subscription.prefix.runFromMeridian", "am");
        assertOptionSelected("filter.subscription.prefix.runToHours", "12");
        assertOptionSelected("filter.subscription.prefix.runToMeridian", "am");
    }

    /**
     * Advanced trigger that has both minutes and hours increment which is not supported by the Cron Editor UI
     */
    public void testAdvancedExpressionWithBothMinuteAndHourIncrements()
    {
        final String cronExpr = "0 0/15 */3 ? * *";
        tester.clickLinkWithText("Add subscription");
        tester.checkCheckbox("filter.subscription.prefix.dailyWeeklyMonthly", "advanced");
        tester.setFormElement("filter.subscription.prefix.cronString", cronExpr);
        tester.submit("Subscribe");
        assertTextPresent(cronExpr);

        // check to make sure that all the edit form elements are set correctly
        clickLink("edit_subscription");
        setWorkingForm("filter-subscription");
        assertOptionSelectedById("filter.subscription.prefix.dailyWeeklyMonthly", "advanced");
        assertFormElementHasValue("filter-subscription", "filter.subscription.prefix.cronString", cronExpr);
    }

    /**
     * Advanced trigger that has minutes increment but single hour which is not supported by the Cron Editor UI
     */
    public void testAdvancedExpressionWithMinuteIncrementButSingleHour()
    {
        final String cronExpr = "0 0/15 7 ? * *";
        tester.clickLinkWithText("Add subscription");
        tester.checkCheckbox("filter.subscription.prefix.dailyWeeklyMonthly", "advanced");
        tester.setFormElement("filter.subscription.prefix.cronString", cronExpr);
        tester.submit("Subscribe");
        tester.assertTextPresent(cronExpr);

        // check to make sure that all the edit form elements are set correctly
        tester.clickLink("edit_subscription");
        tester.setWorkingForm("filter-subscription");
        assertOptionSelectedById("filter.subscription.prefix.dailyWeeklyMonthly", "advanced");
        assertFormElementHasValue("filter-subscription", "filter.subscription.prefix.cronString", cronExpr);
    }

    /**
     * Advanced trigger that has minutes increment but single hour with range, which gets interpretted correctly and
     * is supported by the Cron Editor UI
     */
    public void testAdvancedExpressionWithMinuteIncrementAndSingleHourRange()
    {
        final String cronExpr = "0 0/15 7-7 ? * *";
        tester.clickLinkWithText("Add subscription");
        tester.checkCheckbox("filter.subscription.prefix.dailyWeeklyMonthly", "advanced");
        tester.setFormElement("filter.subscription.prefix.cronString", cronExpr);
        tester.submit("Subscribe");
        assertTextPresent(cronExpr);
        assertTextPresent("Daily every 15 minutes from 7:00 am to 8:00 am");

        // check to make sure that all the edit form elements are set correctly
        tester.clickLink("edit_subscription");
        tester.setWorkingForm("filter-subscription");
        assertOptionSelectedById("filter.subscription.prefix.dailyWeeklyMonthly", "daily");
        assertOptionSelected("filter.subscription.prefix.interval", "every 15 minutes");
        assertOptionSelected("filter.subscription.prefix.runFromHours", "7");
        assertOptionSelected("filter.subscription.prefix.runFromMeridian", "am");
        assertOptionSelected("filter.subscription.prefix.runToHours", "8");
        assertOptionSelected("filter.subscription.prefix.runToMeridian", "am");
    }

    public void testDailyInvalidRange()
    {
        tester.clickLinkWithText("Add subscription");
        tester.checkCheckbox("filter.subscription.prefix.dailyWeeklyMonthly", "daily");
        tester.selectOption("filter.subscription.prefix.interval", "every 3 hours");
        tester.selectOption("filter.subscription.prefix.runFromHours", "1"); // 1am
        tester.selectOption("filter.subscription.prefix.runToHours", "12"); // 12 am is before 1am
        tester.submit("Subscribe");
        assertTextPresent("You must select a from time that is before the to time.");
    }

    public void testDefaultDaysPerWeekSingleDay()
    {
        tester.clickLinkWithText("Add subscription");
        tester.checkCheckbox("filter.subscription.prefix.dailyWeeklyMonthly", "daysOfWeek");
        //Monday
        tester.checkCheckbox("filter.subscription.prefix.weekday", "2");
        submit("Subscribe");
        assertTextPresent("0 0 1 ? * 2");
        assertTextPresent("Monday at 1:00 am");
    }

    public void testDefaultDaysPerWeekMultipleDays()
    {
        tester.clickLinkWithText("Add subscription");
        tester.checkCheckbox("filter.subscription.prefix.dailyWeeklyMonthly", "daysOfWeek");
        //Monday, Tue And Thurs
        tester.checkCheckbox("filter.subscription.prefix.weekday", "2");
        tester.checkCheckbox("filter.subscription.prefix.weekday", "3");
        tester.checkCheckbox("filter.subscription.prefix.weekday", "5");
        tester.submit("Subscribe");
        assertTextPresent("0 0 1 ? * 2,3,5");
        assertTextPresent("Each Monday, Tuesday and Thursday at 1:00 am");
    }

    public void testNoDaysPerWeekSelected()
    {
        tester.clickLinkWithText("Add subscription");
        tester.checkCheckbox("filter.subscription.prefix.dailyWeeklyMonthly", "daysOfWeek");
        tester.submit("Subscribe");
        assertTextPresent("You must select one or more days of the week for the Days per Week mode.");
    }

    public void testDaysPerWeekInvalidRange()
    {
        tester.clickLinkWithText("Add subscription");
        tester.checkCheckbox("filter.subscription.prefix.dailyWeeklyMonthly", "daysOfWeek");
        tester.checkCheckbox("filter.subscription.prefix.weekday", "1"); //Sunday
        tester.selectOption("filter.subscription.prefix.interval", "every 3 hours");
        tester.selectOption("filter.subscription.prefix.runFromHours", "1"); // 1am
        tester.selectOption("filter.subscription.prefix.runToHours", "12"); // 12 am is before 1am
        tester.submit("Subscribe");
        assertTextPresent("You must select a from time that is before the to time.");
    }

    /**
     * Check that having an invalid range in a mode that accepts a range doesn't
     * cause a validation failure for the daysOfMonth mode which doesn't accept
     * ranges. The form has been written to share the range or "run once" time
     * fields so mode switching only fixes the range problem in this case of
     * the mode that doesn't have range fields.
     */
    public void testInvalidForOneModeButValidForThisMode()
    {
        tester.clickLinkWithText("Add subscription");
        tester.checkCheckbox("filter.subscription.prefix.dailyWeeklyMonthly", "daysOfWeek");
        tester.selectOption("filter.subscription.prefix.interval", "every 3 hours");
        tester.selectOption("filter.subscription.prefix.runToHours", "12");
        tester.submit("Subscribe");
        // check we get invalid state fot the days of week mode
        assertTextPresent("You must select a from time that is before the to time.");
        // then leave that invalid stuff there but switch mode
        tester.checkCheckbox("filter.subscription.prefix.dailyWeeklyMonthly", "daysOfMonth");
        tester.selectOption("filter.subscription.prefix.runOnceHours", "3");
        tester.submit("Subscribe");
        // check that now its OK because this mode doesn't have the (invalid) range
        assertTextPresent("0 0 3 1 * ?");
        assertTextPresent("The 1st day of every month at 3:00 am");
    }

    /**
     * Check that the form can be edited into each mode in turn.
     */
    public void testEditDaily()
    {
        tester.clickLinkWithText("Add subscription");
        tester.submit("Subscribe");
        assertTextPresent("0 0 1 ? * *");
        assertTextPresent("Daily at 1:00 am");
        tester.clickLink("edit_subscription");
        // edit to use a range including a pm time
        tester.checkCheckbox("filter.subscription.prefix.dailyWeeklyMonthly", "daily");
        tester.selectOption("filter.subscription.prefix.interval", "every 2 hours");
        tester.selectOption("filter.subscription.prefix.runFromHours", "10");
        tester.selectOption("filter.subscription.prefix.runToHours", "8");
        tester.selectOption("filter.subscription.prefix.runToMeridian", "pm");
        tester.submit("Update");

        // since the "to" hours is exclusive in the UI but inclusive in the cron expression, check that the cron expression
        // has the "to" hours decremented by 1, but the pretty description still says "8pm"
        assertTextPresent("0 0 10-19/2 ? * *");
        assertTextPresent("Daily every 2 hours from 10:00 am to 8:00 pm");

        // switch mode and use a time with minutes in it
        tester.clickLink("edit_subscription");
        tester.selectOption("filter.subscription.prefix.interval", "once per day");
        tester.checkCheckbox("filter.subscription.prefix.dailyWeeklyMonthly", "daysOfWeek");
        tester.checkCheckbox("filter.subscription.prefix.weekday", "2");
        tester.selectOption("filter.subscription.prefix.runOnceHours", "6");
        tester.selectOption("filter.subscription.prefix.runOnceMins", "35");
        tester.submit("Update");
        assertTextPresent("0 35 6 ? * 2");
        assertTextPresent("Each Monday at 6:35 am");

        // edit to day of month
        tester.clickLink("edit_subscription");
        tester.checkCheckbox("filter.subscription.prefix.dailyWeeklyMonthly", "daysOfMonth");
        tester.submit("Update");
        assertTextPresent("0 35 6 1 * ?");
        assertTextPresent("The 1st day of every month at 6:35 am");

        // edit to day of week of month mode with last wednesday of month
        tester.clickLink("edit_subscription");
        tester.checkCheckbox("filter.subscription.prefix.daysOfMonthOpt", "dayOfWeekOfMonth");
        tester.selectOption("filter.subscription.prefix.week", "last");
        tester.selectOption("filter.subscription.prefix.day", "Wednesday");
        tester.selectOption("filter.subscription.prefix.runOnceMeridian", "pm");
        tester.submit("Update");
        assertTextPresent("0 35 18 ? * 4L");
        assertTextPresent("The last Wednesday of every month at 6:35 pm");
    }

    public void testDefaultDayOfMonthMode()
    {
        tester.clickLinkWithText("Add subscription");
        tester.checkCheckbox("filter.subscription.prefix.dailyWeeklyMonthly", "daysOfMonth");
        tester.submit("Subscribe");
        assertTextPresent("0 0 1 1 * ?");
        assertTextPresent("The 1st day of every month at 1:00 am");
    }

    public void testLastDayOfMonthMode()
    {
        tester.clickLinkWithText("Add subscription");
        tester.checkCheckbox("filter.subscription.prefix.dailyWeeklyMonthly", "daysOfMonth");
        tester.selectOption("filter.subscription.prefix.monthDay", "last");
        tester.selectOption("filter.subscription.prefix.runOnceHours", "12");
        tester.selectOption("filter.subscription.prefix.runOnceMins", "35");
        tester.submit("Subscribe");
        assertTextPresent("0 35 0 L * ?");
        assertTextPresent("The last day of every month at 12:35 am");
    }

    public void testComplexMonthMode()
    {
        tester.clickLinkWithText("Add subscription");
        tester.checkCheckbox("filter.subscription.prefix.dailyWeeklyMonthly", "daysOfMonth");
        tester.checkCheckbox("filter.subscription.prefix.daysOfMonthOpt", "dayOfWeekOfMonth");
        //Every fourth Thursday at 4:35 AM
        tester.selectOption("filter.subscription.prefix.week", "fourth");
        tester.selectOption("filter.subscription.prefix.day", "Thursday");
        tester.selectOption("filter.subscription.prefix.runOnceHours", "4");
        tester.selectOption("filter.subscription.prefix.runOnceMins", "35");
        tester.submit("Subscribe");
        assertTextPresent("0 35 4 ? * 5#4");
        assertTextPresent("The fourth Thursday of every month at 4:35 am");
    }

    public void testAdvancedMode()
    {
        tester.clickLinkWithText("Add subscription");
        tester.checkCheckbox("filter.subscription.prefix.dailyWeeklyMonthly", "advanced");
        tester.setFormElement("filter.subscription.prefix.cronString", "Invalid");
        tester.submit("Subscribe");
        assertTextPresent("Illegal characters for this position: INV");

        // * * * is invalid for Quartz.  Should have eithher * * ? or ? * *
        tester.setFormElement("filter.subscription.prefix.cronString", "0 35 4 * * *");
        tester.submit("Subscribe");
        assertTextPresent("Support for specifying both a day-of-week AND a day-of-month parameter is not implemented.");

        tester.setFormElement("filter.subscription.prefix.cronString", "0 37 4 * * ?");
        tester.submit("Subscribe");
        assertTextPresent("0 37 4 * * ?");
        assertTextNotPresent("Daily at 4:37 am");
    }

    public void testDeleteSubscription()
    {
        tester.clickLinkWithText("Add subscription");
        tester.checkCheckbox("filter.subscription.prefix.dailyWeeklyMonthly", "daysOfMonth");
        tester.submit("Subscribe");
        assertTextPresent("0 0 1 1 * ?");
        assertTextPresent("The 1st day of every month at 1:00 am");
        tester.clickLinkWithText("Delete");
        assertions.assertNodeHasText(new CssLocator(tester, ".aui-page-header h2"), "Subscriptions");
        assertTextNotPresent("0 0 1 1 * ?");
    }

    public void testSubscribeToFilterWithNoPermissions()
    {
        navigation.logout();
        navigation.login(FRED_USERNAME);
        tester.gotoPage("/secure/FilterSubscription!default.jspa?filterId=10000");
        assertTextPresent("The selected filter is not available to you, perhaps it has been deleted or had its permissions changed.");
        tester.submit("Subscribe");
        assertTextSequence(new String[] {"Access Denied", "It seems that you have tried to perform an operation which you are not permitted to perform."});
    }

    public void testDeleteGroupWithSubscription()
    {
        navigation.logout();
        navigation.login(ADMIN_USERNAME);

        navigation.gotoAdminSection("group_browser");
        tester.setFormElement("addName", "delete-me");
        tester.submit("add_group");



        String filter = backdoor.filters().createFilter("", "delete-group-filter");
        backdoor.filterSubscriptions().addSubscription(Long.parseLong(filter, 10), "delete-me", "0 0 0 ? 1 MON#3", false);

        navigation.gotoAdmin();
        tester.clickLink("group_browser");
        tester.clickLink("del_delete-me");

        text.assertTextPresent(locator.page(), "This group is referenced in the following filter subscriptions:");
        text.assertTextPresent(locator.page(), "'delete-group-filter' owned by user : 'admin'");
        text.assertTextPresent(locator.page(), "These filter subscriptions will be automatically deleted.");

        tester.submit("Delete");
        navigation.manageFilters().goToDefault();
        text.assertTextNotPresent(locator.page(), "1 Subscription");
    }
}
