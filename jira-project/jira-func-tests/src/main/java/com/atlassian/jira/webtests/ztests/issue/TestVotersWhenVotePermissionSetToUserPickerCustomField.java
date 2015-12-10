package com.atlassian.jira.webtests.ztests.issue;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * Tests issue voting scenarios where the project has a permission scheme that sets the "View Voters and Watchers" permission to
 * check for the value of the User Picker custom field.
 */
@WebTest ({ Category.FUNC_TEST, Category.BROWSING })
public class TestVotersWhenVotePermissionSetToUserPickerCustomField extends FuncTestCase
{
    private static final String ISSUE_KEY = "TP-1";
    private static final String ISSUE_ID = "10000";
    private static final String USER_NOT_SELECTED_ON_USER_PICKER = "admin";
    private static final String USER_SELECTED_ON_USER_PICKER = "test";

    @Override
    protected void setUpTest()
    {
        restoreJiraWithVotedIssueInProjectWhereVotersCanOnlyBeSeenByUserSpecifiedOnUserPicker();
    }

    public void testIssueCanNotBeVotedByADifferentUserAsTheOneSpecifiedOnTheUserPickerCustomField()
    {
        navigation.login(USER_NOT_SELECTED_ON_USER_PICKER);

        navigation.issue().gotoIssue(ISSUE_KEY);

        assertUserCanNotVoteUsingTheLinksOnThePeopleSection();
        assertUserCanNotNavigateDirectlyToTheViewVoteHistoryPage(ISSUE_ID);
    }

    public void testIssueCanBeVotedByTheUserSpecifiedOnTheUserPickerCustomField()
    {
        navigation.login(USER_SELECTED_ON_USER_PICKER);

        navigation.issue().gotoIssue(ISSUE_KEY);

        assertUserCanVoteUsingTheLinksOnThePeopleSection();
        assertUserCanNavigateDirectlyToTheViewVoteHistoryPage(ISSUE_ID);
    }
    
    private void assertUserCanVoteUsingTheLinksOnThePeopleSection()
    {
        tester.assertLinkPresent("vote-toggle");
        tester.assertLinkPresent("view-voter-list");
    }

    private void assertUserCanNotVoteUsingTheLinksOnThePeopleSection()
    {
        tester.assertLinkNotPresent("vote-toggle");
        tester.assertLinkNotPresent("view-voter-list");
    }

    private void assertUserCanNavigateDirectlyToTheViewVoteHistoryPage(final String issueId)
    {
        navigateToViewVoteHistoryPage(issueId);
        tester.assertTextPresent("Vote history");
    }

    private void assertUserCanNotNavigateDirectlyToTheViewVoteHistoryPage(final String issueId)
    {
        navigateToViewVoteHistoryPage(issueId);
        tester.assertTextPresent("Access Denied");
    }

    private void navigateToViewVoteHistoryPage(final String issueId)
    {
        navigation.gotoPage(String.format("secure/ViewVoters!default.jspa?id=" + issueId));
    }

    private void restoreJiraWithVotedIssueInProjectWhereVotersCanOnlyBeSeenByUserSpecifiedOnUserPicker()
    {
        // Scenario:
        //
        // Two users: "admin", "test"
        // An user picker custom field: "CF User Picker"
        //
        // A single project: "TP"
        // "TP" has a permission scheme that defines the "View Voters and Watchers" to be decided by the value set on "CF User Picker"
        //
        // A single issue "TP-1", that has "test" as a voter and has a value of "test" on the field "CF User Picker"
        administration.restoreData("TestVotersWhenVotePermissionSetToUserPickerCustomField.xml");
    }
}
