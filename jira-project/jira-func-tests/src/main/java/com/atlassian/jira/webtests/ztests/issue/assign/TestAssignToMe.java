package com.atlassian.jira.webtests.ztests.issue.assign;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import static com.atlassian.jira.webtests.Groups.DEVELOPERS;
import static com.atlassian.jira.webtests.Groups.USERS;

/**
 * <p>
 * Responsible for verifying the behaviour of the assign to me operation.
 * </p>
 *
 * <p>
 * Assign to Me should assign an issue to the user that's currently logged in.
 * </p>
 */
@WebTest ({ Category.FUNC_TEST, Category.BROWSING })
public class TestAssignToMe extends FuncTestCase
{
    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("TestAssignToMe.xml");
        backdoor.darkFeatures().enableForSite("ka.NO_GLOBAL_SHORTCUT_LINKS");
    }

    /**
     * Verifies that the assign to me operation works when the current logged in user is the reporter of the issue.
     */
    public void testAssignToMeWhenTheUserReportedTheIssue()
    {
        navigation.issue().gotoIssue("MKY-1");
        text.assertTextPresent(locator.id("assignee-val"), FRED_FULLNAME);
        text.assertTextPresent(locator.id("reporter-val"), ADMIN_FULLNAME);

        tester.clickLink("assign-to-me");

        text.assertTextPresent(locator.id("assignee-val"), ADMIN_FULLNAME);
        text.assertTextPresent(locator.id("reporter-val"), ADMIN_FULLNAME);

        tester.assertLinkNotPresent("assign-to-me");
    }

    /**
     * Verifies that the assign to me operation works correctly for usernames which contain non-alphanumeric characters.
     * e.g. #
     */
    public void testAssignToMeWhenUserNamesContainNonAlphaNumericCharacters()
    {
        navigation.logout();
        navigation.login("#test");

        navigation.issue().gotoIssue("MKY-1");
        text.assertTextPresent(locator.id("assignee-val"), FRED_FULLNAME);
        text.assertTextPresent(locator.id("reporter-val"), ADMIN_FULLNAME);

        tester.clickLink("assign-to-me");

        text.assertTextPresent(locator.id("assignee-val"),"#test");
        tester.assertLinkNotPresent("assign-to-me");
        text.assertTextPresent(locator.id("reporter-val"), ADMIN_FULLNAME);

        navigation.logout();
        navigation.login(ADMIN_USERNAME);
    }

    public void testAssignToMeNotVisibleIfNoAssignIssuePermission()
    {
        navigation.issue().gotoIssue("MKY-1");
        tester.assertLinkPresent("assign-to-me");

        backdoor.permissionSchemes().removeGroupPermission(0, ASSIGN_ISSUE, DEVELOPERS);
        navigation.issue().gotoIssue("MKY-1");
        tester.assertLinkNotPresent("assign-to-me");
    }

    public void testAssignToMeNotVisibleIfNoAssignableUserPermission()
    {
        navigation.issue().gotoIssue("MKY-1");
        tester.assertLinkPresent("assign-to-me");

        backdoor.permissionSchemes().removeGroupPermission(0, ASSIGNABLE_USER, DEVELOPERS);
        backdoor.permissionSchemes().removeGroupPermission(0, ASSIGNABLE_USER, USERS);
        navigation.issue().gotoIssue("MKY-1");
        tester.assertLinkNotPresent("assign-to-me");
    }

    public void testAssignToMeNotVisibleIfIssueIsNotEditable()
    {
        navigation.issue().gotoIssue("MKY-1");
        tester.assertLinkPresent("assign-to-me");

        navigation.issue().closeIssue("MKY-1", "Fixed", "Fixed");

        navigation.issue().gotoIssue("MKY-1");
        tester.assertLinkNotPresent("assign-to-me");
    }

    public void testAssignToMeNotVisibleIfAnonymousUser()
    {
        navigation.issue().gotoIssue("MKY-1");
        tester.assertLinkPresent("assign-to-me");

        navigation.logout();
        navigation.issue().gotoIssue("MKY-1");
        text.assertTextPresent(locator.id("assignee-val"), FRED_FULLNAME);
        tester.assertLinkNotPresent("assign-to-me");
    }
}