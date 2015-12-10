package com.atlassian.jira.webtests.ztests.filter;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import java.io.IOException;

/**
 * Func tests for the new cron editor XSS protection.
 */
@WebTest ({ Category.FUNC_TEST, Category.FILTERS, Category.SECURITY })
public class TestFilterSubscriptionXss extends FuncTestCase
{

    public void setUpTest()
    {
        administration.restoreBlankInstance();
    }

    public void testXssInInterval() throws Exception
    {
        tester.gotoPage("/secure/FilterSubscription.jspa?"
                + "groupName=Personal+Subscription"
                + "&filter.subscription.prefix.dailyWeeklyMonthly=advanced"
                + "&filter.subscription.prefix.daysOfMonthOpt=dayOfMonth"
                + "&filter.subscription.prefix.monthDay=1"
                + "&filter.subscription.prefix.week=1"
                + "&filter.subscription.prefix.day=1"
                + "&filter.subscription.prefix.interval=0"
                + "&filter.subscription.prefix.runOnceHours=1"
                + "&filter.subscription.prefix.runOnceMins=0"
                + "&filter.subscription.prefix.runOnceMeridian=am"
                + "&filter.subscription.prefix.runFromHours=1"
                + "&filter.subscription.prefix.runFromMeridian=am"
                + "&filter.subscription.prefix.runToHours=1"
                + "&filter.subscription.prefix.runToMeridian=am"
                + "&filter.subscription.prefix.cronString=%22%3E%3Cimg%20src=//xhtml.im/nyan-cat.gif%20onload=alert(/xss/)%3E"
                + "&lastRun="
                + "&nextRun="
                + "&subId="
                + "&filterId=10000"
                + "&Subscribe=Subscribe");
        assertFalse(tester.getDialog().getResponse().getText().contains("<img src=/xhtml.im/nyan-cat.gif onload=alert(/xss/)>"));
    }

    public void testXssInMonthDayParam() throws IOException
    {
        tester.gotoPage("/secure/FilterSubscription.jspa?filter.subscription.prefix.interval=180&groupName=jira-users"
                + "&filter.subscription.prefix.runFromMins=00"
                + "&nextRun="
                + "&filter.subscription.prefix.runToMins=00"
                + "&filter.subscription.prefix.runToMeridian=pm"
                + "&filter.subscription.prefix.week=2"
                + "&filter.subscription.prefix.runOnceMeridian=pm"
                + "&filter.subscription.prefix.day=2"
                + "&filter.subscription.prefix.runOnceMins=5"
                + "&filter.subscription.prefix.runFromMeridian=pm"
                + "&filter.subscription.prefix.monthDay=<script>alert('XSS!');</script>"
                + "&subId="
                + "&atl_token=b1719c444f52dc051d1d99a5a0cc8d5b8690a864"
                + "&filter.subscription.prefix.runToHours=2"
                + "&lastRun="
                + "&filter.subscription.prefix.cronString=555-555-0199@example.com"
                + "&Subscriure=Subscriure"
                + "&filter.subscription.prefix.runOnceHours=2"
                + "&filter.subscription.prefix.runFromHours=2"
                + "&filterId=10000"
                + "&filter.subscription.prefix.daysOfMonthOpt=dayOfWeekOfMonth"
                + "&emailOnEmpty=on"
                + "&filter.subscription.prefix.dailyWeeklyMonthly=daysOfWeek");
        assertFalse(tester.getDialog().getResponse().getText().contains("<script>alert('XSS!');</script>"));
    }

    public void testXssInRunFromMinsParam() throws IOException
    {
        tester.gotoPage("/secure/FilterSubscription.jspa?filter.subscription.prefix.interval=180&groupName=jira-users"
                + "&filter.subscription.prefix.runFromMins=<script>alert('XSS!');</script>"
                + "&nextRun="
                + "&filter.subscription.prefix.runToMins=00"
                + "&filter.subscription.prefix.runToMeridian=pm"
                + "&filter.subscription.prefix.week=2"
                + "&filter.subscription.prefix.runOnceMeridian=pm"
                + "&filter.subscription.prefix.day=2"
                + "&filter.subscription.prefix.runOnceMins=5"
                + "&filter.subscription.prefix.runFromMeridian=pm"
                + "&filter.subscription.prefix.monthDay=1"
                + "&subId="
                + "&atl_token=b1719c444f52dc051d1d99a5a0cc8d5b8690a864"
                + "&filter.subscription.prefix.runToHours=2"
                + "&lastRun="
                + "&filter.subscription.prefix.cronString=555-555-0199@example.com"
                + "&Subscriure=Subscriure"
                + "&filter.subscription.prefix.runOnceHours=2"
                + "&filter.subscription.prefix.runFromHours=2"
                + "&filterId=10000"
                + "&filter.subscription.prefix.daysOfMonthOpt=dayOfWeekOfMonth"
                + "&emailOnEmpty=on"
                + "&filter.subscription.prefix.dailyWeeklyMonthly=daysOfWeek");
        assertFalse(tester.getDialog().getResponse().getText().contains("<script>alert('XSS!');</script>"));
    }

    public void testXssInRunToMinsParam() throws IOException
    {
        tester.gotoPage("/secure/FilterSubscription.jspa?filter.subscription.prefix.interval=180&groupName=jira-users"
                + "&filter.subscription.prefix.runFromMins=00"
                + "&nextRun="
                + "&filter.subscription.prefix.runToMins=<script>alert('XSS!');</script>"
                + "&filter.subscription.prefix.runToMeridian=pm"
                + "&filter.subscription.prefix.week=2"
                + "&filter.subscription.prefix.runOnceMeridian=pm"
                + "&filter.subscription.prefix.day=2"
                + "&filter.subscription.prefix.runOnceMins=5"
                + "&filter.subscription.prefix.runFromMeridian=pm"
                + "&filter.subscription.prefix.monthDay=1"
                + "&subId="
                + "&atl_token=b1719c444f52dc051d1d99a5a0cc8d5b8690a864"
                + "&filter.subscription.prefix.runToHours=2"
                + "&lastRun="
                + "&filter.subscription.prefix.cronString=555-555-0199@example.com"
                + "&Subscriure=Subscriure"
                + "&filter.subscription.prefix.runOnceHours=2"
                + "&filter.subscription.prefix.runFromHours=2"
                + "&filterId=10000"
                + "&filter.subscription.prefix.daysOfMonthOpt=dayOfWeekOfMonth"
                + "&emailOnEmpty=on"
                + "&filter.subscription.prefix.dailyWeeklyMonthly=daysOfWeek");
        assertFalse(tester.getDialog().getResponse().getText().contains("<script>alert('XSS!');</script>"));
    }

    public void testXssInRunToMeridianParam() throws IOException
    {
        tester.gotoPage("/secure/FilterSubscription.jspa?filter.subscription.prefix.interval=180&groupName=jira-users"
                + "&filter.subscription.prefix.runFromMins=00"
                + "&nextRun="
                + "&filter.subscription.prefix.runToMins=00"
                + "&filter.subscription.prefix.runToMeridian=<script>alert('XSS!');</script>"
                + "&filter.subscription.prefix.week=2"
                + "&filter.subscription.prefix.runOnceMeridian=pm"
                + "&filter.subscription.prefix.day=2"
                + "&filter.subscription.prefix.runOnceMins=5"
                + "&filter.subscription.prefix.runFromMeridian=pm"
                + "&filter.subscription.prefix.monthDay=1"
                + "&subId="
                + "&atl_token=b1719c444f52dc051d1d99a5a0cc8d5b8690a864"
                + "&filter.subscription.prefix.runToHours=2"
                + "&lastRun="
                + "&filter.subscription.prefix.cronString=555-555-0199@example.com"
                + "&Subscriure=Subscriure"
                + "&filter.subscription.prefix.runOnceHours=2"
                + "&filter.subscription.prefix.runFromHours=2"
                + "&filterId=10000"
                + "&filter.subscription.prefix.daysOfMonthOpt=dayOfWeekOfMonth"
                + "&emailOnEmpty=on"
                + "&filter.subscription.prefix.dailyWeeklyMonthly=daysOfWeek");
        assertFalse(tester.getDialog().getResponse().getText().contains("<script>alert('XSS!');</script>"));
    }

    public void testXssInWeekParam() throws IOException
    {
        tester.gotoPage("/secure/FilterSubscription.jspa?filter.subscription.prefix.interval=180&groupName=jira-users"
                + "&filter.subscription.prefix.runFromMins=00"
                + "&nextRun="
                + "&filter.subscription.prefix.runToMins=00"
                + "&filter.subscription.prefix.runToMeridian=00"
                + "&filter.subscription.prefix.week=<script>alert('XSS!');</script>"
                + "&filter.subscription.prefix.runOnceMeridian=pm"
                + "&filter.subscription.prefix.day=2"
                + "&filter.subscription.prefix.runOnceMins=5"
                + "&filter.subscription.prefix.runFromMeridian=pm"
                + "&filter.subscription.prefix.monthDay=1"
                + "&subId="
                + "&atl_token=b1719c444f52dc051d1d99a5a0cc8d5b8690a864"
                + "&filter.subscription.prefix.runToHours=2"
                + "&lastRun="
                + "&filter.subscription.prefix.cronString=555-555-0199@example.com"
                + "&Subscriure=Subscriure"
                + "&filter.subscription.prefix.runOnceHours=2"
                + "&filter.subscription.prefix.runFromHours=2"
                + "&filterId=10000"
                + "&filter.subscription.prefix.daysOfMonthOpt=dayOfWeekOfMonth"
                + "&emailOnEmpty=on"
                + "&filter.subscription.prefix.dailyWeeklyMonthly=daysOfWeek");
        assertFalse(tester.getDialog().getResponse().getText().contains("<script>alert('XSS!');</script>"));
    }

    public void testXssInRunOnceMeridianParam() throws IOException
    {
        tester.gotoPage("/secure/FilterSubscription.jspa?filter.subscription.prefix.interval=180&groupName=jira-users"
                + "&filter.subscription.prefix.runFromMins=00"
                + "&nextRun="
                + "&filter.subscription.prefix.runToMins=00"
                + "&filter.subscription.prefix.runToMeridian=00"
                + "&filter.subscription.prefix.week=2"
                + "&filter.subscription.prefix.runOnceMeridian=<script>alert('XSS!');</script>"
                + "&filter.subscription.prefix.day=2"
                + "&filter.subscription.prefix.runOnceMins=5"
                + "&filter.subscription.prefix.runFromMeridian=pm"
                + "&filter.subscription.prefix.monthDay=1"
                + "&subId="
                + "&atl_token=b1719c444f52dc051d1d99a5a0cc8d5b8690a864"
                + "&filter.subscription.prefix.runToHours=2"
                + "&lastRun="
                + "&filter.subscription.prefix.cronString=555-555-0199@example.com"
                + "&Subscriure=Subscriure"
                + "&filter.subscription.prefix.runOnceHours=2"
                + "&filter.subscription.prefix.runFromHours=2"
                + "&filterId=10000"
                + "&filter.subscription.prefix.daysOfMonthOpt=dayOfWeekOfMonth"
                + "&emailOnEmpty=on"
                + "&filter.subscription.prefix.dailyWeeklyMonthly=daysOfWeek");
        assertFalse(tester.getDialog().getResponse().getText().contains("<script>alert('XSS!');</script>"));
    }

    public void testXssInDayParam() throws IOException
    {
        tester.gotoPage("/secure/FilterSubscription.jspa?filter.subscription.prefix.interval=180&groupName=jira-users"
                + "&filter.subscription.prefix.runFromMins=00"
                + "&nextRun="
                + "&filter.subscription.prefix.runToMins=00"
                + "&filter.subscription.prefix.runToMeridian=00"
                + "&filter.subscription.prefix.week=2"
                + "&filter.subscription.prefix.runOnceMeridian=pm"
                + "&filter.subscription.prefix.day=<script>alert('XSS!');</script>"
                + "&filter.subscription.prefix.runOnceMins=5"
                + "&filter.subscription.prefix.runFromMeridian=pm"
                + "&filter.subscription.prefix.monthDay=1"
                + "&subId="
                + "&atl_token=b1719c444f52dc051d1d99a5a0cc8d5b8690a864"
                + "&filter.subscription.prefix.runToHours=2"
                + "&lastRun="
                + "&filter.subscription.prefix.cronString=555-555-0199@example.com"
                + "&Subscriure=Subscriure"
                + "&filter.subscription.prefix.runOnceHours=2"
                + "&filter.subscription.prefix.runFromHours=2"
                + "&filterId=10000"
                + "&filter.subscription.prefix.daysOfMonthOpt=dayOfWeekOfMonth"
                + "&emailOnEmpty=on"
                + "&filter.subscription.prefix.dailyWeeklyMonthly=daysOfWeek");
        assertFalse(tester.getDialog().getResponse().getText().contains("<script>alert('XSS!');</script>"));
    }

    public void testXssInRunOnceMinsParam() throws IOException
    {
        tester.gotoPage("/secure/FilterSubscription.jspa?filter.subscription.prefix.interval=180&groupName=jira-users"
                + "&filter.subscription.prefix.runFromMins=00"
                + "&nextRun="
                + "&filter.subscription.prefix.runToMins=00"
                + "&filter.subscription.prefix.runToMeridian=00"
                + "&filter.subscription.prefix.week=2"
                + "&filter.subscription.prefix.runOnceMeridian=pm"
                + "&filter.subscription.prefix.day=2"
                + "&filter.subscription.prefix.runOnceMins=<script>alert('XSS!');</script>"
                + "&filter.subscription.prefix.runFromMeridian=pm"
                + "&filter.subscription.prefix.monthDay=1"
                + "&subId="
                + "&atl_token=b1719c444f52dc051d1d99a5a0cc8d5b8690a864"
                + "&filter.subscription.prefix.runToHours=2"
                + "&lastRun="
                + "&filter.subscription.prefix.cronString=555-555-0199@example.com"
                + "&Subscriure=Subscriure"
                + "&filter.subscription.prefix.runOnceHours=2"
                + "&filter.subscription.prefix.runFromHours=2"
                + "&filterId=10000"
                + "&filter.subscription.prefix.daysOfMonthOpt=dayOfWeekOfMonth"
                + "&emailOnEmpty=on"
                + "&filter.subscription.prefix.dailyWeeklyMonthly=daysOfWeek");
        assertFalse(tester.getDialog().getResponse().getText().contains("<script>alert('XSS!');</script>"));
    }

    public void testXssInRunFromMeridianParam() throws IOException
    {
        tester.gotoPage("/secure/FilterSubscription.jspa?filter.subscription.prefix.interval=180&groupName=jira-users"
                + "&filter.subscription.prefix.runFromMins=00"
                + "&nextRun="
                + "&filter.subscription.prefix.runToMins=00"
                + "&filter.subscription.prefix.runToMeridian=00"
                + "&filter.subscription.prefix.week=2"
                + "&filter.subscription.prefix.runOnceMeridian=pm"
                + "&filter.subscription.prefix.day=2"
                + "&filter.subscription.prefix.runOnceMins=5"
                + "&filter.subscription.prefix.runFromMeridian=<script>alert('XSS!');</script>"
                + "&filter.subscription.prefix.monthDay=1"
                + "&subId="
                + "&atl_token=b1719c444f52dc051d1d99a5a0cc8d5b8690a864"
                + "&filter.subscription.prefix.runToHours=2"
                + "&lastRun="
                + "&filter.subscription.prefix.cronString=555-555-0199@example.com"
                + "&Subscriure=Subscriure"
                + "&filter.subscription.prefix.runOnceHours=2"
                + "&filter.subscription.prefix.runFromHours=2"
                + "&filterId=10000"
                + "&filter.subscription.prefix.daysOfMonthOpt=dayOfWeekOfMonth"
                + "&emailOnEmpty=on"
                + "&filter.subscription.prefix.dailyWeeklyMonthly=daysOfWeek");
        assertFalse(tester.getDialog().getResponse().getText().contains("<script>alert('XSS!');</script>"));
    }

    public void testXssInRunToHoursParam() throws IOException
    {
        tester.gotoPage("/secure/FilterSubscription.jspa?filter.subscription.prefix.interval=180&groupName=jira-users"
                + "&filter.subscription.prefix.runFromMins=00"
                + "&nextRun="
                + "&filter.subscription.prefix.runToMins=00"
                + "&filter.subscription.prefix.runToMeridian=00"
                + "&filter.subscription.prefix.week=2"
                + "&filter.subscription.prefix.runOnceMeridian=pm"
                + "&filter.subscription.prefix.day=2"
                + "&filter.subscription.prefix.runOnceMins=5"
                + "&filter.subscription.prefix.runFromMeridian=pm"
                + "&filter.subscription.prefix.monthDay=1"
                + "&subId="
                + "&atl_token=b1719c444f52dc051d1d99a5a0cc8d5b8690a864"
                + "&filter.subscription.prefix.runToHours=<script>alert('XSS!');</script>"
                + "&lastRun="
                + "&filter.subscription.prefix.cronString=555-555-0199@example.com"
                + "&Subscriure=Subscriure"
                + "&filter.subscription.prefix.runOnceHours=2"
                + "&filter.subscription.prefix.runFromHours=2"
                + "&filterId=10000"
                + "&filter.subscription.prefix.daysOfMonthOpt=dayOfWeekOfMonth"
                + "&emailOnEmpty=on"
                + "&filter.subscription.prefix.dailyWeeklyMonthly=daysOfWeek");
        assertFalse(tester.getDialog().getResponse().getText().contains("<script>alert('XSS!');</script>"));
    }

    public void testXssInCronStringParam() throws IOException
    {
        tester.gotoPage("/secure/FilterSubscription.jspa?filter.subscription.prefix.interval=180&groupName=jira-users"
                + "&filter.subscription.prefix.runFromMins=00"
                + "&nextRun="
                + "&filter.subscription.prefix.runToMins=00"
                + "&filter.subscription.prefix.runToMeridian=00"
                + "&filter.subscription.prefix.week=2"
                + "&filter.subscription.prefix.runOnceMeridian=pm"
                + "&filter.subscription.prefix.day=2"
                + "&filter.subscription.prefix.runOnceMins=5"
                + "&filter.subscription.prefix.runFromMeridian=pm"
                + "&filter.subscription.prefix.monthDay=1"
                + "&subId="
                + "&atl_token=b1719c444f52dc051d1d99a5a0cc8d5b8690a864"
                + "&filter.subscription.prefix.runToHours=2"
                + "&lastRun="
                + "&filter.subscription.prefix.cronString=<script>alert('XSS!');</script>"
                + "&Subscriure=Subscriure"
                + "&filter.subscription.prefix.runOnceHours=2"
                + "&filter.subscription.prefix.runFromHours=2"
                + "&filterId=10000"
                + "&filter.subscription.prefix.daysOfMonthOpt=dayOfWeekOfMonth"
                + "&emailOnEmpty=on"
                + "&filter.subscription.prefix.dailyWeeklyMonthly=daysOfWeek");
        assertFalse(tester.getDialog().getResponse().getText().contains("<script>alert('XSS!');</script>"));
    }

    public void testXssInRunOnceHoursParam() throws IOException
    {
        tester.gotoPage("/secure/FilterSubscription.jspa?filter.subscription.prefix.interval=180&groupName=jira-users"
                + "&filter.subscription.prefix.runFromMins=00"
                + "&nextRun="
                + "&filter.subscription.prefix.runToMins=00"
                + "&filter.subscription.prefix.runToMeridian=00"
                + "&filter.subscription.prefix.week=2"
                + "&filter.subscription.prefix.runOnceMeridian=pm"
                + "&filter.subscription.prefix.day=2"
                + "&filter.subscription.prefix.runOnceMins=5"
                + "&filter.subscription.prefix.runFromMeridian=pm"
                + "&filter.subscription.prefix.monthDay=1"
                + "&subId="
                + "&atl_token=b1719c444f52dc051d1d99a5a0cc8d5b8690a864"
                + "&filter.subscription.prefix.runToHours=2"
                + "&lastRun="
                + "&filter.subscription.prefix.cronString=555-555-0199@example.com"
                + "&Subscriure=Subscriure"
                + "&filter.subscription.prefix.runOnceHours=<script>alert('XSS!');</script>"
                + "&filter.subscription.prefix.runFromHours=2"
                + "&filterId=10000"
                + "&filter.subscription.prefix.daysOfMonthOpt=dayOfWeekOfMonth"
                + "&emailOnEmpty=on"
                + "&filter.subscription.prefix.dailyWeeklyMonthly=daysOfWeek");
        assertFalse(tester.getDialog().getResponse().getText().contains("<script>alert('XSS!');</script>"));
    }

    public void testXssInRunFromHoursParam() throws IOException
    {
        tester.gotoPage("/secure/FilterSubscription.jspa?filter.subscription.prefix.interval=180&groupName=jira-users"
                + "&filter.subscription.prefix.runFromMins=00"
                + "&nextRun="
                + "&filter.subscription.prefix.runToMins=00"
                + "&filter.subscription.prefix.runToMeridian=00"
                + "&filter.subscription.prefix.week=2"
                + "&filter.subscription.prefix.runOnceMeridian=pm"
                + "&filter.subscription.prefix.day=2"
                + "&filter.subscription.prefix.runOnceMins=5"
                + "&filter.subscription.prefix.runFromMeridian=pm"
                + "&filter.subscription.prefix.monthDay=1"
                + "&subId="
                + "&atl_token=b1719c444f52dc051d1d99a5a0cc8d5b8690a864"
                + "&filter.subscription.prefix.runToHours=2"
                + "&lastRun="
                + "&filter.subscription.prefix.cronString=555-555-0199@example.com"
                + "&Subscriure=Subscriure"
                + "&filter.subscription.prefix.runOnceHours=2"
                + "&filter.subscription.prefix.runFromHours=<script>alert('XSS!');</script>"
                + "&filterId=10000"
                + "&filter.subscription.prefix.daysOfMonthOpt=dayOfWeekOfMonth"
                + "&emailOnEmpty=on"
                + "&filter.subscription.prefix.dailyWeeklyMonthly=daysOfWeek");
        assertFalse(tester.getDialog().getResponse().getText().contains("<script>alert('XSS!');</script>"));
    }

    public void testXssInDaysOfMonthOptParam() throws IOException
    {
        tester.gotoPage("/secure/FilterSubscription.jspa?filter.subscription.prefix.interval=180&groupName=jira-users"
                + "&filter.subscription.prefix.runFromMins=00"
                + "&nextRun="
                + "&filter.subscription.prefix.runToMins=00"
                + "&filter.subscription.prefix.runToMeridian=00"
                + "&filter.subscription.prefix.week=2"
                + "&filter.subscription.prefix.runOnceMeridian=pm"
                + "&filter.subscription.prefix.day=2"
                + "&filter.subscription.prefix.runOnceMins=5"
                + "&filter.subscription.prefix.runFromMeridian=pm"
                + "&filter.subscription.prefix.monthDay=1"
                + "&subId="
                + "&atl_token=b1719c444f52dc051d1d99a5a0cc8d5b8690a864"
                + "&filter.subscription.prefix.runToHours=2"
                + "&lastRun="
                + "&filter.subscription.prefix.cronString=555-555-0199@example.com"
                + "&Subscriure=Subscriure"
                + "&filter.subscription.prefix.runOnceHours=2"
                + "&filter.subscription.prefix.runFromHours=2"
                + "&filterId=10000"
                + "&filter.subscription.prefix.daysOfMonthOpt=<script>alert('XSS!');</script>"
                + "&emailOnEmpty=on"
                + "&filter.subscription.prefix.dailyWeeklyMonthly=daysOfWeek");
        assertFalse(tester.getDialog().getResponse().getText().contains("<script>alert('XSS!');</script>"));
    }

    public void testXssInDailyWeeklyMonthlyParam() throws IOException
    {
        tester.gotoPage("/secure/FilterSubscription.jspa?filter.subscription.prefix.interval=180&groupName=jira-users"
                + "&filter.subscription.prefix.runFromMins=00"
                + "&nextRun="
                + "&filter.subscription.prefix.runToMins=00"
                + "&filter.subscription.prefix.runToMeridian=00"
                + "&filter.subscription.prefix.week=2"
                + "&filter.subscription.prefix.runOnceMeridian=pm"
                + "&filter.subscription.prefix.day=2"
                + "&filter.subscription.prefix.runOnceMins=5"
                + "&filter.subscription.prefix.runFromMeridian=pm"
                + "&filter.subscription.prefix.monthDay=1"
                + "&subId="
                + "&atl_token=b1719c444f52dc051d1d99a5a0cc8d5b8690a864"
                + "&filter.subscription.prefix.runToHours=2"
                + "&lastRun="
                + "&filter.subscription.prefix.cronString=555-555-0199@example.com"
                + "&Subscriure=Subscriure"
                + "&filter.subscription.prefix.runOnceHours=2"
                + "&filter.subscription.prefix.runFromHours=2"
                + "&filterId=10000"
                + "&filter.subscription.prefix.daysOfMonthOpt=dayOfWeekOfMonth"
                + "&emailOnEmpty=on"
                + "&filter.subscription.prefix.dailyWeeklyMonthly=<script>alert('XSS!');</script>");
        assertFalse(tester.getDialog().getResponse().getText().contains("<script>alert('XSS!');</script>"));
    }
}
