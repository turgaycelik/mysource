package com.atlassian.jira.webtests.ztests.upgrade.tasks;

import com.atlassian.jira.functest.framework.page.IssueSearchPage;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.ztests.navigator.jql.AbstractJqlFuncTest;
import junit.framework.AssertionFailedError;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Element;

import java.util.Arrays;
import java.util.List;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;
import static com.atlassian.jira.functest.framework.suite.Category.FUNC_TEST;
import static com.atlassian.jira.functest.framework.suite.Category.RENAME_USER;
import static com.atlassian.jira.functest.framework.suite.Category.UPGRADE_TASKS;

/**
 * Responsible for verifying that references to users in various database fields are forced to lowercase
 * so that they will match the user's key.  Many of these will already be entirely in lowercase, but
 * we check them anyway.
 *
 * @since v6.0
 */
@WebTest ({ FUNC_TEST, RENAME_USER, UPGRADE_TASKS })
public class TestUpgradeTask6039 extends AbstractJqlFuncTest
{
    private static final String DASHBOARD_SYSTEM = "System Dashboard";
    private static final String DASHBOARD_MIXED_CASE_USER = "Mixed Case User Dashboard";
    private static final String FILTER_GIMME_EVERYTHING = "Gimme Everything";
    private static final String FILTER_SHOW_ALL_THE_THINGS = "Show All The Things";

    private static final String[] DEFAULT_SEARCH_COLUMNS = {
        "T", "Key", "Summary", "Assignee", "Reporter", "P", "Status", "Resolution", "Created", "Updated", "Due"
    };

    @Override
    protected void setUpTest()
    {
        // MSSQL uses case-insensitive collation by default; our other supported databases don't.
        // Don't import rows that will violate constraints in a case-insensitive database if that's what we're testing against.
        final String testDataFile = instanceIsMSSQL() ? "TestUpgradeTask6039-mssql.xml" : "TestUpgradeTask6039.xml";
        administration.restoreDataWithBuildNumber(testDataFile, 6003);
    }

    private boolean instanceIsMSSQL()
    {
        tester.gotoPage("/internal-error");
        return locator.xpath("//table/tbody/tr/th[text()='Database type']/following-sibling::td[text()='mssql']").exists();
    }

    public void testSearchForMixedCaseUsernames()
    {
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        assertSearchWithResults("assignee = Mixed", "TEST-1");
        assertSearchWithResults("assignee = Admin", "TEST-8", "TEST-7", "TEST-6", "TEST-5", "TEST-4", "TEST-3", "TEST-2");
        assertSearchWithResults("reporter = Mixed", "TEST-3");
        assertSearchWithResults("\"Single User\" = Mixed", "TEST-5");
        assertSearchWithResults("\"Multi User\" = Mixed", "TEST-6");
        assertSearchWithResults("voter = Mixed", "TEST-7");
        assertSearchWithResults("watcher = Mixed", "TEST-8");
        assertSearchWithResults("\"Multi User\" = Mixed or watcher = Mixed", "TEST-8", "TEST-6");
        assertSearchWithResults("assignee was Mixed", "TEST-2", "TEST-1");
        assertSearchWithResults("reporter was Mixed", "TEST-4", "TEST-3");
        assertSearchWithResults("assignee was Mixed and reporter was Mixed");
        assertSearchWithResults("\"Single User\" = Mixed and (reporter was Mixed or assignee = Mixed)");
    }

    public void testDashboardsAndFavouriteAssociations()
    {
        navigation.logout();
        try
        {
            navigation.login("Mixed");
            navigation.dashboard().navigateToFavourites();
            tester.assertTextPresent(DASHBOARD_SYSTEM);
            tester.assertTextNotPresent(DASHBOARD_MIXED_CASE_USER);
            navigation.dashboard().navigateToMy();
            tester.assertTextNotPresent(DASHBOARD_SYSTEM);
            tester.assertTextPresent(DASHBOARD_MIXED_CASE_USER);
        }
        finally
        {
            navigation.logout();
            navigation.login(ADMIN_USERNAME);
        }
        navigation.dashboard().navigateToFavourites();
        tester.assertTextNotPresent(DASHBOARD_SYSTEM);
        tester.assertTextPresent(DASHBOARD_MIXED_CASE_USER);
        navigation.dashboard().navigateToMy();
        tester.assertTextNotPresent(DASHBOARD_SYSTEM);
        tester.assertTextNotPresent(DASHBOARD_MIXED_CASE_USER);
    }

    public void testCommentAuthor()
    {
        navigation.issue().viewIssue("TEST-1");
        assertCommentAuthor("Administrator", "10000");
        tester.assertTextPresent("edit_comment_10000");
        tester.assertTextPresent("delete_comment_10000");

        navigation.issue().viewIssue("TEST-2");
        assertCommentAuthor("Mixed Case User", "10001");
        tester.assertTextNotPresent("edit_comment_10001");
        tester.assertTextPresent("delete_comment_10001");

        navigation.issue().viewIssue("TEST-3");
        assertCommentAuthor("Administrator", "10002");
        tester.assertTextPresent("edit_comment_10002");
        tester.assertTextPresent("delete_comment_10002");

        navigation.logout();
        try
        {
            navigation.login("Mixed");

            navigation.issue().viewIssue("TEST-1");
            assertCommentAuthor("Administrator", "10000");
            tester.assertTextNotPresent("edit_comment_10000");
            tester.assertTextNotPresent("delete_comment_10000");

            navigation.issue().viewIssue("TEST-2");
            assertCommentAuthor("Mixed Case User", "10001");
            tester.assertTextPresent("edit_comment_10001");
            tester.assertTextPresent("delete_comment_10001");

            navigation.issue().viewIssue("TEST-3");
            assertCommentAuthor("Administrator", "10002");
            tester.assertTextNotPresent("edit_comment_10002");
            tester.assertTextNotPresent("delete_comment_10002");
        }
        finally
        {
            navigation.logout();
            navigation.login(ADMIN_USERNAME);
        }
    }

    private void assertCommentAuthor(String expectedAuthor, String commentId)
    {
        assertEquals("Author for commentId " + commentId, expectedAuthor, getCommentAuthor(commentId));
    }

    private String getCommentAuthor(String commentId)
    {
        Element commentAuthor = tester.getDialog().getElement("commentauthor_"+commentId+"_verbose");
        if (commentAuthor == null || !commentAuthor.getTagName().equalsIgnoreCase("a"))
        {
            throw new AssertionFailedError("Could not find an author for a comment with id " + commentId);
        }
        return StringUtils.trim(commentAuthor.getTextContent());
    }

    public void testSearchRequestsAndFavouriteAssociations()
    {
        navigation.logout();
        try
        {
            navigation.login("Mixed");
            navigation.manageFilters().favouriteFilters();
            tester.assertTextPresent(FILTER_GIMME_EVERYTHING);
            tester.assertTextNotPresent(FILTER_SHOW_ALL_THE_THINGS);
            navigation.manageFilters().myFilters();
            tester.assertTextNotPresent(FILTER_GIMME_EVERYTHING);
            tester.assertTextPresent(FILTER_SHOW_ALL_THE_THINGS);
        }
        finally
        {
            navigation.logout();
            navigation.login(ADMIN_USERNAME);
        }
        navigation.manageFilters().favouriteFilters();
        tester.assertTextNotPresent(FILTER_GIMME_EVERYTHING);
        tester.assertTextNotPresent(FILTER_SHOW_ALL_THE_THINGS);
        navigation.manageFilters().myFilters();
        tester.assertTextPresent(FILTER_GIMME_EVERYTHING);
        tester.assertTextNotPresent(FILTER_SHOW_ALL_THE_THINGS);
    }

    public void testUseSavedColumnLayoutForMixedCaseUsername()
    {
        try
        {
            navigation.logout();
            navigation.login("Mixed");
            assertSearchColumns("Reporter", "T", "Key", "Summary", "Assignee");
        }
        finally
        {
            navigation.logout();
            navigation.login(ADMIN_USERNAME);
        }
        assertSearchColumns(DEFAULT_SEARCH_COLUMNS);
    }


    private void assertSearchColumns(String... expectedColumns)
    {
        final IssueSearchPage issueSearchPage = navigation.issueNavigator().runPrintableSearch("");
        final List<String> actualColumns = issueSearchPage.getResultsTable().getHeadingList();
        assertEquals(Arrays.asList(expectedColumns), actualColumns);
    }
}
