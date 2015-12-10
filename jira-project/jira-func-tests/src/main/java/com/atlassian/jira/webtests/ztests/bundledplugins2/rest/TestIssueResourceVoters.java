package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.testkit.client.restclient.Vote;
import com.atlassian.jira.testkit.client.restclient.VotesClient;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Tests scenarios where the project has a permission scheme that sets the "View Voters and Watchers" permission to
 * check for the value of the User Picker custom field.
 */
@WebTest ({ Category.FUNC_TEST, Category.REST })
public class TestIssueResourceVoters extends RestFuncTest
{
    private static final String ISSUE_KEY = "TP-1";
    private static final String USER_NOT_SELECTED_ON_USER_PICKER = "admin";
    private static final String USER_SELECTED_ON_USER_PICKER = "test";

    private VotesClient votesClient;

    @Override
    public void setUpTest()
    {
        super.setUpTest();
        votesClient = new VotesClient(getEnvironmentData());

        restoreJiraWithVotedIssueInProjectWhereVotersCanOnlyBeSeenByUserSpecifiedOnUserPicker();
    }

    public void testVotersCanNotBeReadByADifferentUserThanTheOneSpecifiedOnTheUserPickerCustomField()
    {
        votesClient.loginAs(USER_NOT_SELECTED_ON_USER_PICKER);

        Vote vote = votesClient.get(ISSUE_KEY);

        assertTrue(vote.voters.isEmpty());
    }

    public void testVotersCanBeReadByTheUserThatIsSpecifiedOnTheUserPickerCustomField()
    {
        votesClient.loginAs(USER_SELECTED_ON_USER_PICKER);

        Vote vote = votesClient.get(ISSUE_KEY);

        assertThat(vote.voters.size(), is(1));
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
        administration.restoreData("TestIssueResourceVoters.xml");
    }
}
