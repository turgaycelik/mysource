package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.testkit.client.restclient.Issue;
import com.atlassian.jira.testkit.client.restclient.IssueClient;
import com.atlassian.jira.testkit.client.restclient.IssueLink;

import java.util.List;

/**
 * Func tests for parent/subtask linking in REST API.
 *
 * @since v4.2
 */
@WebTest ({ Category.FUNC_TEST, Category.REST })
public class TestIssueResourceSubtasks extends RestFuncTest
{
    private IssueClient issueClient;

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        issueClient = new IssueClient(getEnvironmentData());
        administration.restoreData("TestIssueResourceSubtasks.xml");
    }

    /**
     * Verifies that the link to a subtask has all the required information.
     *
     * @throws Exception if anything goes wrong
     */
    public void testSubtaskLink() throws Exception
    {
        Issue issue = issueClient.get("HSP-1");

        // make sure the subtask info is correct
        List<IssueLink.IssueLinkRef> subtasks = issue.fields.subtasks;
        assertEquals(1, subtasks.size());

        final String baseUrl = getEnvironmentData().getBaseUrl().toExternalForm();

        IssueLink.IssueLinkRef lnkHsp2 = subtasks.get(0);
        assertEquals("HSP-2", lnkHsp2.key());
        assertEquals(baseUrl + "/rest/api/2/issue/10001", lnkHsp2.self());
    }

    /**
     * Verifies that the link to the parent has all the required information.
     *
     * @throws Exception if anything goes wrong
     */
    public void testParentLink() throws Exception
    {
        Issue issue = issueClient.get("HSP-2");

        final String baseUrl = getEnvironmentData().getBaseUrl().toExternalForm();

        IssueLink.IssueLinkRef parent = issue.fields.parent;
        assertEquals("HSP-1", parent.key());
        assertEquals(baseUrl + "/rest/api/2/issue/10000", parent.self());
    }
}
