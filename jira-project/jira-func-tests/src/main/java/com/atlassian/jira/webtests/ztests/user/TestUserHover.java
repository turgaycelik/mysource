package com.atlassian.jira.webtests.ztests.user;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.admin.GeneralConfiguration;
import com.atlassian.jira.functest.framework.locator.WebPageLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import org.joda.time.DateTimeZone;
import org.w3c.dom.Node;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Tests that the links on the view profile page works.
 *
 * @since v3.12
 */
@WebTest ( { Category.FUNC_TEST, Category.USERS_AND_GROUPS })
public class TestUserHover extends FuncTestCase
{
    protected void setUpTest()
    {
        administration.restoreData("TestUserHover.xml");
    }

    public void testUserHover()
    {
        //first test getting the hover for a nonexistent user. Should just print the username without links
        gotoUserHover("unknownuser");
        text.assertTextPresent(new WebPageLocator(tester), "User does not exist: unknownuser");
        tester.assertLinkNotPresentWithText("Current Issues");
        tester.assertLinkNotPresentWithText("Profile");

        gotoUserHover("admin");
        text.assertTextPresent(new WebPageLocator(tester), ADMIN_FULLNAME);
        tester.assertLinkPresentWithText("admin@example.com");
        tester.assertLinkPresentWithText("Current Issues");
        tester.assertLinkPresentWithText("Profile");
        //already got an avatar set so shouldn't have an update link
        tester.assertLinkNotPresent("update_avatar_link");

        gotoUserHover("brad");
        text.assertTextPresent(new WebPageLocator(tester), "Brad Baker");
        tester.assertLinkPresentWithText("brad@example.com");
        tester.assertLinkPresentWithText("Current Issues");
        tester.assertLinkPresentWithText("Profile");


        //now lets try logging out and accessing the user hover
        navigation.logout();
        gotoUserHover("brad");
        text.assertTextPresent(new WebPageLocator(tester), "Brad Baker");
        tester.assertLinkNotPresentWithText("brad@example.com");
        tester.assertLinkNotPresentWithText("Current Issues");
        tester.assertLinkNotPresentWithText("Profile");

        //finally check the e-mail format is respected
        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);
        administration.generalConfiguration().setUserEmailVisibility(GeneralConfiguration.EmailVisibility.MASKED);
        gotoUserHover("brad");
        text.assertTextPresent(new WebPageLocator(tester), "Brad Baker");
        text.assertTextPresent(new WebPageLocator(tester), "brad at example dot com");
        tester.assertLinkNotPresentWithText("brad@example.com");
        tester.assertLinkPresentWithText("Current Issues");
        tester.assertLinkPresentWithText("Profile");

        //lets create a new user and login as them. The user hover should show a link to for the user hover img to
        //go to the profile to add a new avatar
        backdoor.usersAndGroups().addUser(BOB_USERNAME);
        navigation.logout();
        navigation.login(BOB_USERNAME);
        gotoUserHover("bob");
        tester.assertLinkPresent("update_avatar_link");

        //make sure logged out users don't see information they shouldn't be able to
        navigation.logout();
        gotoUserHover("admin");
        text.assertTextPresent(new WebPageLocator(tester), ADMIN_FULLNAME);
        tester.assertLinkNotPresentWithText("admin@example.com");
        tester.assertTextNotPresent("admin@example.com");
        tester.assertLinkNotPresentWithText("Current Issues");
        tester.assertLinkNotPresentWithText("Profile");
    }

    public void testLocalTimeShouldBeDisplayedInUserHover() throws Exception
    {
        // admin is in the default time zone
        gotoUserHover("admin");
        final String hourOfDayAdmin = hourOfDay();
        assertThat("time of day info is present", locator.css(".time-zone-info").getNode(), notNullValue());
        assertThat("hour icon shows correct hour=" + hourOfDayAdmin, locator.css(".user-time-icon.hour-of-day-" + hourOfDayAdmin).getNode(), notNullValue());

        // now test getting the hover for a nonexistent user. should not print TZ stuff
        gotoUserHover("unknownuser");
        assertThat("time of day info is present", locator.css(".time-zone-info").getNode(), nullValue());

        // this guy is in a different time zone
        gotoUserHover("brad");
        final String hourOfDayBrad = hourOfDay("Pacific/Palau");
        assertThat("time of info is present", locator.css(".time-zone-info").getNode(), notNullValue());
        assertThat("hour icon shows correct hour=" + hourOfDayBrad, locator.css(".user-time-icon.hour-of-day-" + hourOfDayBrad).getNode(), notNullValue());

        // if we are not logged in, we should not get time of day info
        navigation.logout();
        gotoUserHover("brad");
        assertThat("time of day info is present", locator.css(".time-zone-info").getNode(), nullValue());
    }

    protected void gotoUserHover(String username)
    {
        navigation.gotoPage("/secure/ViewUserHover!default.jspa?decorator=none&username=" + username);
    }

    protected String hourOfDay()
    {
        return hourOfDay(DateTimeZone.getDefault().getID());
    }

    protected String hourOfDay(String timeZoneID)
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH");
        dateFormat.setTimeZone(TimeZone.getTimeZone(timeZoneID));

        return dateFormat.format(new Date());
    }
}
