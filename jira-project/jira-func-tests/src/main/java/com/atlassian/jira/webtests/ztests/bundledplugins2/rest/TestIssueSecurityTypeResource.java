package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.testkit.client.restclient.IssueSecurityType;
import com.atlassian.jira.testkit.client.restclient.IssueSecurityTypeClient;
import com.atlassian.jira.testkit.client.restclient.Response;

/**
 * Func tests for SecurityTypeResource.
 *
 * @since v5.0
 */
@WebTest ({ Category.FUNC_TEST, Category.REST })
public class TestIssueSecurityTypeResource extends RestFuncTest
{
    private static final String SECURITY_TYPE_ID = "10000";
    private IssueSecurityTypeClient securityTypeClient;

    /**
     * Tests the case where the user can see all available issue types.
     *
     * @throws Exception if anything goes wrong
     */
    public void testSecurityTypeVisible() throws Exception
    {
        IssueSecurityType securityType = securityTypeClient.get(SECURITY_TYPE_ID);

        // expected:
        //
        // {
        //   "self": "http://localhost:8090/jira/rest/api/2/securityType/3",
        //   "description": "A task that needs to be done.",
        //   "iconUrl": "http://localhost:8090/jira/images/icons/issuetypes/task.png",
        //   "name": "Task",
        //   "subtask": false
        // }
        assertEquals(getBaseUrl() + "/rest/api/2/securitylevel/" + SECURITY_TYPE_ID, securityType.self);
        assertEquals("Only the good guys can see this", securityType.description);
        assertEquals("Secure", securityType.name);
        assertEquals(SECURITY_TYPE_ID, securityType.id);
    }

    /**
     * Tests the case where not all available types are visible by user.
     *
     * @throws Exception if anything goes wrong
     */
    public void testSecurityTypeNotFound() throws Exception
    {
        // the issue doesn't exist. should return 404 NOT FOUND
        Response response = securityTypeClient.loginAs(FRED_USERNAME).getResponse("zzz");
        assertEquals(404, response.statusCode);
        assertTrue(response.entity.errorMessages.contains("The security level with id 'zzz' does not exist."));
    }

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        securityTypeClient = new IssueSecurityTypeClient(getEnvironmentData());
        administration.restoreData("TestIssueSecurityTypeResource.xml");
    }
}
