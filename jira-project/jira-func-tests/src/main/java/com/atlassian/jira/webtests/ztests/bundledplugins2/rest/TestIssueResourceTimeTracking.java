package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import com.atlassian.jira.testkit.client.restclient.Issue;
import com.atlassian.jira.testkit.client.restclient.IssueClient;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import java.io.IOException;

/**
 * Tests the time tracking field.
 */
@WebTest ({ Category.FUNC_TEST, Category.REST })
public class TestIssueResourceTimeTracking extends RestFuncTest
{
    private IssueClient issueClient;

    public void testTimeTrackingDisabled() throws Exception
    {
        restoreData(false);

        Issue issue = issueClient.get("FUNC-3");
        assertNull("Time tracking shouldn't be in response when time tracking is disabled", issue.fields.timetracking);
    }

    public void testIssueWithNoTimeTracking() throws Exception
    {
        restoreData(true);
        
        Issue issue = issueClient.get("FUNC-1");
        assertNull(issue.fields.timetracking.originalEstimate);
        assertNull(issue.fields.timetracking.remainingEstimate);
        assertNull(issue.fields.timetracking.timeSpent);

        assertNotNull(issue.fields.progress);
        assertEquals(Long.valueOf(0), issue.fields.progress.total());
        assertEquals(Long.valueOf(0), issue.fields.progress.progress());
        assertNull(issue.fields.progress.percent());

        assertNotNull(issue.fields.aggregateprogress);
        assertEquals(Long.valueOf(0), issue.fields.aggregateprogress.total());
        assertEquals(Long.valueOf(0), issue.fields.aggregateprogress.progress());
        assertNull(issue.fields.aggregateprogress.percent());

    }

    public void testIssueWithOriginalEstimate() throws Exception
    {
        restoreData(true);

        Issue issue = issueClient.get("FUNC-3");
        assertNotNull(issue.fields.timetracking);
        assertEquals("3d", issue.fields.timetracking.originalEstimate);
    }

    public void testIssueWithTimeSpent() throws Exception
    {
        restoreData(true);

        Issue issue = issueClient.get("FUNC-3");
        assertNotNull(issue.fields.timetracking);
        assertEquals("1d", issue.fields.timetracking.timeSpent);
    }

    public void testIssueWithTimeRemaining() throws Exception
    {
        restoreData(true);

        Issue issue = issueClient.get("FUNC-3");
        assertNotNull(issue.fields.timetracking);
        assertEquals("2d", issue.fields.timetracking.remainingEstimate);
    }

    public void testProgressWithSubtasks() throws Exception
    {
        restoreData(true);

        Issue issue = issueClient.get("FUNC-4");
        assertNotNull(issue.fields.progress);
        assertEquals(Long.valueOf(36000), issue.fields.progress.total());
        assertEquals(Long.valueOf(7200), issue.fields.progress.progress());
        assertEquals(Long.valueOf(20), issue.fields.progress.percent());

        assertNotNull(issue.fields.aggregateprogress);
        assertEquals(Long.valueOf(43200), issue.fields.aggregateprogress.total());
        assertEquals(Long.valueOf(14400), issue.fields.aggregateprogress.progress());
        assertEquals(Long.valueOf(33), issue.fields.aggregateprogress.percent());

        issue = issueClient.get("FUNC-5");
        assertNotNull(issue.fields.progress);
        assertEquals(Long.valueOf(7200), issue.fields.progress.total());
        assertEquals(Long.valueOf(7200), issue.fields.progress.progress());
        assertEquals(Long.valueOf(100), issue.fields.progress.percent());

        assertNotNull(issue.fields.aggregateprogress);
        assertEquals(Long.valueOf(7200), issue.fields.aggregateprogress.total());
        assertEquals(Long.valueOf(7200), issue.fields.aggregateprogress.progress());
        assertEquals(Long.valueOf(100), issue.fields.aggregateprogress.percent());
    }

    protected void restoreData(boolean timeTrackingEnabled) throws IOException
    {
        administration.restoreData("TestIssueResourceTimeTracking.xml");
        if (!timeTrackingEnabled)
        {
            administration.timeTracking().disable();
        }
    }

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        issueClient = new IssueClient(getEnvironmentData());
    }
}
