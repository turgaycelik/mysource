package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.testkit.client.restclient.IssueType;
import com.atlassian.jira.testkit.client.restclient.IssueTypeClient;
import com.atlassian.jira.testkit.client.restclient.Response;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Func tests for IssueTypeResource.
 *
 * @since v4.2
 */
@WebTest ({ Category.FUNC_TEST, Category.REST })
public class TestIssueTypeResource extends RestFuncTest
{
    private static final String BUG_ISSUE_TYPE_ID = "1";
    private static final String TASK_ISSUE_TYPE_ID = "3";
    private IssueTypeClient issueTypeClient;

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        backdoor.restoreDataFromResource("TestIssueTypeResource.xml");
        issueTypeClient = new IssueTypeClient(getEnvironmentData());
    }

    /**
     * Tests the case where the user can see all available issue types.
     *
     * @throws Exception if anything goes wrong
     */
    public void testIssueTypeVisible() throws Exception
    {
        IssueType issueType = issueTypeClient.get(TASK_ISSUE_TYPE_ID);

        // expected:
        //
        // {
        //   "self": "http://localhost:8090/jira/rest/api/2/issueType/3",
        //   "description": "A task that needs to be done.",
        //   "iconUrl": "http://localhost:8090/jira/images/icons/issuetypes/task.png",
        //   "name": "Task",
        //   "subtask": false
        // }
        assertEquals(getBaseUrl() + "/rest/api/2/issuetype/" + TASK_ISSUE_TYPE_ID, issueType.self);
        assertEquals("A task that needs to be done.", issueType.description);
        assertEquals(getBaseUrl() + "/images/icons/issuetypes/task.png", issueType.iconUrl);
        assertEquals("Task", issueType.name);
        assertEquals(false, issueType.subtask);
        assertEquals(TASK_ISSUE_TYPE_ID, issueType.id);
    }

    /**
     * Tests the case where not all available types are visible by user.
     *
     * @throws Exception if anything goes wrong
     */
    public void testIssueTypeNotFound() throws Exception
    {
        // the issue is not visible
        Response response = issueTypeClient.loginAs(FRED_USERNAME).getResponse(TASK_ISSUE_TYPE_ID);
        assertTrue(response.entity.errorMessages.contains("The issue type with id '" + TASK_ISSUE_TYPE_ID + "' does not exist"));

        // the issue doesn't exist. should return 404 NOT FOUND
        Response responseZzz = issueTypeClient.loginAs(FRED_USERNAME).getResponse("zzz");
        assertEquals(404, responseZzz.statusCode);
        assertTrue(responseZzz.entity.errorMessages.contains("The issue type with id 'zzz' does not exist"));
    }

    public void testIssueTypeResourceShouldSupportAbsoluteUrlInIconUrl() throws Exception
    {
        IssueType response = issueTypeClient.loginAs(FRED_USERNAME).get(BUG_ISSUE_TYPE_ID);
        
        assertThat(response.iconUrl, equalTo("https://jira.atlassian.com/images/icons/bug.gif"));
    }

    /**
     * Tests the case where not all available types are visible by user.
     *
     * @throws Exception if anything goes wrong
     */
    public void testGetAllIssueTypes() throws Exception
    {
        List<IssueType> issueTypes = issueTypeClient.get();

        assertEquals(4, issueTypes.size());
        assertIssueTypesContain(issueTypes, "1");
        assertIssueTypesContain(issueTypes, "2");
        assertIssueTypesContain(issueTypes, "3");
        assertIssueTypesContain(issueTypes, "4");

        // Fred should only see 1 & 2
        issueTypes = issueTypeClient.loginAs(FRED_USERNAME).get();

        assertEquals(2, issueTypes.size());
        assertIssueTypesContain(issueTypes, "1");
        assertIssueTypesContain(issueTypes, "2");
        // Jack should see all
        issueTypes = issueTypeClient.loginAs("jack").get();
        assertEquals(4, issueTypes.size());
        assertIssueTypesContain(issueTypes, "1");
        assertIssueTypesContain(issueTypes, "2");
        assertIssueTypesContain(issueTypes, "3");
        assertIssueTypesContain(issueTypes, "4");

    }

    private void assertIssueTypesContain(List<IssueType> issueTypes, String id)
    {
        for (IssueType issueType : issueTypes)
        {
            if (issueType.id.equals(id))
            {
                return;
            }
        }
        fail("IssueType " + id + " not in list");
    }
}
