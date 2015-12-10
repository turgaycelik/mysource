package com.atlassian.jira.webtests.ztests.upgrade.tasks;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;

/**
 * @since v5.2
 */
@WebTest ({ Category.FUNC_TEST, Category.UPGRADE_TASKS })
public class TestUpgradeTask807 extends FuncTestCase
{
    public void testUpgradeTask()
    {
        backdoor.restoreDataFromResource("xml/TestUpgradeTask807.xml");
        try
        {
            backdoor.workflowSchemes().getWorkflowSchemeDraftByProjectKey("HSP");
            fail("The draft should have been removed. Expecting a 404.");
        }
        catch (UniformInterfaceException e)
        {
            final ClientResponse response = e.getResponse();
            assertEquals(ClientResponse.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        }
    }
}
