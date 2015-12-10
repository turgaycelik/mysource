package com.atlassian.jira.webtests.ztests.user;

import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.locator.WebPageLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Checks the import and upgrade of users and groups for the migration to Embedded Crowd..
 *
 * @since v4.2
 */
@WebTest ({ Category.FUNC_TEST, Category.UPGRADE_TASKS })
public class TestUpgradeTask602 extends JIRAWebTest
{
    public static final String PAGE_GROUP_BROWSER = "/secure/admin/user/GroupBrowser.jspa";

    public TestUpgradeTask602(String name)
    {
        super(name);
    }

    public void testUserDataiIsCorrect()   throws Exception
    {
        restoreData("TestUpgradeTask602.xml");

        /* For efficiency, this is all in a single test.  We just goto each user and test that the data in their profile is as expected. */

        navigateToUser("watson");
        assertTextPresentBeforeText("Username:", "watson");
        assertTextPresentBeforeText("Full Name:", "Shane Watson");
        assertLinkPresentWithText("swatson@shartrec.com");
        text.assertTextPresent(new IdLocator(tester, "loginCount"), "Not recorded");
        text.assertTextPresent(new IdLocator(tester, "lastLogin"), "Not recorded");
        text.assertTextPresent(new IdLocator(tester, "previousLogin"), "Not recorded");

        navigateToUser("katich");
        assertTextPresentBeforeText("Username:", "katich");
        assertTextPresentBeforeText("Full Name:", "Simon Katich");
        assertLinkPresentWithText("skatich@shartrec.com");
        text.assertTextPresent(new IdLocator(tester, "loginCount"), "Not recorded");
        text.assertTextPresent(new IdLocator(tester, "lastLogin"), "Not recorded");
        text.assertTextPresent(new IdLocator(tester, "previousLogin"), "Not recorded");

        navigateToUser("ponting");
        assertTextPresentBeforeText("Username:", "ponting");
        assertTextPresentBeforeText("Full Name:", "Ricky Ponting");
        assertLinkPresentWithText("rponting@shartrec.com");
        text.assertTextPresent(new IdLocator(tester, "loginCount"), "Not recorded");
        text.assertTextPresent(new IdLocator(tester, "lastLogin"), "Not recorded");
        text.assertTextPresent(new IdLocator(tester, "previousLogin"), "Not recorded");

        navigateToUser("hussey");
        assertTextPresentBeforeText("Username:", "hussey");
        assertTextPresentBeforeText("Full Name:", "Michael Hussey");
        assertLinkPresentWithText("mhussey@shartrec.com");
        text.assertTextPresent(new IdLocator(tester, "loginCount"), "Not recorded");
        text.assertTextPresent(new IdLocator(tester, "lastLogin"), "Not recorded");
        text.assertTextPresent(new IdLocator(tester, "previousLogin"), "Not recorded");
        assertTextPresentBeforeText("TESTattrib1", "Value1");
        assertTextPresentBeforeText("TESTattrib2", "Value2");

        navigateToUser("haddin");
        assertTextPresentBeforeText("Username:", "haddin");
        assertTextPresentBeforeText("Full Name:", "Brad Haddin");
        assertLinkPresentWithText("bhaddin@shartrec.com");
        text.assertTextPresent(new IdLocator(tester, "loginCount"), "Not recorded");
        text.assertTextPresent(new IdLocator(tester, "lastLogin"), "Not recorded");
        text.assertTextPresent(new IdLocator(tester, "previousLogin"), "Not recorded");
        assertTextPresentBeforeText("TESTattrib1", "Value11");
        assertTextPresentBeforeText("TESTattrib2", "Value12");

        navigateToUser("johnson");
        assertTextPresentBeforeText("Username:", "johnson");
        assertTextPresentBeforeText("Full Name:", "michael johnson");
        assertLinkPresentWithText("mjohnson@shartrec.com");
        text.assertTextPresent(new IdLocator(tester, "loginCount"), "2");
        text.assertTextNotPresent(new IdLocator(tester, "lastLogin"), "Not recorded");
        text.assertTextNotPresent(new IdLocator(tester, "previousLogin"), "Not recorded");

        navigateToUser("clarke");
        assertTextPresentBeforeText("Username:", "clarke");
        assertTextPresentBeforeText("Full Name:", "Michael Clark");
        assertLinkPresentWithText("mclarke@shartrec.com");
        text.assertTextPresent(new IdLocator(tester, "loginCount"), "Not recorded");
        text.assertTextPresent(new IdLocator(tester, "lastLogin"), "Not recorded");
        text.assertTextPresent(new IdLocator(tester, "previousLogin"), "Not recorded");

        navigateToUser("north");
        assertTextPresentBeforeText("Username:", "north");
        assertTextPresentBeforeText("Full Name:", "Marcus North");
        assertLinkPresentWithText("MNORTH@shartrec.com");
        text.assertTextPresent(new IdLocator(tester, "loginCount"), "Not recorded");
        text.assertTextPresent(new IdLocator(tester, "lastLogin"), "Not recorded");
        text.assertTextPresent(new IdLocator(tester, "previousLogin"), "Not recorded");

        navigateToUser("hauritz");
        assertTextPresentBeforeText("Username:", "hauritz");
        assertTextPresentBeforeText("Full Name:", "Nathan Hauritz");
        text.assertTextPresent(new IdLocator(tester, "loginCount"), "Not recorded");
        text.assertTextPresent(new IdLocator(tester, "lastLogin"), "Not recorded");
        text.assertTextPresent(new IdLocator(tester, "previousLogin"), "Not recorded");

        navigateToUser("siddle");
        assertTextPresentBeforeText("Username:", "siddle");
        assertLinkPresentWithText("psiddle@shartrec.com");
        text.assertTextPresent(new IdLocator(tester, "loginCount"), "Not recorded");
        text.assertTextPresent(new IdLocator(tester, "lastLogin"), "Not recorded");
        text.assertTextPresent(new IdLocator(tester, "previousLogin"), "Not recorded");

        navigateToUser("bollinger");
        assertTextPresentBeforeText("Username:", "bollinger");
        text.assertTextPresent(new IdLocator(tester, "loginCount"), "Not recorded");
        text.assertTextPresent(new IdLocator(tester, "lastLogin"), "Not recorded");
        text.assertTextPresent(new IdLocator(tester, "previousLogin"), "Not recorded");

        navigateToUser("Yousef");
        assertTextPresentBeforeText("Username:", "Yousef");
        assertTextPresentBeforeText("Full Name:", "Mohammad Yousuf");
        assertLinkPresentWithText("myousef@pcb.com.pk");
        text.assertTextPresent(new IdLocator(tester, "loginCount"), "Not recorded");
        text.assertTextPresent(new IdLocator(tester, "lastLogin"), "Not recorded");
        text.assertTextPresent(new IdLocator(tester, "previousLogin"), "Not recorded");

        logout();
        // go back to admin user
        login(ADMIN_USERNAME, ADMIN_PASSWORD);
//            restoreBlankInstance();
    }

    public void testGroupDataiIsCorrect() throws Exception
    {
        restoreData("TestUpgradeTask602.xml");

        /* For efficiency, this is all in a single test.  We just goto each user and test that the data in their profile is as expected. */
        // Now for the Groups
        navigateToGroup("group1");
        assertTextPresent("watson");
        assertTextPresent("katich");
        text.assertTextPresent(new WebPageLocator(tester), "Displaying users 1 to 2 of 2");

        navigateToGroup("Group2");
        assertTextPresent("watson");
        assertTextPresent("katich");
        assertTextPresent("hussey");
        text.assertTextPresent(new WebPageLocator(tester), "Displaying users 1 to 3 of 3");

        navigateToGroup("group3");
        assertTextPresent("watson");
        text.assertTextPresent(new WebPageLocator(tester), "Displaying users 1 to 1 of 1");

        navigateToGroup("GrouP4");
        text.assertTextPresent(new WebPageLocator(tester), "Displaying users 0 to 0 of 0");

        navigateToGroup("jira-users");
        assertTextPresent("watson");
        assertTextPresent("katich");
        assertTextPresent("hussey");
        assertTextPresent("ponting");
        assertTextPresent("haddin");
        assertTextPresent("johnson");
        assertTextPresent("clarke");
        assertTextPresent("north");
        assertTextPresent("hauritz");
        assertTextPresent("siddle");
        assertTextPresent("bollinger");
        assertTextPresent("Yousef");
        text.assertTextPresent(new WebPageLocator(tester), "Displaying users 1 to 13 of 13");

        logout();
        // go back to admin user
        login(ADMIN_USERNAME, ADMIN_PASSWORD);
    }


    public void navigateToGroup(String groupName)
    {
        log("Navigating in GroupBrowser to Group " +  groupName);
        gotoPage(PAGE_GROUP_BROWSER);
        clickLinkWithText(groupName);
        clickLink("view_group_members");
    }


    /**
     * This converts a local time string (Which is what we have in the export file we are testing against, to a string in the
     * format we expect to be stored in User Attributes.
     * @param localTimeString  Date/Time as a String in the system local time
     * @return DateTime as a String in UTC time zone
     */
    private String convertLocalTimeStringToUTC(String localTimeString) throws Exception
    {
        DateFormat localDf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        localDf.setTimeZone(TimeZone.getDefault());
        Date date = localDf.parse(localTimeString);

        DateFormat utcDf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        utcDf.setTimeZone(TimeZone.getTimeZone("UTC"));

        return utcDf.format(date);
    }

}
