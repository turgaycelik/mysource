package com.atlassian.jira.rest.v1.issues;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.user.MockUser;
import junit.framework.TestCase;

import javax.ws.rs.core.Response;
import java.util.Arrays;

/**
 * Test case for {@link com.atlassian.jira.rest.v1.issues.IssueResource} endpoint,
 * issue voter manipulation API.
 *
 * @since v4.2
 */
public class TestIssueResourceVoterEndpoint extends TestCase
{
    private static final long TEST_ISSUE_ID = 1L;
    private static final int TEST_RESULT_VOTES = 5;

    private IssueResourceEndpointTester tester;
    private IssueResource tested;

    @Override
    protected void setUp() throws Exception
    {
        tester = new IssueResourceEndpointTester();
        tested = tester.createTested();

    }

    public void testUnauthenticatedAddVoteRequest()
    {
        Response response = tested.addVoter(TEST_ISSUE_ID);
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    }

    public void testAddVoteOnNotExistingIssue()
    {
        User user = new MockUser("mock");
        tester.authenticateUser(user);
        tester.returnEmptyIssueResultFor(user, TEST_ISSUE_ID);
        Response response = tested.addVoter(TEST_ISSUE_ID);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    public void testAddVoteUnsuccessful()
    {
        User user = new MockUser("mock");
        MutableIssue issue = tester.newMutableIssue(TEST_ISSUE_ID);
        tester.authenticateUser(user);
        tester.returnValidIssueResultFor(user, TEST_ISSUE_ID, issue);
        tester.validateUnsuccessfully(user, issue, "error1", "error2", "error3");
        Response response = tested.addVoter(TEST_ISSUE_ID);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertTrue(tester.isXmlErrorCollection(response.getEntity()));
        assertEquals(Arrays.asList("error1", "error2", "error3"), tester.getErrorMessages(response.getEntity()));
    }

    public void testAddVoteSuccessful()
    {
        User user = new MockUser("mock");
        MutableIssue issue = tester.newMutableIssue(TEST_ISSUE_ID);
        tester.authenticateUser(user);
        tester.returnValidIssueResultFor(user, TEST_ISSUE_ID, issue);
        tester.validateSuccessfully(user, issue, TEST_RESULT_VOTES);
        Response response = tested.addVoter(TEST_ISSUE_ID);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertTrue(tester.isXmlVoteResult(response.getEntity()));
        assertEquals(TEST_RESULT_VOTES, tester.getVoteCount(response.getEntity()));
    }

    public void testUnauthenticatedRemoveVoteRequest()
    {
        Response response = tested.removeVoter(TEST_ISSUE_ID);
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    }
}
