package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.testkit.client.restclient.Issue;
import com.atlassian.jira.testkit.client.restclient.IssueClient;
import com.atlassian.jira.testkit.client.restclient.Response;
import com.atlassian.jira.testkit.client.restclient.User;
import com.atlassian.jira.testkit.client.restclient.Vote;
import com.atlassian.jira.testkit.client.restclient.VotesClient;

/**
 * @since v4.2
 */
@WebTest ({ Category.FUNC_TEST, Category.REST })
public class TestIssueResourceVotes extends RestFuncTest
{
    String issueKey;
    String issueREST;
    private VotesClient votesClient;
    private IssueClient issueClient;

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        issueClient = new IssueClient(getEnvironmentData());
        votesClient = new VotesClient(getEnvironmentData());
        administration.restoreBlankInstance();

        issueKey = navigation.issue().createIssue("monkey", "Bug", "Issue for voting test");
        issueREST = String.format("/rest/api/2/issue/%s", issueKey);

        // admin can't vote on his own issues so vote as fred
        navigation.login(FRED_USERNAME);
        navigation.issue().voteIssue(issueKey);

        // but switch back to admin because that's what most tests are going to expect
        navigation.login(ADMIN_USERNAME);
    }

    public void testVote_issueDoesNotExist() throws Exception
    {
        // have to login as fred so he can unvote for himself
        final Response response = votesClient.loginAs(FRED_USERNAME).deleteResponse("HSP-204");
        assertEquals(404, response.statusCode);
        assertTrue(response.entity.errorMessages.contains("Issue Does Not Exist"));
    }

    public void testVote_votingDisabled() throws Exception
    {
        administration.generalConfiguration().disableVoting();
        backdoor.usersAndGroups().addUser("barney");

        final Response response = votesClient.loginAs("barney").deleteResponse(issueKey);
        assertEquals(404, response.statusCode);
        assertTrue(response.entity.errorMessages.contains("Voting for issues is currently not enabled for this JIRA instance."));
    }

    public void testVote_reporter() throws Exception
    {
        final Response response = votesClient.postResponse(issueKey);
        assertEquals(404, response.statusCode);
        assertTrue(response.entity.errorMessages.contains("You cannot vote for an issue you have reported."));
        // ensure that no voting happened
        assertEquals(1, getVotes());
        assertFalse(i_voted());
    }

    public void testVote_successful() throws Exception
    {
        backdoor.usersAndGroups().addUser("barney");

        final Response response = votesClient.loginAs("barney").postResponse(issueKey);
        assertEquals(204, response.statusCode);
        assertEquals(2, getVotes());
        assertTrue(i_voted("barney"));
    }

    public void testViewVoters_issueDoesNotExist() throws Exception
    {
        final Response response = votesClient.getResponse("HSP-55");

        assertEquals(404, response.statusCode);
        assertTrue(response.entity.errorMessages.contains("Issue Does Not Exist"));
    }

    public void testViewVoters_votingDisabled() throws Exception
    {
        administration.generalConfiguration().disableVoting();

        final Response response = votesClient.getResponse(issueKey);
        assertEquals(404, response.statusCode);
        assertTrue(response.entity.errorMessages.contains("Voting for issues is currently not enabled for this JIRA instance."));
    }

    public void testViewVoters_noPermission() throws Exception
    {
        // Fred isn't in jira-developers so he can't view the voters
        final Vote vote = votesClient.loginAs(FRED_USERNAME).get(issueKey);
        assertEquals(1, vote.votes);
        assertEquals(0, vote.voters.size());
    }

    public void testViewVoters_successful() throws Exception
    {
        final Vote voters = votesClient.get(issueKey);
        assertEquals(1, voters.votes);

        final User user = voters.voters.get(0);
        assertNotNull(user.self);
        assertEquals(FRED_USERNAME, user.name);
        assertEquals(FRED_FULLNAME, user.displayName);
    }

    public void testUnvote_issueDoesNotExist() throws Exception
    {
        // have to login as fred so he can unvote for himself
        final Response response = votesClient.loginAs(FRED_USERNAME).deleteResponse("HSP-204");
        assertEquals(404, response.statusCode);
        assertTrue(response.entity.errorMessages.contains("Issue Does Not Exist"));
    }

    public void testUnvote_votingDisabled() throws Exception
    {
        administration.generalConfiguration().disableVoting();

        // have to login as fred so he can unvote for himself
        final Response response = votesClient.loginAs(FRED_USERNAME).deleteResponse(issueKey);
        assertEquals(404, response.statusCode);
        assertTrue(response.entity.errorMessages.contains("Voting for issues is currently not enabled for this JIRA instance."));
    }

    public void testUnvote_reporter() throws Exception
    {
        final Response response = votesClient.deleteResponse(issueKey);
        assertEquals(404, response.statusCode);
        assertTrue(response.entity.errorMessages.contains("You cannot vote for an issue you have reported."));
        // ensure that no unvoting happened
        assertEquals(1, getVotes());
    }

    public void testUnvote_successful() throws Exception
    {
        final Response response = votesClient.loginAs(FRED_USERNAME).deleteResponse(issueKey);
        assertEquals(204, response.statusCode);
        assertEquals(0, getVotes());
        assertFalse(i_voted(FRED_USERNAME));
    }

    private int getVotes()
    {
        return issueClient.get(issueKey).fields.votes.votes;
    }

    private boolean i_voted()
    {
        return i_voted(null);
    }

    private boolean i_voted(String user)
    {
        Issue issue;
        if (user != null)
        {
            issue = issueClient.loginAs(user).get(issueKey);
        }
        else
        {
            issue = issueClient.get(issueKey);
        }

        return issue.fields.votes.hasVoted;
    }
}
