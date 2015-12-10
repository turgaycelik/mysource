package com.atlassian.jira.rest.v1.issues;

import junit.framework.TestCase;

import javax.ws.rs.core.Response;

/**
 * Test case for {@link IssueResource} endpoint, issue watcher
 * manipulation API.
 *
 * @since v4.2
 */
public class TestIssueResourceWatcherEndpoint extends TestCase
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

    public void testUnauthenticatedAddWatcherRequest()
    {
        Response response = tested.addWatcher(TEST_ISSUE_ID);
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    }

    public void testUnauthenticatedRemoveWatcherRequest()
    {
        Response response = tested.removeWatcher(TEST_ISSUE_ID);
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    }
}