package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.testkit.client.restclient.IssueClient;
import com.atlassian.jira.testkit.client.restclient.Response;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Func tests for issue delete in REST API.
 *
 * @since v5.0
 */
@WebTest ({ Category.FUNC_TEST, Category.REST })
public class TestIssueResourceDelete extends RestFuncTest
{
    private IssueClient issueClient;

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        issueClient = new IssueClient(getEnvironmentData());
        administration.restoreData("TestIssueResourceDelete.xml");
    }

    /**
     * Test issue delete.
     *
     * @throws Exception if anything goes wrong
     */
    public void testSimpleDelete() throws Exception
    {
        Response resp1 = issueClient.delete("HSP-5", null);
        assertThat(resp1.statusCode, equalTo(204));
        resp1 = issueClient.getResponse("HSP-5");
        assertThat(resp1.statusCode, equalTo(404));
        assertTrue(resp1.entity.errorMessages.contains("Issue Does Not Exist"));

        // Delete with an id
        resp1 = issueClient.delete("10013", null);
        assertThat(resp1.statusCode, equalTo(204));
        resp1 = issueClient.getResponse("HSP-6");
        assertThat(resp1.statusCode, equalTo(404));
        assertTrue(resp1.entity.errorMessages.contains("Issue Does Not Exist"));
    }

    /**
     * Test issue delete errors.
     *
     * @throws Exception if anything goes wrong
     */
    public void testErrors() throws Exception
    {
        // Not found
        Response resp1 = issueClient.delete("HSP-567", null);
        assertThat(resp1.statusCode, equalTo(404));
        assertTrue(resp1.entity.errorMessages.contains("Issue Does Not Exist"));

        // Not found
        resp1 = issueClient.loginAs(FRED_USERNAME).delete("HSP-5", null);
        assertThat(resp1.statusCode, equalTo(403));
        assertTrue(resp1.entity.errorMessages.contains("You do not have permission to delete issues in this project."));
    }

    /**
     * Test issue delete.
     *
     * @throws Exception if anything goes wrong
     */
    public void testSubtasks() throws Exception
    {
        // can't delete HSP-7 as it has subtasks
        Response resp1 = issueClient.delete("HSP-7", null);
        assertThat(resp1.statusCode, equalTo(400));
        assertTrue(resp1.entity.errorMessages.contains("The issue 'HSP-7' has subtasks.  You must specify the 'deleteSubtasks' parameter to delete this issue and all its subtasks."));

        // can't delete HSP-7 as it has subtasks
        resp1 = issueClient.delete("HSP-7", "false");
        assertThat(resp1.statusCode, equalTo(400));
        assertTrue(resp1.entity.errorMessages.contains("The issue 'HSP-7' has subtasks.  You must specify the 'deleteSubtasks' parameter to delete this issue and all its subtasks."));

        // Delete HSP-7 along with it's subtasks
        resp1 = issueClient.delete("HSP-7", "true");
        assertThat(resp1.statusCode, equalTo(204));
        resp1 = issueClient.getResponse("HSP-7");
        assertThat(resp1.statusCode, equalTo(404));
        assertTrue(resp1.entity.errorMessages.contains("Issue Does Not Exist"));

        // can't delete HSP-7 as it has subtasks
        resp1 = issueClient.delete("HSP-1", null);
        assertThat(resp1.statusCode, equalTo(400));
        assertTrue(resp1.entity.errorMessages.contains("The issue 'HSP-1' has subtasks.  You must specify the 'deleteSubtasks' parameter to delete this issue and all its subtasks."));

        // Delete subtasks first then the top level
        resp1 = issueClient.delete("HSP-2", null);
        resp1 = issueClient.delete("HSP-3", null);
        resp1 = issueClient.delete("HSP-4", null);
        resp1 = issueClient.delete("HSP-1", null);
        resp1 = issueClient.getResponse("HSP-1");
        assertThat(resp1.statusCode, equalTo(404));
        resp1 = issueClient.getResponse("HSP-2");
        assertThat(resp1.statusCode, equalTo(404));
        resp1 = issueClient.getResponse("HSP-3");
        assertThat(resp1.statusCode, equalTo(404));
        resp1 = issueClient.getResponse("HSP-4");
        assertThat(resp1.statusCode, equalTo(404));
    }
}
