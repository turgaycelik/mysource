package com.atlassian.jira.webtests.ztests.navigator.jql;

import com.atlassian.jira.functest.framework.Splitable;
import com.atlassian.jira.functest.framework.navigation.IssueNavigatorNavigation;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;

/**
 * Test for JQL included JQL system functions.
 *
 * @since v4.0
 */
@Splitable
@WebTest ({ Category.FUNC_TEST, Category.JQL })
public class TestJqlSystemFunctions extends AbstractJqlFuncTest
{
    public void testLatestReleasedVersion() throws Exception
    {
        administration.restoreData("TestJqlReleasedVersionsFunctions.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, FRED_USERNAME);

        // test correctness of results
        createSearchAndAssertIssues("fixVersion = latestReleasedVersion(MKY)", "MKY-2");
        createSearchAndAssertIssues("affectedVersion = latestReleasedVersion(MKY)", "MKY-1");
        createSearchAndAssertIssues("VP = latestReleasedVersion(MKY)", "MKY-1");
        createSearchAndAssertIssues("VP = latestReleasedVersion(MK)", "MK-1");

        // resolution of project argument happens in the following order: key, name, id; case-insensitive
        createSearchAndAssertIssues("VP = latestReleasedVersion(HSP)", "HSP-1");
        createSearchAndAssertIssues("VP = latestReleasedVersion(hsp)", "HSP-1");
        createSearchAndAssertIssues("VP = latestReleasedVersion(homosapien)", "HSP-1");
        createSearchAndAssertIssues("VP = latestReleasedVersion(HOMOSAPIEN)", "HSP-1");
        createSearchAndAssertIssues("VP = latestReleasedVersion(10000)", "NUMBER-1");
        createSearchAndAssertIssues("VP = latestReleasedVersion(monkey)", "MKY-1");
        createSearchAndAssertIssues("VP = latestReleasedVersion(MKY)", "MKY-1");
        createSearchAndAssertIssues("VP = latestReleasedVersion(MK)", "MK-1");
        createSearchAndAssertIssues("VP = latestReleasedVersion(10001)", "MKY-1");

        // Test with no argument (all projects)
        createSearchAndAssertIssues("VP = latestReleasedVersion()", "NUMBER-1", "MKY-1", "MK-1", "HSP-1");

        // Test with multiple projects
        createSearchAndAssertIssues("VP = latestReleasedVersion(HSP, MKY)", "MKY-1", "HSP-1");
        createSearchAndAssertIssues("VP = latestReleasedVersion(HSP, MKY, MK)", "MKY-1", "MK-1", "HSP-1");
        createSearchAndAssertIssues("VP = latestReleasedVersion(HSP, 10001)", "MKY-1", "HSP-1");


        // filter specifies "HSP" as function argument; admin can see HSP, so no sanitisation
        navigation.issueNavigator().loadFilter(10020, IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);
        tester.assertTextPresent("VP = latestReleasedVersion(HSP)");

        // fred cannot see project HSP
        navigation.logout();
        navigation.login(FRED_USERNAME);
        issueTableAssertions.assertSearchWithErrorForUser(FRED_USERNAME, "VP = latestReleasedVersion(HSP)", "Could not resolve the project 'HSP' provided to function 'latestReleasedVersion'.");
    }

    public void testAllUnreleasedVersions() throws Exception
    {
        administration.restoreData("TestJqlReleasedVersionsFunctions.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, FRED_USERNAME);

        // test correctness of results
        createSearchAndAssertIssues("fixVersion in unreleasedVersions()", "NUMBER-1", "MKY-1", "MK-1", "HSP-2", "HSP-1");
        createSearchAndAssertIssues("affectedVersion in unreleasedVersions()", "NUMBER-2", "MKY-2", "MK-2", "HSP-3", "HSP-2");
        createSearchAndAssertIssues("VP in unreleasedVersions()", "NUMBER-2", "MKY-2", "MK-2", "HSP-3", "HSP-2");

        // resolution of project argument happens in the following order: key, name, id; case-insensitive
        createSearchAndAssertIssues("VP in unreleasedVersions(HSP)", "HSP-3", "HSP-2");
        createSearchAndAssertIssues("VP in unreleasedVersions(hsp)", "HSP-3", "HSP-2");
        createSearchAndAssertIssues("VP in unreleasedVersions(homosapien)", "HSP-3", "HSP-2");
        createSearchAndAssertIssues("VP in unreleasedVersions(HOMOSAPIEN)", "HSP-3", "HSP-2");
        createSearchAndAssertIssues("VP in unreleasedVersions(10000)", "NUMBER-2");
        createSearchAndAssertIssues("VP in unreleasedVersions(monkey)", "MKY-2");
        createSearchAndAssertIssues("VP in unreleasedVersions(MKY)", "MKY-2");
        createSearchAndAssertIssues("VP in unreleasedVersions(MK)", "MK-2");
        createSearchAndAssertIssues("VP in unreleasedVersions(10001)", "MKY-2");

        // filter specifies "HSP" as function argument; admin can see HSP, so no sanitisation
        navigation.issueNavigator().loadFilter(10010, IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);
        tester.assertTextPresent("VP in unreleasedVersions(HSP)");

        // fred cannot see project HSP
        navigation.logout();
        navigation.login(FRED_USERNAME);
        createSearchAndAssertIssues("fixVersion in unreleasedVersions()", "NUMBER-1", "MKY-1", "MK-1");
        createSearchAndAssertIssues("affectedVersion in unreleasedVersions()", "NUMBER-2", "MKY-2", "MK-2");

        // filter specifies "HSP" as function argument, but fred cannot see HSP, so it will be sanitised to "10000"
        navigation.issueNavigator().loadFilter(10010, IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);
        tester.assertTextPresent("VP in unreleasedVersions(10000)");
    }

    public void testEarliestUnreleasedVersion() throws Exception
    {
        administration.restoreData("TestJqlReleasedVersionsFunctions.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, FRED_USERNAME);

        // test correctness of results
        createSearchAndAssertIssues("fixVersion = earliestUnreleasedVersion(MKY)", "MKY-1");
        createSearchAndAssertIssues("affectedVersion = earliestUnreleasedVersion(MKY)", "MKY-2");
        createSearchAndAssertIssues("VP = earliestUnreleasedVersion(MKY)", "MKY-2");
        createSearchAndAssertIssues("VP = earliestUnreleasedVersion(MK)", "MK-2");

        // resolution of project argument happens in the following order: key, name, id; case-insensitive
        createSearchAndAssertIssues("VP = earliestUnreleasedVersion(HSP)", "HSP-3");
        createSearchAndAssertIssues("VP = earliestUnreleasedVersion(hsp)", "HSP-3");
        createSearchAndAssertIssues("VP = earliestUnreleasedVersion(homosapien)", "HSP-3");
        createSearchAndAssertIssues("VP = earliestUnreleasedVersion(HOMOSAPIEN)", "HSP-3");
        createSearchAndAssertIssues("VP = earliestUnreleasedVersion(10000)", "NUMBER-2");
        createSearchAndAssertIssues("VP = earliestUnreleasedVersion(monkey)", "MKY-2");
        createSearchAndAssertIssues("VP = earliestUnreleasedVersion(MKY)", "MKY-2");
        createSearchAndAssertIssues("VP = earliestUnreleasedVersion(MK)", "MK-2");
        createSearchAndAssertIssues("VP = earliestUnreleasedVersion(10001)", "MKY-2");

        // Test with no argument (all projects)
        createSearchAndAssertIssues("VP = earliestUnreleasedVersion()", "NUMBER-2", "MKY-2", "MK-2", "HSP-3");

        // Test with multiple projects
        createSearchAndAssertIssues("VP = earliestUnreleasedVersion(HSP, MKY)", "MKY-2", "HSP-3");
        createSearchAndAssertIssues("VP = earliestUnreleasedVersion(HSP, MKY, MK)", "MKY-2", "MK-2", "HSP-3");
        createSearchAndAssertIssues("VP = earliestUnreleasedVersion(HSP, 10001)", "MKY-2", "HSP-3");

        // filter specifies "HSP" as function argument; admin can see HSP, so no sanitisation
        navigation.issueNavigator().loadFilter(10030, IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);
        tester.assertTextPresent("VP = earliestUnreleasedVersion(HSP)");

        // fred cannot see project HSP
        navigation.logout();
        navigation.login(FRED_USERNAME);
        issueTableAssertions.assertSearchWithErrorForUser(FRED_USERNAME, "VP = earliestUnreleasedVersion(HSP)", "Could not resolve the project 'HSP' provided to function 'earliestUnreleasedVersion'.");

        // filter specifies "HSP" as function argument, but fred cannot see HSP, so it will be sanitised to "10000"
        navigation.issueNavigator().loadFilter(10030, IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);
        tester.assertTextPresent("VP = earliestUnreleasedVersion(10000)");
    }

    private void createSearchAndAssertIssues(String jqlQuery, String...keys)
    {
        //Make sure we find the issues in the past.
        navigation.issueNavigator().createSearch(jqlQuery);
        assertIssues(keys);
    }
}
