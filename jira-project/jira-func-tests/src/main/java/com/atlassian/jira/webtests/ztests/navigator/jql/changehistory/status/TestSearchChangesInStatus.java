package com.atlassian.jira.webtests.ztests.navigator.jql.changehistory.status;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.ztests.navigator.jql.changehistory.AbstractChangeHistoryFuncTest;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;

/**
 * Responsible for verifying that a user is able to query all issues that have been in a specific status.
 *
 * Story @ JRADEV-3734
 *
 * @since v4.0
 */
@WebTest ({ Category.FUNC_TEST, Category.JQL, Category.CHANGE_HISTORY })
public class TestSearchChangesInStatus extends AbstractChangeHistoryFuncTest
{
    @Override
    protected void setUpTest()
    {
        administration.restoreData("TestWasSearch.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);
    }

    public void testReturnsAllIssuesThatWereInASpecificStatus()
    {
        navigation.login(BOB_USERNAME);

        final String[] openIssues = { "HSP-4", "HSP-3", "HSP-2", "HSP-1" };
        assertSearchWithResults("status was Open", openIssues);
    }

    public void testReturnsAllIssuesThatWereNotEverInASpecificStatus() throws Exception
    {
        navigation.login(BOB_USERNAME);

        final String[] openIssues = { "HSP-4", "HSP-3", "HSP-2", "HSP-1" };
        assertSearchWithResults("Status was not 'In Progress'", openIssues);
    }

    public void testReturnsAllIssuesThatWereInASetOfSpecificStatuses() throws Exception
    {
        navigation.login(BOB_USERNAME);

        final String[] openIssues = { "HSP-4", "HSP-3", "HSP-2", "HSP-1" };
        assertSearchWithResults("status was in (Open,'In Progress')", openIssues);
    }

    public void testReturnsAllIssuesThatWereNotEverInASetOfSpecificStatuses() throws Exception
    {
        navigation.login(BOB_USERNAME);

        final String[] openIssues = { "HSP-4", "HSP-3", "HSP-2", "HSP-1" };
        assertSearchWithResults("status was not in ('In Progress',Reopened,Closed)", openIssues);
    }

    public void testReturnsZeroIssuesForAStatusThatHasNeverBeenSet() throws Exception
    {
        navigation.login(BOB_USERNAME);

        assertSearchWithResults("status was Closed");
    }

    public void testIgnoresTheCaseOfTheStatusValue() throws Exception
    {
        navigation.login(BOB_USERNAME);

        assertSearchWithResults("status was Open", "HSP-4", "HSP-3", "HSP-2", "HSP-1");
        assertSearchWithResults("Status was open", "HSP-4", "HSP-3", "HSP-2", "HSP-1");
        assertSearchWithResults("Status was oPEn", "HSP-4", "HSP-3", "HSP-2", "HSP-1");
    }

    public void testReturnsAnErrorForANonExistingStatus()
    {
        navigation.login(BOB_USERNAME);

        assertSearchWithError("status was dingbat", "The value 'dingbat' does not exist for the field 'status'.");
    }

    public void testTakesIntoAccountUpdatesToTheStatusOfAnIssue()
    {
        navigation.login(FRED_USERNAME);
        final String issueToBeUpdated = "HSP-1";
        final String[] openIssues = { "HSP-4", "HSP-3", "HSP-2", issueToBeUpdated };

        navigation.issue().closeIssue(issueToBeUpdated, "Fixed", "Fixed");

        assertSearchWithResults("status was Open", openIssues);

        assertSearchWithResults("status was Closed", issueToBeUpdated);
    }

    public void testAddingANewStatusAllowsSearchingForIssuesInThatStatus()
    {
        navigation.login(ADMIN_USERNAME);

        tester.gotoPage("secure/admin/AddStatus!default.jspa");
        tester.setFormElement("name", "myStatus");
        tester.submit("Add");

        // No matching issues
        assertSearchWithResults("status was myStatus");
    }

    public void testStatusFieldRename()
    {
        navigation.login(ADMIN_USERNAME);
        navigation.issue().closeIssue("HSP-1","Fixed","Fixed");

        assertSearchWithResults("status was Closed", "HSP-1");

        tester.gotoPage("secure/admin/EditStatus!default.jspa?id=6");
        tester.setFormElement("name","Shut");
        tester.submit("Update");

        assertSearchWithResults("Status was Closed",  "HSP-1");
        assertSearchWithResults("Status was shut",  "HSP-1");
    }
}