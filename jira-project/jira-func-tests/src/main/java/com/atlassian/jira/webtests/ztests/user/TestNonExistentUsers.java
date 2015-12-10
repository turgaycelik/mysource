package com.atlassian.jira.webtests.ztests.user;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.locator.TableCellLocator;
import com.atlassian.jira.functest.framework.locator.WebPageLocator;
import com.atlassian.jira.functest.framework.navigation.IssueNavigatorNavigation;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;

/**
 * Tests to ensure that non existent users are handled correctly.
 *
 * @since v3.13
 */
@WebTest ({ Category.FUNC_TEST, Category.USERS_AND_GROUPS })
public class TestNonExistentUsers extends FuncTestCase
{
    protected void setUpTest()
    {
        administration.restoreData("TestNonExistentUsers.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);
    }

    // TODO: JRADEV-16613
//    public void testNonExistentSearcher()
//    {
//        tester.gotoPage("/secure/IssueNavigator.jspa?mode=hide&requestId=10000");
//        assertEquals(IssueNavigatorNavigation.NavigatorMode.SUMMARY, navigation.issueNavigator().getCurrentMode());
//        tester.clickLink("editfilter");
//        text.assertTextSequence(new WebPageLocator(tester), new String[] { "Could not find username:", FRED_USERNAME });
//        text.assertTextPresent(new WebPageLocator(tester), "There are errors with your search query on the left, please correct them before continuing.");
//        tester.assertLinkNotPresentWithText(FRED_USERNAME);
//    }

    /**
     * This tests where the user name was at one point a valid user but is no longer
     */
    // TODO: JRADEV-16613
//    public void testNonExistentReporterAndAssignee()
//    {
//        navigation.issue().viewIssue("HSP-1");
//        text.assertTextPresent(new IdLocator(tester, "assignee-val"), FRED_USERNAME);
//        text.assertTextPresent(new IdLocator(tester, "reporter-val"), FRED_USERNAME);
//        tester.assertLinkNotPresentWithText(FRED_USERNAME);
//
//        //also check the issue navigator.
//        tester.gotoPage("/secure/IssueNavigator.jspa?mode=hide&requestId=10000");
//        assertEquals(IssueNavigatorNavigation.NavigatorMode.SUMMARY, navigation.issueNavigator().getCurrentMode());
//        tester.clickLink("editfilter");
//        text.assertTextSequence(new WebPageLocator(tester), new String[] { "Could not find username:", FRED_USERNAME });
//        text.assertTextPresent(new WebPageLocator(tester), "There are errors with your search query on the left, please correct them before continuing.");
//    }

    /**
     * This tests where the user name was NEVER present
     */
    public void testNullReporterAndAssignee()
    {
        administration.restoreData("TestNullAssigneeAndReporter.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        assertViewIssueAssigneeAndReporter("AA-1", "Unassigned", "Anonymous");
        assertViewIssueAssigneeAndReporter("AA-2", "Unassigned", ADMIN_USERNAME);
        assertViewIssueAssigneeAndReporter("AA-3", ADMIN_USERNAME, "Anonymous");

        navigation.issueNavigator().displayAllIssues();
        assertNavigatorAssigneeAndReporter(1, "AA-3", ADMIN_USERNAME, "Anonymous");
        assertNavigatorAssigneeAndReporter(2, "AA-2", "Unassigned", ADMIN_USERNAME);
        assertNavigatorAssigneeAndReporter(3, "AA-1", "Unassigned", "Anonymous");

    }

    private void assertNavigatorAssigneeAndReporter(final int row, final String issueKey, final String assigneeName, final String reporterName)
    {
        text.assertTextPresent(new TableCellLocator(tester, "issuetable", row, 1), issueKey);
        text.assertTextPresent(new TableCellLocator(tester, "issuetable", row, 3), assigneeName);
        text.assertTextPresent(new TableCellLocator(tester, "issuetable", row, 4), reporterName);
    }

    private void assertViewIssueAssigneeAndReporter(String issueKey, String assigneeName, String reporterName)
    {
        navigation.issue().viewIssue(issueKey);
        text.assertTextSequence(new IdLocator(tester, "assignee-val"), assigneeName);
        text.assertTextSequence(new IdLocator(tester, "reporter-val"), reporterName);

    }

    public void testUserWithNoFullName()
    {
        administration.restoreData("TestNonExistentUsersNoFullname.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        navigation.issueNavigator().displayAllIssues();
        assertions.assertProfileLinkPresent("assignee_admin", ADMIN_USERNAME);
        assertions.assertProfileLinkPresent("reporter_admin", ADMIN_USERNAME);

        navigation.issue().viewIssue("HSP-1");
        assertions.assertProfileLinkPresent("commentauthor_10000_verbose", ADMIN_USERNAME);
    }
}
